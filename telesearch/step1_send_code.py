"""Step 1: Send verification code to phone number."""
import asyncio
import os
import json
from telethon import TelegramClient

API_ID = 31671257
API_HASH = "ec880ffa330d0c826b77a7ffc3a16255"
SESSION_PATH = os.path.join(os.path.dirname(__file__), "session")
STATE_FILE = os.path.join(os.path.dirname(__file__), ".tg_auth_state.json")

async def main():
    phone = "+16043737469"
    client = TelegramClient(SESSION_PATH, API_ID, API_HASH)
    await client.connect()

    if await client.is_user_authorized():
        me = await client.get_me()
        print(f"Already authorized as: {me.first_name} (@{me.username})")
        await client.disconnect()
        return

    print(f"Sending code to {phone}...")
    result = await client.send_code_request(phone)
    print("Code sent! Check your Telegram app/SMS.")

    # Save state for step 2 (phone_code_hash is required for sign_in)
    with open(STATE_FILE, "w") as f:
        json.dump({"phone": phone, "phone_code_hash": result.phone_code_hash}, f)
    print("State saved. Run step2_sign_in.py with the code.")

    await client.disconnect()

if __name__ == "__main__":
    asyncio.run(main())
