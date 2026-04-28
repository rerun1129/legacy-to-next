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

4. **트렁크 머지**: 각 worktree 브랜치를 순차 `git merge <branch>` × N회. 충돌 시 Mediator 호출.

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
