"""
Search Telegram for public groups/channels matching a query.
Uses Telethon with your existing authenticated session.

Usage:
    python search_channels.py "database"
    python search_channels.py "AI companion" --type channel
    python search_channels.py "PostgreSQL" --sort members
    python search_channels.py "Python" --sort members --min 1000 --type group
"""
import asyncio
import sys
import os
import argparse
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

from telethon import TelegramClient
from telethon.tl.functions.contacts import SearchRequest
from telethon.tl.types import Channel, Chat, User

API_ID   = 31671257
API_HASH = "ec880ffa330d0c826b77a7ffc3a16255"
SESSION  = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), "sanctum", "sanctum_setup.session")


def member_count(e) -> int:
    return getattr(e, 'participants_count', 0) or 0


def query_variations(query: str) -> list[str]:
    """Generate variations to cast a wider net — Telegram API caps each call at ~10 results."""
    q = query.strip()
    words = q.split()
    seen, variants = set(), [q]
    def add(v):
        v = v.strip()
        if v and v.lower() not in seen:
            seen.add(v.lower())
            variants.append(v)
    add(q.lower())
    add(q.upper())
    add(q.title())
    # word subsets
    for w in words:
        add(w)
    # common abbreviations / joined forms
    add("".join(words))
    add("_".join(words))
    add("-".join(words))
    # partial prefixes (first word + each subsequent)
    for i in range(1, len(words)):
        add(" ".join(words[:i+1]))
    return variants


async def search(query: str, limit: int, filter_type: str, sort: str, min_members: int, max_members: int | None = None) -> None:
    async with TelegramClient(SESSION, API_ID, API_HASH) as client:
        variants = query_variations(query)
        print(f'\nSearching "{query}" — {len(variants)} query variants to maximise results...\n')

        seen_ids: set[int] = set()
        channels, groups, bots = [], [], []

        for i, variant in enumerate(variants):
            try:
                result = await client(SearchRequest(q=variant, limit=100))
                for entity in result.chats + result.users:
                    if entity.id in seen_ids:
                        continue
                    seen_ids.add(entity.id)
                    if isinstance(entity, Channel):
                        if entity.broadcast:
                            channels.append(entity)
                        else:
                            groups.append(entity)
                    elif isinstance(entity, Chat):
                        groups.append(entity)
                    elif isinstance(entity, User) and entity.bot:
                        bots.append(entity)
                if i < len(variants) - 1:
                    await asyncio.sleep(0.4)   # stay under flood limits
            except Exception as e:
                if "flood" in str(e).lower():
                    print(f"  Rate limited — waiting 10s...")
                    await asyncio.sleep(10)
                continue

        # Filter by member range
        if min_members > 0:
            channels = [c for c in channels if member_count(c) >= min_members]
            groups   = [g for g in groups   if member_count(g) >= min_members]
        if max_members is not None:
            channels = [c for c in channels if member_count(c) <= max_members]
            groups   = [g for g in groups   if member_count(g) <= max_members]

        # Sort
        if sort == "members":
            channels.sort(key=member_count, reverse=True)
            groups.sort(key=member_count, reverse=True)
        # default: Telegram's relevance order (no sort)

        def fmt(e, kind="channel"):
            uname   = e.username if getattr(e, 'username', None) else None
            link    = f"t.me/{uname}" if uname else "(private)"
            members = member_count(e)
            m_str   = f"{members:,}" if members else "?"
            verified = " [verified]" if getattr(e, 'verified', False) else ""
            scam     = " [SCAM]"    if getattr(e, 'scam', False) else ""
            tag      = "CH" if kind == "channel" else "GR"
            return f"  [{tag}] {m_str:>8} members  {e.title}{verified}{scam}\n           {link}"

        # Combined sorted list if sorting by members
        if sort == "members" and filter_type == "all":
            combined = (
                [(c, "channel") for c in channels] +
                [(g, "group")   for g in groups]
            )
            combined.sort(key=lambda x: member_count(x[0]), reverse=True)

            header = f'"{query}"'
            if min_members:
                header += f"  min={min_members:,}"
            print(f"\n{header}  ({len(combined)} results, sorted by members)\n")

            if not combined:
                print("  No results found.")
            for e, kind in combined:
                print(fmt(e, kind))
            if bots:
                print(f"\n  -- {len(bots)} bots (not ranked by members) --")
                for b in bots:
                    print(f"  [BOT] @{b.username or '?'}  {b.first_name}")
        else:
            header = f'"{query}"'
            if min_members:
                header += f"  min={min_members:,}"
            print(f"\n{header}\n")

            if filter_type in ("all", "channel") and channels:
                print(f"-- CHANNELS ({len(channels)}) --")
                for c in channels:
                    print(fmt(c, "channel"))
                print()

            if filter_type in ("all", "group") and groups:
                print(f"-- GROUPS ({len(groups)}) --")
                for g in groups:
                    print(fmt(g, "group"))
                print()

            if filter_type in ("all", "bot") and bots:
                print(f"-- BOTS ({len(bots)}) --")
                for b in bots:
                    print(f"  @{b.username or '?'}  {b.first_name}")
                print()

            total = len(channels) + len(groups) + len(bots)
            if total == 0:
                print("  No results.")
            else:
                print(f"Total: {total}  ({len(channels)} channels, {len(groups)} groups, {len(bots)} bots)")


def main():
    p = argparse.ArgumentParser(description="Search Telegram public chats")
    p.add_argument("query")
    p.add_argument("--limit",  type=int, default=100,       help="Max results from Telegram (default 100)")
    p.add_argument("--type",   choices=["all","channel","group","bot"], default="all")
    p.add_argument("--sort",   choices=["relevance","members"], default="relevance",
                   help="Sort by Telegram relevance (default) or member count")
    p.add_argument("--min",    type=int, default=0,          dest="min_members",
                   help="Only show results with at least N members")
    p.add_argument("--max",    type=int, default=None,       dest="max_members",
                   help="Only show results with at most N members")
    args = p.parse_args()
    asyncio.run(search(args.query, args.limit, args.type, args.sort, args.min_members, args.max_members))


if __name__ == "__main__":
    main()
