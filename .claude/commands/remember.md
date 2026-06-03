---
description: 현재 세션에서 기억해둘 사항을 auto-memory에 적재(신규/갱신) + MEMORY.md 인덱스 반영.
argument-hint: "[기억할 내용 (생략 시 세션에서 자동 식별)]"
allowed-tools: Read, Write, Edit, Glob, Grep
---

현재 세션에서 기억해 둘 사항을 이 프로젝트의 auto-memory에 적재한다(향후 세션에서 자동 recall되도록).

## 메모리 경로
`<홈>/.claude/projects/C--vive-coding-portfolio-legacy-to-next-legacy-to-next/memory/`
- 홈: Windows `$env:USERPROFILE`, 그 외 `$HOME`(`~`). **username 하드코딩 금지** — PC마다 다름.
- 인덱스 파일: 같은 폴더의 `MEMORY.md`.

## 입력 해석
- `$ARGUMENTS`가 있으면 그 내용을 적재 대상으로 삼는다.
- 비어 있으면 현재 세션에서 **향후에 도움될 비자명한 사항**을 직접 1건 이상 식별한다. 다음은 제외: 코드/파일 구조·git 히스토리·CLAUDE.md로 이미 자명한 것, 이번 대화에만 의미 있는 것.

## 적재 절차 (사항 건당)
1. **type 분류**: `user`(사용자 정체성/선호) · `feedback`(작업 방식 지침, 교정·확정 모두) · `project`(진행 작업/목표/제약) · `reference`(외부 자원 포인터).
2. **중복 확인**: Grep/Glob로 같은 주제의 기존 파일을 찾는다. 있으면 **그 파일을 갱신**(새 파일 생성 금지). 틀린 것으로 판명된 기존 메모리는 삭제.
3. **파일 작성**: 파일명 `<type>_<kebab-slug>.md`.
   - frontmatter: `name`, `description`(recall 판단용 한 줄), `metadata.type`.
   - 본문: 사실 1건. `feedback`/`project`는 **Why:** 와 **How to apply:** 줄을 포함. 관련 메모리는 `[[name]]`로 링크(아직 없는 대상이어도 무방).
   - 상대 날짜는 절대 날짜(yyyy-MM-dd)로 변환.
4. **UTF-8 저장**: 반드시 Write/Edit 도구만 사용(한글 인코딩 보존).
5. **인덱스 갱신**: `MEMORY.md`에 `- [제목](파일.md) — 한 줄 훅` 추가(기존 항목이면 그 줄 갱신). 본문 내용은 인덱스에 넣지 않는다(한 줄만).

## 종료
적재·갱신한 파일명과 MEMORY.md 인덱스 줄을 사용자에게 요약 보고.
