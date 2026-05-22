import sqlite3
conn = sqlite3.connect('/opt/telesearch/telesearch.db')
try:
    conn.execute("ALTER TABLE flagged_groups ADD COLUMN flag_category TEXT DEFAULT 'other'")
    print("Added flag_category column")
except sqlite3.OperationalError as e:
    if "duplicate" in str(e).lower():
        print("Column already exists")
    else:
        print("Error:", e)
conn.commit()
conn.close()
print("Done")