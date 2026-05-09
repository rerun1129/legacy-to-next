---
description: rules/master_coding_rules.md 기준으로 지정 파일을 Haiku Refactorer로 리팩토링. 변경만 하고 종료(git commit·QA·Reviewer 미수행).
allowed-tools: Agent, Bash, Read, Write, Edit, Glob, Grep
---

## 진입 처리

1. `rules/master_coding_rules.md` 존재 확인. 없으면 즉시 종료: "규칙 파일(rules/master_coding_rules.md)이 없습니다."
2. 규칙 파일을 Read하여 **전체 내용을 사용자에게 출력**한다 (이번 사이클에 적용되는 규칙을 시각적으로 확인).
3. **`touch .claude/.review_pending` 절대 금지**. sentinel 생성 일체 없음.

## 대상 결정

`$ARGUMENTS` 토큰을 파싱한다:

- **빈 인자 또는 `--scan`** → 스캔 모드. 아래 패턴으로 Grep 후 후보 파일 목록만 출력하고 종료:
  - `/api/v[0-9]+/` — URL 버전 위반 후보
  - `\.map\([A-Z]\w*::from\)` — 컨트롤러 직접 매핑 후보
  - `[A-Z]\w*\.from\(` — 직접 변환 호출 후보
  - `\blist\(|\bprocess\(|\bhandle\(` — 모호한 메서드명 후보
  - `"[가-힣]` — 하드코딩 메시지 후보
  - 종료 메시지: "후보 파일 N개. 구체 파일을 골라 `/refactor-by-rules <파일경로>`로 재호출하세요."

- **`--changed`** → `git diff --name-only origin/master..HEAD` 결과를 대상으로.
- **`--staged`** → `git diff --name-only --cached` 결과를 대상으로.
- **그 외** → 파일 경로·디렉토리·glob 정규화 후 코드 파일(`.java`, `.ts`, `.tsx`)만 선별.

대상 파일이 0개면 종료.

## 1차: 메인이 직접 탐지

메인이 대상 파일을 Read하여 규칙 위반을 직접 분류한다. Haiku(Refactorer)는 복합 구조 위반(다중 라인 DTO 매핑, 1회 참조 변수 등)을 놓칠 수 있으므로 탐지는 메인 책임이다.

분류 결과:
- **즉시 처리 가능** → Refactorer에 Edit 위임
- **메인 개입 필요** (NAME-1 / PRES-1 / MSG-1) → 게이트 진행

서브 에이전트 호출 시 `isolation` 옵션은 사용하지 않는다. 모든 에이전트는 메인 작업 디렉토리에서 직접 작업한다.

## 의미적 결정 게이트 (메인 — Refactorer 보고 기반)

Refactorer가 보고한 **메인 개입 필요 항목**을 처리한다. 메인은 Refactorer 보고 내용만으로 판단하며 파일을 직접 Read하지 않는다.

- **NAME-1** — Refactorer 보고 기반으로 메서드명 후보 2~3개 제시. 사용자가 한 줄로 선택. 미응답 시 진행 중단.
- **PRES-1 (어셈블러 미존재)** — 어셈블러 클래스명·메서드 시그니처 후보 제시. 사용자 1줄 승인.
- **MSG-1 (MessageCode 멤버 미존재)** — 상수명·메시지 문자열 후보 제시. 사용자 1줄 승인.

해당 항목이 없으면 게이트 스킵.

## 테스트 파일 처리 (Refactorer 위임 — 사용자 승인 후)

Refactorer가 `[TEST_BREAK]`를 보고한 경우:
1. 메인이 변경 범위(Refactorer 보고 내용)를 사용자에게 요약·보고한다.
2. **사용자의 명시적 승인** 후 Refactorer에 Edit를 위임한다 (CLAUDE.md 정책).
3. 메인이 직접 Read·Edit하지 않는다.

## 사전 골격 생성 (메인 명세 → Refactorer Edit)

메인은 내용(명세)만 결정하고, 실제 파일 수정은 Refactorer에 위임한다:

1. **신규 어셈블러 클래스** — 메인이 직접 Write로 생성한다 (Refactorer는 Write 도구 없음). 기존 어셈블러 패턴 참조.
2. **MessageCode enum 멤버 추가** — 메인이 상수명·메시지 문자열 확정 → Refactorer가 Edit.
3. **메서드명 일괄 변경** — 메인이 변경 대상 파일·메서드명 목록 확정 → Refactorer가 Grep·Edit. (인터페이스+구현체 동시 변경 명세는 메인 책임)

## Refactorer 2차 호출 (있는 경우만)

메인 개입 항목이 있었고 처리 완료된 경우 Refactorer에 위임한다.

**프롬프트 작성 원칙 (토큰 절약):**
- 확정된 식별자 목록(구명 → 신명)과 대상 파일 목록만 전달한다.
- 파일별 상세 변경 명세를 작성하지 않는다. Refactorer가 파일을 읽고 판단한다.
- 보고는 "항목명 + 완료/미처리 여부만, 코드 블록 금지" 지시를 포함한다.

## 종료

- **git commit 안 함**. **`touch .claude/.review_pending` 금지**.
- sentinel 생성·정리 일체 없음.
- 종료 메시지:

```
리팩토링 완료. 변경 파일:
  - <path1>
  - <path2>

검토 후 직접 commit하세요. 빌드/테스트 검증:
  back-end/java-spring/gradlew.bat -p back-end/java-spring test
```
