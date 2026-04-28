---
description: Planner→Coder→merge→commit→Reviewer→QA를 한 번에 실행. 단일 작업 단위일 때 사용.
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

풀 파이프라인을 한 번에 실행한다. 누적이 필요 없는 단일 기능 단위일 때 사용한다.

## 진입 처리

이전 누적 모드 잔존 sentinel을 제거한다:

```bash
rm -f .claude/.review_skip
```

## 수행 절차

`.claude/agents/PIPELINE.md` 전체 흐름(#2~#7)을 수행한다.

1. **Planner 호출**: `subagent_type=Planner`로 Agent 도구 호출 → 기획서를 사용자에게 출력 → **명시적 승인 대기**.

2. **Coder × N 병렬 호출**: 승인 후 각 작업 단위를 `subagent_type=Coder`, `isolation: "worktree"` 옵션으로 병렬 호출 → 모든 Coder 완료까지 대기.

3. **트렁크 머지 (사전 충돌 감지 적용)**:

   3-1. **시뮬레이션**: 각 worktree 브랜치에 대해 `git merge-tree --write-tree --name-only --messages <TRUNK_HEAD> <branch>` 실행. exit 0 = 클린, 1 = 충돌, ≥2 = 오류(사용자 보고 후 중단). 결과로 CLEAN/CONFLICT 목록 분류.

   3-2. **클린 일괄 머지**: `git merge --ff-only <branch>` × CLEAN_N 회 (ff 불가 시 `git merge <branch>` 폴백).

   3-3. **충돌 브랜치 1개씩 처리**: CONFLICT 목록을 순회하며 `git merge --no-ff --no-commit <branch>` → 워킹트리에 충돌 마커 생성. `git diff --name-only --diff-filter=U`로 unmerged 파일 확인. unmerged가 비어있으면 즉시 commit하고 다음 브랜치. 그렇지 않으면 `subagent_type=Mediator` 호출 (충돌 파일 목록 + 머지 중인 브랜치의 Coder 의도 + 잔여 CONFLICT 브랜치 의도 요약 전달). Mediator 종료 후 `git diff --name-only --diff-filter=U`가 비어 있는지 재확인 → `git add <resolved-files>` + `git commit -m "merge: resolve conflicts from parallel Coders (<branch>)"` → 다음 충돌 브랜치 반복.

   3-4. **비정상 시**: Mediator 1회 재호출 후에도 unmerged 잔존 / 시뮬레이션 rc≥2 / 머지 실패 → `git merge --abort`로 복구 + 사용자 보고 후 중단.

4. **종료 직전 sentinel 명시 청소** (마커 생성 직전 안전망 — sentinel과 마커 동시 존재 방지):

   ```bash
   rm -f .claude/.review_skip
   ```

5. **리뷰 마커 생성**:

   ```bash
   touch .claude/.review_pending
   ```

6. **종료**: "구현 완료. Stop 훅이 Reviewer를 호출합니다." 출력 후 응답을 종료한다.

## 이후 자동 진행

`/pipeline-review`와 동일한 Reviewer→QA 자동 사이클이 진행된다.

- **REJECTED (blocking ≥ 1)** → exit 2 → Coder 재호출(`isolation: "worktree"`) → 재머지·재commit → `touch .claude/.review_pending` → Stop 훅 재실행 (PIPELINE.md 참조)
- **REJECTED (blocking = 0, deferrable만)** → `.claude/deferred_review.json` 누적 → APPROVED 등가 → QA 자동 진행. 누적 임계 도달 시 메인이 QA 완료 후 사용자에게 일괄 처리 위임.
- **APPROVED** → APPROVED_MARKER 생성·exit 2 → 메인이 `subagent_type=QA` 호출 → worktree 정리 → 사용자 보고
