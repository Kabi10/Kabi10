# GitHub Repo Organization - Recent Repos

Generated from `Kabi10` repos created in the last 3-4 days, checked on 2026-05-04 PDT.

Last updated: 2026-05-04 PDT after duplicate cleanup, repo renames, and Replit-Kabi split.

## Quick State

- Recent repos checked: 19
- Active private app repos: 15
- Archived duplicate repos: 2 (`Attachment-List`, `Ai-Ghost-Writing`)
- Split-out repos created: 2 (`What-Ifs`, `Project-Ideas`)
- Public fork/tool repos: 1 (`yarle`)
- Empty shell repos: 1 archived (`Attachment-List`)
- Locally cloned under `C:\dev`: 1 (`Replit-Kabi`)
- Common stack: TypeScript, pnpm workspace, Replit scaffold
- Common cleanup gap: most active repos still need descriptions, READMEs, and GitHub topics

## Recommended Groups

### Keep Active

These look like real app/workspace repos and should get descriptions and topics first.

| Repo | Working Label | Suggested Description | Suggested Topics |
| --- | --- | --- | --- |
| `Hereticreader` | AI research / belief challenge | Dark academia research app that stress-tests beliefs with AI reasoning and academic-source workflows. | `replit-export`, `typescript`, `ai-research`, `knowledge-tools` |
| `Poet` | AI poetry | AI poetry and creative writing workspace. | `replit-export`, `typescript`, `ai`, `writing` |
| `Opssuite` | Operations tracker | Operations tracking workspace for tasks, workflows, and team execution. | `replit-export`, `typescript`, `operations`, `productivity` |
| `Constellation` | Constellation workspace | Constellation app workspace. Needs a clearer product description after inspection. | `replit-export`, `typescript`, `needs-review` |
| `Dreamweaver` | Aether dream journal | AI dream journal with analysis, sleep tracking, lucid dreaming coaching, and community features. | `replit-export`, `typescript`, `ai`, `dream-journal`, `sleep-tracking` |
| `Memoirstudio` | Memos ghostwriting | AI ghostwriting platform that turns voice and text memos into polished written content. | `replit-export`, `typescript`, `ai`, `ghostwriting`, `writing` |
| `Ai-Abstract-Art-Generator` | Abstract art generator | AI app that turns abstract concepts, emotions, and ideas into generated visual art. | `replit-export`, `typescript`, `ai-art`, `image-generation`, `creative-tools` |
| `Ai-Art-Forensics` | AI art forensics | AI art forensics and authentication workspace. | `replit-export`, `typescript`, `ai-art`, `forensics`, `authentication` |
| `Storyteller` | Fiction creator | AI-assisted fiction and story creation workspace. | `replit-export`, `typescript`, `ai`, `fiction`, `writing` |
| `Organiverse` | Organ chat app | Group chat app where internal organs text each other in real time. | `replit-export`, `typescript`, `health`, `education`, `chat` |
| `Pathways` | TransitDoc | Dependency-aware document readiness tracker for international relocations. | `replit-export`, `typescript`, `pwa`, `relocation`, `documents` |
| `Awakened` | Awakened workspace | Awakened app workspace. Needs a clearer product description after inspection. | `replit-export`, `typescript`, `needs-review` |
| `Chembench` | Chemistry bench | Chemistry bench workspace. Needs a clearer product description after inspection. | `replit-export`, `typescript`, `chemistry`, `education` |
| `Batchcard` | Batchcard | Batchcard workspace. Needs a clearer product description after inspection. | `replit-export`, `typescript`, `needs-review` |
| `Memory-Builder` | MemoryAI | AI-powered event album builder for imported photos, ranking, captions, and exportable albums. | `replit-export`, `typescript`, `ai`, `photo-albums`, `memory` |
| `Replit-Kabi` | Replit hub / agent handoff | Replit workspace hub with agent notes, monitoring summary, and GitHub workflow handoff. | `replit-export`, `typescript`, `agent-handoff`, `monitoring`, `replit-hub` |

### Split From Replit-Kabi

These repos were split out from `Replit-Kabi` while leaving the original hub repo intact.

| Repo | Contents | Verification |
| --- | --- | --- |
| `What-Ifs` | `artifacts/api-server`, `artifacts/what-ifs`, `artifacts/what-ifs-mobile`, shared `lib/*` packages. | `pnpm run typecheck` passed. `pnpm run build` passed after local Windows/Replit env fixes. |
| `Project-Ideas` | `artifacts/project-ideas` only, with simplified pnpm workspace config. | `pnpm run typecheck` passed. `pnpm run build` passed. |

Note: existing repo `ideas` is separate. It is a broader private ideas/docs repository, not the runnable Replit `Project-Ideas` app.

### Completed Cleanup

These duplicate or misleading repos were handled on GitHub.

| Repo | Finding | Action Taken |
| --- | --- | --- |
| `Attachment-List` | Empty private repo. Description pointed to the same Replit project as `Replit-Kabi`. | Archived. |
| `Ai-Ghost-Writing` | Same file tree as `Memoirstudio` except `.replit`; same README and `memos-web` artifact. | Archived. `Memoirstudio` is canonical. |
| `Ai-art-authenticator` | Name suggested authentication, but `replit.md` described abstract-concept art generation. | Renamed to `Ai-Abstract-Art-Generator`. |
| `Art-art-forensics` | Name had duplicated wording and did not match the `ai-art-forensics` artifact clearly. | Renamed to `Ai-Art-Forensics`. |

### Leave Separate

| Repo | Reason | Suggested Topics |
| --- | --- | --- |
| `yarle` | Public fork of an Evernote-to-Markdown converter. Different from the Replit app group. | `fork`, `evernote`, `markdown`, `converter` |

## Suggested GitHub Cleanup Order

1. Add descriptions and topics to the remaining "Keep Active" repos.
2. Add `needs-review` topic to vague repos instead of guessing their purpose.
3. Add short READMEs to repos that currently only have `replit.md`.
4. Clone only the active repos you plan to work on locally.

## Low-Risk Metadata Commands

Run only after confirming the descriptions and topics are acceptable.

```powershell
gh repo edit Kabi10/Hereticreader --description "Dark academia research app that stress-tests beliefs with AI reasoning and academic-source workflows." --add-topic replit-export --add-topic typescript --add-topic ai-research --add-topic knowledge-tools
gh repo edit Kabi10/Dreamweaver --description "AI dream journal with analysis, sleep tracking, lucid dreaming coaching, and community features." --add-topic replit-export --add-topic typescript --add-topic ai --add-topic dream-journal --add-topic sleep-tracking
gh repo edit Kabi10/Memory-Builder --description "AI-powered event album builder for imported photos, ranking, captions, and exportable albums." --add-topic replit-export --add-topic typescript --add-topic ai --add-topic photo-albums --add-topic memory
```

Already applied:

```powershell
gh repo archive Kabi10/Attachment-List --yes
gh repo archive Kabi10/Ai-Ghost-Writing --yes
gh repo rename Ai-Abstract-Art-Generator --repo Kabi10/Ai-art-authenticator --yes
gh repo rename Ai-Art-Forensics --repo Kabi10/Art-art-forensics --yes
gh repo edit Kabi10/Memoirstudio --description "AI ghostwriting platform that turns voice and text memos into polished written content." --add-topic replit-export --add-topic typescript --add-topic ai --add-topic ghostwriting --add-topic writing
gh repo edit Kabi10/Ai-Abstract-Art-Generator --description "AI app that turns abstract concepts, emotions, and ideas into generated visual art." --add-topic replit-export --add-topic typescript --add-topic ai-art --add-topic image-generation --add-topic creative-tools
gh repo edit Kabi10/Ai-Art-Forensics --description "AI art forensics and authentication workspace." --add-topic replit-export --add-topic typescript --add-topic ai-art --add-topic forensics --add-topic authentication
gh repo edit Kabi10/Replit-Kabi --description "Replit workspace hub with agent notes, monitoring summary, and GitHub workflow handoff." --add-topic replit-export --add-topic typescript --add-topic agent-handoff --add-topic monitoring --add-topic replit-hub
```
