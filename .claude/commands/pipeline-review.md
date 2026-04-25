---
description: 누적된 변경분을 외부 Reviewer에 일괄 검토 의뢰. APPROVED 시 QA·worktree 정리까지 자동 진행.
allowed-tools: Bash, Read, Agent
---

누적된 변경분을 한꺼번에 외부 Reviewer에 넘긴다. 응답 종료 시 Stop 훅이 Reviewer를 호출하고 APPROVED 시 QA까지 자동으로 완료된다.

## 수행 절차

1. **변경분 확인**: 다음 명령을 실행하고 결과를 사용자에게 출력한다.

   ```bash
   git diff --stat origin/master..HEAD
   ```

2. **누적 모드 sentinel 명시 청소** — 변경분 유무와 무관하게 모든 종료 경로에서 sentinel을 제거하기 위해 변경분 확인 직후에 수행한다:

   ```bash
   rm -f .claude/.review_skip
   ```

3. **변경분 없으면 조기 종료**: 1단계 결과가 비어 있으면 "리뷰할 변경분이 없습니다. 누적 모드 종료." 안내 후 응답 종료 (마커 미생성). sentinel은 2단계에서 이미 청소됨.

4. **리뷰 마커 생성**: Stop 훅에 Reviewer 실행을 알린다.

   ```bash
   touch .claude/.review_pending
   ```

5. **종료**: "외부 코드 감사를 시작합니다. Stop 훅이 Reviewer를 호출합니다." 출력 후 응답을 종료한다.

## 이후 자동 진행 (PIPELINE.md #8~#13)

- Stop 훅 → Reviewer (Sonnet 1차, ESCALATE 시 Opus 2차)
- **REJECTED** → exit 2로 메인 재개 → Coder 재호출(`isolation: "worktree"`) → 재머지·재commit → `touch .claude/.review_pending` → 메인 재종료 → 재 Stop 훅
- **REJECTED 2회 이상** → `[ESCALATE_TO_USER]` stderr 주입 → 메인이 위반 내용을 사용자에게 직접 보고 후 대기
- **APPROVED** → APPROVED_MARKER 생성·exit 2 → 메인이 `subagent_type=QA` 호출 → worktree 정리 → 사용자 보고

REJECTED 후 재작업 흐름에서 메인은 `.claude/agents/PIPELINE.md`의 "REJECTED 후 Coder 재작업" 절차를 따른다.
