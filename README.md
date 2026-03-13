# aMess — Work Notes

A swift note-taking workspace for work. Capture meeting notes, minutes, work instructions, and discussion summaries — structured, versioned, searchable, and git-committed.

## Quick Start

Open this repo in Claude Code and run:

```
/note
```

Claude will guide you through a fast flow:
1. Pick a note type (or let it auto-detect from your content)
2. Give it a title
3. Dump your raw content — Claude structures it, saves it, and commits it

**Power move:** include everything in the invocation and skip all questions:
```
/note sprint retro — Alice Bob Carol, decided to cut feature X, Carol owns the refactor by Friday
```

---

## Commands

### Capture

| Command | What it does |
|---------|-------------|
| `/note` | Interactive note capture — guided 3-step flow |
| `/note-agent` | Autonomous mode — paste a raw transcript/Slack dump, zero Q&A |

### Retrieve

| Command | What it does |
|---------|-------------|
| `/note-search <query>` | Search notes with filters (`type:`, `after:`, `before:`, `tag:`) |
| `/note-actions` | List all open action items grouped by owner |
| `/note-actions --owner Alice` | Open actions for a specific person |
| `/note-actions --overdue` | Only overdue action items |

### Continue

| Command | What it does |
|---------|-------------|
| `/note-followup` | Start a follow-up note pre-filled with open actions from the last relevant note |
| `/note-followup sprint retro` | Follow up a specific note by title pattern |
| `/note-digest` | Weekly digest: all notes + open actions from the past 7 days |
| `/note-digest --days 14` | Digest for the past N days |

---

## Note Types

| Type | Use For | Example |
|------|---------|---------|
| `meeting` | Informal meeting capture | Standup, sync, 1:1 |
| `minutes` | Formal meeting minutes | Board meeting, project kickoff |
| `instruction` | Work SOP / how-to guide | Onboarding steps, process docs |
| `discussion` | Conversation summaries | Decision discussion, brainstorm |
| `quick` | Anything fast | Random thought, quick ref |

---

## Folder Structure

```
notes/
├── INDEX.md           # Auto-generated dashboard (updated on every save)
├── meetings/          # Informal meeting notes
├── minutes/           # Formal meeting minutes
├── instructions/      # Work instructions / SOPs
├── discussions/       # Discussion summaries
└── quick/             # Quick / freeform notes
```

Files are named: `YYYY-MM-DD-title-slug.md`

Example: `notes/meetings/2026-03-13-sprint-planning.md`

Every note includes YAML frontmatter with `type`, `title`, `date`, `tags`, `attendees`, and `has_actions` — enabling all search and index features.

---

## Searching Notes

**Using the skill (recommended):**
```
/note-search retro
/note-search budget type:meeting after:2026-01-01
/note-search "" tag:sprint
```

**Using the script directly:**
```bash
bash scripts/note-search.sh "keyword" [type:<type>] [after:<date>] [tag:<tag>]
```

**Raw grep fallback:**
```bash
grep -r "keyword" notes/
```

---

## Action Items

**View all open actions:**
```
/note-actions
```

**Filter by owner or overdue:**
```
/note-actions --owner Alice
/note-actions --overdue
```

**Using the script directly:**
```bash
bash scripts/note-actions.sh [--owner "Alice"] [--overdue]
```

---

## Tips

- Don't format when dumping — Claude handles it
- Use `/note-agent` for Slack threads, email chains, or long transcripts
- Use `/note-followup` before recurring meetings — it pre-loads last session's open items
- Check `notes/INDEX.md` for a browsable dashboard of everything
- All notes are git-committed — full history, no data loss
