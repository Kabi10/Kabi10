import asyncio
from telethon import TelegramClient
from telethon.tl.functions.messages import SearchGlobalRequest
from telethon.tl.types import InputPeerEmpty, InputMessagesFilterEmpty
from telethon.tl.functions.contacts import SearchRequest

client = TelegramClient('/opt/telesearch/session', 31671257, 'ec880ffa330d0c826b77a7ffc3a16255')

async def test():
    await client.connect()
    print('Connected:', client.is_connected())
    print('Auth:', await client.is_user_authorized())

    # Test SearchGlobalRequest
    print('\n--- messages.SearchGlobalRequest (groups_only=True) ---')
    r = await client(SearchGlobalRequest(
        q='crypto',
        filter=InputMessagesFilterEmpty(),
        min_date=None,
        max_date=None,
        offset_rate=0,
        offset_peer=InputPeerEmpty(),
        offset_id=0,
        limit=20,
        groups_only=True,
    ))
    print('Msgs:', len(r.messages))
    print('Chats:', len(r.chats))
    for c in r.chats[:5]:
        print('  ', type(c).__name__, getattr(c, 'title', 'N/A'), 'members:', getattr(c, 'participants_count', 'N/A'))

    # Test contacts.SearchRequest for comparison
    print('\n--- contacts.SearchRequest ---')
    r2 = await client(SearchRequest(q='crypto', limit=20))
    print('Chats:', len(r2.chats))
    for c in r2.chats[:5]:
        print('  ', type(c).__name__, getattr(c, 'title', 'N/A'), 'members:', getattr(c, 'participants_count', 'N/A'))

    # Test SearchGlobalRequest without groups_only (just to see difference)
    print('\n--- messages.SearchGlobalRequest (no filter) ---')
    r3 = await client(SearchGlobalRequest(
        q='crypto',
        filter=InputMessagesFilterEmpty(),
        min_date=None,
        max_date=None,
        offset_rate=0,
        offset_peer=InputPeerEmpty(),
        offset_id=0,
        limit=20,
    ))
    print('Msgs:', len(r3.messages))
    print('Chats:', len(r3.chats))
    for c in r3.chats[:5]:
        print('  ', type(c).__name__, getattr(c, 'title', 'N/A'), 'members:', getattr(c, 'participants_count', 'N/A'))

    await client.disconnect()

asyncio.run(test())
