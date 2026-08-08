#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/../.."

MARKER_FILE=".claude/.notion-doc-sync-marker"
NOTION_URL="https://app.notion.com/p/3b680dc142a981d3ab9fdb0046a48666"

hash_api_files() {
  find src/main/java -type f -name '*.java' \( -path '*/controller/*' -o -path '*/dto/*' \) 2>/dev/null \
    | sort | xargs cat 2>/dev/null | shasum -a 256 | awk '{print $1}'
}

if [ "${1:-}" == "--mark-synced" ]; then
  hash_api_files > "$MARKER_FILE"
  echo "마커 갱신 완료"
  exit 0
fi

CURRENT_HASH="$(hash_api_files)"

if [ ! -f "$MARKER_FILE" ]; then
  echo "$CURRENT_HASH" > "$MARKER_FILE"
  echo '{}'
  exit 0
fi

STORED_HASH="$(cat "$MARKER_FILE")"

if [ "$CURRENT_HASH" != "$STORED_HASH" ]; then
  jq -n --arg reason "API 컨트롤러/DTO 파일이 변경되었습니다. Notion API 명세 문서($NOTION_URL)를 갱신한 뒤 'bash .claude/hooks/check-notion-doc-sync.sh --mark-synced' 를 실행하고 나서 마무리하세요." \
    '{decision: "block", reason: $reason}'
else
  echo '{}'
fi
