"""Step 2: Complete sign-in with verification code."""
import asyncio
import os
import sys
import json
from telethon import TelegramClient

API_ID = 31671257
API_HASH = "ec880ffa330d0c826b77a7ffc3a16255"
SESSION_PATH = os.path.join(os.path.dirname(__file__), "session")
STATE_FILE = os.path.join(os.path.dirname(__file__), ".tg_auth_state.json")

async def main():
    if not os.path.exists(STATE_FILE):
        print("Error: Run step1_send_code.py first.")
        sys.exit(1)

    with open(STATE_FILE) as f:
        state = json.load(f)
    phone = state["phone"]
    phone_code_hash = state["phone_code_hash"]

    if len(sys.argv) < 2:
        print("Usage: python step2_sign_in.py <5-digit-code>")
        sys.exit(1)

    code = sys.argv[1].strip()

    client = TelegramClient(SESSION_PATH, API_ID, API_HASH)
    await client.connect()

    print(f"Signing in with code {code}...")
    await client.sign_in(phone=phone, code=code, phone_code_hash=phone_code_hash)
    me = await client.get_me()
    print(f"\nSUCCESS! Logged in as: {me.first_name} (@{me.username})")
    print(f"Session file: {SESSION_PATH}.session")
    print("\nUpload to server:")
    print(f'  scp {SESSION_PATH}.session root@65.21.53.29:/opt/telesearch/session')
    print("Then restart: ssh root@65.21.53.29 'supervisorctl restart telesearch'")

    os.remove(STATE_FILE)
    await client.disconnect()

if __name__ == "__main__":
    asyncio.run(main())
