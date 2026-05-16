# Wiki Instructions

## Purpose

This is a personal knowledge base. Domains emerge organically from content added over time — no fixed taxonomy is imposed upfront.

## Page Format

All pages are Markdown files with YAML frontmatter:

```yaml
---
title: Page Title
tags: [tag1, tag2]
created: YYYY-MM-DD
sources:
  - url or description
related:
  - [[other-page]]
---
```

## Page Types

- **concept** — an idea, principle, or technique
- **source-summary** — distilled notes from a URL, paper, article, or book
- **entity** — a person, org, tool, or project
- **comparison** — side-by-side analysis of two or more things
- **synthesis** — original thinking that connects multiple wiki pages

## Categories & Tags

Do **not** enforce fixed categories. Tags are freeform. `index.md` groupings emerge organically as pages accumulate — reorganize when natural clusters appear, not upfront.

---

## Workflows

### Ingest (URL or file → wiki page)

1. Fetch or read the source material.
2. Extract key information: claims, definitions, data, context.
3. Write a summary page to `wiki/` using the frontmatter schema above.
4. Update `index.md` — add the new page to the appropriate section (or create a new grouping if a cluster is emerging).
5. Append to `log.md` with today's date and the source title.
6. Scan existing wiki pages for relevance — update their `related` fields and add cross-references where useful.

### Query (answer a question from the wiki)

1. Read `index.md` first to identify candidate pages.
2. Read the relevant pages.
3. Synthesize an answer with `[[page]]` citations.
4. Offer to file the answer as a new **synthesis** page in `wiki/`.

### Lint (health check)

Check for and report:

- **Orphan pages** — files in `wiki/` not listed in `index.md`
- **Missing cross-references** — pages on related topics that don't link to each other
- **Contradictions** — conflicting claims across pages
- **Shallow pages** — stubs or pages with very little substance

---

## Log Format

Entries in `log.md` follow this pattern:

```
## [YYYY-MM-DD] action | title
```

Examples:

```
## [2026-04-05] ingest | How Transformers Work
## [2026-04-05] synthesis | Attention vs Convolution
## [2026-04-05] lint | Fixed 3 orphan pages
```
