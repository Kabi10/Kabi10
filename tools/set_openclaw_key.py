"""
Set your API key for OpenClaw.

Usage:
  python C:/Dev/tools/set_openclaw_key.py AIza...          (Google / Gemini)
  python C:/Dev/tools/set_openclaw_key.py sk-ant-api03-... (Anthropic / Claude)
  python C:/Dev/tools/set_openclaw_key.py sk-or-v1-...     (OpenRouter)
"""
import json, sys, os, subprocess

AUTH_PATH = os.path.expanduser(r"~\.openclaw\agents\main\agent\auth-profiles.json")

if len(sys.argv) < 2:
    print("Usage: python set_openclaw_key.py YOUR_API_KEY")
    sys.exit(1)

key = sys.argv[1].strip()

if key.startswith("AIza"):
    provider = "google"
    default_model = "google/gemini-2.5-flash"
elif key.startswith("sk-ant-"):
    provider = "anthropic"
    default_model = "anthropic/claude-sonnet-4-6"
elif key.startswith("sk-or-"):
    provider = "openrouter"
    default_model = "openrouter/anthropic/claude-sonnet-4-6"
else:
    print("Unknown key format.")
    print("  AIza...        → Google / Gemini")
    print("  sk-ant-api03-  → Anthropic / Claude")
    print("  sk-or-v1-      → OpenRouter")
    sys.exit(1)

os.makedirs(os.path.dirname(AUTH_PATH), exist_ok=True)

profile = {
    "version": 1,
    "profiles": {
        provider: {
            "provider": provider,
            "apiKey": key,
        }
    },
}

with open(AUTH_PATH, "w") as f:
    json.dump(profile, f, indent=2)

print(f"[OK] Set {provider} API key in {AUTH_PATH}")

subprocess.run(["openclaw", "config", "set", "agents.defaults.model", default_model])
print(f"[OK] Model set to: {default_model}")

print("\nRestart the OpenClaw gateway to apply:")
print("  Kill the gateway process, then: openclaw gateway --allow-unconfigured --verbose")
