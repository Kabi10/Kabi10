# CROPIX Revised Spec (post-API verification)

Based on real findings, here’s a pragmatic and weekend-shippable revision for KAB-10’s CROPIX integration.

---

### **1. What we can realistically integrate this weekend**

✅ **Yes, this weekend:**

- Crop categories, crops, and varieties (confirmed endpoint)
- District-based crop list (likely works, data categories exist)
- Static agricultural structure data (zones, seasons, crop management metadata)

❌ **No, not via CROPIX:**

- Real-time market prices — use alternative sources
- Yield forecasting — not available yet
- Real-time alerts or farmer dashboards

🔍 **Needs testing:**

- Historical cultivation datasets — might be accessible but requires parameter discovery

---

### **2. Revised endpoint list (real base URL)**

**Base URL:** `https://www.apiweb.doa.gov.lk/cropix/api/v1/`

**Confirmed:**

- `GET /crop-details/crop-categories`  
  Returns list of crop categories (e.g., Vegetables, Fruits, Grains)

**Likely available (based on data categories):**

- `GET /crop-details/crops` (or similar) — crops by category
- `GET /crop-details/varieties` — varieties by crop
- `GET /geographical/structure` — zones, districts, regions
- `GET /crop-look/distribution` — cultivation distribution by district (if public)

**Request format:**
All requests require `?access_key=YOUR_KEY` as query parameter.

---

### **3. Alternatives for missing price/forecast data**

**Market Prices:**

- **World Food Programme (WFP) Price Database** — has Sri Lanka market data (JSON/CSV)
- **Department of Agriculture weekly bulletins** (PDF) — could scrape or manually update
- **FAO GIEWS** — global but includes Sri Lanka price trends

**Yield forecasts:**

- Use **historical yield averages** from DoA annual reports (static dataset for now)
- Add disclaimer: “Based on historical data — CROPIX real-time forecasts coming soon”

**Recommended fallback for weekend:**

- Implement static CSV price data for 5–10 major crops, sourced from latest DoA bulletin
- Label clearly as “Sample Price Data” until live API integration is available

---

### **4. Should we still call it CROPIX-integrated in Play Store?**

**Yes, but with accurate phrasing.**

**Suggested Play Store bullet points:**

- “Crop registry & varieties via Sri Lanka’s official CROPIX/DoA API”
- “District-wise crop cultivation data from Department of Agriculture”
- “Agricultural seasons and crop management guidelines”
- “Market prices from WFP & DoA sources” _(transparent about source)_

**Avoid:**

- “Real-time CROPIX market prices”
- “Live yield forecasts from CROPIX”

---

### **5. API key acquisition steps**

**Step 1:** Visit https://www.apiweb.doa.gov.lk/api-key-view.php  
**Step 2:** Fill registration form (organization: “KAB-10 – Farmer App”)  
**Step 3:** Wait for email with `access_key` (could be manual approval)  
**Step 4:** Test with `crop-categories` endpoint  
**Step 5:** Hardcode key in app for weekend; move to config later

**Note:** If key isn’t approved in time, use **mock data** based on their documentation’s sample structure, with a clear “Demo Mode” badge.

---

### **Revised Weekend Implementation Plan**

**Day 1 (Today):**

1. Register for API key
2. Implement crop categories & varieties endpoints (with fallback mock data)
3. Integrate WFP price API or static price CSV

**Day 2 (Tomorrow):**

1. Add district-based crop filtering
2. Implement “Crop Details” screen with management info (pest/disease/activity)
3. Add data source labels throughout UI

**Day 3 (Wrap):**

1. Polish, test with/without internet
2. Update Play Store listing with accurate descriptions
3. Prepare follow-up items: historical data, better price integration

---

**Bottom line:** We can deliver a **genuine but limited** CROPIX integration this weekend, focused on crop registry and district data, supplemented with alternative price sources. This sets a foundation for expansion as the CROPIX API matures.
