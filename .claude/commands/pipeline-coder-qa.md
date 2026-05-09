---
description: Planner 없이 Backend/Frontend-coder→QA만 실행. 작은 수정·빠른 검증용.
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

사용자 작업 지시(`$ARGUMENTS`)를 받아 도메인별 Backend/Frontend-coder 구현 + QA 빌드/테스트 검증만 수행한다.

## 진입 처리

**인자 확인**: `$ARGUMENTS`가 비면 다음을 출력 후 종료:
"작업 지시가 비어 있습니다. `/pipeline-coder-qa <작업 내용>` 형식으로 호출하세요."

## 수행 절차

1. **도메인별 순차 구현**: 작업 지시(`$ARGUMENTS`) + 관련 파일 경로를 직접 전달. **Planner 호출하지 않음**.

   > ⚠️ 아래 bash 명령의 경로는 **반드시 상대경로 그대로** 실행할 것. `C:\...` 절대경로로 변환 금지.

   - **back-end 변경 있으면**:
     ```bash
     echo "backend" > .claude/.coder_scope
     ```
     → `subagent_type=Backend-coder` 호출 → 완료 대기 →
     ```bash
     rm -f .claude/.coder_scope
     git add <변경파일목록>
     git commit -m "feat: <작업 설명>"
     ```

   - **front-end 변경 있으면**:
     ```bash
     echo "frontend" > .claude/.coder_scope
     ```
     → `subagent_type=Frontend-coder` 호출 → 완료 대기 →
     ```bash
     rm -f .claude/.coder_scope
     git add <변경파일목록>
     git commit -m "feat: <작업 설명>"
     ```

   - **공유영역(schema/, docs/, .claude/ 등) 변경 있으면**: 메인 직접 처리 → `git add + commit`.
   - 종료 시 반드시 `.claude/.coder_scope` 삭제: `rm -f .claude/.coder_scope`

2. **QA 호출**: `subagent_type=QA`로 호출. 변경 파일 목록 전달.

3. **QA 결과 분기**:
   - **PASS** → 사용자에게 결과 보고 후 종료.
   - **FAIL (1회차)**: 해당 도메인 Backend/Frontend-coder 재호출 → commit → QA 재실행.
   - **FAIL (2회차)**: 사용자에게 실패 항목 보고 + 중단.

## 종료 메시지

- QA PASS: "Backend/Frontend-coder 구현 + QA 검증 완료."
- QA FAIL 2회 연속: 실패 항목 명시 + "사용자 개입이 필요합니다."
