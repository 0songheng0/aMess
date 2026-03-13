#!/usr/bin/env bash
# note-save.sh — resolve the save path for a note
# Usage: bash scripts/note-save.sh <type> <title> <date>
# Outputs the full relative file path for the note to be written to

set -euo pipefail

TYPE="${1:-quick}"
TITLE="${2:-untitled}"
DATE="${3:-$(date +%Y-%m-%d)}"

# Validate type
case "$TYPE" in
  meeting|minutes|instruction|discussion|quick) ;;
  *) TYPE="quick" ;;
esac

# Slugify title: lowercase, spaces/special chars → hyphens, trim
SLUG=$(echo "$TITLE" \
  | tr '[:upper:]' '[:lower:]' \
  | sed 's/[^a-z0-9]/-/g' \
  | sed 's/-\+/-/g' \
  | sed 's/^-//;s/-$//')

FILENAME="${DATE}-${SLUG}.md"
OUTPUT_PATH="notes/${TYPE}/${FILENAME}"

# Ensure directory exists
mkdir -p "notes/${TYPE}"

echo "$OUTPUT_PATH"
