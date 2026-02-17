# CLAUDE.md

## Project Overview

Agrimarket (Sri Lanka Farmers Marketplace) is a **mobile-first Android application** with supporting backend and web components. The Android app is the primary product surface and is built using modern Android development practices. The repository contains a functioning but evolving codebase with ongoing cleanup, verification, and feature completion. The project prioritizes correctness, traceability, and incremental improvement over greenfield rewrites.

This file exists to **orient AI coding agents** so they can work effectively without guessing intent, duplicating effort, or wasting tokens.

Documentation status is audited and accurate as of **January 4, 2026**.

---

## Authoritative Documentation (Load by Default)

The following documents are considered the **primary sources of truth**. Agents should read these before making changes:

1. **README.md**

   * Project vision, scope, tech stack, and quick start
   * High-level status of the MVP

2. **PRODUCTION_READINESS_ASSESSMENT.md**

   * Production status, security posture, scalability considerations
   * Canonical checklist for MVP completeness

3. **docs/DOCUMENTATION.md**

   * Consolidated architecture overview
   * Development standards and patterns
   * Cross-references to backend and web components

4. **backend/README.md** (load when working on backend or APIs)

   * API endpoints and backend-specific behavior
   * Authentication, data flow, and service structure

Agents should assume these documents are intentionally overlapping but audience-specific.

---

## Supporting Documentation (Load Only When Relevant)

The following documents provide useful context but should **not** be loaded by default:

* **docs/PRE_LAUNCH_CHECKLIST.md** — QA, testing, and launch verification
* **web/README.md** — Web landing page overview
* **web/DEPLOYMENT.md** — Web deployment procedures
* **scripts/clickup_automation/README.md** — Automation tooling documentation

Load these only when directly working in their respective areas.

---

## Historical Documentation (Do Not Auto-Load)

These files are point-in-time artifacts and should be treated as historical reference only:

* **docs/PHASE_1_REPORT.md** (December 2025 test report)
* **scripts/clickup_automation/SYNC_SUMMARY_2025-11-26.md** (November 2025 sync report)

They should not be used to infer current system state.

---

## Known Ambiguities & How to Resolve Them

Agents may encounter minor inconsistencies across documents. Resolve them as follows:

* **Test counts**: Prefer the values in `docs/DOCUMENTATION.md`. If uncertain, verify via actual test output rather than documentation.
* **API routes**: `backend/README.md` is the canonical source for endpoint definitions and prefixes.
* **Project naming**: "Sri Lanka Farmers Marketplace" is the current name. References to earlier names reflect rebranding in progress.

If discrepancies affect behavior or architecture, stop and request clarification rather than guessing.

---

## How to Explore This Repository Safely

When starting work:

1. Read **README.md** fully.
2. Review **docs/DOCUMENTATION.md** for architecture context.
3. Inspect relevant subdirectories (`backend/`, `web/`, `scripts/`) before making assumptions.
4. Assume features may be partially implemented or intentionally staged.

Do **not** delete files, rename directories, or refactor broadly without explicit instruction.

---

## Android App — Technical Baseline

The Android application should be treated as the **primary system of record** for user-facing behavior.

Unless explicitly stated otherwise, agents should assume:

* **Language**: Kotlin
* **UI**: Jetpack Compose (no XML layouts)
* **Architecture**: MVVM
* **Navigation**: Single-Activity + Compose Navigation
* **Dependency Injection**: Hilt
* **Async**: Coroutines + Flow
* **Persistence**: Room (local), remote API via Retrofit/OkHttp
* **State Handling**: Immutable UI state, sealed classes for events and state

Agents must not introduce legacy Android patterns (Fragments-heavy navigation, XML UI, AsyncTask, etc.).

---

## Android Build & Verification Expectations

Before considering work complete, agents should:

* Ensure the project **builds successfully** using Gradle
* Run relevant unit tests and instrumented tests
* Avoid speculative dependency upgrades unless explicitly requested
* Prefer Gradle Version Catalogs if dependencies are added

If exact commands are unclear, inspect existing Gradle files rather than guessing.

---

## Working Rules for AI Agents

* Plan before implementing.
* Prefer small, reversible changes.
* Do not refactor for style alone.
* Do not rewrite documentation unless explicitly asked.
* Do not introduce new Android architectural patterns without approval.
* Ask before adding dependencies, migrations, or breaking API changes.
* If context feels insufficient, pause and ask rather than hallucinate.

---

## Model Collaboration Guidelines

This repository may be worked on using multiple AI models:

* **Claude (this agent)**: code changes, tests, refactors, repository-aware work
* **Other models (e.g., Gemini)**: product thinking, UX flows, spec drafting

Claude should treat this file as the controlling context and assume other model outputs are reviewed by a human before implementation.

---

## Intent Lock

This file exists to preserve **human intent** at this stage of the project. If instructions appear ambiguous or contradictory, default to caution and request guidance rather than proceeding.

Incremental progress > speculative completeness.
