from __future__ import annotations

import re
from datetime import datetime, timezone

from .organs import ORGANS, Organ


ORGAN_BY_ID = {organ.id: organ for organ in ORGANS}
ORGAN_IDS = [organ.id for organ in ORGANS]
ORGAN_NAME_TO_ID = {organ.name.lower(): organ.id for organ in ORGANS}
ORGAN_NAME_TO_ID.update({organ.id.lower(): organ.id for organ in ORGANS})

DEFAULT_SETTINGS = {
    "enabledOrgans": ORGAN_IDS,
    "verbosity": "normal",
    "soundEnabled": False,
    "achievementsEnabled": True,
    "volume": 0.35,
    "replyLanguage": "auto",
}

NATURAL_RELATIONSHIPS = {
    "brain|stomach": -20,
    "heart|lungs": 15,
    "kidneys|liver": 10,
}

ACHIEVEMENTS = {
    "first_argument": {
        "title": "First Argument",
        "organId": "brain",
        "message": "Brain: Finally, some intellectual conflict.",
    },
    "enabler": {
        "title": "Enabler",
        "organId": "liver",
        "message": "Liver: I've stopped being surprised.",
    },
    "hypochondriac": {
        "title": "Hypochondriac",
        "organId": "lungs",
        "message": "Lungs: See? I told you this would happen.",
    },
    "peacemaker": {
        "title": "Peacemaker",
        "organId": "kidneys",
        "message": "Kidneys: We noticed. We're choosing to be cautiously optimistic.",
    },
    "oversharer": {
        "title": "Oversharer",
        "organId": "brain",
        "message": "Brain: I'm building a very detailed profile of your poor decisions.",
    },
    "ignored": {
        "title": "Finally Acknowledged",
        "organId": "kidneys",
        "message": "Kidneys: ...oh. You know we exist.",
    },
    "relationship_goals": {
        "title": "Relationship Goals",
        "organId": "heart",
        "message": "Heart: This is beautiful. I'm literally beating faster.",
    },
    "civil_war": {
        "title": "Civil War",
        "organId": "stomach",
        "message": "Stomach: I don't want to talk about it.",
    },
}

FOOD_WORDS = {
    "ate",
    "burger",
    "pizza",
    "taco",
    "fries",
    "sandwich",
    "snack",
    "meal",
    "breakfast",
    "lunch",
    "dinner",
    "cake",
    "chips",
    "salad",
    "protein",
}
JUNK_WORDS = {"burger", "pizza", "fries", "chips", "cake", "candy", "donut", "fast food"}
ALCOHOL_WORDS = {"alcohol", "beer", "wine", "vodka", "whiskey", "tequila", "cocktail", "drink"}
CAFFEINE_WORDS = {"coffee", "espresso", "caffeine", "energy drink", "latte", "cold brew"}
WATER_WORDS = {"water", "hydrated", "hydration", "electrolyte"}
EXERCISE_WORDS = {"run", "running", "marathon", "workout", "gym", "walk", "hike", "exercise"}
STRESS_WORDS = {"stress", "stressed", "anxiety", "panic", "spiral", "deadline", "fight", "overwhelmed"}
CRISIS_WORDS = {"crisis", "panic attack", "chest pain", "can't breathe", "cant breathe", "emergency"}
HEALTHY_WORDS = {"salad", "water", "walk", "slept", "sleep", "meditated", "stretch", "vegetables"}


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def pair_key(left: str, right: str) -> str:
    return "|".join(sorted([left, right]))


def clamp(value: int, low: int = -100, high: int = 100) -> int:
    return max(low, min(high, value))


def create_initial_state(session_id: str) -> dict:
    relationships = {}
    for index, left in enumerate(ORGAN_IDS):
        for right in ORGAN_IDS[index + 1 :]:
            relationships[pair_key(left, right)] = 0
    relationships.update(NATURAL_RELATIONSHIPS)

    created_at = now_iso()
    return {
        "sessionId": session_id,
        "createdAt": created_at,
        "updatedAt": created_at,
        "relationships": relationships,
        "moods": {organ_id: 0 for organ_id in ORGAN_IDS},
        "events": [],
        "eventCount": 0,
        "crisisCount": 0,
        "crisisActive": False,
        "rebellionActive": False,
        "substanceTally": {},
        "junkFoodCount": 0,
        "hydrationCount": 0,
        "lastFoods": [],
        "achievements": {},
        "diaries": {},
        "settings": DEFAULT_SETTINGS.copy(),
    }


def ensure_state_shape(session_id: str, state: dict | None) -> dict:
    if not state:
        return create_initial_state(session_id)

    fresh = create_initial_state(session_id)
    fresh.update(state)
    fresh["relationships"] = {
        **fresh["relationships"],
        **state.get("relationships", {}),
    }
    fresh["moods"] = {**fresh["moods"], **state.get("moods", {})}
    fresh["settings"] = {**DEFAULT_SETTINGS, **state.get("settings", {})}
    return fresh


def get_relationship(state: dict, left: str, right: str) -> int:
    return int(state["relationships"].get(pair_key(left, right), 0))


def shift_relationship(state: dict, left: str, right: str, delta: int) -> None:
    if left == right:
        return
    key = pair_key(left, right)
    state["relationships"][key] = clamp(int(state["relationships"].get(key, 0)) + delta)


def shift_mood(state: dict, organ_id: str, delta: int) -> None:
    state["moods"][organ_id] = clamp(int(state["moods"].get(organ_id, 0)) + delta)


def analyze_event(content: str) -> dict:
    text = content.lower()
    words = set(re.findall(r"[a-z']+", text))

    def has_any(terms: set[str]) -> bool:
        return any(term in text or term in words for term in terms)

    substances = []
    if has_any(ALCOHOL_WORDS):
        substances.append("alcohol")
    if has_any(CAFFEINE_WORDS):
        substances.append("caffeine")

    foods = []
    if has_any(FOOD_WORDS):
        foods = [
            word
            for word in FOOD_WORDS
            if word in text and word not in {"ate", "meal", "lunch", "dinner"}
        ]
        foods = foods[:3] or ["food"]

    return {
        "food": has_any(FOOD_WORDS),
        "alcohol": has_any(ALCOHOL_WORDS),
        "caffeine": has_any(CAFFEINE_WORDS),
        "water": has_any(WATER_WORDS),
        "exercise": has_any(EXERCISE_WORDS),
        "stress": has_any(STRESS_WORDS),
        "crisis": has_any(CRISIS_WORDS),
        "junk": has_any(JUNK_WORDS),
        "healthy": has_any(HEALTHY_WORDS) or has_any(WATER_WORDS) or has_any(EXERCISE_WORDS),
        "substances": substances,
        "foods": foods,
    }


def parse_specialty_mention(content: str) -> tuple[str | None, str]:
    match = re.match(r"^\s*@([a-zA-Z]+)\b\s*(.*)$", content)
    if not match:
        return None, content
    organ_id = ORGAN_NAME_TO_ID.get(match.group(1).lower())
    return organ_id, match.group(2).strip() or content


def apply_user_event(state: dict, content: str, user_name: str = "You") -> list[dict]:
    analysis = analyze_event(content)
    state["eventCount"] = int(state.get("eventCount", 0)) + 1
    state["crisisActive"] = bool(analysis["crisis"])
    event = {
        "content": content,
        "userName": user_name,
        "createdAt": now_iso(),
        "categories": [key for key, value in analysis.items() if value is True],
    }
    state.setdefault("events", []).append(event)
    state["events"] = state["events"][-40:]

    if analysis["crisis"]:
        state["crisisCount"] = int(state.get("crisisCount", 0)) + 1
        shift_mood(state, "heart", -22)
        shift_mood(state, "lungs", -24)
        shift_mood(state, "brain", -14)
        shift_relationship(state, "heart", "lungs", 8)
    if analysis["stress"]:
        shift_mood(state, "heart", -14)
        shift_mood(state, "lungs", -16)
        shift_mood(state, "brain", -9)
        shift_relationship(state, "heart", "lungs", 5)
    if analysis["food"]:
        shift_mood(state, "stomach", 12)
        shift_mood(state, "brain", -4)
        shift_relationship(state, "brain", "stomach", -5)
        state.setdefault("lastFoods", []).extend(analysis["foods"])
        state["lastFoods"] = state["lastFoods"][-3:]
    if analysis["junk"]:
        state["junkFoodCount"] = int(state.get("junkFoodCount", 0)) + 1
    if analysis["alcohol"]:
        shift_mood(state, "liver", -20)
        shift_mood(state, "kidneys", -12)
        shift_mood(state, "brain", -7)
        shift_relationship(state, "liver", "kidneys", 5)
    if analysis["caffeine"]:
        shift_mood(state, "brain", 5)
        shift_mood(state, "heart", -8)
        shift_mood(state, "stomach", -6)
    if analysis["water"]:
        shift_mood(state, "kidneys", 20)
        shift_mood(state, "stomach", 4)
        shift_relationship(state, "liver", "kidneys", 5)
        state["hydrationCount"] = int(state.get("hydrationCount", 0)) + 1
    if analysis["exercise"]:
        shift_mood(state, "heart", 8)
        shift_mood(state, "brain", 6)
        shift_mood(state, "lungs", -5 if analysis["stress"] else 6)
    if analysis["healthy"]:
        for organ_id in ORGAN_IDS:
            shift_mood(state, organ_id, 4)

    for substance in analysis["substances"]:
        tally = state.setdefault("substanceTally", {})
        tally[substance] = int(tally.get(substance, 0)) + 1

    was_rebelling = bool(state.get("rebellionActive"))
    average_mood = sum(state["moods"].values()) / len(state["moods"])
    if average_mood <= -34 or int(state.get("crisisCount", 0)) >= 2:
        state["rebellionActive"] = True
    if was_rebelling and analysis["healthy"]:
        state["rebellionActive"] = False
        for organ_id in ORGAN_IDS:
            shift_mood(state, organ_id, 12)

    return unlock_achievements(state, analysis, was_rebelling=was_rebelling)


def update_relationship_from_message(state: dict, speaker_id: str, content: str) -> list[dict]:
    text = content.lower()
    negative = any(
        word in text
        for word in ["blame", "primitive", "pretentious", "ignore", "ridiculous", "betrayal", "undercut"]
    )
    positive = any(
        phrase in text
        for phrase in ["agree", "right", "valid", "support", "with you", "thank", "ally"]
    )

    for organ in ORGANS:
        if organ.id == speaker_id:
            continue
        if organ.name.lower() in text or organ.id in text:
            if negative:
                shift_relationship(state, speaker_id, organ.id, -5)
            if positive:
                shift_relationship(state, speaker_id, organ.id, 5)

    return unlock_achievements(state, {}, speaker_id=speaker_id, message_text=text)


def unlock_achievements(
    state: dict,
    analysis: dict,
    *,
    was_rebelling: bool = False,
    speaker_id: str | None = None,
    message_text: str = "",
) -> list[dict]:
    unlocked: list[dict] = []
    current = state.setdefault("achievements", {})

    def unlock(key: str) -> None:
        if current.get(key):
            return
        achievement = {**ACHIEVEMENTS[key], "id": key, "unlockedAt": now_iso()}
        current[key] = achievement
        unlocked.append(achievement)

    if any(score <= -20 for score in state.get("relationships", {}).values()):
        unlock("first_argument")
    if int(state.get("substanceTally", {}).get("alcohol", 0)) + int(state.get("junkFoodCount", 0)) >= 3:
        unlock("enabler")
    if int(state.get("crisisCount", 0)) >= 2:
        unlock("hypochondriac")
    if was_rebelling and analysis.get("healthy") and not state.get("rebellionActive"):
        unlock("peacemaker")
    if int(state.get("eventCount", 0)) >= 10:
        unlock("oversharer")
    if speaker_id and speaker_id != "kidneys" and "kidney" in message_text:
        unlock("ignored")
    if any(score >= 80 for score in state.get("relationships", {}).values()):
        unlock("relationship_goals")
    if any(score <= -80 for score in state.get("relationships", {}).values()):
        unlock("civil_war")

    return unlocked


def weather_for_state(state: dict) -> dict:
    if state.get("rebellionActive"):
        return {"id": "hurricane", "label": "Hurricane", "intensity": 100}
    if state.get("crisisActive"):
        return {"id": "tornado", "label": "Tornado", "intensity": 92}

    moods = list(state.get("moods", {}).values()) or [0]
    average = sum(moods) / len(moods)
    stressed = sum(1 for mood in moods if mood <= -25)

    if stressed >= 3 or average <= -24:
        return {"id": "thunderstorm", "label": "Thunderstorm", "intensity": 82}
    if stressed >= 1 or average <= 4:
        return {"id": "partly_cloudy", "label": "Partly Cloudy", "intensity": 45}
    return {"id": "clear", "label": "Clear Skies", "intensity": 12}


def public_state(state: dict) -> dict:
    relationships = []
    for key, score in state.get("relationships", {}).items():
        left, right = key.split("|")
        relationships.append({"source": left, "target": right, "score": score})

    return {
        "sessionId": state["sessionId"],
        "createdAt": state["createdAt"],
        "updatedAt": state["updatedAt"],
        "relationships": relationships,
        "moods": state.get("moods", {}),
        "eventCount": state.get("eventCount", 0),
        "crisisCount": state.get("crisisCount", 0),
        "crisisActive": state.get("crisisActive", False),
        "rebellionActive": state.get("rebellionActive", False),
        "memorySummary": memory_summary(state),
        "weather": weather_for_state(state),
        "achievements": list(state.get("achievements", {}).values()),
        "settings": state.get("settings", DEFAULT_SETTINGS),
    }


def memory_summary(state: dict) -> str:
    recent_events = state.get("events", [])[-15:]
    event_lines = [
        (
            f"{event.get('createdAt', '')} "
            f"{event.get('userName', 'User')} "
            f"[{', '.join(event.get('categories', [])) or 'general'}]: "
            f"{event.get('content', '')}"
        )
        for event in recent_events
    ]
    substances = state.get("substanceTally", {})
    foods = state.get("lastFoods", [])
    hydration_count = int(state.get("hydrationCount", 0))

    parts = []
    if event_lines:
        parts.append("Last 15 user events with timestamps/categories: " + " | ".join(event_lines))
    if substances:
        parts.append(
            "Substance tally: "
            + ", ".join(f"{name} x{count}" for name, count in substances.items())
        )
    if foods:
        parts.append("Stomach remembers: " + ", ".join(foods))
    parts.append(f"Kidneys hydration count this session: {hydration_count}")
    if hydration_count == 0 and state.get("eventCount", 0):
        parts.append("Kidneys note: no hydration events logged yet; passive aggression should increase.")
    return "\n".join(parts) or "No user events logged yet."


def relationship_context(state: dict, organ_id: str) -> str:
    lines = []
    for other_id in ORGAN_IDS:
        if other_id == organ_id:
            continue
        score = get_relationship(state, organ_id, other_id)
        other = ORGAN_BY_ID[other_id].name
        if score >= 45:
            label = f"you respect and often defend {other}"
        elif score <= -45:
            label = f"you have low tolerance for {other} and will undercut them when possible"
        else:
            label = "neutral"
        lines.append(f"{other}: {score} ({label})")
    return "\n".join(lines)


def active_organs(state: dict) -> list[Organ]:
    enabled = set(state.get("settings", DEFAULT_SETTINGS).get("enabledOrgans", ORGAN_IDS))
    return [organ for organ in ORGANS if organ.id in enabled]


def organ_sequence(state: dict, target_organ_id: str | None) -> list[Organ]:
    enabled = active_organs(state)
    if not target_organ_id:
        return enabled

    target = ORGAN_BY_ID.get(target_organ_id)
    if not target:
        return enabled

    selected = [target]
    for organ in enabled:
        if organ.id == target.id:
            continue
        score = get_relationship(state, target.id, organ.id)
        if score > 50:
            selected.append(organ)
    return selected[:3]


def build_report_card(state: dict) -> dict:
    moods = state.get("moods", {})
    tally = state.get("substanceTally", {})
    event_count = max(1, int(state.get("eventCount", 0)))

    def grade(score: int) -> str:
        if score >= 18:
            return "A"
        if score >= 4:
            return "B"
        if score >= -12:
            return "C"
        if score >= -30:
            return "D"
        return "F"

    rows = [
        {
            "organId": "heart",
            "domain": "Emotional health",
            "grade": grade(moods.get("heart", 0)),
            "comment": "I graded with feeling, which is legally the most accurate kind.",
        },
        {
            "organId": "brain",
            "domain": "Cognitive choices",
            "grade": grade(moods.get("brain", 0)),
            "comment": f"{event_count} logged decisions. Some were data, some were crimes against planning.",
        },
        {
            "organId": "liver",
            "domain": "Substance intake",
            "grade": "F" if tally.get("alcohol", 0) >= 3 else grade(moods.get("liver", 0)),
            "comment": f"Alcohol tally: {tally.get('alcohol', 0)}. I have seen enough spreadsheets.",
        },
        {
            "organId": "lungs",
            "domain": "Stress management",
            "grade": grade(moods.get("lungs", 0)),
            "comment": "Breathing was requested multiple times by me, the obvious professional.",
        },
        {
            "organId": "stomach",
            "domain": "Nutrition",
            "grade": grade(moods.get("stomach", 0)),
            "comment": "I am judging the meal pattern and saving my harshest remarks for later.",
        },
        {
            "organId": "kidneys",
            "domain": "Hydration",
            "grade": grade(moods.get("kidneys", 0)),
            "comment": "Hydration was either considered or cruelly treated as optional.",
        },
    ]
    for row in rows:
        row["organName"] = ORGAN_BY_ID[row["organId"]].name
        row["color"] = ORGAN_BY_ID[row["organId"]].color
    return {
        "title": f"Body Report Card #{event_count // 5}",
        "rows": rows,
        "createdAt": now_iso(),
    }
