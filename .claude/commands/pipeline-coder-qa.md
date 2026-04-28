---
description: Planner와 Reviewer 없이 Coder→QA만 실행. 작은 수정·빠른 검증용.
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

사용자 작업 지시(`$ARGUMENTS`)를 받아 Coder 구현 + QA 빌드/테스트 검증만 수행한다. Reviewer 미발동.

## 진입 처리

1. **인자 확인**: `$ARGUMENTS`가 비면 다음을 출력 후 종료:
   "작업 지시가 비어 있습니다. `/pipeline-coder-qa <작업 내용>` 형식으로 호출하세요."

2. **Reviewer 차단 sentinel 생성**:
   ```bash
   touch .claude/.review_skip
   ```

## 수행 절차

1. **Coder × N 호출**: 작업 단위가 단일이면 Coder 1개, 명확히 독립 분할 가능하면 `subagent_type=Coder`, `isolation: "worktree"`로 병렬 호출. **Planner 호출하지 않음**. 작업 지시(`$ARGUMENTS`) + 관련 파일 경로를 직접 전달.

2. **트렁크 머지**: 각 worktree 브랜치를 순차 `git merge <branch>` × N 회.
   - 충돌 없음 → 계속
   - 충돌 발생 → `git merge --abort` → `subagent_type=Mediator` 호출 → 해결 후 `git add <resolved-files>` + `git commit -m "merge: resolve conflicts from parallel Coders"` → 나머지 브랜치 계속

3. **변경 파일 수집**:
   ```bash
   git diff --name-only origin/master..HEAD
   ```

4. **QA 호출**: `subagent_type=QA`로 호출. 변경 파일 목록 전달.

5. **QA 결과 분기**:
   - **PASS** → 6단계로
   - **FAIL (1회차)**: Coder 재호출(`isolation: "worktree"`) → 실패 항목 + 수정 대상 전달 → 머지 → commit → QA 재실행
   - **FAIL (2회차)**: 메인이 사용자에게 실패 항목 보고 + 자동 재시도 중단. 7단계(sentinel 정리)는 반드시 수행.

6. **worktree 정리**:
   ```bash
   git worktree list
   git worktree remove -f -f <path> × N
   git branch -D <worktree-branch> × N
   ```

7. **sentinel 정리** (성공·실패·중단 모든 종료 경로):
   ```bash
   rm -f .claude/.review_skip
   ```

## 절대 수행 금지

- `touch .claude/.review_pending` — Reviewer 미발동이 이 커맨드의 핵심. 마커 생성 금지.
- `subagent_type=Planner` 호출 — 기획이 필요하면 `/pipeline` 사용.

## 종료 메시지

- QA PASS: "Coder 구현 + QA 검증 완료. Reviewer 미발동."
- QA FAIL 2회 연속: 실패 항목 명시 + "사용자 개입이 필요합니다."
