"""
DistroKid Form Selector Discovery Script
Run this ONCE to capture real DOM selectors from the live form.
Output: selectors.json (used by upload_bot.py)

Usage:
    python discover_selectors.py
"""

import json
import time
from playwright.sync_api import sync_playwright

USER_DATA_DIR = r"C:\Users\Tharma\AppData\Local\Google\Chrome\User Data"
PROFILE_NAME = "Default"
OUTPUT_FILE = "selectors.json"

JS_EXTRACT = """
() => {
    const results = { inputs: [], selects: [], textareas: [], fileInputs: [], labels: [], buttons: [] };

    // All inputs
    document.querySelectorAll('input').forEach(el => {
        const label = el.labels && el.labels[0] ? el.labels[0].innerText.trim() : null;
        const ariaLabel = el.getAttribute('aria-label');
        const placeholder = el.placeholder;
        results.inputs.push({
            type: el.type,
            name: el.name || null,
            id: el.id || null,
            label,
            ariaLabel,
            placeholder,
            value: el.value || null,
            className: el.className.substring(0, 80),
            required: el.required,
            data: Object.fromEntries([...el.attributes]
                .filter(a => a.name.startsWith('data-'))
                .map(a => [a.name, a.value]))
        });
    });

    // All selects
    document.querySelectorAll('select').forEach(el => {
        const label = el.labels && el.labels[0] ? el.labels[0].innerText.trim() : null;
        const options = [...el.options].map(o => ({ value: o.value, text: o.text }));
        results.selects.push({
            name: el.name || null,
            id: el.id || null,
            label,
            options: options.slice(0, 20),  // cap at 20
            required: el.required,
            className: el.className.substring(0, 80)
        });
    });

    // All textareas
    document.querySelectorAll('textarea').forEach(el => {
        const label = el.labels && el.labels[0] ? el.labels[0].innerText.trim() : null;
        results.textareas.push({
            name: el.name || null,
            id: el.id || null,
            label,
            placeholder: el.placeholder,
            className: el.className.substring(0, 80)
        });
    });

    // All labels with their for attribute
    document.querySelectorAll('label').forEach(el => {
        results.labels.push({
            for: el.htmlFor || null,
            text: el.innerText.trim().substring(0, 100),
            className: el.className.substring(0, 80)
        });
    });

    // Buttons
    document.querySelectorAll('button, input[type=submit], input[type=button]').forEach(el => {
        results.buttons.push({
            type: el.type,
            text: el.innerText ? el.innerText.trim().substring(0, 80) : el.value,
            id: el.id || null,
            name: el.name || null,
            className: el.className.substring(0, 80)
        });
    });

    return results;
}
"""

JS_PAGE_TEXT = """
() => {
    // Get visible text content of the page to understand form sections
    const body = document.body;
    const walker = document.createTreeWalker(body, NodeFilter.SHOW_TEXT);
    const texts = [];
    let node;
    while ((node = walker.nextNode())) {
        const t = node.nodeValue.trim();
        if (t.length > 2 && t.length < 200) texts.push(t);
    }
    return texts.slice(0, 200);
}
"""

def run():
    with sync_playwright() as p:
        print(f"[*] Launching Chrome with profile: {PROFILE_NAME}")
        context = p.chromium.launch_persistent_context(
            user_data_dir=USER_DATA_DIR,
            channel="chrome",
            headless=False,
            args=[f"--profile-directory={PROFILE_NAME}"]
        )

        page = context.new_page()
        print("[*] Navigating to distrokid.com/new ...")
        page.goto("https://distrokid.com/new", wait_until="networkidle", timeout=30000)
        time.sleep(2)

        print("[*] Taking initial screenshot...")
        page.screenshot(path="page_initial.png")
        print("    Saved: page_initial.png")

        # Try selecting "1 song" to trigger form expansion
        print("[*] Trying to trigger form expansion (select 1 song)...")
        try:
            # Try common selectors for number of songs
            for selector in ["select[name='numSongs']", "select[name='num_songs']",
                             "select[name='numberOfSongs']", "#numSongs", "#num_songs"]:
                el = page.query_selector(selector)
                if el:
                    print(f"    Found song count selector: {selector}")
                    page.select_option(selector, "1")
                    time.sleep(2)
                    break
            else:
                print("    Could not find song count selector automatically.")
                print("    Will still dump current DOM selectors.")
        except Exception as e:
            print(f"    Warning during form trigger: {e}")

        print("[*] Taking post-trigger screenshot...")
        page.screenshot(path="page_form.png")
        print("    Saved: page_form.png")

        print("[*] Extracting all form elements...")
        selectors = page.evaluate(JS_EXTRACT)
        page_texts = page.evaluate(JS_PAGE_TEXT)

        output = {
            "url": page.url,
            "page_title": page.title(),
            "page_texts_sample": page_texts[:100],
            **selectors
        }

        with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
            json.dump(output, f, indent=2, ensure_ascii=False)

        print(f"\n[✓] Saved {OUTPUT_FILE}")
        print(f"    inputs:    {len(selectors['inputs'])}")
        print(f"    selects:   {len(selectors['selects'])}")
        print(f"    textareas: {len(selectors['textareas'])}")
        print(f"    labels:    {len(selectors['labels'])}")
        print(f"    buttons:   {len(selectors['buttons'])}")
        print("\n--- INPUTS SUMMARY ---")
        for i, inp in enumerate(selectors['inputs']):
            print(f"  [{i}] type={inp['type']:10} name={inp['name'] or '':30} id={inp['id'] or '':30} label={inp['label'] or ''} placeholder={inp['placeholder'] or ''}")

        print("\n--- SELECTS SUMMARY ---")
        for i, sel in enumerate(selectors['selects']):
            opts = [o['text'] for o in sel['options'][:5]]
            print(f"  [{i}] name={sel['name'] or '':30} id={sel['id'] or '':30} label={sel['label'] or ''} options={opts}")

        print("\n[*] Pausing 60s — inspect the browser, then close it or press Ctrl+C")
        try:
            time.sleep(60)
        except KeyboardInterrupt:
            pass

        context.close()
        print(f"\n[✓] Done. Review {OUTPUT_FILE} and page_form.png")


if __name__ == "__main__":
    run()
