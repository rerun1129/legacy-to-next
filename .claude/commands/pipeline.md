---
description: Planner→Backend/Frontend-coder→QA를 한 번에 실행. 단일 작업 단위일 때 사용.
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

풀 파이프라인을 한 번에 실행한다.

## 수행 절차

1. **Planner 호출**: `subagent_type=Planner`로 Agent 도구 호출 → 기획서를 사용자에게 출력 → **명시적 승인 대기**.

2. **도메인별 순차 구현** (승인 후):

   2-1. **back-end 변경 있으면**:
   ```bash
   echo "backend" > .claude/.coder_scope
   ```
   → `subagent_type=Backend-coder` 호출 → 완료 대기 →
   ```bash
   rm -f .claude/.coder_scope
   git add <변경파일목록>
   git commit -m "feat: <작업 설명>"
   ```

   2-2. **front-end 변경 있으면**:
   ```bash
   echo "frontend" > .claude/.coder_scope
   ```
   → `subagent_type=Frontend-coder` 호출 → 완료 대기 →
   ```bash
   rm -f .claude/.coder_scope
   git add <변경파일목록>
   git commit -m "feat: <작업 설명>"
   ```

   2-3. **공유영역(schema/, docs/, .claude/ 등) 변경 있으면**: 메인 직접 처리 → `git add + commit`.

3. **QA 호출**: `subagent_type=QA`로 호출. 변경 파일 목록 전달.

4. **QA 결과 분기**:
   - **PASS** → 사용자에게 결과 보고 후 종료.
   - **FAIL (1회차)**: 해당 도메인 Backend/Frontend-coder 재호출 → commit → QA 재실행.
   - **FAIL (2회차)**: 사용자에게 실패 항목 보고 + 중단.
