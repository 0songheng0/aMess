#!/usr/bin/env bash
# note-actions.sh — extract open action items from all notes
# Usage: bash scripts/note-actions.sh [--owner <name>] [--overdue]
# Scans meeting and minutes notes for action table rows with status "Open"

set -euo pipefail

NOTES_DIR="notes"
FILTER_OWNER=""
FILTER_OVERDUE=false
TODAY=$(date +%Y-%m-%d)

# ---- parse args -------------------------------------------------------------

while [[ $# -gt 0 ]]; do
  case "$1" in
    --owner)  FILTER_OWNER="$2"; shift 2 ;;
    --overdue) FILTER_OVERDUE=true; shift ;;
    *) shift ;;
  esac
done

# ---- helpers ----------------------------------------------------------------

parse_field() {
  local field="$1" file="$2"
  awk '/^---/{found++; next} found==1 && /^'"$field"':/{sub(/^[^:]+: */,""); print; exit}' "$file"
}

# Parse a date string yyyy-mm-dd for comparison; returns empty if not a valid date
normalize_date() {
  local d="$1"
  if [[ "$d" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]]; then
    echo "$d"
  else
    echo ""
  fi
}

# ---- collect actions --------------------------------------------------------

declare -A OWNER_ACTIONS  # owner -> newline-separated action lines

while IFS= read -r f; do
  type=$(parse_field "type" "$f")
  title=$(parse_field "title" "$f")
  date=$(parse_field "date" "$f")
  [[ -z "$title" ]] && title=$(basename "$f" .md)

  # Only process types that have action tables
  [[ "$type" == "meeting" || "$type" == "minutes" || "$type" == "discussion" ]] || continue

  # Extract table rows that contain "Open" status
  # Table format: | Action | Owner | Due | Status | (meeting) or | # | Action | Owner | Due | Status | (minutes)
  while IFS= read -r row; do
    # Skip header rows and separator rows
    [[ "$row" =~ ^[[:space:]]*\|[[:space:]]*[-:] ]] && continue
    [[ "$row" =~ Action.*Owner ]] && continue
    [[ "$row" =~ ^[[:space:]]*#[[:space:]]*\| ]] && continue

    # Must contain "Open"
    echo "$row" | grep -qi "open" || continue

    # Parse columns by splitting on |
    IFS='|' read -ra cols <<< "$row"
    # Remove empty first/last from leading/trailing |
    local_cols=()
    for c in "${cols[@]}"; do
      trimmed=$(echo "$c" | xargs)
      local_cols+=("$trimmed")
    done

    # Determine column layout
    # meeting:  | Action | Owner | Due | Status |        → 4 data cols
    # minutes:  | # | Action | Owner | Due Date | Status | → 5 data cols
    # discussion: | Follow-up | Owner | By When | Status | → 4 data cols
    ncols=${#local_cols[@]}

    if [[ $ncols -ge 5 ]]; then
      # minutes style: col1=# col2=action col3=owner col4=due col5=status
      action="${local_cols[1]}"
      owner="${local_cols[2]}"
      due="${local_cols[3]}"
    elif [[ $ncols -ge 4 ]]; then
      # meeting/discussion style: col1=action col2=owner col3=due col4=status
      action="${local_cols[0]}"
      owner="${local_cols[1]}"
      due="${local_cols[2]}"
    else
      continue
    fi

    [[ -z "$action" || "$action" == "..." ]] && continue

    # Apply owner filter
    if [[ -n "$FILTER_OWNER" ]]; then
      echo "$owner" | grep -qi "$FILTER_OWNER" || continue
    fi

    # Apply overdue filter
    if [[ "$FILTER_OVERDUE" == true ]]; then
      due_norm=$(normalize_date "$due")
      if [[ -z "$due_norm" || "$due_norm" > "$TODAY" || "$due_norm" == "$TODAY" ]]; then
        continue
      fi
    fi

    # Normalize owner
    [[ -z "$owner" || "$owner" == "—" ]] && owner="Unassigned"

    note_ref="[${title}](${f#notes/}) (${date})"
    line="  - [ ] ${action} — ${note_ref} — due: ${due:-TBD}"

    if [[ -v OWNER_ACTIONS[$owner] ]]; then
      OWNER_ACTIONS[$owner]+=$'\n'"$line"
    else
      OWNER_ACTIONS[$owner]="$line"
    fi

  done < <(grep -P '^\s*\|' "$f" 2>/dev/null || true)

done < <(find "$NOTES_DIR" -name "*.md" ! -name ".gitkeep" ! -name "INDEX.md" | sort)

# ---- output -----------------------------------------------------------------

if [[ ${#OWNER_ACTIONS[@]} -eq 0 ]]; then
  echo "No open action items found."
  exit 0
fi

echo "## Open Action Items"
echo ""

if [[ "$FILTER_OVERDUE" == true ]]; then
  echo "> Showing **overdue** items only (before ${TODAY})"
  echo ""
fi

if [[ -n "$FILTER_OWNER" ]]; then
  echo "> Filtered by owner: **${FILTER_OWNER}**"
  echo ""
fi

# Sort owners alphabetically
mapfile -t SORTED_OWNERS < <(printf '%s\n' "${!OWNER_ACTIONS[@]}" | sort)

for owner in "${SORTED_OWNERS[@]}"; do
  echo "### ${owner}"
  echo "${OWNER_ACTIONS[$owner]}"
  echo ""
done
