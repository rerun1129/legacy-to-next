#!/bin/bash
# 메인 에이전트 Stop 훅: 외부 Reviewer를 headless로 호출해 변경분을 감사받음
#
# 2단계 리뷰:
#   1차) Sonnet — $CLAUDE_PROJECT_DIR/CLAUDE.md 기반 판정.
#                모호하면 ESCALATE + 핸드오프 패키지 작성
#   2차) Opus  — 핸드오프만 보고 최종 판정 (파일 재독 최소화)
#
# 재작업 사이클:
#   REJECTED → exit 2 피드백 → 메인 재개(Coder 재작업) → 재종료 → Reviewer 재실행
#   APPROVED → 마커 파일 생성 → exit 2 QA 지시 → 메인 재개(QA) → 재종료 → exit 0
#
# 종료 코드:
#   0  → 승인 후 QA 완료 또는 스킵 (최종 종료)
#   2  → 피드백/지시/오류 주입 후 메인 재개
set -uo pipefail

# claude CLI 존재 확인
command -v claude >/dev/null 2>&1 || { echo "[reviewer] claude CLI not found in PATH" >&2; exit 0; }

# ─── 설정 ──────────────────────────────────────────────────────────────
PRIMARY_MODEL="${PRIMARY_MODEL:-claude-sonnet-4-6}"
ESCALATION_MODEL="${ESCALATION_MODEL:-claude-opus-4-7}"
USED_MODEL="$PRIMARY_MODEL"

# reviewer_paths.md 에서 경로 순서대로 시도
PATHS_FILE="$CLAUDE_PROJECT_DIR/.claude/reviewer_paths.md"
REVIEWER_DIR=""
if [ -f "$PATHS_FILE" ]; then
  while IFS= read -r candidate; do
    candidate="${candidate/#\~/$HOME}"
    if [ -d "$candidate" ]; then
      REVIEWER_DIR="$candidate"
      break
    fi
  done < <(grep -E '^\s*-\s+\S' "$PATHS_FILE" | sed 's/^\s*-\s*//')
fi
LOG_DIR="$CLAUDE_PROJECT_DIR/.claude/review_log"
REVIEW_PENDING="$CLAUDE_PROJECT_DIR/.claude/.review_pending"
APPROVED_MARKER="$LOG_DIR/.reviewer_approved"
REJECT_COUNT_FILE="$LOG_DIR/.reject_count"

mkdir -p "$LOG_DIR"

# ─── stdin 파싱 ────────────────────────────────────────────────────────
HOOK_INPUT=$(cat)
STOP_HOOK_ACTIVE=$(echo "$HOOK_INPUT" | jq -r '.stop_hook_active // false')

# APPROVED_MARKER 존재 시 stop_hook_active 무관하게 청소 후 최종 종료
if [ -f "$APPROVED_MARKER" ]; then
  rm -f "$APPROVED_MARKER"
  exit 0
fi

# .review_pending 없으면 트렁크 커밋 자동 감지 후 마커 복구 시도
if [ ! -f "$REVIEW_PENDING" ]; then
  _AHEAD=0
  if git -C "$CLAUDE_PROJECT_DIR" rev-parse --git-dir >/dev/null 2>&1; then
    _AHEAD=$(git -C "$CLAUDE_PROJECT_DIR" rev-list --count "origin/master..HEAD" 2>/dev/null || echo 0)
  fi
  if [ "$_AHEAD" -gt 0 ]; then
    touch "$REVIEW_PENDING"
  else
    exit 0
  fi
fi
# .review_pending은 verdict 확정 후 소비 — 그 이전 실패는 마커를 유지해 재시도 가능

# ─── 변경분 수집 ───────────────────────────────────────────────────────
cd "$CLAUDE_PROJECT_DIR" || exit 0
git rev-parse --git-dir >/dev/null 2>&1 || exit 0

# 머지 베이스 기준으로 diff: origin/master → HEAD~1 순 폴백
BASE=$(git merge-base HEAD origin/master 2>/dev/null || \
       git rev-parse "HEAD~1" 2>/dev/null || \
       true)
if [ -z "$BASE" ]; then
  rm -f "$REVIEW_PENDING"
  echo "[reviewer] baseline 없음 (initial commit?) — 리뷰 스킵." >&2
  exit 0
fi

CHANGED=$(git diff --name-only "$BASE" HEAD 2>/dev/null)
CHANGED=$(echo "$CHANGED" | sort -u | grep -v '^$' || true)

if [ -z "$CHANGED" ]; then
  rm -f "$REVIEW_PENDING"
  echo "[reviewer] 변경 내용 없음 — git commit 누락 가능성을 확인하세요." >&2
  exit 2
fi

DIFF_STAT=$(git diff --stat "$BASE" HEAD 2>/dev/null)
COMMIT_LOG=$(git log --oneline "$BASE"..HEAD 2>/dev/null)
CHANGED_COUNT=$(echo "$CHANGED" | grep -c . || true)

if [ "$CHANGED_COUNT" -lt 1 ]; then
  rm -f "$REVIEW_PENDING"
  echo "[reviewer] 변경 파일 없음 — 리뷰 스킵." >&2
  exit 0
fi

# ─── 외부 Reviewer 디렉토리 확인 ──────────────────────────────────────
if [ -z "$REVIEWER_DIR" ]; then
  {
    echo "════════════════════════════════════════════════════════"
    echo "  [reviewer] 오류: 유효한 Reviewer 디렉토리를 찾지 못했습니다."
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo ".claude/reviewer_paths.md 에 나열된 경로가 모두 존재하지 않습니다."
    echo "경로 수정은 사용자가 직접 .claude/reviewer_paths.md 를 편집하세요."
  } >&2
  exit 2
fi
cd "$REVIEWER_DIR"

# ─── 리뷰 요청 구성 ────────────────────────────────────────────────────
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REQUEST_FILE="$LOG_DIR/${TIMESTAMP}_request.md"
PRIMARY_RESPONSE="$LOG_DIR/${TIMESTAMP}_primary.json"
HANDOFF_FILE="$LOG_DIR/${TIMESTAMP}_handoff.json"
ESCALATION_RESPONSE="$LOG_DIR/${TIMESTAMP}_escalation.json"

FILES_ABS=$(echo "$CHANGED" | while read -r f; do
  [ -f "$CLAUDE_PROJECT_DIR/$f" ] && echo "- $CLAUDE_PROJECT_DIR/$f"
done)

# 프로젝트 구조 힌트 (상위 2단계, 코드 파일만)
PROJECT_TREE=$(cd "$CLAUDE_PROJECT_DIR" && \
  git ls-files 2>/dev/null | grep -E '\.(ts|tsx|js|jsx|py|java|sql|vue)$' | \
  awk -F/ '{ if (NF >= 2) print $1"/"$2; else print $1 }' | \
  sort -u | head -40)

cat > "$REQUEST_FILE" <<EOF
# 리뷰 요청

## 프로젝트 루트
$CLAUDE_PROJECT_DIR

## 프로젝트 구조 (상위 2단계, 코드 파일만)
\`\`\`
$PROJECT_TREE
\`\`\`

## 커밋 이력 (base → HEAD)
\`\`\`
$COMMIT_LOG
\`\`\`

## 변경 요약 (git diff --stat)
\`\`\`
$DIFF_STAT
\`\`\`

## 변경된 파일 (절대경로)
$FILES_ABS

## 리뷰 지침
- 변경된 파일을 Read 도구로 직접 읽어 **현재 상태** 기준으로 판단하세요.
- 판단이 어려우면 관련 Port 인터페이스·인접 레이어 파일도 Read/Glob/Grep 으로 추가 탐색하세요.
- $CLAUDE_PROJECT_DIR/CLAUDE.md 의 규칙을 기준으로 리뷰하세요.
- 인프라·스크립트·마이그레이션·설정 파일 등 비도메인 파일은 ARCH 규칙 적용 대상 외입니다.
EOF

# ─── 1차: Sonnet ───────────────────────────────────────────────────────
RAW=$(claude -p \
  --model "$PRIMARY_MODEL" \
  --output-format json \
  --permission-mode acceptEdits \
  --add-dir "$CLAUDE_PROJECT_DIR" \
  --allowedTools "Read,Glob,Grep" \
  < "$REQUEST_FILE" 2>/dev/null)

if [ -z "$RAW" ]; then
  rm -f "$REVIEW_PENDING"
  {
    echo "[reviewer] 1차 호출 실패."
    echo "재시도하려면: touch .claude/.review_pending 후 메인 응답 종료."
  } >&2
  exit 2
fi
echo "$RAW" > "$PRIMARY_RESPONSE"

REVIEW_TEXT=$(echo "$RAW" | jq -r '.result // empty')
# 순수 JSON 먼저 시도, 실패 시 마크다운 코드블록 안쪽 추출
REVIEW_JSON=$(echo "$REVIEW_TEXT" | jq -c . 2>/dev/null)
if [ -z "$REVIEW_JSON" ]; then
  REVIEW_JSON=$(echo "$REVIEW_TEXT" | awk '/^```/{if(flag)exit;flag=1;next}flag' | jq -c . 2>/dev/null)
fi

if [ -z "$REVIEW_JSON" ]; then
  rm -f "$REVIEW_PENDING"
  echo "[reviewer] 1차 응답 파싱 실패. 원본: $PRIMARY_RESPONSE" >&2
  exit 2
fi

VERDICT=$(echo "$REVIEW_JSON" | jq -r '.verdict')
rm -f "$REVIEW_PENDING"  # verdict 확정 → 마커 소비

# ─── 2차: ESCALATE 시 Opus 핸드오프 ────────────────────────────────────
if [ "$VERDICT" = "ESCALATE" ]; then
  HANDOFF=$(echo "$REVIEW_JSON" | jq -c '.handoff // empty')

  if [ -z "$HANDOFF" ] || [ "$HANDOFF" = "null" ]; then
    echo "[reviewer] ESCALATE 인데 handoff 누락. 1차 결과를 그대로 사용." >&2
  else
    echo "$HANDOFF" | jq . > "$HANDOFF_FILE"
    HANDOFF_BYTES=$(echo "$HANDOFF" | wc -c)

    ESCALATE_PROMPT=$(cat <<ESCALATE_EOF
당신은 외부 코드 감사자의 **2차 리뷰어**입니다. 1차 리뷰어(Sonnet)가
판단을 보류한 사안에 대해 최종 판정을 내립니다.

1차 리뷰어가 핵심 증거를 핸드오프 패키지에 발췌해 넘겼습니다.
원칙적으로 핸드오프만으로 판단하세요. 추가 파일 읽기는
\`handoff.specific_files_for_opus_to_read\` 에 명시된 경우에만.

## 1차 리뷰어의 핸드오프
\`\`\`json
$(echo "$HANDOFF" | jq .)
\`\`\`

## 참고: 원래의 변경 정보 (재독은 권장하지 않음)
$(cat "$REQUEST_FILE")

## 판정 지시

- \`verdict\` 는 반드시 "APPROVED" 또는 "REJECTED" 중 하나. ESCALATE 사용 불가.
- handoff.competing_interpretations 중 어느 쪽이 맞는지 명확히 결정.
- 1차와 동일한 출력 JSON 스키마 (단, handoff 필드는 비워둠: "handoff": null).
- 첫 글자 \`{\` 마지막 글자 \`}\`. 마크다운 펜스 금지.
ESCALATE_EOF
)

    RAW2=$(echo "$ESCALATE_PROMPT" | claude -p \
      --model "$ESCALATION_MODEL" \
      --output-format json \
      --permission-mode acceptEdits \
      --add-dir "$CLAUDE_PROJECT_DIR" \
      --allowedTools "Read,Glob,Grep" \
      2>/dev/null)

    if [ -n "$RAW2" ]; then
      echo "$RAW2" > "$ESCALATION_RESPONSE"
      REVIEW_TEXT2=$(echo "$RAW2" | jq -r '.result // empty')
      REVIEW_JSON2=$(echo "$REVIEW_TEXT2" | jq -c . 2>/dev/null)
      if [ -z "$REVIEW_JSON2" ]; then
        REVIEW_JSON2=$(echo "$REVIEW_TEXT2" | awk '/^```/{if(flag)exit;flag=1;next}flag' | jq -c . 2>/dev/null)
      fi

      if [ -n "$REVIEW_JSON2" ]; then
        REVIEW_JSON="$REVIEW_JSON2"
        VERDICT=$(echo "$REVIEW_JSON" | jq -r '.verdict')
        USED_MODEL="$ESCALATION_MODEL"
        echo "[reviewer] escalated to $ESCALATION_MODEL (handoff: $HANDOFF_BYTES bytes) → $VERDICT" >&2
      else
        echo "[reviewer] 2차 응답 파싱 실패. 1차 결과로 진행." >&2
      fi
    else
      echo "[reviewer] 2차 호출 실패. 1차 결과로 진행." >&2
    fi
  fi
fi

# ─── summary 기록 및 임시 파일 정리 ────────────────────────────────────
_DATE="${TIMESTAMP:0:4}-${TIMESTAMP:4:2}-${TIMESTAMP:6:2}"
_TIME="${TIMESTAMP:9:2}:${TIMESTAMP:11:2}:${TIMESTAMP:13:2}"
_ISO="${_DATE}T${_TIME}"
DATE_KEY="${TIMESTAMP:0:8}"
DAILY_LOG="$LOG_DIR/${DATE_KEY}.json"
_DURATION=$(echo "$RAW" | jq '.duration_ms // 0' 2>/dev/null || echo 0)

_ENTRY=$(echo "$REVIEW_JSON" | jq \
  --arg ts "$_ISO" \
  --arg model "$USED_MODEL" \
  --argjson dur "$_DURATION" \
  '{
    timestamp: $ts,
    verdict: .verdict,
    severity: (.severity // null),
    model: $model,
    duration_ms: $dur,
    summary: (.summary // null),
    violations: (.violations // []),
    suggestions: (.suggestions // [])
  }')

echo "$_ENTRY" >> "$DAILY_LOG"

rm -f "$REQUEST_FILE" "$PRIMARY_RESPONSE" "$HANDOFF_FILE" "$ESCALATION_RESPONSE"

# ─── 결과 처리 ─────────────────────────────────────────────────────────
if [ "$VERDICT" = "APPROVED" ] || [ "$VERDICT" = "ESCALATE" ]; then
  rm -f "$REJECT_COUNT_FILE"
  touch "$APPROVED_MARKER"
  {
    echo "════════════════════════════════════════════════════════"
    echo "  외부 코드 감사 결과: APPROVED"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "Reviewer 검토가 통과되었습니다. QA 단계를 진행하세요:"
    echo ""
    echo "  1. 아래 명령으로 변경 파일 목록을 확인 후 QA에 전달하세요:"
    echo "     git diff --name-only $BASE HEAD"
    echo "  2. QA 서브 에이전트를 호출하여 빌드·테스트를 최종 검증하세요."
    echo ""
    echo "$REVIEW_JSON" | jq -r 'if .summary then "## Reviewer 종합\n\(.summary)" else "" end'
    echo ""
    echo "(전체 응답 로그: $LOG_DIR/${TIMESTAMP}_*.json)"
  } >&2
  exit 2
fi

# REJECTED 카운터 증가
_RCOUNT=$(( $(cat "$REJECT_COUNT_FILE" 2>/dev/null || echo 0) + 1 ))
echo "$_RCOUNT" > "$REJECT_COUNT_FILE"

# REJECTED → stderr로 피드백 + exit 2 (Coder 재작업 후 Reviewer 재실행)
{
  echo "════════════════════════════════════════════════════════"
  echo "  외부 코드 감사 결과: REJECTED (${_RCOUNT}회차)"
  echo "════════════════════════════════════════════════════════"
  echo ""
  echo "리뷰어가 다음 문제를 지적했습니다. Coder 재작업 후 자동으로 재검토됩니다."
  echo ""
  echo "$REVIEW_JSON" | jq -r '
    if (.violations | length) > 0 then
      "## 규칙 위반\n" + (.violations | map(
        "- [\(.rule)] \(.file):\(.line // "?") — \(.detail)"
      ) | join("\n"))
    else "" end,
    "",
    if (.suggestions | length) > 0 then
      "## 구조 개선 제안\n" + (.suggestions | map(
        "- \(.file): \(.issue)\n  → \(.proposal)"
      ) | join("\n"))
    else "" end,
    "",
    if .summary then "## 종합\n\(.summary)" else "" end
  '
  echo ""
  echo "(전체 응답 로그: $LOG_DIR/${TIMESTAMP}_*.json)"
} >&2

if [ "$_RCOUNT" -ge 2 ]; then
  {
    echo ""
    echo "[ESCALATE_TO_USER] 동일 변경분에 대해 ${_RCOUNT}회 연속 REJECTED됐습니다."
    echo "Coder 재작업 대신 사용자에게 직접 위반 내용을 보고하고 검토를 요청하세요."
    rm -f "$REJECT_COUNT_FILE"
  } >&2
fi

exit 2
