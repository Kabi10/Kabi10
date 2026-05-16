import logging
import asyncio
from telegram import Update, InlineKeyboardButton, InlineKeyboardMarkup
from telegram.ext import (
    Application, CommandHandler, MessageHandler,
    CallbackQueryHandler, filters, ContextTypes,
)

from config import TELEGRAM_TOKEN, MAX_CONTEXT_MESSAGES, PERSONAS, DEFAULT_PERSONA
from database import (
    init_db, upsert_user, log_message, load_recent_messages,
    get_user_memory, update_user_memory,
    get_user_persona, set_user_persona,
    get_display_name, set_display_name,
)
from ai_client import get_reply, summarise_memory

logging.basicConfig(
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    level=logging.INFO,
)
logger = logging.getLogger(__name__)

# ── In-session context ────────────────────────────────────────────────────────
sessions: dict[int, list[dict]] = {}
message_counts: dict[int, int]  = {}
SUMMARISE_EVERY = 15


async def get_session(user_id: int) -> list[dict]:
    if user_id not in sessions:
        sessions[user_id] = await load_recent_messages(user_id, MAX_CONTEXT_MESSAGES)
    return sessions[user_id]


def trim_context(messages: list[dict]) -> list[dict]:
    return messages[-MAX_CONTEXT_MESSAGES:]


# ── /start ────────────────────────────────────────────────────────────────────
async def start(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    user = update.effective_user
    await upsert_user(user.id, user.username, user.first_name)
    sessions[user.id] = []

    persona_id = await get_user_persona(user.id)
    p = PERSONAS.get(persona_id, PERSONAS[DEFAULT_PERSONA])

    await update.message.reply_text(
        f"Welcome to *Sanctum* 🏛️\n\n"
        f"You're now speaking with {p['name']} {p['emoji']}\n"
        f"_{p['description']}_\n\n"
        "Your conversations are private. No one else can read them.\n\n"
        "📋 Commands:\n"
        "/persona — choose your companion\n"
        "/profile — view your profile\n"
        "/setname [name] — set what I call you\n"
        "/memory — see what I remember about you\n"
        "/reset — clear conversation",
        parse_mode="Markdown",
    )


# ── /persona — show inline keyboard ──────────────────────────────────────────
async def persona_cmd(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    user_id = update.effective_user.id
    current = await get_user_persona(user_id)

    buttons = []
    for pid, p in PERSONAS.items():
        label = f"{'✅ ' if pid == current else ''}{p['emoji']} {p['name']} — {p['description']}"
        buttons.append([InlineKeyboardButton(label, callback_data=f"persona:{pid}")])

    await update.message.reply_text(
        "👥 *Choose your companion:*",
        parse_mode="Markdown",
        reply_markup=InlineKeyboardMarkup(buttons),
    )


# ── Persona selection callback ────────────────────────────────────────────────
async def persona_callback(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    query = update.callback_query
    await query.answer()

    user_id   = query.from_user.id
    persona_id = query.data.split(":", 1)[1]

    if persona_id not in PERSONAS:
        await query.edit_message_text("Unknown persona.")
        return

    current = await get_user_persona(user_id)
    if persona_id == current:
        await query.edit_message_text(f"You're already chatting with {PERSONAS[persona_id]['name']} {PERSONAS[persona_id]['emoji']}!")
        return

    await set_user_persona(user_id, persona_id)
    sessions[user_id] = []  # fresh context

    p = PERSONAS[persona_id]
    await query.edit_message_text(
        f"Switched to *{p['name']}* {p['emoji']}\n_{p['description']}_\n\nFresh start — say hello!",
        parse_mode="Markdown",
    )


# ── /profile ──────────────────────────────────────────────────────────────────
async def profile_cmd(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    user       = update.effective_user
    persona_id = await get_user_persona(user.id)
    name       = await get_display_name(user.id)
    memory     = await get_user_memory(user.id)
    p          = PERSONAS.get(persona_id, PERSONAS[DEFAULT_PERSONA])

    text = (
        f"👤 *Your Profile*\n\n"
        f"**Telegram:** {user.first_name}" + (f" (@{user.username})" if user.username else "") + "\n"
        f"**Called by:** {name or '(not set)'}\n"
        f"**Active persona:** {p['emoji']} {p['name']}\n\n"
        f"💭 **Memory:** {'Set ✅' if memory else 'Not yet — keep chatting!'}\n\n"
        f"Use `/setname YourName` to set what I call you."
    )
    await update.message.reply_text(text, parse_mode="Markdown")


# ── /setname ──────────────────────────────────────────────────────────────────
async def setname_cmd(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    user_id = update.effective_user.id
    if not context.args:
        await update.message.reply_text("Usage: `/setname YourName`", parse_mode="Markdown")
        return

    name = " ".join(context.args).strip()[:50]
    await set_display_name(user_id, name)

    persona_id = await get_user_persona(user_id)
    p = PERSONAS.get(persona_id, PERSONAS[DEFAULT_PERSONA])
    await update.message.reply_text(
        f"Got it! {p['name']} will now call you *{name}* {p['emoji']}",
        parse_mode="Markdown",
    )


# ── /memory ───────────────────────────────────────────────────────────────────
async def memory_cmd(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    user_id = update.effective_user.id
    memory  = await get_user_memory(user_id)
    if memory:
        await update.message.reply_text(f"💭 *What I remember about you:*\n\n{memory}", parse_mode="Markdown")
    else:
        await update.message.reply_text("I'm still getting to know you! Keep chatting with me.")


# ── /reset ────────────────────────────────────────────────────────────────────
async def reset(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    user_id = update.effective_user.id
    sessions[user_id] = []
    persona_id = await get_user_persona(user_id)
    p = PERSONAS.get(persona_id, PERSONAS[DEFAULT_PERSONA])
    await update.message.reply_text(f"Context cleared! Fresh start with {p['name']} {p['emoji']}")


# ── Chat handler ──────────────────────────────────────────────────────────────
async def chat(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    user         = update.effective_user
    user_id      = user.id
    user_message = update.message.text

    await upsert_user(user_id, user.username, user.first_name)

    session = await get_session(user_id)
    session.append({"role": "user", "content": user_message})
    await log_message(user_id, "user", user_message)
    sessions[user_id] = trim_context(session)

    await context.bot.send_chat_action(chat_id=update.effective_chat.id, action="typing")

    persona_id   = await get_user_persona(user_id)
    memory       = await get_user_memory(user_id)
    display_name = await get_display_name(user_id)

    try:
        reply, prompt_tokens, completion_tokens, cost = await get_reply(
            sessions[user_id], persona_id, memory, display_name
        )

        sessions[user_id].append({"role": "assistant", "content": reply})
        await log_message(user_id, "assistant", reply, prompt_tokens, completion_tokens, cost, persona_id)

        logger.info(
            "User %s | persona: %s | %d+%d tokens | $%.6f",
            user_id, persona_id, prompt_tokens, completion_tokens, cost,
        )

        await update.message.reply_text(reply)

        message_counts[user_id] = message_counts.get(user_id, 0) + 1
        if message_counts[user_id] % SUMMARISE_EVERY == 0:
            asyncio.create_task(_update_memory(user_id, memory, sessions[user_id]))

    except Exception as e:
        logger.error("DeepSeek error for user %s: %s", user_id, e)
        fallbacks = [
            "Sorry, I got a little distracted. Could you say that again?",
            "One second — something came up. What were you saying?",
            "My mind wandered for a moment! Repeat that for me?",
        ]
        await update.message.reply_text(fallbacks[message_counts.get(user_id, 0) % len(fallbacks)])


async def _update_memory(user_id: int, existing_memory: str, recent_messages: list[dict]) -> None:
    try:
        new_memory = await summarise_memory(existing_memory, recent_messages)
        await update_user_memory(user_id, new_memory)
        logger.info("Updated memory for user %s", user_id)
    except Exception as e:
        logger.warning("Memory update failed for user %s: %s", user_id, e)


# ── Main ──────────────────────────────────────────────────────────────────────
async def post_init(app: Application) -> None:
    await init_db()
    logger.info("Database ready")


def main() -> None:
    app = (
        Application.builder()
        .token(TELEGRAM_TOKEN)
        .post_init(post_init)
        .build()
    )

    app.add_handler(CommandHandler("start",   start))
    app.add_handler(CommandHandler("reset",   reset))
    app.add_handler(CommandHandler("persona", persona_cmd))
    app.add_handler(CommandHandler("profile", profile_cmd))
    app.add_handler(CommandHandler("setname", setname_cmd))
    app.add_handler(CommandHandler("memory",  memory_cmd))
    app.add_handler(CallbackQueryHandler(persona_callback, pattern=r"^persona:"))
    app.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, chat))

    logger.info("Sanctum online 🏛️")
    app.run_polling()


if __name__ == "__main__":
    main()
