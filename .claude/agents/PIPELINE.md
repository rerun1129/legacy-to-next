# Agent Pipeline

메인 에이전트는 오케스트레이션 전담. 각 서브 에이전트의 입출력을 전달하고 사용자 승인 게이트를 관리한다. 직접 코드 작성/검수/실행하지 않는다.

## 흐름

```
사용자 요청
  → [Planner]                    기획 + 도메인별 작업 단위 분할
  → [메인] 사용자 승인 요청
  → (승인) 도메인별 순차 구현:
      · back-end 변경 있으면:
            echo "backend" > .claude/.coder_scope
            → [Backend-coder] 구현 (빌드·테스트 미수행, QA 담당)
            → rm -f .claude/.coder_scope
            → [메인] git add <변경파일> && git commit
      · front-end 변경 있으면:
            echo "frontend" > .claude/.coder_scope
            → [Frontend-coder] 구현 (빌드·테스트 미수행, QA 담당)
            → rm -f .claude/.coder_scope
            → [메인] git add <변경파일> && git commit
      · 공유영역(schema/, docs/, .claude/ 등) → 메인 직접 처리 → git add + commit
  → [QA]                         통합 빌드·테스트
      · PASS → 사용자 보고 후 종료
      · FAIL → 해당 도메인 Backend/Frontend-coder 재호출 → commit → QA 재실행
             → FAIL 2회차 → 메인이 사용자에게 실패 항목 보고 + 중단
```

## .coder_scope 마커 관리

메인은 각 Backend/Frontend-coder 호출 전후 반드시 마커 파일을 생성/삭제한다. PreToolUse 훅이 이 마커를 읽어 도메인 외 경로 편집을 차단한다.

```bash
# Backend-coder 호출 전
echo "backend" > .claude/.coder_scope
# Backend-coder 호출 후
rm -f .claude/.coder_scope

# Frontend-coder 호출 전
echo "frontend" > .claude/.coder_scope
# Frontend-coder 호출 후
rm -f .claude/.coder_scope
```

- 공유영역 작업(메인 직접 처리)에는 마커를 생성하지 않는다.
- Backend/Frontend-coder 호출이 에러로 중단되더라도 마커를 반드시 삭제해 다음 호출을 오염시키지 않는다.
- **경로 표기는 항상 `.claude/.coder_scope` 상대경로만 사용**. 프로젝트 절대경로(예: `C:\...\.claude\.coder_scope`) 금지. 향후 추가되는 모든 센티넬 파일도 동일하게 `.claude/` 하위 상대경로로 표기.
- **마커 라이프사이클은 메인 에이전트의 단독 책임**. Backend-coder / Frontend-coder는 이 파일을 직접 읽거나 생성·삭제하지 않는다(코더 정의에서는 PreToolUse 훅이 차단 메커니즘으로만 동작하며, 마커 관리 자체는 코더 책임 밖).

## 핸드오프

| 구간 | 트리거 | 전달 내용 |
|------|--------|-----------|
| Main → Planner | Agent 도구 호출 | 사용자 요청 + 관련 파일 경로 |
| Planner → 사용자 | 메인이 기획서 출력 | 승인 게이트 (도메인별 작업 단위 포함) |
| Main → Backend-coder | Agent 도구 순차 호출 (사용자 승인 후) | 백엔드 작업 단위 기획 |
| Main → Frontend-coder | Agent 도구 순차 호출 | 프론트엔드 작업 단위 기획 |
| Backend/Frontend-coder → Main | 완료 반환 | 변경 파일 목록 |
| Main → QA | Backend/Frontend-coder 완료 후 Agent 도구 호출 | 변경 파일 목록 |
| QA → Main (FAIL 시) | 미통과 보고 | Backend/Frontend-coder 재작업 항목 + 실패 명령어 출력 |
| Main → Backend/Frontend-coder (재작업 — QA FAIL) | Agent 도구 호출 | 실패 항목 목록 + 수정 대상 파일 |

## 메인 에이전트 참조 명령어

| 시점 | 명령어 |
|------|--------|
| Backend-coder 호출 전 | `echo "backend" > .claude/.coder_scope` |
| Backend-coder 호출 후 | `rm -f .claude/.coder_scope` |
| Frontend-coder 호출 전 | `echo "frontend" > .claude/.coder_scope` |
| Frontend-coder 호출 후 | `rm -f .claude/.coder_scope` |
| Backend/Frontend-coder 작업 완료 후 | `git add <변경된 파일들>` → `git commit -m "feat: <작업 설명>"` |
| QA FAIL 후 Backend/Frontend-coder 재작업 완료 시 | `git add <files>` → `git commit -m "fix: rework per QA failure"` |

## 공통 규칙

- **300줄 초과: 분리 검토 / 500줄 초과: 강제 분리** — 두 경우 모두 사용자 보고.
- 본인 tools 범위 밖 작업 금지.
- 추측 금지. 명령어 출력 또는 코드 인용으로 근거 제시.

## 환경 설정

- PreToolUse 훅: `.claude/hooks/restrict_coder_path.sh` 등록. `.coder_scope` 마커 기반으로 Backend/Frontend-coder의 도메인 외 경로 편집 차단.

## Backend/Frontend-coder→QA 독립 흐름 (`/pipeline-coder-qa`)

Planner 미발동 단축 사이클 (작은 수정·빠른 검증용). 상세 절차는 `commands/pipeline-coder-qa.md` 참조.
