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

0. **Coder 호출 전 main HEAD 확인** (worktree base 기준점 보장):
   ```bash
   git status --short && git log -1 --oneline
   ```
   uncommitted 변경이 있거나 예상과 다른 HEAD면 사용자에게 보고 후 중단. 이 시점의 HEAD가 worktree base가 된다.

1. **Coder × N 호출**: 작업 단위가 단일이면 Coder 1개, **변경 파일 집합이 겹치지 않는 경우에만** `subagent_type=Coder`, `isolation: "worktree"`로 병렬 호출. domain 모델·entry 컴포넌트 등 공유 파일이 여러 Coder에 걸치면 단일 Coder 직렬 처리. **Planner 호출하지 않음**. 작업 지시(`$ARGUMENTS`) + 관련 파일 경로를 직접 전달.

2. **트렁크 머지 (사전 충돌 감지 적용)**:

   2-1. **시뮬레이션**: 각 worktree 브랜치에 대해 `git merge-tree --write-tree --name-only --messages <TRUNK_HEAD> <branch>` 실행. exit 0 = 클린, 1 = 충돌, ≥2 = 오류(사용자 보고 후 중단). 결과로 CLEAN/CONFLICT 목록 분류.

   2-2. **클린 일괄 머지**: `git merge --ff-only <branch>` × CLEAN_N 회 (ff 불가 시 `git merge <branch>` 폴백).

   2-3. **충돌 브랜치 1개씩 처리**: CONFLICT 목록을 순회하며 `git merge --no-ff --no-commit <branch>` → 워킹트리에 충돌 마커 생성. `git diff --name-only --diff-filter=U`로 unmerged 파일 확인. unmerged가 비어있으면 즉시 commit하고 다음 브랜치. 그렇지 않으면 `subagent_type=Mediator` 호출 (충돌 파일 목록 + 머지 중인 브랜치의 Coder 의도 + 잔여 CONFLICT 브랜치 의도 요약 전달). Mediator 종료 후 `git diff --name-only --diff-filter=U`가 비어 있는지 재확인 → `git add <resolved-files>` + `git commit -m "merge: resolve conflicts from parallel Coders (<branch>)"` → 다음 충돌 브랜치 반복.

   2-4. **비정상 시**: Mediator 1회 재호출 후에도 unmerged 잔존 / 시뮬레이션 rc≥2 / 머지 실패 → `git merge --abort`로 복구 + 사용자 보고 후 중단.

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

> Reviewer 미호출이므로 `.claude/deferred_review.json`은 이 사이클에서 변하지 않는다.

## 종료 메시지

- QA PASS: "Coder 구현 + QA 검증 완료. Reviewer 미발동."
- QA FAIL 2회 연속: 실패 항목 명시 + "사용자 개입이 필요합니다."
