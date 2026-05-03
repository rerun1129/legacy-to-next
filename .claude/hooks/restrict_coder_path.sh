#!/usr/bin/env bash
# PreToolUse hook: Backend-coder / Frontend-coder 경로 범위 강제
#
# 동작 조건:
#   .claude/.coder_scope 마커 파일이 존재할 때만 동작.
#   값: "backend" → back-end/ 만 허용
#        "frontend" → front-end/ 만 허용
#
# 메인 에이전트는 각 Backend/Frontend-coder 호출 직전 마커를 생성하고 호출 직후 삭제한다:
#   echo "backend" > .claude/.coder_scope  (호출 전)
#   <Backend-coder 호출>
#   rm -f .claude/.coder_scope             (호출 후)

set -euo pipefail

REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || echo "")
SCOPE_FILE="${REPO_ROOT}/.claude/.coder_scope"

# 마커 없으면 통과 (메인 직접 작업 또는 Backend/Frontend-coder가 아닌 에이전트)
if [[ ! -f "$SCOPE_FILE" ]]; then
  exit 0
fi

SCOPE=$(cat "$SCOPE_FILE" | tr -d '[:space:]')

# stdin으로 훅 JSON 수신
INPUT=$(cat)

TOOL_NAME=$(echo "$INPUT" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('tool_name', ''))
" 2>/dev/null || echo "")

# Edit / Write 이외 통과
if [[ "$TOOL_NAME" != "Edit" && "$TOOL_NAME" != "Write" ]]; then
  exit 0
fi

FILE_PATH=$(echo "$INPUT" | python3 -c "
import sys, json
d = json.load(sys.stdin)
ti = d.get('tool_input', {})
print(ti.get('file_path', ''))
" 2>/dev/null || echo "")

if [[ -z "$FILE_PATH" ]]; then
  exit 0
fi

# 절대경로 → 리포 상대경로 변환
if [[ -n "$REPO_ROOT" ]]; then
  FILE_PATH="${FILE_PATH#${REPO_ROOT}/}"
  FILE_PATH="${FILE_PATH#${REPO_ROOT}\\}"
fi

# 경로 검사
case "$SCOPE" in
  backend)
    if [[ "$FILE_PATH" != back-end/* && "$FILE_PATH" != *\\back-end\\* && "$FILE_PATH" != *back-end/* ]]; then
      echo "【경로 차단】Backend-coder는 back-end/ 외부 편집 불가: $FILE_PATH" >&2
      exit 2
    fi
    ;;
  frontend)
    if [[ "$FILE_PATH" != front-end/* && "$FILE_PATH" != *\\front-end\\* && "$FILE_PATH" != *front-end/* ]]; then
      echo "【경로 차단】Frontend-coder는 front-end/ 외부 편집 불가: $FILE_PATH" >&2
      exit 2
    fi
    ;;
  *)
    echo "【경로 차단】알 수 없는 .coder_scope 값 '$SCOPE' — 안전하게 차단" >&2
    exit 2
    ;;
esac

exit 0
