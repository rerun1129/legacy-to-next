---
description: 마지막 핸드오프/이식 이후 변경된 auto-memory만 추출해 handoff_yyyyMMdd.md 생성(다른 PC 이식용).
argument-hint: "[기준 날짜 yyyyMMdd (선택, 생략 시 마커 자동 판별)]"
allowed-tools: Read, Write, Edit, Glob, Grep, Bash
---

이 PC의 auto-memory에서 **마지막 핸드오프(또는 이식) 이후 변경된 메모리만** 추출해 `handoff_<yyyyMMdd>.md` 한 파일로 만든다. 다른 PC가 이 파일만으로 메모리를 따라잡을 수 있게 하는 게 목적이다.

## 메모리 경로 (MEM)
`<홈>/.claude/projects/C--vive-coding-portfolio-legacy-to-next-legacy-to-next/memory/`
- 홈: Windows `$env:USERPROFILE`, 그 외 `$HOME`. **username 하드코딩 금지** — PC마다 다름.

## 1. 기준 시점(lastSync) 결정 — 우선순위
1. `$ARGUMENTS`에 기준 날짜(yyyyMMdd)가 주어졌으면 그것을 사용.
2. `MEM/.last-handoff` 마커 파일이 있으면 **그 파일의 mtime**을 기준으로 삼는다.
3. 없으면 `MEM/handoff_*.md` 중 가장 최근 파일의 mtime을 기준으로 삼는다.
4. 그것도 없으면(최초 실행): 사용자에게 "동기화 기준이 없습니다. 전체를 핸드오프할까요, 아니면 기준 날짜(yyyyMMdd)를 지정할까요?"라고 물어 결정.

## 2. 변경 파일 수집 (Bash)
- `MEM` 최상위 `*.md` 중 기준 시점보다 mtime이 새로운 파일을 찾는다.
  - 마커 기준 예: `find "$MEM" -maxdepth 1 -name '*.md' -newer "$MEM/.last-handoff" ! -name 'handoff_*.md' ! -name 'MEMORY.md'`
  - 날짜 기준일 때는 해당 날짜 이후 mtime으로 필터.
- **제외 대상**: `handoff_*.md`(과거 핸드오프), `MEMORY.md`(인덱스는 3단계에서 별도 첨부).
- 수집된 파일 목록을 먼저 확인한다. **0건이면 "변경 없음" 보고 후 파일을 만들지 않고 종료.**

## 3. handoff 파일 작성
- 출력 경로: `MEM/handoff_<yyyyMMdd>.md` (오늘 날짜). 같은 파일이 이미 있으면 덮어쓰기 전에 사용자에게 확인.
- **반드시 Write 도구로 UTF-8 저장** — 메모리를 md 텍스트로 합칠 때 한글이 CP949로 깨진 전례가 있다([[reference_memory_migration]]). 작성은 Write만 사용하고 `cat`/리다이렉트로 만들지 말 것.
- 구성:

  ```
  # Memory Handoff <yyyyMMdd>
  - 생성: <ISO 시각>
  - 기준(lastSync): <기준 시점/날짜>
  - 포함 파일: <N>건

  ## 적용법 (대상 PC)
  각 `===== MEMORY FILE: <name> =====` 블록의 내용을 같은 파일명으로 memory/ 에 저장(UTF-8, 덮어쓰기). 맨 끝 "MEMORY.md 인덱스(변경분)" 줄들을 대상 PC의 MEMORY.md에 병합. 적용을 마치면 대상 PC에서 `.last-handoff`를 갱신(touch)해 기준을 맞춘다.

  ===== MEMORY FILE: <파일명1> =====
  <원본 파일 전체 내용 (frontmatter 포함, 그대로)>
  ===== END =====

  ... (수집한 파일마다 반복)

  ## MEMORY.md 인덱스 (변경분)
  <수집한 파일들에 해당하는 MEMORY.md 인덱스 줄만 복사>
  ```
- 원본 내용은 가공 없이 그대로 옮긴다. 구분자 `===== MEMORY FILE:` / `===== END =====`는 메모리 본문에 등장하지 않는 유니크 마커라 충돌하지 않는다.

## 4. 프로젝트 루트로 복사
- 생성한 `handoff_<yyyyMMdd>.md`를 **프로젝트 루트(현재 작업 디렉토리)** 에도 그대로 사본으로 둔다 — 다른 PC로 옮기기 쉽게(별도 요청 없이 기본 수행).
- **Bash `cp`로 바이트 복사**(재인코딩 방지): `cp "$MEM/handoff_<yyyyMMdd>.md" "./handoff_<yyyyMMdd>.md"`. PowerShell 리다이렉트/`Set-Content`/`Out-File`로 만들지 말 것(CP949 깨짐, [[reference_memory_migration]]).

## 5. 마커 갱신
- `MEM/.last-handoff`에 현재 시각 ISO를 Write로 기록한다(이 파일의 mtime이 다음 추출의 기준이 됨).

## 종료
생성한 `handoff_<yyyyMMdd>.md`의 **MEM 경로 + 프로젝트 루트 사본 경로**, 포함 파일 목록(건수), 대상 PC 적용 절차를 요약 보고.
