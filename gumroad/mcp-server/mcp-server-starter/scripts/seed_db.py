"""
scripts/seed_db.py

Recreates and seeds the sample database.
Run this if you've modified the schema or want to reset to the default data.

Usage:
    python scripts/seed_db.py
    python scripts/seed_db.py --db-path ./data/custom.db
"""

import argparse
import sqlite3
from pathlib import Path


SCHEMA = """
CREATE TABLE IF NOT EXISTS products (
    id       INTEGER PRIMARY KEY,
    name     TEXT    NOT NULL,
    category TEXT    NOT NULL,
    price    REAL    NOT NULL,
    stock    INTEGER NOT NULL,
    sku      TEXT    UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id         INTEGER PRIMARY KEY,
    product_id INTEGER REFERENCES products(id),
    quantity   INTEGER NOT NULL,
    status     TEXT    NOT NULL,
    created_at TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory_log (
    id         INTEGER PRIMARY KEY,
    product_id INTEGER REFERENCES products(id),
    change     INTEGER NOT NULL,
    reason     TEXT    NOT NULL,
    logged_at  TEXT    NOT NULL
);
"""

PRODUCTS = [
    (1, "Widget A",       "hardware",      9.99,  150, "WGT-001"),
    (2, "Widget B",       "hardware",     14.99,   80, "WGT-002"),
    (3, "Connector Kit",  "hardware",     24.99,   45, "CON-001"),
    (4, "User Manual",    "documentation", 4.99,  500, "DOC-001"),
    (5, "Pro Subscription","software",    99.99,  999, "SUB-PRO"),
    (6, "Starter Pack",   "bundle",       49.99,   30, "BDL-001"),
]

ORDERS = [
    (1, 1,  5, "completed", "2024-01-10T09:00:00"),
    (2, 2,  2, "completed", "2024-01-11T10:30:00"),
    (3, 3,  1, "pending",   "2024-01-12T14:00:00"),
    (4, 5,  3, "completed", "2024-01-13T08:15:00"),
    (5, 1, 10, "shipped",   "2024-01-14T11:00:00"),
    (6, 6,  1, "pending",   "2024-01-15T16:45:00"),
]

INVENTORY_LOG = [
    (1, 1,  -5, "order #1 fulfilled",    "2024-01-10T09:05:00"),
    (2, 2,  -2, "order #2 fulfilled",    "2024-01-11T10:35:00"),
    (3, 1,  50, "restock from supplier", "2024-01-12T08:00:00"),
    (4, 3,  -1, "order #3 allocated",    "2024-01-12T14:05:00"),
    (5, 1, -10, "order #5 allocated",    "2024-01-14T11:05:00"),
]


def seed(db_path: str) -> None:
    path = Path(db_path)
    path.parent.mkdir(parents=True, exist_ok=True)

    conn = sqlite3.connect(path)
    c = conn.cursor()

    # Drop and recreate for a clean reset
    c.executescript("DROP TABLE IF EXISTS inventory_log; DROP TABLE IF EXISTS orders; DROP TABLE IF EXISTS products;")
    c.executescript(SCHEMA)

    c.executemany("INSERT INTO products VALUES (?,?,?,?,?,?)", PRODUCTS)
    c.executemany("INSERT INTO orders VALUES (?,?,?,?,?)", ORDERS)
    c.executemany("INSERT INTO inventory_log VALUES (?,?,?,?,?)", INVENTORY_LOG)

    conn.commit()
    conn.close()

    print(f"Database seeded: {path.resolve()}")
    print(f"  {len(PRODUCTS)} products")
    print(f"  {len(ORDERS)} orders")
    print(f"  {len(INVENTORY_LOG)} inventory log entries")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Seed the MCP starter kit database.")
    parser.add_argument("--db-path", default="./data/data.db", help="Path to the SQLite database file")
    args = parser.parse_args()
    seed(args.db_path)
