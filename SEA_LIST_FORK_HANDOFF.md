# Sea List Fork 작업 인계 (2026-05-07 작성)

이전 세션에서 **Air House List**와 **Air Master List**를 fork·구현했음. 이 문서는 다음 작업(Sea House List + Sea Master List)에서 동일 패턴을 그대로 활용하고, 같은 실수를 반복하지 않기 위한 인계 메모임.

## 1. 이전 세션 작업 결과 (커밋 완료)

| 커밋 | 내용 |
|---|---|
| `11f716f` | Air House List (Truck B/L List fork) — 25 files, 1616 insertions |
| `adbbe8a` | Air Master List (Air House List fork) — 24 files, 1376 insertions |

### Air House List
- 라우트: `/fms/house-bl/air-exp/list`, `/fms/house-bl/air-imp/list` 정적 라우트
- 컴포넌트 1세트, 두 라우트가 `bound` prop만 다르게 전달 (EXP/IMP)
- 백엔드 패키지: `airhouse`, 엔드포인트 `POST /api/air-house/search`
- 검색 조건 14개, 그리드 컬럼 30개
- 핵심: `JobDiv.AIR` 하드코딩 + `LEFT JOIN HouseBlAir` + `bound` 필수

### Air Master List
- 라우트: `/fms/master-bl/air-exp/list`, `/fms/master-bl/air-imp/list` 정적 라우트
- 백엔드 패키지: `airmaster`, 엔드포인트 `POST /api/air-master/search`
- 검색 조건 7개, 그리드 컬럼 22개
- 핵심: `MasterBlJobDiv.AIR` + `LEFT JOIN MasterBlAir` + `bound` 필수
- **House AWB Count** 컬럼은 `JPAExpressions` scalar subquery로 집계
- Master는 `actualCustomer/docPartner/salesMan/incoterms/salesClass/hblNo` 컬럼 부재 → Air House 대비 6개 제거

---

## 2. Sea List 작업 시작 시 적용할 패턴

### 2.1 fork 베이스 결정
- **Sea House List** = Air House List를 1:1 fork 후 도메인 치환
- **Sea Master List** = Air Master List를 1:1 fork 후 도메인 치환

### 2.2 핵심 치환 규칙

| Air | Sea |
|---|---|
| `airhouse` / `airmaster` (BE 패키지) | `seahouse` / `seamaster` |
| `air-house` / `air-master` (FE 디렉터리) | `sea-house` / `sea-master` |
| `JobDiv.AIR` | `JobDiv.SEA` |
| `MasterBlJobDiv.AIR` | `MasterBlJobDiv.SEA` |
| `QHouseBlAirJpaEntity` | `QHouseBlSeaJpaEntity` (확인 필요) |
| `QMasterBlAirJpaEntity` | `QMasterBlSeaJpaEntity` |
| `house_bl_air` / `master_bl_air` | `house_bl_sea` / `master_bl_sea` |
| `airline_code` | (해상은 다른 컬럼 — `liner_code`, `vessel_name`, `voyage_no` 등) |
| `/api/air-house` `/api/air-master` | `/api/sea-house` `/api/sea-master` |
| 라우트 `air-exp` `air-imp` | `sea-exp` `sea-imp` |
| 더블클릭 Entry 경로 | 동일 (`/fms/house-bl/sea-exp/entry` 등) |

### 2.3 사전 확인 필수 항목 (Sea 도메인 차이점)
탐색으로 다음을 먼저 확인할 것:
1. `house_bl_sea` 테이블 DDL — 어떤 컬럼이 있는가? (`schema/V1__fms_initial_schema.sql`)
2. `master_bl_sea` 테이블 DDL (라인 69~91 부근)
3. `HouseBlSeaJpaEntity` 필드 목록 (Air의 airlineCode·chargeWeightKg에 대응하는 sea 고유 필드)
4. `MasterBlSeaJpaEntity` 필드 목록
5. 사용자 명세 검색조건/그리드 컬럼은 fork 후 별도 지시 예정

### 2.4 사이드바·메뉴는 이미 정의됨 (수정 0건)
- `front-end/src/components/layout/sidebar.tsx`: Sea Export List/Entry, Sea Import List/Entry 모두 등록됨
- `front-end/src/lib/use-tabs.ts`: 라벨 매핑 등록됨
- `front-end/src/lib/bl-variants.ts`: `sea-exp`/`sea-imp` MasterVariantConfig 정의됨
- 정적 라우트(`sea-exp/list/page.tsx`, `sea-imp/list/page.tsx`)만 신규 추가하면 동적 `[variant]`보다 우선 매칭

### 2.5 신규 파일 개수 추정
- 백엔드: 패키지 신규 14개 (4계층 + 슬라이스 테스트)
- 프론트엔드: 신규 9개 + `lib/ports.ts` 수정 1개

---

## 3. 자주 한 실수 모음 (반복 금지)

### 실수 1: Filter accessor 메서드명 시그니처 오타
- **사례**: Backend-coder가 `AirHouseRepositoryImpl`에서 `filter.airHouseBlNo()` 호출 → 실제 record는 `hblNo()` 필드 → 컴파일 실패
- **방지**: Filter record 정의 후 RepositoryImpl 작성 시 **반드시 Filter 파일을 다시 읽어** 실제 accessor 메서드명 확인
- **체크포인트**: Filter record 필드명 ≠ Repository 호출 메서드명일 가능성

### 실수 2: 테스트 데이터 VARCHAR 길이 초과
- **사례**: `house_bl_air.airline_code` VARCHAR(10) 컬럼에 `"AIRLINE-EXP"` (11자) 입력 → `JdbcSQLDataException`
- **방지**: 테스트 fixture 작성 시 DDL의 `VARCHAR(N)` 길이 사전 확인
- **권장**: 7자 이하 짧은 코드 사용 (`"AIR-EXP"`, `"AIR-IMP"`, `"SEA-EXP"`, `"SEA-IMP"` 등)

### 실수 3: Filter.of() 시그니처 변경 시 기존 테스트 컴파일 깨짐
- **사례**: `AirHouseFilter.of()` 파라미터 10개 → 16개 변경 시 기존 슬라이스 테스트의 모든 호출부 컴파일 오류. 동일 패턴이 `AirMasterFilter.of()` 12개 → 10개에서도 발생.
- **방지**: Filter 필드 변경 시 즉시 `FilterImplSliceTest`의 `of(...)` 호출 grep해서 인자 수 동기화
- **CLAUDE.md 규칙**: 테스트 수정은 **사용자 명시 승인** 필수. Coder 단독 진행 금지.

### 실수 4: Projection 필드 제거 시 테스트의 직접 접근 코드 깨짐
- **사례**: `AirHouseSummary`/`AirMasterSummary`에서 `jobDiv`, `createdAt` 필드 제거 시 테스트의 `s.jobDiv()`, `s.createdAt()` 호출이 컴파일 오류
- **방지**: Projection record 필드 변경 시 슬라이스 테스트의 record accessor 호출 모두 grep해서 점검
- **수정 시**: jobDiv는 어차피 RepositoryImpl 하드코딩으로 보장되므로 검증 라인 제거. createdAt 같은 단순 not-null 검증은 다른 not-null 필드(`hblNo`/`mblNo`)로 교체.

### 실수 5: 백엔드 도메인 enum 위치 혼동
- 일반 enum: `domain/common/enums/` (`ShipmentType`, `Incoterms` 등)
- HouseBl 전용 enum: `domain/housebl/enums/` (`JobDiv`, `DateKind`, `PartyKind`, `PortKind`, `Bound` 등)
- MasterBl 전용 enum: `domain/masterbl/enums/MasterBlJobDiv` (House의 JobDiv와 다름! TRUCK/NON_BL 없음)
- 신규 작업 시 import 경로 헷갈리지 않게 IDE 자동 import 사용 권장

---

## 4. 작업 순서 표준 (이번 세션 검증된 흐름)

1. **Plan 모드**: 사용자 요청 파악 → 1~3개 Explore agent 병렬 호출 → plan 파일 작성 → ExitPlanMode
2. **Backend-coder**: BE 신규 파일 14개 (4계층 + 슬라이스 테스트). `.claude/.coder_scope`에 `backend` 마커
3. **Frontend-coder**: FE 신규 파일 9개 + `lib/ports.ts` 수정. `.claude/.coder_scope`에 `frontend` 마커
4. **QA**: BE build/test + FE lint/build (`/pipeline-coder-qa` 단축 사이클)
5. **QA FAIL 시**: 해당 도메인 Coder 재호출 (단, 테스트 수정은 사용자 승인 필요)
6. **사용자 명시 요청 시에만 git commit** (자동 commit 금지 — Memory 규칙)

### 검색조건/그리드 컬럼 적용 단계 분리
사용자는 **fork 1단계 → 검색조건 → 그리드 컬럼** 순으로 단계별 명세를 줌. 1단계 fork 시 검색조건/컬럼은 base와 동일하게 두고, 후속 단계에서 사용자 명세대로 개편.

---

## 5. 컨벤션 (반복 적용)

### 백엔드
- Filter record는 `domain/{도메인}/`에 위치
- Command record는 `application/{도메인}/command/`
- Projection record는 `application/{도메인}/projection/`
- Application 경계에서 enum은 String 직렬화 (P8 규칙)
- QueryDSL Projections.constructor에서 enum은 `.stringValue()`
- Repository 3분할: `XxxRepository`(JpaRepository) + `XxxRepositoryCustom` + `XxxRepositoryImpl`
- BooleanExpression null 반환 헬퍼 체이닝 (BooleanBuilder 누적 금지)

### 프론트엔드
- 캐시 정책: `staleTime: Infinity`, `refetchOnMount: false`, `enabled: extraFilter !== null`
- Search 클릭 시만 `qc.invalidateQueries`
- `gridId` 도메인별 분리 (localStorage 컬럼 가시성 키)
- `queryKey`: `[domain, 'list', bound, extraFilter, page]`
- SCOPE: `usePathname()` 사용 (라우트별 자동 분리)
- Name 컬럼은 백엔드에 데이터 없으면 `''` 고정 + 그리드 표시 유지 (사용자 결정)
- partyKind 옵션 3개 (SHIPPER/CONSIGNEE/NOTIFY), Settle Partner는 단독 CodeBox
- Master AWB 라벨드롭은 `LcnLabel` + `<input>` 인라인 조합

### 라우트
- 정적 `sea-exp/list/page.tsx`가 동적 `[variant]/list/page.tsx`보다 우선 매칭
- 페이지 파일에서 `<XxxListClient bound="EXP|IMP" />` 호출

---

## 6. 시작 명령 (다음 세션에서)

```
이 파일(C:\vive-coding\portfolio-legacy-to-next\legacy-to-next\SEA_LIST_FORK_HANDOFF.md)
읽고 Sea House List부터 fork 시작해줘. 검색조건이랑 그리드 컬럼은 fork 후 줄게.
```
