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

3. **트렁크 머지**: 각 worktree 브랜치를 순차 `git merge <branch>` × N 회.
   - 충돌 없음 → 계속
   - 충돌 발생 → `git merge --abort` → `subagent_type=Mediator` 호출 → 해결 후 `git add <resolved-files>` + `git commit -m "merge: resolve conflicts from parallel Coders"` → 나머지 브랜치 계속

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
