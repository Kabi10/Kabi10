-- Migration: Add flagged_groups table for background privacy scanner
PRAGMA foreign_keys = OFF;

CREATE TABLE IF NOT EXISTS flagged_groups (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    telegram_id     BIGINT  UNIQUE NOT NULL,
    title           TEXT    NOT NULL,
    username        TEXT,
    members         INTEGER,
    type            TEXT    NOT NULL DEFAULT 'group',
    keywords_matched TEXT    DEFAULT '[]',
    scan_date       DATETIME DEFAULT CURRENT_TIMESTAMP,
    reviewed        INTEGER DEFAULT 0,
    notes           TEXT    DEFAULT ''
);

CREATE INDEX IF NOT EXISTS idx_flagged_reviewed ON flagged_groups(reviewed);
CREATE INDEX IF NOT EXISTS idx_flagged_scan_date ON flagged_groups(scan_date);
CREATE INDEX IF NOT EXISTS idx_flagged_members ON flagged_groups(members);
