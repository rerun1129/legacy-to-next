---
description: 누적된 deferrable 위반을 Coder 재작업으로 일괄 정리 후 deferred_review.json 초기화.
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

`.claude/deferred_review.json`에 누적된 모든 deferrable 위반을 한 번에 Coder × N에 의뢰해 정리한다.

## 진입 조건 확인

```bash
cat .claude/deferred_review.json 2>/dev/null | jq '.cycles | length'
```

`0` 이거나 파일 없으면 "누적된 deferrable 위반 없습니다." 출력 후 종료.

## 수행 절차

1. **누적 내용 출력**: `.claude/deferred_review.json`의 모든 사이클을 사용자에게 출력.

2. **Planner 호출**: 누적된 모든 violations와 suggestions를 단일 리팩터 작업 지시로 묶어 `subagent_type=Planner`로 전달 → 기획서 출력 → **명시적 승인 대기**.

3. **Coder × N 병렬 호출**: 승인 후 각 작업 단위를 `subagent_type=Coder`, `isolation: "worktree"` 옵션으로 병렬 호출.

4. **트렁크 머지 (사전 충돌 감지 적용)**:

   4-1. **시뮬레이션**: 각 worktree 브랜치에 대해 `git merge-tree --write-tree --name-only --messages <TRUNK_HEAD> <branch>` 실행. exit 0 = 클린, 1 = 충돌, ≥2 = 오류(사용자 보고 후 중단). 결과로 CLEAN/CONFLICT 목록 분류.

   4-2. **클린 일괄 머지**: `git merge --ff-only <branch>` × CLEAN_N 회 (ff 불가 시 `git merge <branch>` 폴백).

   4-3. **충돌 브랜치 1개씩 처리**: CONFLICT 목록을 순회하며 `git merge --no-ff --no-commit <branch>` → 워킹트리에 충돌 마커 생성. `git diff --name-only --diff-filter=U`로 unmerged 파일 확인. unmerged가 비어있으면 즉시 commit하고 다음 브랜치. 그렇지 않으면 `subagent_type=Mediator` 호출 (충돌 파일 목록 + 머지 중인 브랜치의 Coder 의도 + 잔여 CONFLICT 브랜치 의도 요약 전달). Mediator 종료 후 `git diff --name-only --diff-filter=U`가 비어 있는지 재확인 → `git add <resolved-files>` + `git commit -m "merge: resolve conflicts from parallel Coders (<branch>)"` → 다음 충돌 브랜치 반복.

   4-4. **비정상 시**: Mediator 1회 재호출 후에도 unmerged 잔존 / 시뮬레이션 rc≥2 / 머지 실패 → `git merge --abort`로 복구 + 사용자 보고 후 중단.

5. **deferred 초기화**: commit 직후, 마커 생성 직전:

   ```bash
   echo '{"cycles":[]}' > .claude/deferred_review.json
   rm -f .claude/.deferred_snooze
   git add .claude/deferred_review.json
   git commit --amend --no-edit
   ```

   > **주의**: amend는 직전 commit이 이 flush 작업의 commit인 경우에만 사용. 아니라면 별도 commit으로 추가.

6. **리뷰 마커 생성 + 종료**:

   ```bash
   touch .claude/.review_pending
   ```

   "deferred 위반 정리 완료. Stop 훅이 Reviewer를 호출합니다." 출력 후 응답 종료.

## 이후 자동 진행

`/pipeline-review`와 동일한 Reviewer→QA 사이클. 이번 사이클에서는 누적분이 비어 있으므로 violations가 새로 없으면 APPROVED 기대.
