# ENTRY_MIGRATION_GUIDE.md phase 단위 분할 플랜

> **상태**: Plan만 보관. 실행 보류 (사용자 요청 2026-05-13). 차기 세션 이어가기용.

## Context

`ENTRY_MIGRATION_GUIDE.md`가 1504줄로 누적. **에이전트가 마이그레이션을 수행할 때 phase별로 하나의 문서만 컨텍스트에 담아 작업 → 다음 phase 문서 로드 → 반복**하는 워크플로우를 만들기 위해 가이드를 사용 시점별로 분할.

분할 목표:
- 각 phase 시작 시 해당 phase 파일 1개만 read → 그 안에 필요한 모든 패턴·체크리스트·안티패턴이 있어야 함 (cross-file 참조 최소화).
- 한 phase 분량은 단일 컨텍스트로 충분히 담을 수 있는 크기 (~300~600줄).
- 차기 회귀 패턴 추가 시 어느 파일에 들어가는지 명확.

현재 구조 (1504줄):
- §1 작업 흐름 요약 (12줄)
- §2 카탈로그 매핑 표 (25줄)
- §3 RHF / §4 Enum / §5 그리드 (~130줄)
- **§6 재발 방지 체크리스트 49개 sub-section (~1080줄)** — 전체의 ~72%
- §7 CSS / §8 검증 / §9 commit (~50줄)
- §10 이미 완료된 공통 작업 로그 (~73줄, 정보성)
- §11 가이드 갱신 시점 / §12 후속 결정 / §13 도메인별 리마인드 (~100줄)

§6의 각 sub-section은 **실제 마이그레이션 작업 중 언제 참조하는지**로 분류 가능 → phase별 분할의 기준이 된다.

---

## 분할 결과 (5개 본문 + 1개 운영/history)

```
docs/migration/
  README.md              ~120줄  — 진입점·인덱스·진행 사이클 안내
  phase1-setup.md        ~280줄  — Plan 단계: 사전 점검 + 도메인 리마인드
  phase2-ui.md           ~500줄  — UI/컴포넌트 카탈로그 구현
  phase3-data-flow.md    ~600줄  — 풀스택 정합·CRUD·캐시·라우팅
  phase4-qa.md           ~60줄   — 검증·커밋
  history.md             ~80줄   — 이미 완료된 공통 작업 로그 (참조용)
```

**기존 `ENTRY_MIGRATION_GUIDE.md` 처리 (사용자 결정 2026-05-13)** — **분할 검증 기간 동안 원본 그대로 유지**. thin pointer 축약은 추후 결정. docs/migration/ 6개 파일과 원본을 병행 보관하며 분할이 잘 동작하는지 확인한 뒤 정리 여부 판단.

---

## 각 파일 구성

### README.md (~120줄)
- **목적**: 진입점. 에이전트가 처음 가이드를 인용할 때 이 파일부터 read.
- **포함**:
  - 분할 정책 한 줄 요약
  - **사이클 안내** (가장 중요):
    1. 새 도메인 마이그레이션 시작 → `phase1-setup.md` read → Plan 수립
    2. UI 카탈로그 작업 → `phase2-ui.md` read → 구현
    3. 풀스택 정합 작업 → `phase3-data-flow.md` read → 구현
    4. 검증·커밋 → `phase4-qa.md` read → 마무리
    5. 회귀 발견 또는 패턴 추가 시 → 해당 phase 파일에 추가 (분류 표 참조)
  - 5개 phase 파일 1줄 인덱스
  - **§1 작업 흐름 요약** (8단계, 12줄) 이전
  - **§2 카탈로그 컴포넌트 매핑 표** (25줄) 이전 — phase2-ui.md에 복제 게재(cross-file read 회피).
  - **§11 본 가이드 후속 갱신 시점** (회귀 발견 시 어디에 추가하는지 분류 가이드) — 이전.

### phase1-setup.md (~280줄)
**언제 read**: 새 도메인 entry 마이그레이션 plan 단계에서 작업 범위 결정 시.
- §6.48 ⚠️ 신규 도메인 마이그레이션 의무 체크리스트 (Non B/L → Truck B/L 재발 패턴, ~48줄)
- §6.49 🚨 FE 카탈로그 사전 점검 매트릭스 (READ FIRST, 12개 누락 패턴 + 11개 사전 점검 + Round 1~3 사례, ~110줄)
- §6.11 Coder plan mode 진입 시 메인 보고 의무 (~15줄)
- §12 후속 결정 대기 사항 (§12.1 / §12.2 / §12.3) (~40줄)
- §13 도메인별 entry 마이그레이션 리마인드 체크리스트 (§13.1~§13.6) (~55줄)
- (소계 ~268줄)

### phase2-ui.md (~500줄)
**언제 read**: UI 컴포넌트를 카탈로그로 1:1 교체할 때.
- §2 카탈로그 컴포넌트 매핑 표 (README에서 복제, ~25줄)
- §3 Form 통합 (RHF spread/Controller, schema/defaults 동시 갱신) (~62줄)
- §4 Enum 드롭다운 (useEnumOptions, 등록된 enum 23개) (~22줄)
- §5 그리드 패널 패턴 (GridList 분기, 표준 props, 셀 input) (~52줄)
- **§6 UI/컴포넌트 안티패턴 (19개)**:
  - §6.1 CodeBox 외부 wrapper 중첩 금지
  - §6.2 DateBox className/style 전달 금지
  - §6.4 카탈로그 페이지 등록 + 300줄 분리
  - §6.6 로컬 wrapper 컴포넌트 제거
  - §6.7 인라인 style 일괄 제거
  - §6.10 GridList onClearRow 누락 금지
  - §6.12 그리드 셀 native input 금지
  - §6.13 백엔드 관리 필드 hidden register
  - §6.15 ComboBox register spread 금지
  - §6.20 위젯 편집 type="button" 명시
  - §6.38 그리드 +/- 버튼 클래스 SSOT
  - §6.39 그리드 행 삭제 포커싱 셀 행 우선
  - §6.40 GridList outside-click 가드
  - §6.41 useVirtualizer getItemKey row.id 기반
  - §6.42 li__input--tight 자식 width 고정
  - §6.43 CodeBox lcn/code-only width
  - §6.44 CodeBox kind 명칭 (LCN/code-only/party-cn)
  - §6.45 enum option label = value 재매핑
  - §6.46 ComboBox cell variant Controller 필수
- (소계 ~500줄)

### phase3-data-flow.md (~600줄)
**언제 read**: form↔BE 매핑·CRUD mutation·라우팅·캐시 흐름 작업 시.
- **§6 풀스택·CRUD·라우팅 안티패턴 (27개)**:
  - §6.3 schema + defaults 동시 갱신
  - §6.5 useCallback/useEffect deps
  - §6.8 register spread + ref 충돌
  - §6.9 row.id 비교 String
  - §6.14 Container pkgUnit 자유 텍스트
  - §6.16 sessionStorage hot-marker 라우팅
  - §6.17 useState lazy initializer
  - §6.18 useBlDraftSync — unmount cleanup으로 clearDraft 금지 + didRestoreFromDraftRef 가드
  - §6.19 Entry Search EXACT 단건 PK
  - §6.21 Entry mutation 후 List 자동 invalidate 금지
  - §6.22 Entry plan에 List filter 금지
  - §6.23 product 컴포넌트 stub 금지
  - §6.24 Entry form Enter implicit submission 차단
  - §6.25 NonBl @NotBlank UI required 기준
  - §6.26 List 더블클릭 → Entry path-param 진입
  - §6.27 ScreenGuard 풀 오버레이
  - §6.28 자식 collection UPDATE — row id PUT 포함 필수
  - §6.29 BE 응답 시그니처 ↔ FE zod 정합 (Read flow critical)
  - §6.30 bulk DELETE clearAutomatically 부작용
  - §6.31 부모 삭제 SSOT — DB CASCADE 금지, ID-only subquery bulk delete
  - §6.32 ER 재구조화 — 단독 자식 vs 공유 자식 처리 원칙
  - §6.33 HouseBl jobDiv별 Strategy 분리
  - §6.34 1차 캐시 PK 기반만
  - §6.35 Update 흐름 책임 분리 — 도메인별 Port + Adapter
  - §6.36 Entry detail useQuery — staleTime:Infinity + gcTime:Infinity + refetchOnMount:false
  - §6.37 Entry sub-set 화면 — Request DTO·매퍼 sub-set화 필수
  - §6.47 List → Entry detail invalidate + draft clear SSOT
- (소계 ~600줄)

### phase4-qa.md (~60줄)
**언제 read**: 빌드/검증/커밋 단계.
- §7 CSS 토큰화 위치 (~20줄)
- §8 검증 절차 (빌드 통과 / 시각 회귀) (~26줄)
- §9 커밋 메시지 작성 (~14줄)
- (소계 ~60줄)

### history.md (~80줄)
**언제 read**: "이미 완료된 작업"이 무엇인지 확인이 필요할 때 (디테일 검증 / 새 도메인 패턴 참고). 일반 마이그레이션 사이클에서는 read 불필요.
- §10 이미 완료된 공통 작업 (자동 누적 정보성 로그)
- 차기 마이그레이션 commit 후 본 history에 1줄 요약 추가 의무.

---

## §6 49개 sub-section 분류 매트릭스

| sub | 제목 (요약) | Phase |
|---|---|---|
| 6.1 | CodeBox 외부 wrapper 중첩 금지 | 2 |
| 6.2 | DateBox className/style 금지 | 2 |
| 6.3 | schema + defaults 동시 갱신 | 3 |
| 6.4 | 카탈로그 등록 + 300줄 분리 | 2 |
| 6.5 | useCallback/useEffect deps | 3 |
| 6.6 | 로컬 wrapper 제거 | 2 |
| 6.7 | 인라인 style 제거 | 2 |
| 6.8 | register spread + ref 충돌 | 3 |
| 6.9 | row.id 비교 String | 3 |
| 6.10 | GridList onClearRow | 2 |
| 6.11 | Coder plan mode 보고 의무 | 1 |
| 6.12 | 그리드 셀 native input 금지 | 2 |
| 6.13 | 백엔드 관리 필드 hidden register | 2 |
| 6.14 | Container pkgUnit 자유텍스트 | 3 |
| 6.15 | ComboBox register spread 금지 | 2 |
| 6.16 | sessionStorage hot-marker | 3 |
| 6.17 | useState lazy initializer | 3 |
| 6.18 | useBlDraftSync 정정 + didRestoreFromDraftRef | 3 |
| 6.19 | Entry Search EXACT PK | 3 |
| 6.20 | 위젯 편집 type="button" | 2 |
| 6.21 | mutation 후 List invalidate 금지 | 3 |
| 6.22 | Entry plan List filter 금지 | 3 |
| 6.23 | product stub 금지 | 3 |
| 6.24 | Enter implicit submission 차단 | 3 |
| 6.25 | NonBl @NotBlank UI required | 3 |
| 6.26 | List 더블클릭 path-param 진입 | 3 |
| 6.27 | ScreenGuard 오버레이 | 3 |
| 6.28 | 자식 collection row id PUT | 3 |
| 6.29 | BE 응답 ↔ FE zod 정합 | 3 |
| 6.30 | bulk DELETE clearAutomatically | 3 |
| 6.31 | 부모 삭제 ID-only subquery | 3 |
| 6.32 | ER 재구조화 단독 vs 공유 | 3 |
| 6.33 | HouseBl jobDiv Strategy | 3 |
| 6.34 | 1차 캐시 PK 기반만 | 3 |
| 6.35 | Update 흐름 도메인 Port+Adapter | 3 |
| 6.36 | Entry detail useQuery 옵션 | 3 |
| 6.37 | Entry sub-set 매퍼 | 3 |
| 6.38 | 그리드 +/- 버튼 클래스 | 2 |
| 6.39 | 그리드 행 삭제 포커싱 우선 | 2 |
| 6.40 | GridList outside-click 가드 | 2 |
| 6.41 | useVirtualizer getItemKey | 2 |
| 6.42 | li__input--tight 자식 width | 2 |
| 6.43 | CodeBox lcn/code-only width | 2 |
| 6.44 | CodeBox kind 명칭 | 2 |
| 6.45 | enum option label = value | 2 |
| 6.46 | ComboBox cell Controller 필수 | 2 |
| 6.47 | List → Entry invalidate + draft | 3 |
| 6.48 | 신규 도메인 의무 체크리스트 | 1 |
| 6.49 | FE 카탈로그 사전 점검 매트릭스 | 1 |

분포: **Phase 1: 3개 / Phase 2: 19개 / Phase 3: 27개** (49 total).

---

## 운영 정책

### 패턴 추가 시 어느 파일에?
1. **Plan/사전 점검 패턴** (작업 시작 전에 매번 봐야 하는 의무 체크) → `phase1-setup.md`
2. **UI/컴포넌트 안티패턴** (TextBox/ComboBox/CodeBox/GridList 등 사용 규칙) → `phase2-ui.md`
3. **CRUD/캐시/라우팅/풀스택 정합** (form↔BE, mutation, useQuery, 캐시) → `phase3-data-flow.md`
4. **빌드/검증/CSS/commit** (작업 종료 직전) → `phase4-qa.md`
5. **완료된 작업 기록** (변경 history) → `history.md`

판단 어려운 경우 README의 분류 가이드(§11 이전본) 참조.

### cross-file 참조 정책
- 각 phase 파일은 **자기 단독으로 완결**되어야 한다. 다른 phase 파일을 read하지 않아도 그 phase 작업 완수 가능.
- 예외:
  - phase2-ui.md / phase3-data-flow.md 둘 다 §2 매핑 표를 참조하므로 phase2에 복제 게재(README 게재 + phase2 복제). phase3에서는 §2 필요 시 README read.
  - phase별 cross-reference가 있는 경우 짧은 "see §6.XX in phase3-data-flow.md" 형태로 명시.

### 기존 SSOT 보존
- 49개 sub-section의 번호(§6.1~§6.49)와 본문은 **그대로 보존**. 분할은 단순 재배치.
- §6.49의 "본 세션 사례 (2026-05-13)" Round 1~3 단락은 phase1-setup.md에 그대로 이전.
- §10 history는 변경 없이 통째 이관.

### 메모리 (`MEMORY.md`) 갱신
- 기존 메모리에 ENTRY_MIGRATION_GUIDE.md 참조가 있다면 docs/migration/README.md로 redirect 가능 (사용자 결정 후). 현재 grep 결과 메모리에는 가이드 직접 참조 없음 — 메모리 갱신은 불필요할 가능성 높지만 plan 실행 시 확인.

---

## 실행 단계 (Plan 승인 후)

1. **`docs/migration/` 디렉토리 생성** + 6개 파일 신규 작성 (5 phase + history)
2. **§6 49개 sub-section 본문을 매트릭스 분류대로 복사·이관**. 본문은 원본 그대로, 번호 보존.
3. **§1, §2, §11, §12, §13 본문도 분류대로 이관**.
4. **`ENTRY_MIGRATION_GUIDE.md`는 원본 유지** (사용자 결정 2026-05-13). thin pointer 축약은 검증 기간 후 결정.
5. **단일 commit으로 처리** — `docs: ENTRY_MIGRATION_GUIDE.md phase 단위 분할 (docs/migration/ 6개 파일 신설)`. 본문 변경 없는 단순 재배치라 commit 1개로 충분.
6. **검증**: 6개 새 파일을 cross-grep해 모든 §6.X 번호가 정확히 1번씩 나타나는지 확인 (분류 누락/중복 차단). 누락 시 즉시 fix.

분할은 단순 파일 split이라 BE/FE 빌드·테스트 영향 없음 (소스 코드 무변경). QA 에이전트 호출 불필요.

### 실행 시 효율 메모

- 본 가이드 1504줄 전체 read는 ~68k 토큰으로 단일 Read tool 제한 초과. **부분 read 전략 필수**:
  - phase4-qa.md (§7~§9, 1267~1324) → 작은 chunk부터
  - history.md (§10, 1325~1396)
  - phase1-setup.md (§6.11 236~239 + §6.48 1171~1217 + §6.49 1219~1265 + §12 1409~1448 + §13 1450~1504)
  - phase2-ui.md (§2~§5 21~184 + 19개 §6 sub-section)
  - phase3-data-flow.md (27개 §6 sub-section)
  - README.md (§1 8~19 + §2 복제 + §11 1399~1407)
- §6 각 sub-section 시작 라인: 187 §6.1 / 193 §6.2 / 199 §6.3 / 203 §6.4 / 207 §6.5 / 211 §6.6 / 215 §6.7 / 222 §6.8 / 226 §6.9 / 232 §6.10 / 236 §6.11 / 240 §6.12 / 254 §6.13 / 263 §6.15 / 269 §6.16 / 279 §6.17 / 304 §6.18 / 339 §6.19 / 352 §6.20 / 358 §6.21 / 376 §6.22 / 384 §6.23 / 393 §6.14 / 397 §6.24 / 425 §6.25 / 437 §6.26 / 473 §6.27 / 505 §6.28 / 525 §6.29 / 538 §6.30 / 549 §6.31 / 588 §6.32 / 608 §6.33 / 652 §6.34 / 670 §6.35 / 785 §6.36 / 882 §6.37 / 921 §6.38 / 931 §6.39 / 967 §6.40 / 992 §6.41 / 1039 §6.42 / 1056 §6.43 / 1074 §6.44 / 1088 §6.45 / 1102 §6.46 / 1124 §6.47 / 1171 §6.48 / 1219 §6.49. (sub-section 종료 = 다음 sub-section 시작 - 1, §6.49 종료 = 1266)
- 병렬 처리는 어렵다 — 각 phase 파일은 §6 sub-section의 정밀 라인 read가 필요하고 file write가 순차적이라야 한 파일이 완결됨.

---

## 검증 (분할 후 사이클 동작 확인)

다음 시나리오로 사이클이 잘 동작하는지 확인:

1. **새 도메인(Air House Entry) 마이그레이션 plan 단계 모사**:
   - 에이전트가 `docs/migration/README.md` + `docs/migration/phase1-setup.md`만 read한 상태에서 plan 수립 가능한가?
   - §6.48 / §6.49 / §13 등 plan 단계 의무 사전 점검이 phase1-setup.md 안에서 자체 완결되는가?

2. **UI 구현 단계 모사**:
   - phase1 컨텍스트를 비우고 `docs/migration/phase2-ui.md`만 read한 상태에서 카탈로그 교체 구현 가능한가?
   - §2 매핑 표·§3 RHF·§4 Enum·§5 그리드·§6 UI 안티패턴 19개가 모두 phase2에 있는가?

3. **풀스택 단계 모사**:
   - `docs/migration/phase3-data-flow.md`만 read한 상태에서 form↔BE 정합·mutation·useQuery 작업 가능한가?
   - §6 풀스택 안티패턴 27개가 모두 phase3에 있는가? (특히 §6.18 / §6.21 / §6.28 / §6.29 / §6.35 / §6.36 / §6.47)

4. **검증 단계**: phase4-qa.md만 read한 상태에서 검증·commit 가능한가? (§7·§8·§9 모두 포함)

검증 후 누락 발견 시 plan 4단계의 cross-grep 검증으로 fix.

---

## 후속 (분할과 별개)

- Round 3 후속 open 결함 4건(RESUME.md §3) — 별도 작업.
- 가이드 분할은 본 plan 단독 commit 1개로 종료. 후속 마이그레이션 작업은 새 plan으로 시작.
