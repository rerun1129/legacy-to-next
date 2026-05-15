# 미해결 과제 (Backlog)

> 작성: 2026-05-15 · 상태: Sea Master Entry post-migration UI 정리 phase 중 발생한 별개 phase 보류 항목 + 이전 세션 누적 백로그 통합.

본 문서는 `master` 브랜치 기준 진행 중·예약 작업의 단일 진입점이다. 새 작업 시작 전 본 문서를 확인하여 우선순위·의존성을 정한다.

---

## 1. Sea Master Entry post-migration 후속 (이번 세션 발생)

이번 세션에서 layout/UI 정리는 완료(commit `2fc64aa1` `eb01adfc` `2a86c919`)되었으나, 다음 항목은 별개 phase로 보류했다.

### 1.2 Container 패널 데이터 매핑
- 파일: `front-end/src/components/fms/master-bl/tabs/sections/master-container-grid.tsx` (이번 세션 신규)
- 현황: ReadOnly grid + 10컬럼 + 빈 상태 "No rows." 고정
- 작업:
  - BE 응답에 Master 단위 container 집계 데이터가 있는지 확인 (`ConsoledHouseBlSeaSummary`에는 없음 — 별도 응답 필요 가능)
  - 없으면 `HouseBlContainerJpaEntity` 기준 집계 projection 신설 검토
  - FE `MasterContainerGrid`를 props 받는 시그니처로 변경 (현재 매개변수 0개)
- 우선순위: 중

### 1.3 Air Master Document 영역 cols=1 부수 효과 검토
- 파일: `front-end/src/components/fms/master-bl/tabs/sections/master-cargo-doc-panel.tsx`
- 현황: document widget의 FieldItemGrid `cols={1}`이 Sea/Air 공용. Air에서도 Co-Load Type/Flight Type/Security Status/Settle/Operator/Team이 각 한 줄씩 표시됨
- 작업: Air 화면에서 의도와 일치하는지 사용자 시각 확인 후 분기 도입 여부 결정. 분기 필요 시 widget을 mode별로 분리하거나 inline FieldItemGrid를 별도 render로 분리
- 우선순위: 낮음 (Air Master Migration 진행 시 함께 검토)

### 1.4 Air Master Liner / Operator / Team BE 필수 검증
- 파일: `back-end/.../CreateMasterBlRequest.java`
- 현황: `SeaMasterGroup`으로만 `linerCode`/`operatorCode`/`teamCode` 필수 적용. Air는 별도 검증 그룹 없음
- 작업: AIR Master Migration 시점에 `AirMasterGroup` 신설 + 동일 검증 또는 default 그룹 통합
- 우선순위: 중 (Air Master Migration 의존)

### 1.5 stale schema 필드 정리 (coLoadAgent 등)
- 파일: `master-bl-schema.ts`, `master-bl-defaults.ts`, `master-bl-submit.ts` 등
- 현황: 이번 세션에서 UI 제거(`coLoadAgent`)했으나 schema/defaults/submit/payloads에는 잔존
- 작업: 미사용 필드 식별 + 정리 phase 단독 진행 (영향 광범위)
- 우선순위: 낮음

### 1.6 localStorage layout state stale 자동 해소
- 파일: `front-end/src/lib/use-field-layout.ts`, `use-widget-layout.ts`
- 현황: `fms.fieldLayouts.v1` / 위젯 layout 키에 이전 widget 구조가 stale로 남아 빈 행/잘못된 위치 노출. 사용자가 매번 DevTools 캐시 클리어 필요
- 작업: defaultOrder 변경 감지 시 자동 invalidate 로직 또는 version bump (`v1` → `v2`)로 강제 리셋
- 우선순위: 중 (UX 영향)

---

## 2. Air Master Entry 마이그레이션 (예약)

- 출처: `project_sea_master_migration` 메모리 후속 ③
- 패턴: Air House Migration 9 Phase 구조(`docs-migration-ancient-wadler.md`) + Sea Master Migration 9 Phase 패턴(`docs-migration-breezy-quill.md`) 동일 적용
- 의무 grep: §6.60 (namespace 오기입), §6.61 (WebMvcTest 동적 Validator 빈 mock 금지)
- 진입 절차: `HANDOFF.md` 참조

---

## 3. 우선순위 가이드

| 등급 | 항목 |
|---|---|
| 높음 | (현재 없음 — 모두 별개 phase) |
| 중 | 1.2 Container 매핑 · 1.6 localStorage 자동 해소 |
| 낮음 | 1.3 Air Doc cols 검토 · 1.4 Air Liner BE · 1.5 stale schema · 2 Air Master Migration · 3.x 누적 백로그 |

## 4. 진행 정책

- 각 항목 시작 전 본 문서 read → 영향 범위·의존성·우선순위 확인
- 항목 완료 시 본 문서에서 제거 + commit 메시지에 항목 번호 인용 (e.g. "fix(FE): 1.1 House BL 6컬럼 매핑")
- 새 별개 phase 보류 발견 시 본 문서에 즉시 추가 (메모리 갱신은 별도)
- phase 진입 시 사용자 명시 승인 필수 (메모리 `feedback_phase_approval_required`)
