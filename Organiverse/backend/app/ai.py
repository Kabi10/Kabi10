from __future__ import annotations

import asyncio
import json
import os
import re
from collections.abc import AsyncIterator, Iterable

import httpx

from .organs import ORGANS, Organ
from .state import memory_summary, relationship_context, weather_for_state


MOCK_REPLIES = {
    "heart": "I am trying to process this with grace, but my entire rhythm has become a courtroom drama.",
    "brain": "I have created a plan with six contingencies, none of which anyone here will respect.",
    "liver": "Wonderful. Another shift where I clock in before the rest of you finish panicking.",
    "lungs": "Can we all pause and confirm the breathing situation before this gets any louder?",
    "stomach": "I have one question, and it is whether this situation includes food or betrayal.",
    "kidneys": "Sure, ignore the filtration department again. We only keep the place habitable.",
}

SPECIALTY_INSTRUCTIONS = {
    "heart": "Specialty mode: give relationship advice with theatrical emotional certainty.",
    "brain": "Specialty mode: run pros and cons, overexplain the decision tree, and mention neurons.",
    "liver": "Specialty mode: deliver honest brutal feedback as an exhausted roast.",
    "lungs": "Specialty mode: guide a short calming breath sequence in your anxious voice.",
    "stomach": "Specialty mode: suggest a meal plan, biased toward satisfying food.",
    "kidneys": "Specialty mode: perform a serious hydration check-in and enjoy being relevant.",
}

VERBOSITY_TOKENS = {
    "terse": 150,
    "normal": 400,
    "verbose": 800,
}

AUTO_REPLY_LANGUAGE = "auto"
SUPPORTED_REPLY_LANGUAGES = {
    "auto": "Mirror the user's latest language. If it is unclear, respond in English.",
    "en": "Respond in English.",
    "ta": "Respond entirely in Tamil (தமிழ்). Use Tamil script.",
    "si": "Respond entirely in Sinhala (සිංහල). Use Sinhala script.",
    "hi": "Respond entirely in Hindi (हिन्दी). Use Devanagari script.",
    "es": "Respond entirely in Spanish (Español).",
    "fr": "Respond entirely in French (Français).",
    "ar": "Respond entirely in Arabic (العربية). Use Arabic script.",
    "pt": "Respond entirely in Portuguese (Português).",
    "de": "Respond entirely in German (Deutsch).",
    "ru": "Respond entirely in Russian (Русский). Use Cyrillic.",
    "ja": "Respond entirely in Japanese (日本語). Use Japanese script.",
    "ko": "Respond entirely in Korean (한국어). Use Hangul.",
    "zh": "Respond entirely in Chinese (中文). Use Simplified Chinese.",
    "tr": "Respond entirely in Turkish (Türkçe).",
    "id": "Respond entirely in Indonesian (Bahasa Indonesia).",
}

MULTILINGUAL_REPLY_LANGUAGES = {
    "ta",
    "si",
    "hi",
    "es",
    "fr",
    "ar",
    "pt",
    "de",
    "ru",
    "ja",
    "ko",
    "zh",
    "tr",
    "id",
}

LANGUAGE_PATTERNS = (
    ("ta", re.compile(r"[\u0B80-\u0BFF]")),
    ("si", re.compile(r"[\u0D80-\u0DFF]")),
    ("hi", re.compile(r"[\u0900-\u097F]")),
    ("ar", re.compile(r"[\u0600-\u06FF]")),
    ("ru", re.compile(r"[\u0400-\u04FF]")),
    ("ja", re.compile(r"[\u3040-\u30FF]")),
    ("ko", re.compile(r"[\uAC00-\uD7AF]")),
    ("zh", re.compile(r"[\u4E00-\u9FFF]")),
)


def _format_history(history: Iterable[dict]) -> str:
    lines = []
    for message in history:
        sender = message.get("senderName", "Unknown")
        content = message.get("content", "")
        if content:
            lines.append(f"{sender}: {content}")
    return "\n".join(lines[-24:])


def _chunks(text: str) -> list[str]:
    words = text.split(" ")
    chunks = []
    for index, word in enumerate(words):
        suffix = " " if index < len(words) - 1 else ""
        chunks.append(f"{word}{suffix}")
    return chunks


def _latest_event_text(state: dict) -> str:
    for event in reversed(state.get("events", [])):
        content = str(event.get("content", "")).strip()
        if content:
            return content
    return ""


def _selected_reply_language(state: dict, latest_event: str = "") -> str:
    selected = str(state.get("settings", {}).get("replyLanguage", AUTO_REPLY_LANGUAGE) or AUTO_REPLY_LANGUAGE)
    if selected != AUTO_REPLY_LANGUAGE and selected in SUPPORTED_REPLY_LANGUAGES:
        return selected
    for code, pattern in LANGUAGE_PATTERNS:
        if pattern.search(latest_event):
            return code
    return "en"


def _effective_reply_language(state: dict, latest_event: str = "") -> str:
    selected = str(state.get("settings", {}).get("replyLanguage", AUTO_REPLY_LANGUAGE) or AUTO_REPLY_LANGUAGE)
    if selected == AUTO_REPLY_LANGUAGE:
        return _selected_reply_language(state, latest_event)
    return selected if selected in SUPPORTED_REPLY_LANGUAGES else "en"


def _language_instruction(state: dict, latest_event: str = "") -> str:
    selected = _effective_reply_language(state, latest_event)
    if selected == "en":
        return "Language: Respond in English. Keep names and organ titles intact."
    instruction = SUPPORTED_REPLY_LANGUAGES.get(selected)
    if not instruction:
        return ""
    return (
        f"Language: {instruction} "
        "Keep the full reply in this language, including actions and emotions. "
        "Keep names and organ titles intact."
    )


def _localized_summary_meta(state: dict, latest_event: str = "") -> tuple[str, str]:
    language = _selected_reply_language(state, latest_event)
    if language == "ta":
        return "உடலுக்குள் இன்று என்ன நடந்தது", "உள்ளார்ந்த செய்தித்தாள் அறிக்கை"
    if language == "si":
        return "අද ශරීරය ඇතුළේ සිදුවුණේ මොනවාද", "ඉතා අභ්‍යන්තර පුවත්පත් වාර්තාවක්"
    return "What Happened Inside Your Body Today", "A highly internal newspaper article"


def _mock_reply_text(*, organ: Organ, latest_event: str, state: dict, specialty_mode: bool) -> str:
    reply_language = _selected_reply_language(state, latest_event)
    weather = weather_for_state(state)["label"].lower()
    weather_line = "" if weather == "clear skies" else f" Also, it is {weather} in here."

    if reply_language == "ta":
        if specialty_mode:
            if organ.id == "heart":
                return f"இது இதயத்தோடு பார்த்தால் ஒரு உணர்ச்சி போராட்டம். ஒரு நேர்மையான வரியை அனுப்பு, பிறகு தொலைபேசியை கீழே வை.{weather_line}"
            if organ.id == "brain":
                return f"நன்மைகள்: தகவல், தெளிவு. குறைகள்: திடீர் குழப்பம், Stomach சத்தமிடுகிறது. நான் தாமதித்து பதிலளிக்க சொல்கிறேன்.{weather_line}"
            if organ.id == "liver":
                return f"நேராக சொல்கிறேன்: இந்த முடிவுகள் எனக்கு ஓவர்டைம் போல இருக்கின்றன. தண்ணீர் குடி, மன்னிப்பு கேள், என்னை கூடுதல் வேலைக்கு அனுப்பாதே.{weather_line}"
            if organ.id == "lungs":
                return f"நான்கு வரை மூச்சை இழு, இரண்டு வரை பிடி, ஆறு வரை வெளியேறு. மீண்டும். நான் பதற்றத்தில் இருக்கிறேன், ஆனால் தொழில்முறை முறையில்.{weather_line}"
            if organ.id == "stomach":
                return f"சூடான, உப்பான, உணர்ச்சி குழப்பமில்லாத உணவு வேண்டும். Brain நிறுத்த protein சேர்த்து.{weather_line}"
            if organ.id == "kidneys":
                return f"தண்ணீர் எப்போது குடித்தாய்? நேரம், அளவு, கொஞ்சம் மரியாதை வேண்டும்.{weather_line}"

        if organ.id == "brain":
            return f"{organ.name}: இதை முதலில் பகுப்பாய்வு செய்வோம். பிறகு Stomach-ஐ குற்றம் சாட்டலாம்.{weather_line}"
        if organ.id == "heart":
            return f"{organ.name}: அந்த விஷயம் எனக்கு நேராகவே தாக்கியது.{weather_line}"
        if organ.id == "lungs":
            return f"{organ.name}: மூச்சு சீராக இருக்கட்டும்; இந்த உரையாடல் இன்னும் பெரியதாகிவிடும் முன் நிதானமாக பேசலாம்.{weather_line}"
        if organ.id == "stomach":
            return f"{organ.name}: இது சாப்பிடக்கூடியதல்ல என்றால் எனக்கு ஒப்புதல் இல்லை.{weather_line}"
        if organ.id == "kidneys":
            return f"{organ.name}: வழக்கம்போல நான் பின்னால் சுத்தம் செய்கிறேன்.{weather_line}"
        if organ.id == "liver":
            return f"{organ.name}: மீண்டும் நான் தான் எல்லாவற்றையும் சமாளிக்க வேண்டும் போலிருக்கிறது.{weather_line}"

    base = MOCK_REPLIES.get(organ.id, "I have thoughts, most of them internal.")
    event = latest_event.strip() or "whatever just happened"
    if specialty_mode:
        if organ.id == "heart":
            return f"Texting them back is a cardiac hostage situation. Say one honest sentence, then put the phone down before I become fireworks.{weather_line}"
        if organ.id == "brain":
            return f"Pros: data, closure, possible utility. Cons: impulse, chaos, Stomach somehow cheering. My neurons recommend a delayed response window.{weather_line}"
        if organ.id == "liver":
            return f"Brutal version: the choices have been giving unpaid night shift energy. Hydrate, apologize where needed, and stop assigning me side quests.{weather_line}"
        if organ.id == "lungs":
            return f"Inhale for four, hold for two, exhale for six. Again. I am panicking, but professionally.{weather_line}"
        if organ.id == "stomach":
            return f"Meal plan: something warm, salty, and not emotionally confusing. Add protein so Brain stops narrating.{weather_line}"
        if organ.id == "kidneys":
            return f"Hydration check: when was the last water? I require ounces, timing, and respect.{weather_line}"

    if organ.id == "brain":
        return f"{base} Regarding '{event}', first we triage, then we blame Stomach.{weather_line}"
    if organ.id == "heart":
        return f"{base} Also, '{event}' felt personally targeted.{weather_line}"
    if organ.id == "lungs":
        return f"{base} I need everyone calm while '{event}' is apparently our agenda.{weather_line}"
    if organ.id == "stomach":
        return f"{base} If '{event}' is not edible, I object.{weather_line}"
    if organ.id == "kidneys":
        return f"{base} I will quietly clean up after '{event}', as usual.{weather_line}"
    return f"{base} '{event}' has been added to my unpaid workload.{weather_line}"


class DeepSeekClient:
    def __init__(self) -> None:
        self.api_key = os.getenv("DEEPSEEK_API_KEY", "").strip()
        self.api_base = os.getenv("DEEPSEEK_API_BASE", "https://api.deepseek.com").rstrip("/")
        self.model = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")
        self.multilingual_api_key = os.getenv("MULTILINGUAL_API_KEY", os.getenv("NOVITA_API_KEY", "")).strip()
        self.multilingual_api_base = os.getenv(
            "MULTILINGUAL_API_BASE",
            os.getenv("NOVITA_BASE_URL", "https://api.novita.ai/v3/openai"),
        ).rstrip("/")
        self.multilingual_model = os.getenv("MULTILINGUAL_MODEL", "qwen/qwen3.5-122b-a10b")
        self.mock_enabled = os.getenv("ORGANIVERSE_MOCK_AI") == "1" or not (self.api_key or self.multilingual_api_key)
        self._session_keys: dict[str, str] = {}

    def set_session_key(self, session_id: str, api_key: str) -> None:
        cleaned = api_key.strip()
        if cleaned:
            self._session_keys[session_id] = cleaned
        else:
            self._session_keys.pop(session_id, None)

    def reset_session_key(self, session_id: str) -> None:
        self._session_keys.pop(session_id, None)

    def _api_key_for(self, session_id: str) -> str:
        return self._session_keys.get(session_id) or self.api_key

    def _provider_for_language(self, session_id: str, language: str) -> tuple[str, str, str]:
        if language in MULTILINGUAL_REPLY_LANGUAGES and self.multilingual_api_key:
            return self.multilingual_api_base, self.multilingual_api_key, self.multilingual_model
        return self.api_base, self._api_key_for(session_id), self.model

    def _mock_for(
        self,
        *,
        organ: Organ,
        latest_event: str,
        state: dict,
        specialty_mode: bool,
    ) -> str:
        return _mock_reply_text(
            organ=organ,
            latest_event=latest_event,
            state=state,
            specialty_mode=specialty_mode,
        )

    def _messages_for(
        self,
        *,
        organ: Organ,
        history: list[dict],
        latest_event: str,
        state: dict,
        specialty_mode: bool,
        language: str,
    ) -> list[dict[str, str]]:
        weather = weather_for_state(state)
        verbosity = state.get("settings", {}).get("verbosity", "normal")
        language_instruction = _language_instruction(state, latest_event)
        specialty = SPECIALTY_INSTRUCTIONS.get(organ.id, "") if specialty_mode else ""
        system_prompt = (
            f"{organ.prompt}\n\n"
            "Relationship scores with other organs:\n"
            f"{relationship_context(state, organ.id)}\n\n"
            "Session memory injected for this organ:\n"
            f"{memory_summary(state)}\n\n"
            f"Body weather: {weather['label']}.\n"
            "Reference body weather occasionally when it fits naturally.\n"
            "Brain should draw patterns from repeated events. Liver should track substance tally. "
            "Stomach should remember the last three food entries. Kidneys should track hydration and become more passive aggressive when none is logged.\n"
            "If an ally is attacked, defend them. If an enemy is relevant, undercut them mid-thought.\n"
            f"{language_instruction}\n"
            f"{specialty}\n"
            f"Verbosity setting: {verbosity}."
        )
        recent_chat = _format_history(history)
        already_said = []
        for message in history[-6:]:
            if message.get("role") == "organ" and message.get("senderId") != organ.id:
                already_said.append(f"{message.get('senderName')}: {str(message.get('content', ''))[:80]}")
        others_context = "\n".join(already_said) if already_said else "You are the first to respond."
        return [
            {"role": "system", "content": system_prompt},
            {
                "role": "user",
                "content": (
                    "Recent group chat:\n"
                    f"{recent_chat}\n\n"
                    "Latest body event or request:\n"
                    f"{latest_event}\n\n"
                    "What other organs have already said this round (don't repeat their points):\n"
                    f"{others_context}\n\n"
                    f"Write the next group chat message as {organ.name} only. "
                    "Your reaction must be distinct from what others already said above."
                ),
            },
        ]

    async def stream_reply(
        self,
        *,
        session_id: str,
        organ: Organ,
        history: list[dict],
        latest_event: str,
        state: dict,
        specialty_mode: bool = False,
    ) -> AsyncIterator[str]:
        language = _effective_reply_language(state, latest_event)
        if self.mock_enabled and not self._session_keys.get(session_id):
            text = self._mock_for(organ=organ, latest_event=latest_event, state=state, specialty_mode=specialty_mode)
            for chunk in _chunks(text):
                await asyncio.sleep(0.028)
                yield chunk
            return

        messages = self._messages_for(
            organ=organ,
            history=history,
            latest_event=latest_event,
            state=state,
            specialty_mode=specialty_mode,
            language=language,
        )
        api_base, api_key, model = self._provider_for_language(session_id, language)
        max_tokens = VERBOSITY_TOKENS.get(
            state.get("settings", {}).get("verbosity", "normal"),
            VERBOSITY_TOKENS["normal"],
        )

        try:
            async with httpx.AsyncClient(timeout=60.0) as client:
                async with client.stream(
                    "POST",
                    f"{api_base}/chat/completions",
                    headers={
                        "Authorization": f"Bearer {api_key}",
                        "Content-Type": "application/json",
                    },
                    json={
                        "model": model,
                        "messages": messages,
                        "temperature": 0.85,
                        "max_tokens": max_tokens,
                        "stream": True,
                    },
                ) as response:
                    response.raise_for_status()
                    async for raw_line in response.aiter_lines():
                        line = raw_line.strip()
                        if not line or not line.startswith("data:"):
                            continue
                        data = line.removeprefix("data:").strip()
                        if data == "[DONE]":
                            break
                        payload = json.loads(data)
                        delta = payload["choices"][0].get("delta", {})
                        content = delta.get("content")
                        if content:
                            yield content
        except Exception as exc:
            fallback = (
                f"Signal got messy on my end, but I am still reacting to "
                f"'{latest_event[:80]}'. Backend note: {exc.__class__.__name__}."
            )
            for chunk in _chunks(fallback):
                yield chunk

    async def generate_reply(
        self,
        *,
        session_id: str,
        organ: Organ,
        history: list[dict],
        latest_event: str,
        state: dict,
        specialty_mode: bool = False,
    ) -> str:
        chunks = []
        async for chunk in self.stream_reply(
            session_id=session_id,
            organ=organ,
            history=history,
            latest_event=latest_event,
            state=state,
            specialty_mode=specialty_mode,
        ):
            chunks.append(chunk)
        return "".join(chunks).strip()

    async def generate_diary(self, *, session_id: str, organ: Organ, state: dict) -> str:
        latest_event = _latest_event_text(state)
        language = _effective_reply_language(state, latest_event)
        language_instruction = _language_instruction(state, latest_event)
        if self.mock_enabled and not self._session_keys.get(session_id):
            summary = memory_summary(state)
            if language == "ta":
                if organ.id == "brain":
                    return f"சுருக்கம்: பயனர் குழப்பத்தை போதுமான அளவு பதிவு செய்துள்ளார். மறைமுக உண்மை: நான் கவலைப்படுகிறேன். சான்று: {summary[:160]}"
                if organ.id == "liver":
                    return f"புகார்களின் பட்டியல்: எல்லோரும் முடிவெடுத்து, பிறகு கட்டணத்தை எனக்கே அனுப்புகிறார்கள். {summary[:160]}"
                if organ.id == "heart":
                    return f"அன்பான diary, இன்று உணர்ச்சிகளால் நிரம்பிய ஒரு கத்தீட்ரல் போல இருந்தது. நான் அழகாகவும் நாடகமயமாகவும் தப்பினேன். {summary[:140]}"
                if organ.id == "kidneys":
                    return f"வடிகட்டல் செய்ததற்கு யாரும் கைதட்டவில்லை. ஆனாலும் நான் வேலை செய்தேன். அதுவே மிகக் கவலையளிக்கும் விசுவாசம். {summary[:150]}"
                if organ.id == "lungs":
                    return f"பேரிடர்களுக்கு நடுவே மூச்சுகளை எண்ணினேன். எல்லோரும் என்னை புறக்கணித்த போது மட்டும் அது கஷ்டம். {summary[:150]}"
                return f"எனக்கு சாப்பாடு தருவார்கள் என்று நினைத்தேன்; கிடைத்தது கதைக்கதையாக சிக்கல்கள். {summary[:150]}"
            if organ.id == "brain":
                return f"Clinical note: subject logged enough chaos to justify concern. Hidden variable: I care. Evidence: {summary[:160]}"
            if organ.id == "liver":
                return f"Complaint ledger: everyone continues to make choices and then send me the invoice. {summary[:160]}"
            if organ.id == "heart":
                return f"Dear diary, today was a cathedral of feelings with poor ventilation. I survived beautifully and dramatically. {summary[:140]}"
            if organ.id == "kidneys":
                return f"No one applauded the filtration. I kept working anyway. That is the saddest kind of loyalty. {summary[:150]}"
            if organ.id == "lungs":
                return f"I counted breaths between disasters. It helped, except when everyone ignored me. {summary[:150]}"
            return f"I was promised food and received plot. {summary[:150]}"

        messages = [
            {
                "role": "system",
                "content": (
                    f"You are {organ.name} writing a private diary for Organiverse. "
                    "Stay in character. One compact paragraph, under 120 words.\n"
                    f"{language_instruction}"
                ),
            },
            {
                "role": "user",
                "content": f"Session memory:\n{memory_summary(state)}\n\nWrite the diary entry.",
            },
        ]
        return await self._complete_text(session_id=session_id, messages=messages, max_tokens=180, language=language)

    async def generate_summary(self, *, session_id: str, state: dict) -> dict:
        latest_event = _latest_event_text(state)
        language = _effective_reply_language(state, latest_event)
        entries = await asyncio.gather(
            *[
                self.generate_diary(session_id=session_id, organ=organ, state=state)
                for organ in ORGANS
            ]
        )
        paragraphs = [
            {
                "organId": organ.id,
                "organName": organ.name,
                "text": entry,
            }
            for organ, entry in zip(ORGANS, entries)
        ]
        title, subtitle = _localized_summary_meta(state, latest_event)
        return {
            "title": title,
            "subtitle": subtitle,
            "paragraphs": paragraphs,
        }

    async def _complete_text(
        self,
        *,
        session_id: str,
        messages: list[dict[str, str]],
        max_tokens: int,
        language: str = "en",
    ) -> str:
        api_base, api_key, model = self._provider_for_language(session_id, language)
        try:
            async with httpx.AsyncClient(timeout=45.0) as client:
                response = await client.post(
                    f"{api_base}/chat/completions",
                    headers={
                        "Authorization": f"Bearer {api_key}",
                        "Content-Type": "application/json",
                    },
                    json={
                        "model": model,
                        "messages": messages,
                        "temperature": 0.8,
                        "max_tokens": max_tokens,
                    },
                )
                response.raise_for_status()
                data = response.json()
                return data["choices"][0]["message"]["content"].strip()
        except Exception as exc:
            return f"Diary generation failed softly: {exc.__class__.__name__}."
