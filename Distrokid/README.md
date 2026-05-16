# DistroKid Upload Bot

Reads `distrokid_batch_template.xlsx` and auto-fills the DistroKid upload form.

## Setup (one-time)

```bash
pip install pandas openpyxl playwright
python -m playwright install chromium
```

## Usage

> **Before every run: close Chrome completely** (File → Exit, or kill from Task Manager).
> The bot uses your real Chrome profile — Chrome and the bot can't share it simultaneously.

### Step 1 — Discover real form selectors (run once, or when form stops working)
```bash
python upload_bot.py --discover
```
Opens Chrome with your profile, navigates to distrokid.com/new, dumps all field
names/IDs to `selectors.json` and saves screenshots `page_initial.png` /
`page_expanded.png`. Paste the terminal output here so selectors can be fine-tuned.

### Step 2 — Upload
```bash
python upload_bot.py
```
Fills the form from the spreadsheet, then **pauses** — you review and click **Done**
yourself. The bot never auto-submits.

## Spreadsheet

| Sheet | What to fill |
|-------|-------------|
| `Release Info` | Artist, label, genre, release date, stores (once per batch) |
| `Tracks` | One row per song: title, explicit, songwriters, audio file path |

**Audio File Path**: put just the filename (e.g. `Rise of K.ai.wav`) or any path.
If not found, the bot auto-resolves by matching the Song Title in the `Songs/` folder.

## Screenshots

The bot saves debug screenshots at each step:
- `step_01_loaded.png` — page after load
- `step_02_form_expanded.png` — after setting track count
- `step_03_global_filled.png` — after global fields
- `step_04_track01.png` etc — after each track
- `step_05_done.png` — before QC pause

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Fields not filled | Run `--discover`, check `selectors.json`, update selector lists in `upload_bot.py` |
| Login expired | Run `--login` to refresh auth state |
| Audio not found | Put file in `Songs/` or update `🟢 Audio File Path` column in spreadsheet |
