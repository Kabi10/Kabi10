# Go-Live Checklist

## 1. GitHub (30 min)

1. github.com → New repository
   - Name: mcp-server-starter
   - Visibility: Private
   - No README (you have one)

2. Push the code:
   cd mcp-server-starter
   git init
   git add .
   git commit -m "initial release v1.0.0"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/mcp-server-starter.git
   git push -u origin main

3. Verify on GitHub: all files present, README renders correctly

4. Tag the release:
   git tag v1.0.0
   git push origin v1.0.0

5. Set up collaborator invite automation (optional but recommended):
   - Gumroad has a webhook on purchase
   - OR: manually invite buyers via Settings → Collaborators after each sale
   - Start manual. Automate once you have volume.

---

## 2. Gumroad listing (30 min)

1. app.gumroad.com → New Product → Digital Product

2. Product name:
   MCP Server Starter Kit — Production-Ready Python

3. Upload file:
   - Upload mcp-server-starter.zip
   - OR: in the description, note that delivery is via GitHub collaborator invite
     and tell buyers to email/DM you their GitHub username after purchase
   - Recommendation: use the ZIP for now, switch to GitHub invite once you
     have a workflow for it

4. Price: $39.00
   - Enable "Let buyers pay what they want" with $39 minimum if you want

5. Cover image:
   - Open gumroad-thumbnail.html in Chrome
   - Zoom to 100% (Cmd+0)
   - Screenshot at exactly 1280×720
   - Upload as cover image

6. Description:
   - Paste from gumroad-listing-copy.md
   - Add your Loom link at the top once recorded (optional but worth it)

7. Refund policy:
   - Paste from gumroad-listing-copy.md refund section

8. Tags:
   mcp, model context protocol, claude, cursor, python, fastmcp,
   ai tools, developer tools, starter kit, boilerplate

9. Publish

10. Copy your product URL. You'll need it for the Reddit posts.

---

## 3. Test purchase (5 min)

Buy your own product at $0 (set a 100% discount code for yourself).
Confirm the download works, the ZIP extracts cleanly, and the README
renders correctly in the download.

---

## 4. Post in r/ClaudeAI (10 min)

- Copy the r/ClaudeAI post from launch-distribution.md
- Post it
- In the first comment, drop your Gumroad link
- Check back in 2 hours to reply to any questions

---

## 5. Add to your Fiverr gigs (5 min)

On your MCP server Fiverr gig:
- Add "See a production example on Gumroad" to your gig description
- Link to the Gumroad product

Buyers who can't afford a custom build buy the kit instead.
Buyers who buy the kit and get stuck hire you for the custom work.
Both directions work.

---

## Total time to live: ~75 minutes
