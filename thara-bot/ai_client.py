from datetime import datetime
from openai import AsyncOpenAI
from config import (
    DEEPSEEK_API_KEY, DEEPSEEK_MODEL,
    COST_INPUT_PER_TOKEN, COST_OUTPUT_PER_TOKEN,
    PERSONAS, DEFAULT_PERSONA,
)

client = AsyncOpenAI(
    api_key=DEEPSEEK_API_KEY,
    base_url="https://api.deepseek.com",
)


def build_system_prompt(persona_id: str, memory: str = "", display_name: str = "") -> str:
    now = datetime.now()
    time_str = now.strftime("%A, %B %d %Y — %I:%M %p")
    hour = now.hour

    if 5 <= hour < 12:
        time_ctx = "It's morning. Reflect this naturally in your tone."
    elif 12 <= hour < 17:
        time_ctx = "It's afternoon. Reflect this naturally in your tone."
    elif 17 <= hour < 21:
        time_ctx = "It's evening. Be warm and relaxed."
    else:
        time_ctx = "It's late night. Be soft and comforting."

    persona = PERSONAS.get(persona_id) or PERSONAS[DEFAULT_PERSONA]
    prompt = persona["system_prompt"]
    prompt += f"\n\nCurrent date/time: {time_str}. {time_ctx}\n"

    if display_name:
        prompt += f"\nThe user's preferred name is: {display_name}. Use it naturally.\n"

    if memory:
        prompt += f"\nWhat you remember about the user from past conversations:\n{memory}\n"

    return prompt


async def get_reply(
    messages: list[dict],
    persona_id: str = DEFAULT_PERSONA,
    memory: str = "",
    display_name: str = "",
) -> tuple[str, int, int, float]:
    system_prompt = build_system_prompt(persona_id, memory, display_name)
    full_messages = [{"role": "system", "content": system_prompt}] + messages

    response = await client.chat.completions.create(
        model=DEEPSEEK_MODEL,
        messages=full_messages,
        max_tokens=600,
        temperature=1.1,
    )

    reply             = response.choices[0].message.content
    usage             = response.usage
    prompt_tokens     = usage.prompt_tokens     if usage else 0
    completion_tokens = usage.completion_tokens if usage else 0
    cost              = (prompt_tokens * COST_INPUT_PER_TOKEN) + (completion_tokens * COST_OUTPUT_PER_TOKEN)

    return reply, prompt_tokens, completion_tokens, cost


async def summarise_memory(existing_memory: str, recent_messages: list[dict]) -> str:
    convo_text = "\n".join(
        f"{m['role'].upper()}: {m['content']}" for m in recent_messages[-30:]
    )
    prompt = (
        f"You are a memory extractor for a character AI system.\n\n"
        f"Existing memory about the user:\n{existing_memory or '(none yet)'}\n\n"
        f"Recent conversation:\n{convo_text}\n\n"
        "Extract and update a concise bullet-point list of important facts about the user "
        "(name, job, health issues, mood patterns, life events, preferences, things they mentioned). "
        "Keep it under 200 words. Only include clearly stated facts. Return only the bullet list."
    )

    response = await client.chat.completions.create(
        model=DEEPSEEK_MODEL,
        messages=[{"role": "user", "content": prompt}],
        max_tokens=300,
        temperature=0.3,
    )
    return response.choices[0].message.content.strip()
