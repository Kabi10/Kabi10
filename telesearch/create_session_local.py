"""
Create an authenticated Telethon session on your local Windows machine.
This generates a session file you can upload to the server.

Prerequisite: pip install telethon
"""
import asyncio
import os

# ── CONFIG ──────────────────────────────────────────────────────────────
# Using the same credentials as the server app
API_ID = 31671257
API_HASH = "ec880ffa330d0c826b77a7ffc3a16255"
SESSION_PATH = os.path.join(os.path.dirname(__file__), "session")


async def main():
    from telethon import TelegramClient

    client = TelegramClient(SESSION_PATH, API_ID, API_HASH)
    await client.connect()

    if await client.is_user_authorized():
        me = await client.get_me()
        print(f"\n Already authorized! Logged in as: {me.first_name} (@{me.username})")
        print(f" Session file: {SESSION_PATH}.session")
        await client.disconnect()
        return

    print("\n" + "=" * 60)
    print("  TG Search — Session Setup")
    print("=" * 60)
    print("\nThis logs you into Telegram so TG Search can perform")
    print("global channel/group searches. Your bot tokens alone")
    print("cannot do this — a real user account is required.")
    print("\n" + "-" * 60)

    phone = input("\nEnter your phone number (with +country code, e.g. +94771234567): ").strip()
    if not phone:
        print("No phone number. Exiting.")
        return

    try:
        await client.send_code_request(phone)
        print(f"\n Telegram sent a verification code to {phone}")
        code = input("Enter the 5-digit code: ").strip()

        await client.sign_in(phone, code)
        me = await client.get_me()
        print(f"\n  SUCCESS! Logged in as: {me.first_name} (@{me.username})")
        print(f"  Session file created: {SESSION_PATH}.session")
        print("\n  NEXT STEP: Upload this file to your server:")
        print(f"    scp {SESSION_PATH}.session root@65.21.53.29:/opt/telesearch/session")
        print("  Then run: ssh root@65.21.53.29 'supervisorctl restart telesearch'")

    except Exception as e:
        print(f"\n  ERROR: {e}")
    finally:
        await client.disconnect()


if __name__ == "__main__":
    asyncio.run(main())
