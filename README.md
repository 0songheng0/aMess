# aMess — Work Notes

A swift note-taking workspace for work. Capture meeting notes, minutes, work instructions, and discussion summaries — structured, versioned, and searchable.

## Quick Start

Open this repo in Claude Code and run:

```
/note
```

Claude will guide you through a 3-step flow:
1. Pick a note type
2. Give it a title
3. Dump your raw content — Claude structures it for you

The note gets saved to the right folder and committed to git automatically.

---

## Note Types

| Command | Use For | Example |
|---------|---------|---------|
| `/note` → `meeting` | Informal meeting capture | Standup, sync, 1:1 |
| `/note` → `minutes` | Formal meeting minutes | Board meeting, project kickoff |
| `/note` → `instruction` | Work SOP / how-to guide | Onboarding steps, process docs |
| `/note` → `discussion` | Conversation summaries | Decision discussion, brainstorm |
| `/note` → `quick` | Anything fast | Random thought, quick ref |

---

## Folder Structure

```
notes/
├── meetings/          # Informal meeting notes
├── minutes/           # Formal meeting minutes
├── instructions/      # Work instructions / SOPs
├── discussions/       # Discussion summaries
└── quick/             # Quick / freeform notes
```

Files are named: `YYYY-MM-DD-title-slug.md`

Example: `notes/meetings/2026-03-13-sprint-planning.md`

---

## Searching Notes

Find by keyword across all notes:

```bash
grep -r "keyword" notes/
```

Find notes from a specific date:

```bash
ls notes/*/2026-03-13-*.md
```

Find all action items:

```bash
grep -r "Action" notes/
```

---

## Tips

- Don't worry about formatting when dumping content — Claude handles it
- For meeting notes mid-meeting: just type bullet points as things happen, then `/note` to clean up after
- All notes are git-committed — full history, no data loss
