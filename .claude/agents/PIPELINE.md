# Agent Pipeline

메인 에이전트는 오케스트레이션 전담. 각 서브 에이전트의 입출력을 전달하고 사용자 승인 게이트를 관리한다. 직접 코드 작성/검수/실행하지 않는다.

> **Reviewer 주의**: Reviewer는 `.claude/agents/` 서브 에이전트가 아니라 Stop 훅(`invoke_reviewer.sh`)이 별도 프로젝트 디렉토리에서 호출하는 **외부 메인 에이전트**다. Agent 도구로 직접 호출하지 않는다.

## 흐름

```
사용자 요청
  → [메인] 요청 정리·컨텍스트 전달
  → [Planner]                    기획 + 작업 단위 N개 분할
  → [메인] 사용자 승인 요청
  → (승인) [Coder × N]           병렬 구현 (각자 worktree 격리)
  → [메인] 모든 Coder 완료 → 메인 트렁크로 git merge 시도
      · 충돌 없음 → 다음 단계
      · 충돌 있음 → [Mediator] 충돌 파일 해결 → [메인] git add/commit
  → [메인] touch .claude/.review_pending        ← 리뷰 의뢰 신호
  → [Stop 훅] → .review_pending 확인 후 Reviewer 실행 (Sonnet 1차·ESCALATE 시 Opus 2차)
      · REJECTED (blocking ≥ 1) → exit 2로 메인 재개
          · [ESCALATE_TO_USER] 태그 없음 (blocking 1회차) → 메인이 [Coder × N] 재호출 (`isolation: "worktree"`, base: 현재 트렁크 HEAD)
                (deferrable 위반은 stderr에 동봉 — 같은 사이클에 함께 수정 권장)
                → 재작업 완료 → 메인이 worktree 머지 시도
                    · 충돌 없음 → git commit
                    · 충돌 있음 → [Mediator] 호출 → git add/commit
                → [메인] touch .claude/.review_pending
                → 메인 재종료 → Stop 훅 재실행 → Reviewer 재실행 (base→HEAD 누적 검토 — 의도적)
          · [ESCALATE_TO_USER] 태그 있음 (blocking 2회 이상) → 메인이 사용자에게 위반 내용 직접 보고 + 작업 중단 대기
      · REJECTED (blocking = 0, deferrable만) → APPROVED 등가 처리:
          · .claude/deferred_review.json에 cycle entry 추가 (mktemp+mv 원자 갱신)
          · .reject_count 리셋 (gate 통과로 간주)
          · 누적 사이클 < REVIEWER_DEFERRED_THRESHOLD → APPROVED_MARKER 생성 + QA 진행
          · 누적 사이클 ≥ 임계 → APPROVED_MARKER 생성 + [ESCALATE_TO_USER:DEFERRED_BATCH] 주입
                → 메인이 QA 완료 후 사용자에게 일괄 처리 위임 (아래 "Deferrable 위반 누적 처리" 참조)
      · APPROVED → 마커 파일 생성 → exit 2로 메인 재개 → 메인이 [QA] 호출
  → [QA]                         통합 빌드·테스트 (단일)
      · PASS → 다음 단계
      · FAIL → [Coder × N] 재호출 (QA 실패 항목 전달) → merge → commit
             → touch .claude/.review_pending → Reviewer 재실행
  → [메인] worktree 정리 (QA 통과 후)
      → git worktree list로 남은 worktree 확인
      → git worktree remove -f -f <path> × N회
      → git branch -D <worktree-branch> × N회
  → [메인] 사용자 결과 보고 후 종료
      → Stop 훅 재실행 → APPROVED_MARKER 파일 존재 확인 → 마커 삭제 후 exit 0
```

## 병렬 Coder 격리

- 메인은 Agent 도구의 `isolation: "worktree"` 옵션으로 각 Coder를 호출. Claude Code가 임시 git worktree를 자동 생성.
- 각 Coder는 자기 worktree에서 자유롭게 파일 수정. 다른 Coder와의 파일 중복은 머지 단계에서 git이 처리.
- 머지 충돌 시 Mediator가 호출되어 내용 해결, git 마무리는 메인이 담당.
- 조용한 덮어쓰기 위험 없음 (모든 충돌은 명시적 git conflict로 노출).

## 핸드오프

| 구간 | 트리거 | 전달 내용 |
|------|--------|-----------|
| Main → Planner | Agent 도구 호출 | 사용자 요청 + 관련 파일 경로 |
| Planner → 사용자 | 메인이 기획서 출력 | 승인 게이트 (작업 단위 N개 포함) |
| Main → Coder × N | Agent 도구 병렬 호출 (`isolation: "worktree"`, 사용자 승인 후) | 단위별 기획 |
| Coder × N → Main | 완료 반환 | 변경 파일 목록 + worktree 브랜치명 |
| Main → Mediator (조건부) | git merge 충돌 발생 시 | 충돌 파일 목록 + 각 Coder 작업 의도 요약 |
| Mediator → Main | 해결 완료 | 해결된 파일 목록 + 해결 방식 요약 |
| Main → Reviewer | git commit 후 `touch .claude/.review_pending` + Stop 훅 발동 | base→HEAD diff + 변경 파일 목록 |
| Stop 훅 → Main (APPROVED) | exit 2 + stderr 지시문 주입 | Reviewer 종합 의견 |
| Stop 훅 → Main (REJECTED 1회차) | exit 2 + stderr 피드백 주입 | 규칙 위반 목록 + 개선 제안 |
| Stop 훅 → Main (REJECTED 2회 이상) | exit 2 + [ESCALATE_TO_USER] stderr 주입 | 위반 목록 + 사용자 직접 개입 요청 |
| Stop 훅 → Main (REJECTED deferrable-only) | exit 2 + [APPROVED] + deferrable 누적 내용 stderr 주입 | 누적 위반 목록 + 사이클 카운트 |
| Stop 훅 → Main ([ESCALATE_TO_USER:DEFERRED_BATCH]) | exit 2 + QA 지시 + 임계 도달 알림 | 누적된 전체 deferrable 요약 |
| Main → Coder (재작업) | REJECTED 피드백 수신 후 Main이 Agent 도구 호출 | 위반 항목 목록 + 수정 대상 파일 |
| Main → QA | APPROVED 지시문 수신 후 Main이 Agent 도구 호출 | 변경 파일 목록 + Reviewer 종합 의견 |
| QA → 사용자 | 메인이 결과 보고 후 종료 | 통과/미통과 명시 |
| QA → Main (FAIL 시) | 미통과 보고 | Coder 재작업 항목 + 실패 명령어 출력 |
| Main → Coder (재작업 — QA FAIL) | Agent 도구 호출 | 실패 항목 목록 + 수정 대상 파일 |
| Main → Reviewer (QA 재작업 후) | git commit + touch .review_pending | base→HEAD diff |

## 메인 에이전트 참조 명령어

메인이 직접 실행해야 하는 명령어 목록. 자동화 흐름에서 누락 시 파이프라인이 멈춤.

| 시점 | 명령어 |
|------|--------|
| 모든 Coder worktree 머지 (N개 순차 반복, 충돌 없음) | `git merge <branch>` × N회 → `touch .claude/.review_pending` (fast-forward, 별도 commit 불필요) |
| 순차 머지 중 충돌 발생 시 | `git merge --abort` → Mediator 호출 → 해결 후 `git add` + `git commit` → 나머지 브랜치 계속 순차 머지 → `touch .claude/.review_pending` |
| Mediator 충돌 해결 후 | `git add <files>` → `git commit -m "merge: resolve conflicts from parallel Coders"` → `touch .claude/.review_pending` |
| Reviewer REJECTED 후 Coder 재작업 완료 시 | `git add <files>` → `git commit -m "fix: rework per reviewer feedback"` → `touch .claude/.review_pending` |
| Reviewer APPROVED 후 QA 호출 전 | `git diff --name-only <BASE> HEAD` → 결과를 QA에 변경 파일 목록으로 전달 |
| QA FAIL 후 Coder 재작업 완료 시 | `git add <files>` → `git commit -m "fix: rework per QA failure"` → `touch .claude/.review_pending` |
| QA 통과 후 worktree 정리 | `git worktree list` 확인 → `git worktree remove -f -f <path>` × N회 → `git branch -D <worktree-branch>` × N회 |
| [ESCALATE_TO_USER:DEFERRED_BATCH] 수신 + QA 완료 후 | `.claude/deferred_review.json` Read → 사용자에게 누적 목록 출력 → 옵션 제시: 1) `/pipeline-deferred-flush` 2) 계속 누적(`.claude/.deferred_snooze`에 +5 기록) 3) 취소 |
| /pipeline-coder-qa 진입 | `touch .claude/.review_skip` |
| /pipeline-coder-qa 종료 (모든 경로) | `rm -f .claude/.review_skip` |
| /pipeline-coder-qa QA FAIL 1회차 후 | Coder 재호출(`isolation: "worktree"`) → merge → commit → QA 재호출 (`touch .review_pending` 절대 금지) |

> **누적 모드 예외 (`/pipeline-start`)**: 위 표의 모든 `touch .claude/.review_pending`은 생략한다. 진입 시 `.claude/.review_skip` sentinel을 유지하고, `/pipeline-review` 또는 `/pipeline` 호출 시 sentinel 제거 + 마커 생성으로 정상 사이클에 진입한다.

## 공통 규칙

- **300줄 초과: 분리 검토 / 500줄 초과: 강제 분리** — 두 경우 모두 사용자 보고.
- 외부 Reviewer는 `$CLAUDE_PROJECT_DIR/CLAUDE.md` 규칙 기준으로 판정 (300/500줄 규칙 포함).
- 본인 tools 범위 밖 작업 금지.
- 추측 금지. 명령어 출력 또는 코드 인용으로 근거 제시.

## 환경 설정

- Stop 훅: `.claude/settings.json` 등록 완료. `.review_pending` 파일이 있을 때만 Reviewer 실행 — 없으면 중간 Stop으로 간주하고 즉시 통과.
- 외부 Reviewer 경로: `.claude/reviewer_paths.md` 에 경로 목록 관리. 위에서 아래로 시도하여 첫 번째 존재하는 디렉토리 사용. 모두 없으면 리뷰 중단 후 사용자에게 알림. **경로 수정은 사용자가 직접** `.claude/reviewer_paths.md` 를 편집 (메인이 임의 수정 금지).
- diff baseline: `origin/master` → `HEAD~1` 순으로 폴백되는 merge-base. 단일 메인 에이전트 워크플로우를 가정하므로 사이클 도중 baseline 변동 없음. 재작업 사이클에서는 1차+2차 commit 누적 diff를 검토 — 의도적 설계.
- ESCALATE fallback: 2차 Opus 호출·파싱 실패 시 1차 ESCALATE를 APPROVED로 묵시 처리. 리뷰 차단보다 통과 우선의 안전 정책.
- Reviewer 모델: 1차 `claude-sonnet-4-6`, 2차(ESCALATE) `claude-opus-4-7`. 환경변수 `PRIMARY_MODEL` / `ESCALATION_MODEL` 로 오버라이드 가능.

## 누적 모드 (`/pipeline-build` + `/pipeline-start`)

- `/pipeline-build`: 작업 지시를 `.claude/.task_queue`에 적재만 함. commit·sentinel·마커 모두 무관. 진입 시 `.claude/.review_skip` sentinel을 유지해 의도치 않은 Reviewer 발동을 차단.
- `/pipeline-start`: 큐를 일괄 소비해 Planner→Coder×N→merge→commit을 실행. 메인 `touch .claude/.review_pending`(#7)은 생략하고 `.review_skip` sentinel을 유지. 이후 `/pipeline-review` 호출 시 sentinel 제거 + 마커 생성으로 #8~#13 정상 사이클에 진입한다. 반복 호출 사이의 base→HEAD diff는 자연스럽게 합산되어 단일 Reviewer 호출에 전달된다. worktree 정리는 매 사이클의 머지/commit 직후에 즉시 수행한다 (QA 게이트 없음). `.claude/.task_queue` 파일은 사이클 완료 시 삭제한다.

## Coder→QA 독립 흐름 (`/pipeline-coder-qa`)

- 작은 수정·빠른 검증을 위한 단축 사이클. **Planner와 Reviewer 모두 건너뜀**.
- 진입 시 `.claude/.review_skip` sentinel 생성으로 Stop 훅이 Reviewer를 호출하지 않게 차단.
- 흐름: Coder × N (worktree) → merge → commit → QA → worktree 정리 → sentinel 정리 → 종료.
- QA FAIL: 동일 변경분에 대해 최대 2회까지 Coder 재작업·QA 재실행. 2회 연속 FAIL 시 사용자 개입 대기.
- **모든 종료 경로에서 `.review_skip` 정리** — 다음 일반 작업(`/pipeline` 등)에서 Reviewer가 정상 발동하도록.
- `touch .claude/.review_pending` 절대 금지.
- `.claude/deferred_review.json` 변화 없음 (Reviewer 미호출).

## Deferrable 위반 누적 처리

### 정의
외부 Reviewer가 violations 항목에 `blocking: false`로 분류한 위반. 기본적으로 `rules/conventions.md`(CONV1~CONV7)만 해당. 데이터 손실·보안 위험이 없는 순수 코드 컨벤션 위반.

blocking=true 기본 대상: `architecture.md`(ARCH1~ARCH6), `correctness.md`(C1~C8), `ai_specific.md`(A1~A12).

### 누적 저장소
- 위치: `.claude/deferred_review.json` (**git 추적 대상** — PC 간 동기화)
- 단위: Reviewer 호출 1회 = `cycles[]` 항목 1개 (위반 개수 무관)
- 임계: `REVIEWER_DEFERRED_THRESHOLD` 환경변수 (기본값: 5 사이클)

### Snooze
임계 도달 시 사용자가 "계속 누적"을 선택하면 `.claude/.deferred_snooze`에 오프셋(+5)을 기록. 다음 flush 시 자동 삭제.

### 메인 처리 절차 (`[ESCALATE_TO_USER:DEFERRED_BATCH]` 수신 시)
QA 완료 후(성공/실패 무관):
1. `.claude/deferred_review.json` Read → 사이클별 위반을 markdown으로 정리해 사용자에게 출력
2. 3가지 옵션 제시:
   - "지금 일괄 처리" → `/pipeline-deferred-flush` 호출
   - "계속 누적" → `.claude/.deferred_snooze`에 현재 오프셋 +5 기록
   - "취소" → 그대로 종료
3. 사용자 응답대로 실행

### 카운터 정책
- `.reject_count`는 blocking REJECTED만 카운트
- deferrable-only REJECTED는 카운터 리셋 (gate 통과로 간주)
- 결과: `blocking → deferrable-only → blocking` 시퀀스는 [ESCALATE_TO_USER] 미발동 (의도적)

### 누적 모드와의 관계
`/pipeline-start`는 Reviewer를 발동시키지 않으므로 deferrable 누적도 발생하지 않는다. 누적은 `/pipeline-review` 또는 `/pipeline` 사이클에서만 일어난다.
