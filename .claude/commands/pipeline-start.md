---
description: .task_queue에 적재된 작업 지시를 Planner→Coder×N→merge→commit으로 실행한다. Reviewer 미발동.
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

누적된 큐를 실행하는 파이프라인. Reviewer를 발동시키지 않고 Planner→Coder×N→merge→commit까지만 수행한다.

## 진입 처리

### 1. 큐 확인

`.claude/.task_queue` 파일이 없거나 비어 있으면 즉시 종료한다:

"큐가 비어 있습니다. `/pipeline-build <작업지시>`로 작업을 추가한 뒤 다시 호출하세요."

파일이 존재하고 내용이 있으면 큐 전체를 출력하고 계속 진행한다.

### 2. 리뷰 스킵 sentinel 유지

```bash
touch .claude/.review_skip
```

## 수행 절차

`.claude/agents/PIPELINE.md` 흐름을 따르되 `touch .claude/.review_pending`은 절대 수행하지 않는다.

1. **Planner 호출**: `subagent_type=Planner`로 Agent 도구 호출 — 큐의 모든 작업 지시를 한 번에 전달 → 기획서를 사용자에게 출력 → **명시적 승인 대기**.

2. **Coder × N 병렬 호출**: 승인 후 각 작업 단위를 `subagent_type=Coder`, `isolation: "worktree"` 옵션으로 병렬 호출 → 모든 Coder 완료까지 대기.

3. **트렁크 머지 (사전 충돌 감지 적용)**:

   3-1. **시뮬레이션**: 각 worktree 브랜치에 대해 `git merge-tree --write-tree --name-only --messages <TRUNK_HEAD> <branch>` 실행. exit 0 = 클린, 1 = 충돌, ≥2 = 오류(사용자 보고 후 중단). 결과로 CLEAN/CONFLICT 목록 분류.

   3-2. **클린 일괄 머지**: `git merge --ff-only <branch>` × CLEAN_N 회 (ff 불가 시 `git merge <branch>` 폴백).

   3-3. **충돌 브랜치 1개씩 처리**: CONFLICT 목록을 순회하며 `git merge --no-ff --no-commit <branch>` → 워킹트리에 충돌 마커 생성. `git diff --name-only --diff-filter=U`로 unmerged 파일 확인. unmerged가 비어있으면 즉시 commit하고 다음 브랜치. 그렇지 않으면 `subagent_type=Mediator` 호출 (충돌 파일 목록 + 머지 중인 브랜치의 Coder 의도 + 잔여 CONFLICT 브랜치 의도 요약 전달). Mediator 종료 후 `git diff --name-only --diff-filter=U`가 비어 있는지 재확인 → `git add <resolved-files>` + `git commit -m "merge: resolve conflicts from parallel Coders (<branch>)"` → 다음 충돌 브랜치 반복.

   3-4. **비정상 시**: Mediator 1회 재호출 후에도 unmerged 잔존 / 시뮬레이션 rc≥2 / 머지 실패 → `git merge --abort`로 복구 + 사용자 보고 후 중단.

4. **worktree 정리**: 머지 완료 후 `git worktree remove -f -f <path>` × N 회 + `git branch -D <worktree-branch>` × N 회.

5. **큐 초기화**: 파이프라인 완료 후 `.claude/.task_queue` 파일을 삭제한다.

## 디자인 의도

반복 호출 사이의 base→HEAD diff는 자연스럽게 합산되어 단일 `/pipeline-review` 호출에 전달된다. 즉 여러 번의 `/pipeline-build` + `/pipeline-start` 사이클을 누적한 뒤 한 번에 리뷰할 수 있다.

## 절대 수행 금지

`touch .claude/.review_pending` — 이 명령을 실행하면 메인 종료 시 Stop 훅이 Reviewer를 즉시 발동시킨다.

누적 모드에서는 Reviewer가 발동되지 않으므로 `.claude/deferred_review.json`에 deferrable 위반이 누적되지 않는다. `/pipeline-review` 또는 `/pipeline` 호출 시 Reviewer가 실행되어 해당 사이클에서 처음 deferrable 누적이 발생한다.

## 종료

사용자에게 다음 안내 후 응답을 종료한다:

"구현 누적 완료. 추가 작업 시 `/pipeline-build` 재호출, 리뷰를 진행할 준비가 됐다면 `/pipeline-review` 호출."
