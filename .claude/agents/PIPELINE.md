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
  → [메인] 모든 Coder 완료 → 사전 충돌 감지 + 머지
      · [Phase 1] git merge-tree --write-tree <trunk> <branch> × N (read-only 시뮬레이션)
      · [Phase 2] 충돌 없는 브랜치 → git merge (--ff-only 우선) 일괄 적용
      · [Phase 3] 충돌 있는 브랜치 → 1개씩 git merge --no-ff --no-commit
                  → [Mediator] 워킹트리 충돌 마커 해결
                  → [메인] git add + git commit → 다음 충돌 브랜치 반복
      · 시뮬레이션 자체 실패(rc≥2) → 사용자 보고 후 중단
  → [메인] touch .claude/.review_pending        ← 리뷰 의뢰 신호
  → [Stop 훅] → .review_pending 확인 후 Reviewer 실행 (Sonnet 1차·ESCALATE 시 Opus 2차)
      · REJECTED (blocking ≥ 1) → exit 2로 메인 재개
          · [ESCALATE_TO_USER] 태그 없음 (blocking 1회차) → 메인이 [Coder × N] 재호출 (`isolation: "worktree"`, base: 현재 트렁크 HEAD)
                (deferrable 위반은 stderr에 동봉 — 같은 사이클에 함께 수정 권장)
                → 재작업 완료 → 메인이 사전 감지 + 머지 (정상 흐름과 동일 절차)
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
      · FAIL → [Coder × N] 재호출 (QA 실패 항목 전달) → 사전 감지 + 머지 → commit
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
| Main → Mediator (조건부) | 사전 시뮬레이션으로 충돌 분류 후, --no-commit 머지로 워킹트리 충돌 마커 생성 시 | 워킹트리 충돌 파일 목록 + 머지 중인 브랜치의 Coder 의도 + 잔여 충돌 브랜치 의도 요약 |
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
| 사전 충돌 감지 (N개 브랜치 각각) | `git merge-tree --write-tree --name-only --messages <trunk-tip> <branch>` (rc=0 클린 / rc=1 충돌 / rc≥2 오류) — 워킹트리·인덱스 무영향 |
| 충돌 없는 브랜치 일괄 머지 | `git merge --ff-only <branch>` × CLEAN_N 회 (ff 불가 시 `git merge <branch>` 폴백) |
| 충돌 있는 브랜치 머지 (1개씩) | `git merge --no-ff --no-commit <branch>` → 워킹트리에 충돌 마커 생성 → Mediator 호출 |
| Mediator 충돌 해결 후 | `git diff --name-only --diff-filter=U`가 비어 있는지 확인 → 비어 있으면 `git add <resolved-files>` → `git commit -m "merge: resolve conflicts from parallel Coders (<branch>)"` → 잔여 충돌 브랜치 있으면 다음 브랜치로 반복 |
| 모든 머지 완료 후 | `touch .claude/.review_pending` (누적 모드 예외 적용) |
| 시뮬레이션/머지 단계 비정상 종료 | `git merge --abort`로 워킹트리 복구 → 사용자에게 상황 보고 후 중단 (자동 재시도 금지) |
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

큐 적재 + Reviewer 미발동 사이클. 상세 절차는 `commands/pipeline-build.md`, `commands/pipeline-start.md` 참조.

## Coder→QA 독립 흐름 (`/pipeline-coder-qa`)

Planner·Reviewer 미발동 단축 사이클 (작은 수정·빠른 검증용). 상세 절차는 `commands/pipeline-coder-qa.md` 참조.

## Deferrable 위반 누적 처리

Reviewer가 `blocking: false`로 분류한 위반의 누적·일괄 처리 흐름. 일반 사이클에 영향 없음. 정의·저장소·Snooze·메인 처리 절차·카운터 정책은 `.claude/agents/PIPELINE_DEFERRED.md` 참조.
