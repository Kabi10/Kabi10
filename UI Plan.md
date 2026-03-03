# Complete Enhanced Prompt for Claude CLI

---

````
You are Claude Code / Claude CLI.

════════════════════════════════════════════════════════════════════════════════
SECTION 1: PROJECT CONTEXT
════════════════════════════════════════════════════════════════════════════════

We are building **Agrimarket** — a Sri Lankan farmer/buyer marketplace Android app.

TECH STACK:
- Android: Jetpack Compose, MVVM, Hilt, Room, Retrofit
- Backend: Node.js + Express, PostgreSQL + Supabase
- Auth: OTP-based (SMS)

CURRENT VERIFIED STATUS:
- Android Base URLs:
  - Debug: http://10.0.2.2:3000/api/
  - Production: https://backend-psi-tan-18.vercel.app/api/
- Verified endpoints (working end-to-end):
  - POST /api/v1/auth/send-otp (200)
  - POST /api/v1/auth/verify-otp (200)
  - GET  /api/v1/listings (200)
  - GET  /api/v1/listings/:id (200)
  - POST /api/v1/listings (201)
  - GET  /api/v1/transactions (200)
  - GET  /api/v1/transactions/:id (200)
- Android network config already handles cleartext for emulator, HTTPS for prod
- AuthInterceptor skips /auth endpoints, attaches Bearer token for others

DESIGN PHILOSOPHY:
This is NOT a tech product, startup app, or NGO tool. It is the digital version of a traditional village marketplace. The core philosophy is: "Finally, something built for us."

Target user: 45–65 year old farmer, working since dawn, dirty hands, cracked phone screen, bright sunlight, low patience, high practical intelligence.

════════════════════════════════════════════════════════════════════════════════
SECTION 2: DESIGN SYSTEM TOKENS (MANDATORY — USE ONLY THESE)
════════════════════════════════════════════════════════════════════════════════

COLORS (no other colors permitted):
┌─────────┬───────────┬─────────────────────────────────────────────────────┐
│ Token   │ Hex       │ Usage                                               │
├─────────┼───────────┼─────────────────────────────────────────────────────┤
│ Rice    │ #FAF6F1   │ Primary backgrounds, button text on Gold            │
│ Dust    │ #E8E4DF   │ Alternating row backgrounds, secondary areas        │
│ Earth   │ #8B4513   │ Dividers, borders, accent bars, back text, secondary│
│ Gold    │ #B8860B   │ Prices, primary buttons                             │
│ Green   │ #2D5016   │ Success states, active status                       │
│ Ink     │ #1A1A1A   │ Primary text                                        │
│ Stone   │ #6B6B6B   │ Secondary text, labels, metadata                    │
│ Urgent  │ #A63D2F   │ Problems, errors, report actions                    │
└─────────┴───────────┴─────────────────────────────────────────────────────┘

TYPOGRAPHY:
┌──────────────┬─────────────┬────────────────────────────────────────────────┐
│ Style        │ Spec        │ Usage                                          │
├──────────────┼─────────────┼────────────────────────────────────────────────┤
│ Title        │ 24sp Bold   │ Screen titles, home quadrants, product (detail)│
│ Price        │ 28sp Bold   │ Price display                                  │
│ Product      │ 20sp Bold   │ Product names (lists), category rows           │
│ Button       │ 18sp Bold   │ All buttons                                    │
│ Body         │ 16sp Regular│ Units, body text                               │
│ Label        │ 14sp Bold   │ Section labels, back text, metadata            │
│ Small        │ 14sp Regular│ Error messages, timestamps                     │
└──────────────┴─────────────┴────────────────────────────────────────────────┘

CASE TRANSFORM RULES (CRITICAL):
- English: May use .uppercase() for labels, titles, buttons
- Sinhala: NEVER apply case transforms (no uppercase/lowercase exists)
- Tamil: NEVER apply case transforms (no uppercase/lowercase exists)

SPACING TOKENS (use only these):
- 4dp: Dividers, accent bars, label-to-input gaps
- 8dp: Line gaps within rows
- 16dp: Vertical padding, margins between elements
- 24dp: Screen padding, section padding
- 32dp: Large gaps (only when truly needed)

HARD CONSTRAINTS (NON-NEGOTIABLE):
- No icons
- No illustrations
- No photos
- No shadows/elevation
- No rounded corners
- No animations/transitions
- No ripple effects
- Minimum touch target: 56dp (prefer larger)

════════════════════════════════════════════════════════════════════════════════
SECTION 3: COMPLETE UI SPECIFICATIONS
════════════════════════════════════════════════════════════════════════════════

BASELINE: 360 × 800 dp portrait

───────────────────────────────────────────────────────────────────────────────
SCREEN A: HOME (VILLAGE SQUARE)
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Central hub. Four equal directions. This is a place, not a menu.

LAYOUT:
┌─────────────────────┬─────────────────────┐
│                     │                     │
│                     │                     │
│        BUY          │        SELL         │
│     මිලට ගන්න         │      විකුණන්න         │
│                     │                     │
│                     │                     │
├─────────────────────┼─────────────────────┤
│                     │                     │
│                     │                     │
│       PRICES        │       ORDERS        │
│      මිල ගණන්         │       ගනුදෙනු        │
│                     │                     │
│                     │                     │
└─────────────────────┴─────────────────────┘

MEASUREMENTS:
- Root: 360 × 800 dp
- Each quadrant: 180 × 400 dp (178 × 398 dp interior after borders)
- Vertical divider: 4dp wide, 800dp tall, centered at x=178dp, Earth color
- Horizontal divider: 360dp wide, 4dp tall, centered at y=398dp, Earth color
- Text: Centered horizontally and vertically in each quadrant

TEXT STYLES:
- Quadrant labels: 24sp Bold, Ink #1A1A1A, centered

BACKGROUNDS:
- All quadrants: Rice #FAF6F1
- Dividers: Earth #8B4513

PRESSED STATE:
- Earth #8B4513 at 15% opacity overlay on quadrant
- Immediate on touch down, clears on release
- No animation

NAVIGATION:
- BUY → Category Selection (flow="buy")
- SELL → Category Selection (flow="sell")
- PRICES → Market Prices Screen
- ORDERS → Transaction List Screen

───────────────────────────────────────────────────────────────────────────────
SCREEN B: CATEGORY SELECTION (MARKET SECTIONS)
───────────────────────────────────────────────────────────────────────────────

PURPOSE: The farmer chooses what category. This is walking into a market section.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු          ඔබ විකුණන්නේ මොනවාද?          │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│                 එළවළු                      │  ← Row 96dp
│                                           │
├───────────────────────────────────────────┤
│                                           │
│                 පොල්                       │  ← Row 96dp (Dust bg)
│                                           │
├───────────────────────────────────────────┤
│                                           │
│              වී සහ ධාන්‍ය                    │  ← Row 96dp
│                                           │
├───────────────────────────────────────────┤
│                                           │
│                පලතුරු                      │  ← Row 96dp (Dust bg)
│                                           │
├───────────────────────────────────────────┤
│                                           │
│                 මාළු                       │  ← Row 96dp
│                                           │
├───────────────────────────────────────────┤
│                                           │
│                 සතුන්                      │  ← Row 96dp (Dust bg)
│                                           │
└───────────────────────────────────────────┘

HEADER MEASUREMENTS:
- Height: 72dp
- Padding: 16dp vertical, 24dp horizontal
- Back text: Left-aligned, vertically centered, 48×48dp hit area
- Title: Centered horizontally, vertically centered

HEADER TEXT:
- Back: 14sp Bold, Earth #8B4513
- Title (SELL flow): 24sp Bold, Ink #1A1A1A
  - Sinhala: ඔබ විකුණන්නේ මොනවාද?
  - Tamil: நீங்கள் என்ன விற்கிறீர்கள்?
  - English: What are you selling?
- Title (BUY flow):
  - Sinhala: ඔබ හොයන්නේ මොනවාද?
  - Tamil: நீங்கள் என்ன தேடுகிறீர்கள்?
  - English: What are you looking for?

ROW MEASUREMENTS:
- Height: 96dp
- Width: 360dp
- Text: Centered horizontally and vertically
- Text style: 20sp Bold, Ink #1A1A1A

BACKGROUNDS:
- Header: Rice #FAF6F1
- Odd rows (1,3,5): Rice #FAF6F1
- Even rows (2,4,6): Dust #E8E4DF
- No inter-row dividers (alternating backgrounds provide separation)

PRESSED STATE:
- Earth #8B4513 at 15% opacity overlay
- Immediate response, no animation

CATEGORIES (6 total, ordered by agricultural prevalence):
┌─────┬────────────────┬──────────────────────────┬─────────────────┐
│ Row │ Sinhala        │ Tamil                    │ English         │
├─────┼────────────────┼──────────────────────────┼─────────────────┤
│ 1   │ එළවළු           │ காய்கறிகள்                │ VEGETABLES      │
│ 2   │ පොල්            │ தேங்காய்                  │ COCONUT         │
│ 3   │ වී සහ ධාන්‍ය      │ நெல் மற்றும் தானியங்கள்   │ PADDY AND GRAIN │
│ 4   │ පලතුරු          │ பழங்கள்                   │ FRUIT           │
│ 5   │ මාළු            │ மீன்                      │ FISH            │
│ 6   │ සතුන්           │ கால்நடை                  │ LIVESTOCK       │
└─────┴────────────────┴──────────────────────────┴─────────────────┘

NAVIGATION:
- Back text → Previous screen (navController.popBackStack())
- Category row tap → Listings List (if BUY) or Create Listing (if SELL)

───────────────────────────────────────────────────────────────────────────────
SCREEN C: LISTINGS LIST (MARKET STALLS)
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Browse available listings in a category. Each row is a market stall.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු                           එළවළු      │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│▌                                          │
│▌ තක්කාලි                                   │
│▌ රු 280                          1 kg     │  ← Row 112dp
│▌ අනුරාධපුර                        දැන්      │
│▌                                          │
├───────────────────────────────────────────┤
│▌                                          │
│▌ බෝංචි                                     │
│▌ රු 320                          1 kg     │  ← Row 112dp (Dust bg)
│▌ පොළොන්නරුව                       අද       │
│▌                                          │
└───────────────────────────────────────────┘
  ▲
  └── 4dp Earth accent bar

HEADER MEASUREMENTS:
- Height: 72dp
- Padding: 16dp vertical, 24dp horizontal
- Back text: Left-aligned, 14sp Bold, Earth
- Category title: Right-aligned, 24sp Bold, Ink

LISTING ROW MEASUREMENTS:
- Height: 112dp
- Accent bar: 4dp wide, full height, Earth #8B4513, left edge
- Content padding: 24dp left (from accent), 24dp right, 16dp top/bottom
- Line gaps: 8dp vertical between lines

LISTING ROW TEXT:
- Line 1 (Product): 20sp Bold, Ink #1A1A1A, left-aligned
- Line 2 (Price): 28sp Bold, Gold #B8860B, left-aligned
- Line 2 (Unit): 16sp Regular, Ink #1A1A1A, right-aligned, same baseline as price
- Line 3 (Location): 14sp Bold, Stone #6B6B6B, left-aligned
- Line 3 (Time): 14sp Bold, Stone #6B6B6B, right-aligned, same baseline as location

BACKGROUNDS:
- Odd rows: Rice #FAF6F1
- Even rows: Dust #E8E4DF
- Accent bar: Earth #8B4513
- No inter-row dividers

PRESSED STATE:
- Earth #8B4513 at 15% opacity on row background
- Accent bar remains solid

RELATIVE TIME DISPLAY:
┌─────────────────┬────────────────┬───────────────────┬─────────────────┐
│ Time Elapsed    │ Sinhala        │ Tamil             │ English         │
├─────────────────┼────────────────┼───────────────────┼─────────────────┤
│ < 1 hour        │ දැන්            │ இப்போது            │ JUST NOW        │
│ 1-6 hours       │ පැය Xකට පෙර    │ X மணி நேரம் முன்   │ X HOURS AGO     │
│ Same day        │ අද             │ இன்று              │ TODAY           │
│ Yesterday       │ ඊයේ            │ நேற்று             │ YESTERDAY       │
│ 2-6 days        │ දින Xකට පෙර   │ X நாட்கள் முன்     │ X DAYS AGO      │
│ 7+ days         │ සතිය Xකට පෙර  │ X வாரங்கள் முன்    │ X WEEKS AGO     │
└─────────────────┴────────────────┴───────────────────┴─────────────────┘

EMPTY STATE:
- Text centered in content area
- Sinhala: මෙහි තවම කිසිවක් නැත
- Tamil: இங்கே இன்னும் எதுவும் இல்லை
- English: NOTHING HERE YET
- Style: 16sp Regular, Stone #6B6B6B

LOADING STATE:
- Text centered in content area
- Sinhala: රැඳී සිටින්න
- Tamil: காத்திருங்கள்
- English: WAIT
- Style: 16sp Regular, Stone #6B6B6B

ERROR STATE:
- Message centered, button below with 24dp gap
- Message Sinhala: සම්බන්ධ වීමට නොහැකි විය
- Message Tamil: இணைக்க முடியவில்லை
- Message English: COULD NOT CONNECT
- Button Sinhala: නැවත උත්සාහ කරන්න
- Button Tamil: மீண்டும் முயற்சிக்கவும்
- Button English: TRY AGAIN
- Button: SecondaryButton style (Rice bg, Earth border)

NAVIGATION:
- Row tap → Listing Detail screen

───────────────────────────────────────────────────────────────────────────────
SCREEN D: LISTING DETAIL (STALL DETAIL)
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Full details of a listing. The farmer has stopped at a stall.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු                           එළවළු      │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│  තක්කාලි                                   │  ← Product section
│  රු 280                                    │
│  1 kg                                     │
│                                           │
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│  ලබා ගත හැකි කාලය                          │  ← Availability section
│  ජනවාරි 15 - ජනවාරි 20                      │
│                                           │
│  රැගෙන යන ස්ථානය                            │
│  නුවර පොළ අසල                              │
│                                           │
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│  විකුණන්නා                                  │  ← Seller section
│  සුනිල් මහතා                               │
│  නුවර                                      │
│  පැය 3කට පෙර                              │
│                                           │
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │         ඇමතුමක් ගන්න                  │  │  ← Primary button
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │        පණිවිඩයක් යවන්න                │  │  ← Secondary button
│  └─────────────────────────────────────┘  │
│                                           │
└───────────────────────────────────────────┘

SECTION MEASUREMENTS:
- Padding: 24dp all sides
- Section dividers: 4dp Earth #8B4513, full width
- Label-to-value gap: 4dp
- Value-to-next-label gap: 16dp

TEXT STYLES:
- Section labels: 14sp Bold, Stone #6B6B6B
- Section values: 20sp Bold, Ink #1A1A1A
- Product name: 24sp Bold, Ink #1A1A1A
- Price: 28sp Bold, Gold #B8860B
- Unit: 16sp Regular, Ink #1A1A1A

BUTTONS:
- CALL (Primary): 56dp height, full width minus 48dp margins, Gold bg, Rice text
- SEND MESSAGE (Secondary): 56dp height, full width minus 48dp margins, Rice bg, Earth border, Earth text
- Gap between buttons: 16dp

SECTION LABELS:
┌─────────────────────┬──────────────────────────┬────────────────────┐
│ Sinhala             │ Tamil                    │ English            │
├─────────────────────┼──────────────────────────┼────────────────────┤
│ ලබා ගත හැකි කාලය    │ கிடைக்கும் நேரம்          │ AVAILABLE          │
│ රැගෙන යන ස්ථානය      │ எடுக்கும் இடம்            │ PICKUP             │
│ විකුණන්නා            │ விற்பவர்                  │ SELLER             │
│ පළ කළේ              │ பதிவிட்டது                │ POSTED             │
└─────────────────────┴──────────────────────────┴────────────────────┘

BUTTON LABELS:
┌─────────────────────┬──────────────────────────┬────────────────────┐
│ Sinhala             │ Tamil                    │ English            │
├─────────────────────┼──────────────────────────┼────────────────────┤
│ ඇමතුමක් ගන්න         │ அழைக்கவும்                │ CALL               │
│ පණිවිඩයක් යවන්න      │ செய்தி அனுப்பவும்         │ SEND MESSAGE       │
└─────────────────────┴──────────────────────────┴────────────────────┘

CALL BUTTON BEHAVIOR:
- Opens phone dialer with seller's number
- Use Intent.ACTION_DIAL

LOADING/ERROR STATES:
- Same pattern as Listings List

───────────────────────────────────────────────────────────────────────────────
SCREEN E: MARKET PRICES
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Today's going rates. Information only—check before buying or selling.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු                       මිල ගණන්        │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│     අද උදේ 6:00 ට යාවත්කාලීන විය             │  ← Updated bar 40dp (Dust bg)
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│  තක්කාලි                        රු 280/kg  │  ← Price row 72dp
│                                           │
├───────────────────────────────────────────┤
│                                           │
│  බෝංචි                          රු 320/kg  │  ← Price row 72dp (Dust bg)
│                                           │
└───────────────────────────────────────────┘

HEADER: Same as other screens

UPDATED BAR:
- Height: 40dp
- Background: Dust #E8E4DF
- Padding: 24dp horizontal
- Text: 14sp Regular, Stone #6B6B6B, centered
- Sinhala: අද උදේ 6:00 ට යාවත්කාලීන විය
- Tamil: இன்று காலை 6:00 மணிக்கு புதுப்பிக்கப்பட்டது
- English: UPDATED TODAY 6:00 AM

PRICE ROW:
- Height: 72dp
- Padding: 24dp horizontal
- Product name: 20sp Bold, Ink, left-aligned
- Price with unit: 20sp Bold, Gold, right-aligned
- Alternating Rice/Dust backgrounds

SAMPLE DATA (10 rows):
┌─────┬────────────────┬─────────────┐
│ Row │ Product (SI)   │ Price       │
├─────┼────────────────┼─────────────┤
│ 1   │ තක්කාලි         │ රු 280/kg   │
│ 2   │ බෝංචි           │ රු 320/kg   │
│ 3   │ වම්බටු          │ රු 150/kg   │
│ 4   │ ගෝවා           │ රු 180/kg   │
│ 5   │ කැරට්          │ රු 240/kg   │
│ 6   │ පොල්           │ රු 85/nut   │
│ 7   │ වී             │ රු 95/kg    │
│ 8   │ අඹ            │ රු 350/kg   │
│ 9   │ මාළු           │ රු 800/kg   │
│ 10  │ කුකුළු          │ රු 750/kg   │
└─────┴────────────────┴─────────────┘

EMPTY/LOADING/ERROR: Same pattern as other screens

───────────────────────────────────────────────────────────────────────────────
SCREEN F: ORDERS LIST (TRANSACTIONS)
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Ongoing business. Past and active transactions.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු                         ගනුදෙනු      │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│▌                                          │
│▌ සුනිල් මහතා                               │
│▌ රු 2,800                     සක්‍රීයයි     │  ← Order row 96dp
│▌                               දැන්        │
│▌                                          │
├───────────────────────────────────────────┤
│▌                                          │
│▌ කමල් මහතා                                │
│▌ රු 4,500                    අවසන් විය     │  ← Order row 96dp (Dust bg)
│▌                               ඊයේ        │
│▌                                          │
└───────────────────────────────────────────┘

ORDER ROW MEASUREMENTS:
- Height: 96dp
- Accent bar: 4dp Earth, left edge
- Content padding: 24dp left (from accent), 24dp right, 16dp top/bottom
- Line gaps: 8dp

ORDER ROW TEXT:
- Line 1 (Counterpart name): 20sp Bold, Ink
- Line 2 (Amount): 20sp Bold, Gold, left-aligned
- Line 2 (Status): 14sp Bold, status-colored, right-aligned
- Line 3 (Time): 14sp Regular, Stone, right-aligned

STATUS COLORS:
┌────────────┬─────────────────┬─────────────┐
│ Status     │ Color           │ Label (SI)  │
├────────────┼─────────────────┼─────────────┤
│ Active     │ Green #2D5016   │ සක්‍රීයයි     │
│ Completed  │ Stone #6B6B6B   │ අවසන් විය   │
│ Problem    │ Urgent #A63D2F  │ ගැටලුවක්    │
└────────────┴─────────────────┴─────────────┘

STATUS LABELS (all languages):
┌────────────┬────────────────┬───────────────┬─────────────┐
│ Status     │ Sinhala        │ Tamil         │ English     │
├────────────┼────────────────┼───────────────┼─────────────┤
│ Active     │ සක්‍රීයයි        │ செயலில்        │ ACTIVE      │
│ Completed  │ අවසන් විය      │ முடிந்தது      │ DONE        │
│ Problem    │ ගැටලුවක්       │ சிக்கல்        │ PROBLEM     │
└────────────┴────────────────┴───────────────┴─────────────┘

───────────────────────────────────────────────────────────────────────────────
SCREEN G: ORDER DETAIL (TRANSACTION DETAIL)
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Full transaction details. Sections separated by Earth dividers.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු                          #00234     │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│  භාණ්ඩය                                    │
│  තක්කාලි                                   │  ← Item section
│  ප්‍රමාණය                                   │
│  10 kg                                    │
│  එකඟ වූ මිල                                │
│  රු 2,800                                 │
├───────────────────────────────────────────┤  ← 4dp Earth divider
│  රැගෙන යන ස්ථානය                            │
│  නුවර පොළ අසල                              │  ← Logistics section
│  දිනය සහ වේලාව                             │
│  ජනවාරි 15, උදේ 7:00                       │
├───────────────────────────────────────────┤  ← 4dp Earth divider
│  විකුණන්නා                                  │
│  සුනිල් මහතා                               │  ← Counterpart section
│  දිස්ත්‍රික්කය                               │
│  නුවර                                      │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │         ඇමතුමක් ගන්න                  │  │  ← Primary button (Gold)
│  └─────────────────────────────────────┘  │
├───────────────────────────────────────────┤  ← 4dp Earth divider
│  තත්ත්වය                                   │
│  සක්‍රීයයි                                   │  ← Status section
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │     ගැටලුවක් වාර්තා කරන්න              │  │  ← Urgent button
│  └─────────────────────────────────────┘  │
│                                           │
└───────────────────────────────────────────┘

SECTION LABELS:
┌──────────────────────┬────────────────────────────┬─────────────────────┐
│ Sinhala              │ Tamil                      │ English             │
├──────────────────────┼────────────────────────────┼─────────────────────┤
│ භාණ්ඩය               │ பொருள்                      │ PRODUCT             │
│ ප්‍රමාණය              │ அளவு                        │ QUANTITY            │
│ එකඟ වූ මිල           │ ஒப்புக்கொண்ட விலை           │ AGREED PRICE        │
│ රැගෙන යන ස්ථානය       │ எடுக்கும் இடம்              │ PICKUP              │
│ දිනය සහ වේලාව        │ தேதி மற்றும் நேரம்          │ DATE AND TIME       │
│ විකුණන්නා / ගැනුම්කරු │ விற்பவர் / வாங்குபவர்       │ SELLER / BUYER      │
│ දිස්ත්‍රික්කය          │ மாவட்டம்                    │ DISTRICT            │
│ තත්ත්වය              │ நிலை                        │ STATUS              │
└──────────────────────┴────────────────────────────┴─────────────────────┘

BUTTON LABELS:
┌────────────────────────────┬──────────────────────────────┬────────────────────┐
│ Sinhala                    │ Tamil                        │ English            │
├────────────────────────────┼──────────────────────────────┼────────────────────┤
│ ඇමතුමක් ගන්න                │ அழைக்கவும்                    │ CALL               │
│ ගැටලුවක් වාර්තා කරන්න       │ சிக்கலைப் புகாரளிக்கவும்      │ REPORT PROBLEM     │
└────────────────────────────┴──────────────────────────────┴────────────────────┘

REPORT PROBLEM BUTTON:
- Style: UrgentButton (Rice bg, Urgent border, Urgent text)
- Height: 56dp
- Margin: 24dp all sides

───────────────────────────────────────────────────────────────────────────────
SCREEN H: CREATE LISTING FORM
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Farmer enters listing details. Simple form, large inputs.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු              නව දැන්වීමක්             │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│  භාණ්ඩයේ නම                                │  ← Label
│  ┌─────────────────────────────────────┐  │
│  │                                     │  │  ← Input 56dp
│  └─────────────────────────────────────┘  │
│                                           │
│  මිල (රුපියල්)                             │
│  ┌─────────────────────────────────────┐  │
│  │                                     │  │
│  └─────────────────────────────────────┘  │
│                                           │
│  ඒකකය                                     │
│  ┌─────────────────────────────────────┐  │
│  │ තෝරන්න                        ▼     │  │  ← Dropdown
│  └─────────────────────────────────────┘  │
│                                           │
│  ... more fields ...                      │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │       දැන්වීම පළ කරන්න               │  │  ← Primary button
│  └─────────────────────────────────────┘  │
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │         අවලංගු කරන්න                 │  │  ← Secondary button
│  └─────────────────────────────────────┘  │
│                                           │
└───────────────────────────────────────────┘

FORM FIELD COMPONENT:
- Label: 14sp Bold, Stone #6B6B6B
- Gap below label: 4dp
- Input box: 56dp height, 4dp Earth border, Rice background
- Input text: 20sp Regular, Ink #1A1A1A
- Input padding: 16dp horizontal
- Error text (if invalid): 14sp Regular, Urgent #A63D2F, 4dp below input
- Gap between fields: 16dp

FORM FIELDS:
┌────────────────────┬────────────────────────┬─────────────────────┬──────────┐
│ Field              │ Sinhala Label          │ English Label       │ Required │
├────────────────────┼────────────────────────┼─────────────────────┼──────────┤
│ Product Name       │ භාණ්ඩයේ නම             │ PRODUCT NAME        │ Yes      │
│ Price              │ මිල (රුපියල්)          │ PRICE (RUPEES)      │ Yes      │
│ Unit               │ ඒකකය                  │ UNIT                │ Yes      │
│ Quantity           │ ප්‍රමාණය (විකල්ප)       │ QUANTITY (OPTIONAL) │ No       │
│ District           │ දිස්ත්‍රික්කය           │ DISTRICT            │ Yes      │
│ Pickup Location    │ රැගෙන යන ස්ථානය        │ PICKUP LOCATION     │ Yes      │
│ Available From     │ සිට ලබා ගත හැක        │ AVAILABLE FROM      │ No       │
│ Available Until    │ දක්වා ලබා ගත හැක      │ AVAILABLE UNTIL     │ No       │
└────────────────────┴────────────────────────┴─────────────────────┴──────────┘

UNIT DROPDOWN OPTIONS:
┌─────────────────────┬────────────────────────┬──────────────────┐
│ Sinhala             │ Tamil                  │ English          │
├─────────────────────┼────────────────────────┼──────────────────┤
│ කිලෝග්‍රෑම් (kg)      │ கிலோகிராம் (kg)         │ KILOGRAM (kg)    │
│ ග්‍රෑම් (g)          │ கிராம் (g)              │ GRAM (g)         │
│ ලීටර් (L)           │ லிட்டர் (L)             │ LITER (L)        │
│ ගෙඩි                │ எண்ணிக்கை              │ PIECE            │
│ බඳුන               │ கட்டு                   │ BUNDLE           │
└─────────────────────┴────────────────────────┴──────────────────┘

VALIDATION RULES:
- Product name: Non-empty, max 100 chars
- Price: Positive integer, max 999999
- Unit: Must select one
- District: Must select one
- Pickup location: Non-empty, max 200 chars

VALIDATION ERROR MESSAGES:
┌─────────────────────────────────┬─────────────────────────────────┬───────────────────────┐
│ Sinhala                         │ Tamil                           │ English               │
├─────────────────────────────────┼─────────────────────────────────┼───────────────────────┤
│ මෙය පිරවිය යුතුය                 │ இது தேவை                        │ THIS IS REQUIRED      │
│ වලංගු මිලක් ඇතුළත් කරන්න         │ சரியான விலையை உள்ளிடவும்        │ ENTER A VALID PRICE   │
└─────────────────────────────────┴─────────────────────────────────┴───────────────────────┘

VALIDATION BEHAVIOR:
1. Validate on POST button tap
2. Show Urgent error text below first invalid field
3. Scroll to first error
4. Do not submit until all required fields valid

BUTTON LABELS:
┌─────────────────────────────┬───────────────────────────────┬────────────────────┐
│ Sinhala                     │ Tamil                         │ English            │
├─────────────────────────────┼───────────────────────────────┼────────────────────┤
│ දැන්වීම පළ කරන්න            │ பதிவை இடுக                    │ POST LISTING       │
│ අවලංගු කරන්න                │ ரத்து செய்யவும்                │ CANCEL             │
└─────────────────────────────┴───────────────────────────────┴────────────────────┘

───────────────────────────────────────────────────────────────────────────────
SCREEN I: CREATE LISTING SUCCESS
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Confirm listing was posted.

LAYOUT:
┌───────────────────────────────────────────┐
│                                           │
│                                           │
│                                           │
│                                           │
│                                           │
│            දැන්වීම පළ විය                   │  ← Success message
│                                           │
│  ┌─────────────────────────────────────┐  │
│  │              හරි                     │  │  ← Primary button
│  └─────────────────────────────────────┘  │
│                                           │
│                                           │
│                                           │
│                                           │
│                                           │
└───────────────────────────────────────────┘

SUCCESS MESSAGE:
- Position: Centered, offset ~200dp from top
- Text: 24sp Bold, Green #2D5016
- Sinhala: දැන්වීම පළ විය
- Tamil: பதிவு இடப்பட்டது
- English: LISTING POSTED

DONE BUTTON:
- Width: 200dp, centered
- Style: PrimaryButton (Gold bg, Rice text)
- Gap below message: 32dp
- Sinhala: හරි
- Tamil: சரி
- English: DONE

NAVIGATION:
- DONE button → Home screen (clear backstack)

───────────────────────────────────────────────────────────────────────────────
SCREEN J: SETTINGS (LANGUAGE SELECTION)
───────────────────────────────────────────────────────────────────────────────

PURPOSE: Select language. One-time or occasional use.

LAYOUT:
┌───────────────────────────────────────────┐
│  ආපසු                         භාෂාව        │  ← Header 72dp
├───────────────────────────────────────────┤  ← 4dp Earth divider
│                                           │
│                 සිංහල                      │  ← Row 96dp (current: Green text)
│                                           │
├───────────────────────────────────────────┤
│                                           │
│                 தமிழ்                      │  ← Row 96dp (Dust bg)
│                                           │
├───────────────────────────────────────────┤
│                                           │
│                ENGLISH                    │  ← Row 96dp
│                                           │
└───────────────────────────────────────────┘

HEADER TITLE:
- Sinhala: භාෂාව
- Tamil: மொழி
- English: LANGUAGE

LANGUAGE OPTIONS:
- සිංහල (always shown in Sinhala script)
- தமிழ் (always shown in Tamil script)
- ENGLISH (always shown in English)

CURRENT SELECTION INDICATOR:
- Currently selected language: Text color Green #2D5016
- Add suffix: "තෝරා ඇත" / "தேர்ந்தெடுக்கப்பட்டது" / "SELECTED"
- Or simply show Green color without suffix

SELECTION BEHAVIOR:
1. Tap different language row
2. Show confirmation dialog:
   - Sinhala: භාෂාව වෙනස් කරන්නද?
   - Tamil: மொழியை மாற்றவா?
   - English: CHANGE LANGUAGE?
3. Two buttons: YES (Primary) / NO (Secondary)
4. On YES: Save to DataStore, restart app in new language

════════════════════════════════════════════════════════════════════════════════
SECTION 4: NAVIGATION ARCHITECTURE
════════════════════════════════════════════════════════════════════════════════

NAVIGATION FRAMEWORK: Jetpack Compose Navigation with single NavHost

ROUTES:
sealed class NavRoute(val route: String) {
    object Home : NavRoute("home")
    object Category : NavRoute("category/{flow}") // flow = "buy" | "sell"
    object Listings : NavRoute("listings/{category}")
    object ListingDetail : NavRoute("listing/{id}")
    object CreateListing : NavRoute("create_listing/{category}")
    object CreateListingSuccess : NavRoute("create_listing_success")
    object Prices : NavRoute("prices")
    object Orders : NavRoute("orders")
    object OrderDetail : NavRoute("order/{id}")
    object Settings : NavRoute("settings")
}

NAVIGATION FLOWS:
┌─────────────────────────────────────────────────────────────────────────┐
│ BUY FLOW:                                                               │
│ Home → Category(buy) → Listings → ListingDetail                         │
├─────────────────────────────────────────────────────────────────────────┤
│ SELL FLOW:                                                              │
│ Home → Category(sell) → CreateListing → CreateListingSuccess → Home     │
├─────────────────────────────────────────────────────────────────────────┤
│ PRICES FLOW:                                                            │
│ Home → Prices                                                           │
├─────────────────────────────────────────────────────────────────────────┤
│ ORDERS FLOW:                                                            │
│ Home → Orders → OrderDetail                                             │
├─────────────────────────────────────────────────────────────────────────┤
│ SETTINGS FLOW:                                                          │
│ Home → Settings (language change restarts app)                          │
└─────────────────────────────────────────────────────────────────────────┘

BACK BEHAVIOR:
- All screens except Home show back text (top-left)
- Back text tap: navController.popBackStack()
- CreateListingSuccess DONE button: popBackStack to Home (clear intermediate)

════════════════════════════════════════════════════════════════════════════════
SECTION 5: BACKEND API CONTRACTS
════════════════════════════════════════════════════════════════════════════════

CONFIRMED ENDPOINTS (working):

GET /api/v1/listings
Response: {
  listings: [{
    id: string,
    productName: string,
    price: number,
    unit: string,
    district: string,
    pickupLocation: string,
    availableFrom: string | null,
    availableUntil: string | null,
    sellerId: string,
    sellerName: string,
    sellerPhone: string,
    category: string,
    createdAt: string
  }]
}

GET /api/v1/listings/:id
Response: { listing: { ...same as above } }

POST /api/v1/listings
Request: {
  productName: string,
  price: number,
  unit: string,
  quantity: number | null,
  district: string,
  pickupLocation: string,
  availableFrom: string | null,
  availableUntil: string | null,
  category: string
}
Response: { listing: { id, ...rest } }

GET /api/v1/transactions
Response: {
  transactions: [{
    id: string,
    listingId: string,
    productName: string,
    buyerId: string,
    buyerName: string,
    sellerId: string,
    sellerName: string,
    quantity: number,
    agreedPrice: number,
    status: "active" | "completed" | "problem",
    pickupLocation: string,
    scheduledDate: string,
    createdAt: string
  }]
}

GET /api/v1/transactions/:id
Response: { transaction: { ...same as above, plus phone numbers } }

NOT YET IMPLEMENTED (stub with mock data):

GET /api/v1/prices
Mock Response: {
  prices: [{
    product: string,
    price: number,
    unit: string
  }],
  updatedAt: string
}

════════════════════════════════════════════════════════════════════════════════
SECTION 6: IMPLEMENTATION PATTERNS
════════════════════════════════════════════════════════════════════════════════

PRESSED STATE (NO RIPPLE):
```kotlin
// Custom indication that shows Earth overlay on press
object IndustrialIndication : Indication {
    private class IndustrialIndicationInstance(
        private val isPressed: State<Boolean>
    ) : IndicationInstance {
        override fun ContentDrawScope.drawIndication() {
            drawContent()
            if (isPressed.value) {
                drawRect(
                    color = Color(0x268B4513), // Earth at 15% alpha
                    size = size
                )
            }
        }
    }

    @Composable
    override fun rememberUpdatedInstance(
        interactionSource: InteractionSource
    ): IndicationInstance {
        val isPressed = interactionSource.collectIsPressedAsState()
        return remember(interactionSource) {
            IndustrialIndicationInstance(isPressed)
        }
    }
}

// Usage
fun Modifier.industrialClickable(onClick: () -> Unit) = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = IndustrialIndication,
        onClick = onClick
    )
}
````

LANGUAGE HANDLING:

```kotlin
// DataStore for language preference
class LanguagePreferences(private val dataStore: DataStore<Preferences>) {
    val language: Flow<Language> = dataStore.data.map { prefs ->
        Language.valueOf(prefs[LANGUAGE_KEY] ?: Language.SINHALA.name)
    }

    suspend fun setLanguage(language: Language) {
        dataStore.edit { it[LANGUAGE_KEY] = language.name }
    }
}

enum class Language { SINHALA, TAMIL, ENGLISH }

// String helper (NO .uppercase() for Sinhala/Tamil)
fun String.industrialCase(language: Language): String =
    if (language == Language.ENGLISH) uppercase() else this
```

ERROR HANDLING UX:

- Network timeout: 15 seconds
- On error: Show ErrorState with message + RETRY button
- RETRY triggers ViewModel re-fetch
- Show LoadingState while retrying

════════════════════════════════════════════════════════════════════════════════
SECTION 7: FILE STRUCTURE
════════════════════════════════════════════════════════════════════════════════

app/src/main/java/com/agrimarket/
├── ui/
│ ├── theme/
│ │ ├── Color.kt # Token definitions
│ │ ├── Type.kt # Typography
│ │ └── Spacing.kt # Spacing constants
│ ├── components/
│ │ ├── IndustrialHeader.kt # 72dp header with back + title
│ │ ├── IndustrialDivider.kt # 4dp Earth divider
│ │ ├── IndustrialRow.kt # Base row with accent bar option
│ │ ├── Buttons.kt # Primary, Secondary, Urgent, Text
│ │ ├── States.kt # Loading, Empty, Error
│ │ ├── FormFields.kt # TextField, Dropdown
│ │ ├── ListingRow.kt # 112dp listing row
│ │ ├── PriceRow.kt # 72dp price row
│ │ ├── OrderRow.kt # 96dp order row
│ │ ├── CategoryRow.kt # 96dp category row
│ │ └── QuadrantGrid.kt # 2×2 home grid
│ ├── screens/
│ │ ├── home/
│ │ │ ├── HomeScreen.kt
│ │ │ └── HomeViewModel.kt # (minimal, mostly navigation)
│ │ ├── category/
│ │ │ └── CategoryScreen.kt
│ │ ├── listings/
│ │ │ ├── ListingsScreen.kt
│ │ │ └── ListingsViewModel.kt
│ │ ├── listing_detail/
│ │ │ ├── ListingDetailScreen.kt
│ │ │ └── ListingDetailViewModel.kt
│ │ ├── create_listing/
│ │ │ ├── CreateListingScreen.kt
│ │ │ ├── CreateListingViewModel.kt
│ │ │ └── CreateListingSuccessScreen.kt
│ │ ├── prices/
│ │ │ ├── PricesScreen.kt
│ │ │ └── PricesViewModel.kt
│ │ ├── orders/
│ │ │ ├── OrdersScreen.kt
│ │ │ └── OrdersViewModel.kt
│ │ ├── order_detail/
│ │ │ ├── OrderDetailScreen.kt
│ │ │ └── OrderDetailViewModel.kt
│ │ └── settings/
│ │ ├── SettingsScreen.kt
│ │ └── SettingsViewModel.kt
│ └── navigation/
│ ├── NavRoutes.kt
│ └── NavGraph.kt
├── data/
│ ├── api/
│ │ ├── ListingsApi.kt
│ │ ├── TransactionsApi.kt
│ │ └── PricesApi.kt
│ ├── repository/
│ │ ├── ListingsRepository.kt
│ │ ├── TransactionsRepository.kt
│ │ └── PricesRepository.kt
│ └── local/
│ └── LanguagePreferences.kt
├── di/
│ └── AppModule.kt # Hilt module
└── util/
├── StringExtensions.kt # industrialCase()
└── TimeUtils.kt # Relative time formatting

app/src/main/res/
├── values/
│ └── strings.xml # English strings
├── values-si/
│ └── strings.xml # Sinhala strings
└── values-ta/
└── strings.xml # Tamil strings

════════════════════════════════════════════════════════════════════════════════
SECTION 8: ASSIGNMENT
════════════════════════════════════════════════════════════════════════════════

EXECUTE IN THIS ORDER:

1. SCAN EXISTING CODE
   - Identify current navigation setup
   - Identify existing screens, ViewModels, API services
   - Identify existing theme files
   - Report findings before proceeding

2. IMPLEMENT DESIGN SYSTEM (ui/theme/)
   - Color.kt with all tokens
   - Type.kt with typography styles
   - Spacing.kt with dp constants

3. IMPLEMENT COMPONENTS (ui/components/)
   - All components listed in Section 7
   - Each component must have @Preview functions
   - Use IndustrialIndication for pressed states (no ripple)

4. IMPLEMENT SCREENS (ui/screens/)
   - All screens A through J
   - Each with ViewModel where data is needed
   - Connect to existing Retrofit services
   - Show Loading/Empty/Error states properly

5. IMPLEMENT NAVIGATION (ui/navigation/)
   - NavRoutes sealed class
   - NavGraph with all routes wired
   - Back behavior via navController.popBackStack()

6. IMPLEMENT LANGUAGE HANDLING
   - LanguagePreferences DataStore
   - String resources for all three languages
   - industrialCase() helper for English uppercase

7. WIRE DATA LAYER
   - Use existing API services where available
   - Create mock/stub for Prices endpoint
   - Proper error handling with retry

════════════════════════════════════════════════════════════════════════════════
SECTION 9: OUTPUT FORMAT
════════════════════════════════════════════════════════════════════════════════

OUTPUT REQUIREMENTS:

1. SCAN REPORT (first)
   - List of existing relevant files found
   - Current navigation approach
   - Current theme setup
   - Gaps identified

2. IMPLEMENTATION REPORT
   - Files created (with paths)
   - Files modified (with what changed)
   - Key code snippets only where clarification needed (not full dumps)

3. COMPLETION CHECKLIST
   ☐ Design tokens (Color, Type, Spacing)
   ☐ Components (list each)
   ☐ Screens (list each with states)
   ☐ Navigation wired
   ☐ Language handling
   ☐ API integration
   ☐ Loading/Empty/Error states

4. KNOWN ISSUES / TODOs
   - Anything not completed
   - Anything requiring backend changes
   - Anything needing design clarification

════════════════════════════════════════════════════════════════════════════════
SECTION 10: CONSTRAINTS (NON-NEGOTIABLE)
════════════════════════════════════════════════════════════════════════════════

- Do NOT invent new color tokens
- Do NOT add icons or images
- Do NOT use rounded corners, shadows, or animations
- Do NOT use ripple effect (use IndustrialIndication)
- Do NOT apply .uppercase() to Sinhala or Tamil strings
- Do NOT exceed 360×800dp baseline calculations
- Do NOT add "Get started", "Welcome", or marketing copy
- Keep code clean: MVVM, StateFlow, immutable UI state
- Every component should be preview-friendly

════════════════════════════════════════════════════════════════════════════════

BEGIN. Start by scanning existing code and reporting findings.

```

---

## Summary of Enhancements Made

| Area | What Was Added |
|------|----------------|
| **UI Specs** | Complete measurements for all 10 screens with ASCII layouts |
| **String Content** | Full tables for Sinhala, Tamil, English for every label, button, message |
| **API Contracts** | Explicit request/response shapes for all endpoints |
| **Navigation** | Route definitions, flow diagrams, back behavior |
| **Implementation Patterns** | Pressed state code, language handling code, error flow |
| **File Structure** | Complete directory tree with all files |
| **Validation Rules** | Form field requirements, error messages |
| **States** | Loading/Empty/Error specs for every screen |
| **Section Organization** | Clear numbered sections for easy reference |
| **Execution Order** | Step-by-step assignment instructions |
| **Output Format** | Explicit expectations for what Claude should produce |

This prompt is now **self-contained** — Claude CLI has everything needed to implement the full UI without guessing or asking clarifying questions.
```
