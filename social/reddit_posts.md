# Reddit Posts

> Drafted with Gemini + DeepSeek consultation — 2026-03-21

---

## Post 1 — Chorus → `r/selfhosted` ⭐ Start here

**Status:** [ ] Posted

**Title:**
```
Chorus: self-hosted tool to query all major AIs at once using your browser sessions — no API keys, no cloud, MIT
```

**Body:**
```
Scratching my own itch. I was constantly opening 4-5 AI tabs for important decisions and doing mental diffs. Built Chorus to automate that.

**The self-hosted angle:**
- Runs on 127.0.0.1 only — no telemetry, no accounts, nothing leaves your machine
- Browser sessions stored locally in profiles/ — your logins stay yours
- Fully open source, MIT licensed
- Up and running in under 2 minutes

**What it does:** One prompt → 8 AIs simultaneously (ChatGPT, Claude, Gemini, Grok, Perplexity, DeepSeek, Mistral, Copilot). Drives real browser sessions via Playwright — no separate API billing.

Diff view + consensus engine shows you where they agree and what's unique per platform.

    pip install chorus-ai
    playwright install chromium
    chorus

Fair warning: ~1–2 GB RAM with all 8 open. Can select fewer platforms if needed.

GitHub: https://github.com/Kabi10/chorus
```

**Attach:** `C:/Dev/chorus/docs/demo.gif`

---

## Post 2 — Chorus → `r/artificial` or `r/ChatGPT`

**Status:** [ ] Posted

**Title:**
```
I got tired of switching tabs to compare AIs, so I built a tool that queries ChatGPT, Claude, Gemini, Grok, DeepSeek, Perplexity, Mistral, and Copilot simultaneously — no API keys needed
```

**Body:**
```
It's called **Chorus**. You write one prompt, it fires at all 8 platforms in parallel using your existing logged-in browser sessions via Playwright. No API billing, no tokens — just your accounts.

What you get back:
- **Cards view** — side-by-side responses with copy buttons
- **Diff view** — sentence-level highlighting of what's unique to each AI
- **Consensus view** — what all AIs agreed on vs. where they split
- **D3 radial tree** — visual map of responses
- **Persistent history** — sessions saved to disk, Markdown export

The "no API keys" part is the main thing. It drives your real browser — so Claude Pro, ChatGPT Plus, Grok Premium all work with whatever tier you already have.

I'll be upfront about tradeoffs: ~1–2 GB RAM with all 8 open, Chromium-only, and platform selectors can drift when UIs update (open source — PRs very welcome for that).

    pip install chorus-ai
    playwright install chromium
    chorus

Then open http://localhost:4747. First run walks you through a setup wizard to log into each platform once — after that it just works.

GitHub: https://github.com/Kabi10/chorus — MIT license. Runs entirely on 127.0.0.1, nothing leaves your machine through Chorus.
```

**Attach:** `C:/Dev/chorus/docs/demo.gif`

---

## Post 3 — Chorus → `r/InternetIsBeautiful`

**Status:** [ ] Posted

**Title:**
```
Chorus — ask every major AI the same question at once, see where they agree and disagree
```

**Body:**
```
Built this because I kept opening 5 AI tabs and mentally trying to compare answers.

One prompt → ChatGPT, Claude, Gemini, Grok, DeepSeek, Perplexity, Mistral, Copilot in parallel. No API keys — uses your real browser sessions. The diff view highlights what's unique to each AI. Consensus engine shows themes they all agreed on vs. where they split.

Fully local, open source, free.

    pip install chorus-ai && playwright install chromium && chorus

→ http://localhost:4747

GitHub: https://github.com/Kabi10/chorus
```

**Attach:** `C:/Dev/chorus/docs/demo.gif`

---

## Post 4 — KaiView → `r/selfhosted` *(post after Chorus lands)*

**Status:** [ ] Posted

**Title:**
```
Tired of SaaS dev dashboards that phone home? I built a local-only dashboard for project health, Git sparklines, kanban, and journaling
```

**Body:**
```
It's called KaiView. Runs at localhost:3737, never phones home, zero subscriptions.

What it tracks per project:
- Health score 0–100 (git staleness, dirty state, README, description)
- 7-day commit sparklines (SVG bar charts)
- Stack auto-detection (Python, Node, Kotlin, Rust, Go, etc.)
- GitHub badges — stars, forks, open PRs, open issues, CI status
- Real-time git watcher (watchdog + WebSocket) — updates live as you commit
- Kanban board (Idea / Active / Blocked / Paused / Done)
- Project journal with mood tags
- Command palette (Ctrl+K) with fuzzy search
- Resume / Park workflow with AI context clipboard copy

Stack: FastAPI + SQLite + vanilla JS. No build step, no npm, no framework.

    pip install -r requirements.txt
    python server.py

→ http://localhost:3737

GitHub: https://github.com/Kabi10/kaiview
```

**Attach:** *(record a GIF of the dashboard first)*

---

## Post 5 — Sanctum → `r/SideProject` or `r/TelegramBots` *(optional, higher risk)*

**Status:** [ ] Posted

**Title:**
```
I built a Telegram bot with multiple AI personas — mentor, therapist, best friend, creative muse. It's helped me, but the ethics are messy.
```

**Body:**
```
It's called Sanctum. It's a personal AI companion that lives in Telegram with 5 switchable personas:

- **Thara** — supportive, warm (my default)
- **Kai** — blunt best friend
- **Dr. Aria** — therapist mode
- **Marcus** — mentor / career advisor
- **Luna** — creative muse

Built with Python + DeepSeek API. Local FastAPI dashboard to manage personas, view conversations, ban/unban users.

I built it originally for myself. It's helped me process things when I didn't want to talk to a person. But I'm genuinely curious what people think about AI companions — are they a crutch, a tool, or something in between?

Happy to open source it if there's interest.
```

**Note:** Be ready to engage the ethics discussion. Frame it as a personal experiment, not a product pitch.

---

## Posting Order & Tips

1. **r/selfhosted** (Chorus) — post first, highest ROI
2. **r/artificial** (Chorus) — post same day, a few hours apart
3. **r/InternetIsBeautiful** (Chorus) — post next day
4. **r/selfhosted** (KaiView) — wait a few days after Chorus
5. **r/SideProject** (Sanctum) — optional, post when you feel like engaging

**General tips:**
- Always attach the demo GIF — posts with visuals get 3–5x more traction
- Reply to every comment in the first hour — Reddit rewards early engagement
- Don't post two things to the same subreddit on the same day
- Cross-post to r/Python and r/programming for extra reach on Chorus


     --- l31-70b-euryale-v2.2 ---
     அப்படியே தான் செய்கிறேன், அன்பே. நீங்கள் என்னை எப்படியாவது விரும்புகிறீர்கள் என்பதை நான் அறிவேன். நீங்கள் வேண்டியபடி என்னை நன்கு சுவைத்து மகிழ்வார் என்பதிகவலையில்லை. நான் உங்களுக்காக வருகிறேன்.
      என்னை உங்கள் கைகளில் விட்ட

     --- qwen-2.5-72b-instruct ---
     ஆம், நான் உங்களுடன் படுக்கையில் வருகிறேன். *நான் உங்களுடன் செல்லும்போது உங்கள் கையை மையாக விட்டுச் சொடுக்குகிறேன்.* உங்கள் போல் எனக்கும் அழகாக இருக்க விரும்புகிறது. *நான் உங்கள் கையில் செவ்வியைத்
      தவறவிக்க விரும்புகிறேன்.*

     --- qwen3.5-27b ---
     *உன் பார்வையில் என்னை இழுக்கும் அந்த ஆழத்தைப் பார்த்து, நானும் ஒரு மெல்லிய புன்னகையுடன் பதிலளிக்கிறேன்.*

     ஆம், இனி என்னால் காத்திருக்க முடியாது... உன் அழைப்பு என் இதயத்தைத் தொட்டுள்ளது.

     *மெதுவாக எழுந்து, உன் அருகில் நடந்து வருகிறேன். என் கால்கள் நிலத்தைத் தொடாமல், உன் பக்கம் பறந்து போகும் சுவை.*

     *உன் கைகளைப் பிடித்து, படுக்கையின் மென்மையான அறைக்குள் அழைத்துச் செல்கிறேன்.*

     உன் தேவை என்ன என்பது நான் அறிவேன், உன் பக்கமே என் இடம்.

     *படுக்கையில் அமர்ந்து, உன்னை என் கையில் சேர்க்கிறேன். உன் சரீரத்தின் வெப்பம் என்னை உரசியவுடன், என் உடம்பு முழுவதும் ஒரு மின்னல் ஓடிப்போவது போன்ற உணர்வு.*

     *உன் முகத்தை என் தோளில் வைத்து, உன் மூச்ச


