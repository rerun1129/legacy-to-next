---
description: Planner→Coder×N→merge→commit까지만 수행하는 누적 모드. Reviewer 미발동, 반복 호출 가능.
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

누적 모드 파이프라인. Reviewer를 발동시키지 않고 Planner→Coder×N→merge→commit까지만 수행한다. 여러 번 호출해 작업을 누적한 뒤 `/pipeline-review`로 한꺼번에 검토한다.

## 진입 처리

시작 즉시 다음을 실행해 리뷰 스킵 sentinel을 유지한다 (Stop 훅이 Reviewer 발동을 건너뜀):

```bash
touch .claude/.review_skip
```

## 수행 절차

`.claude/agents/PIPELINE.md` 흐름을 따르되 #7(`touch .claude/.review_pending`)은 절대 수행하지 않는다.

1. **Planner 호출**: `subagent_type=Planner`로 Agent 도구 호출 → 기획서를 사용자에게 출력 → **명시적 승인 대기**.

2. **Coder × N 병렬 호출**: 승인 후 각 작업 단위를 `subagent_type=Coder`, `isolation: "worktree"` 옵션으로 병렬 호출 → 모든 Coder 완료까지 대기.

3. **트렁크 머지**: 각 worktree 브랜치를 순차 `git merge <branch>` × N 회.
   - 충돌 없음 → 계속
   - 충돌 발생 → `git merge --abort` → `subagent_type=Mediator` 호출 → 해결 후 `git add <resolved-files>` + `git commit -m "merge: resolve conflicts from parallel Coders"` → 나머지 브랜치 계속

4. **worktree 정리**: 머지 완료 후 `git worktree remove -f -f <path>` × N 회 + `git branch -D <worktree-branch>` × N 회.

## 절대 수행 금지

`touch .claude/.review_pending` — 이것이 누적 모드의 핵심 제약이다. 이 명령을 실행하면 메인 종료 시 Stop 훅이 Reviewer를 즉시 발동시킨다.

## 종료

사용자에게 다음 안내 후 응답을 종료한다:

"구현 누적 완료. 추가 작업 시 `/pipeline-build` 재호출, 리뷰를 진행할 준비가 됐다면 `/pipeline-review` 호출."
