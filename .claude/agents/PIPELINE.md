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
      · REJECTED → exit 2로 메인 재개
          · [ESCALATE_TO_USER] 태그 없음 (1회차) → 메인이 [Coder × N] 재호출 (`isolation: "worktree"`, base: 현재 트렁크 HEAD)
                → 재작업 완료 → 메인이 worktree 머지 시도
                    · 충돌 없음 → git commit
                    · 충돌 있음 → [Mediator] 호출 → git add/commit
                → [메인] touch .claude/.review_pending
                → 메인 재종료 → Stop 훅 재실행 → Reviewer 재실행 (base→HEAD 누적 검토 — 의도적)
          · [ESCALATE_TO_USER] 태그 있음 (2회 이상) → 메인이 사용자에게 위반 내용 직접 보고 + 작업 중단 대기
      · APPROVED → 마커 파일 생성 → exit 2로 메인 재개 → 메인이 [QA] 호출
  → [QA]                         통합 빌드·테스트 (단일)
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
| Main → Coder (재작업) | REJECTED 피드백 수신 후 Main이 Agent 도구 호출 | 위반 항목 목록 + 수정 대상 파일 |
| Main → QA | APPROVED 지시문 수신 후 Main이 Agent 도구 호출 | 변경 파일 목록 + Reviewer 종합 의견 |
| QA → 사용자 | 메인이 결과 보고 후 종료 | 통과/미통과 명시 |

## 메인 에이전트 참조 명령어

메인이 직접 실행해야 하는 명령어 목록. 자동화 흐름에서 누락 시 파이프라인이 멈춤.

| 시점 | 명령어 |
|------|--------|
| 모든 Coder worktree 머지 (N개 순차 반복, 충돌 없음) | `git merge <branch>` × N회 → `touch .claude/.review_pending` (fast-forward, 별도 commit 불필요) |
| 순차 머지 중 충돌 발생 시 | `git merge --abort` → Mediator 호출 → 해결 후 `git add` + `git commit` → 나머지 브랜치 계속 순차 머지 → `touch .claude/.review_pending` |
| Mediator 충돌 해결 후 | `git add <files>` → `git commit -m "merge: resolve conflicts from parallel Coders"` → `touch .claude/.review_pending` |
| Reviewer REJECTED 후 Coder 재작업 완료 시 | `git add <files>` → `git commit -m "fix: rework per reviewer feedback"` → `touch .claude/.review_pending` |
| Reviewer APPROVED 후 QA 호출 전 | `git diff --name-only <BASE> HEAD` → 결과를 QA에 변경 파일 목록으로 전달 |
| QA 통과 후 worktree 정리 | `git worktree list` 확인 → `git worktree remove -f -f <path>` × N회 → `git branch -D <worktree-branch>` × N회 |

> **누적 모드 예외 (`/pipeline-build`)**: 위 표의 모든 `touch .claude/.review_pending`은 생략한다. 진입 시 `.claude/.review_skip` sentinel을 유지하고, `/pipeline-review` 또는 `/pipeline` 호출 시 sentinel 제거 + 마커 생성으로 정상 사이클에 진입한다.

## 공통 규칙

- **300줄 초과: 분리 검토 / 500줄 초과: 강제 분리** — 두 경우 모두 사용자 보고.
- 외부 Reviewer는 `$CLAUDE_PROJECT_DIR/CLAUDE.md` 규칙 기준으로 판정 (300/500줄 규칙 포함).
- 본인 tools 범위 밖 작업 금지.
- 추측 금지. 명령어 출력 또는 코드 인용으로 근거 제시.

## 환경 설정

- Stop 훅: `.claude/settings.json` 등록 완료. `.review_pending` 파일이 있을 때만 Reviewer 실행 — 없으면 중간 Stop으로 간주하고 즉시 통과.
- 외부 Reviewer 경로: `.claude/reviewer_paths.md` 에 경로 목록 관리. 위에서 아래로 시도하여 첫 번째 존재하는 디렉토리 사용. 모두 없으면 리뷰 중단 후 사용자에게 알림. **경로 수정은 사용자가 직접** `.claude/reviewer_paths.md` 를 편집 (메인이 임의 수정 금지).
- diff baseline: `origin/main` → `main` → `HEAD~1` 순으로 폴백되는 merge-base. 단일 메인 에이전트 워크플로우를 가정하므로 사이클 도중 baseline 변동 없음. 재작업 사이클에서는 1차+2차 commit 누적 diff를 검토 — 의도적 설계.
- ESCALATE fallback: 2차 Opus 호출·파싱 실패 시 1차 ESCALATE를 APPROVED로 묵시 처리. 리뷰 차단보다 통과 우선의 안전 정책.
- Reviewer 모델: 1차 `claude-sonnet-4-6`, 2차(ESCALATE) `claude-opus-4-7`. 환경변수 `PRIMARY_MODEL` / `ESCALATION_MODEL` 로 오버라이드 가능.

## 누적 모드 (`/pipeline-build`)

메인 #7(`touch .claude/.review_pending`)을 건너뛰고, 진입 시 `.claude/.review_skip` sentinel을 유지한다. Stop 훅이 sentinel을 보고 즉시 exit 0 처리해 Reviewer 발동을 차단. 이후 `/pipeline-review` 호출 시 sentinel 제거 + 마커 생성으로 #8~#13 정상 사이클에 진입한다. 누적 호출 사이의 base→HEAD diff는 자연스럽게 합산되어 단일 Reviewer 호출에 전달된다.
