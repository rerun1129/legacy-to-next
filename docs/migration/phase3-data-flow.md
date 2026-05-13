# Phase 3 — 풀스택 정합·CRUD·라우팅

> **언제 read**: form↔BE 매핑·CRUD mutation·라우팅·캐시 흐름 작업 시.

본 phase는 schema/defaults · sessionStorage hot-marker · useQuery 옵션 · Port/Adapter SSOT 등 풀스택 결합점을 다룬다. UI/카탈로그 교체는 phase2-ui.md.

> §2 카탈로그 매핑 표가 필요하면 [README.md](README.md) 참조.

---

## 6. 풀스택·CRUD·라우팅 안티패턴 (27개)

### 6.3 새 form 필드 추가 시 schema + defaults 동시 갱신

phase2-ui.md §3 참고. 한 쪽만 갱신하면 register 미연결 또는 초기값 누락.

### 6.5 useCallback / useEffect deps 배열 갱신

ref나 새 변수를 hook 본문에 추가할 때 deps 배열 갱신 필수. lint `react-hooks/exhaustive-deps` 경고로 잡힘. (`useGridCellDrag` 사례: `onActiveRowChangeRef` 추가 시 deps 누락으로 lint FAIL).

### 6.8 register spread + ref 충돌 검증

CodeBox는 `forwardRef`를 받고 codeProps를 spread하는 구조. `codeProps={...register("X")}` spread 안 ref가 input에 정상 연결되는지 빌드 후 실제 폼 submit 시 값 도달 확인 필요.

### 6.9 row.id 비교 시 String 변환

`onRowClick={(row) => setSelectedKey(String(row.id))}` + `rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}`

> **이유**: id가 number일 수 있는데 selectedKey state는 string. 명시적 String() 변환 누락 시 비교 항상 false.

### 6.14 Container pkgUnit — 자유 텍스트 정책

Container Info 그리드의 `pkgUnit` 컬럼은 비표준 단위(CTN/PCS/BAG 등) 가능성으로 **자유 텍스트 유지**. Cargo 패널의 `cargoUnit`(WeightUnit enum)과 다름. House-BL과 동일 정책.

### 6.16 Entry 화면 F5/Save/New 라우팅 — sessionStorage hot-marker 패턴

Entry 화면이 `/entry/[id]` 라우트를 사용할 때 세 케이스를 sessionStorage 1회용 marker로 구분해야 함.

- **F5/딥링크 진입**: marker 없음 → `router.replace("/entry")` → 빈 폼
- **신규 저장 직후**: `onSuccess`에서 marker set → `router.replace(/entry/${saved.id})` → mount 시 marker 소비 + useQuery enabled → `getById` 재조회 → form 배치
- **New 버튼**: `handleResetEntry`에서 `router.replace("/entry")` → id prop 사라짐 → isEdit=false

사례: c918324 — Non B/L Entry 적용.

### 6.17 useState lazy initializer로 set-state-in-effect 회피

`useEffect` 안에서 `setState`를 동기 호출하면 `react-hooks/set-state-in-effect` lint error.
sessionStorage marker처럼 마운트 시 1회 읽어야 하는 값은 **lazy initializer**로 처리.

```ts
// ❌ lint error
const [flag, setFlag] = useState(false);
useEffect(() => {
  if (sessionStorage.getItem(key)) setFlag(true); // error
}, []);

// ✅ lazy initializer
const [flag] = useState<boolean>(() => {
  if (typeof window === "undefined") return false;
  if (sessionStorage.getItem(key)) {
    sessionStorage.removeItem(key);
    return true;
  }
  return false;
});
```

사례: ca66501 — `hydrateAllowed` 상태 초기화.

### 6.18 useBlDraftSync — unmount cleanup으로 clearDraft 호출 금지 (안티패턴 / 과거 권고 정정)

**과거 본 항목은 unmount 시 `clearDraft`를 호출하라 권고했으나 정정한다.** cleanup으로 unmount 시 draft를 일괄 제거하면 사용자가 메뉴를 잠시 갔다 돌아왔을 때 입력값이 사라져 House/Master B/L 동작과 일치하지 않는다.

```ts
// ❌ 안티패턴 — 메뉴 이동 후 복귀 시 사용자 입력 소실
useEffect(() => {
  const draftKey = `truck::${id ?? "new"}`;
  return () => { clearDraft(draftKey); };
}, [clearDraft, id]);
```

**올바른 패턴**: cleanup useEffect를 두지 말고, `useBlDraftSync` 반환의 `didRestoreFromDraftRef`로 detail useEffect에서 분기:

```ts
const { didRestoreFromDraftRef } = useBlDraftSync(form, `truck::${id ?? "new"}`);

useEffect(() => {
  if (detailLoadedRef.current) return;
  if (!detail) return;
  detailLoadedRef.current = true;
  // useBlDraftSync가 이미 stored draft로 form.reset 했으면 detail로 덮어쓰지 않음
  if (didRestoreFromDraftRef.current) return;
  form.reset({ /* detail → form 매핑 */ });
}, [detail, form, didRestoreFromDraftRef]);
```

- `useBlDraftSync`는 **key 변경마다** stored draft 존재 시 자동 `form.reset` 후 `ref.current = true`. 없으면 `false`로 리셋. mount 1회 가드 폐기.
- store를 직접 조회(`getDraft(key) !== undefined`)하는 분기는 **금지**: RHF `form.watch`가 mount 직후 register 과정에서 트리거되어 빈 값이 setDraft되는 케이스가 있어 false positive 발생.
- `handleResetEntry`처럼 명시적 사용자 액션은 별도로 `clearDraft(key)`를 직접 호출하여 draft 정리.

**list → entry 재진입 시 stale 캐시·draft 방어는 §6.47**(list 더블클릭·handleSearch 시점에 invalidate + clearDraft 명시 호출).

사례: 56f8f1d — Truck/Non B/L 엔트리 cleanup 제거 + useBlDraftSync 시그니처 확장(`didRestoreFromDraftRef`).

### 6.19 Entry Search 버튼 — EXACT 단건 PK 조회 endpoint + hot-marker 패턴

Entry 화면의 Search는 **EXACT 매칭 PK 조회 전용 endpoint**(예: `POST /api/non-bl/find-by-hbl-no`, 응답 `data: number[]`)로 0건/다건/단건 분기:
- 빈 입력: toast.info
- 0건: toast.info
- 다건(>1, hbl_no는 UNIQUE 제약 없음으로 우연 중복 가능): toast.info + List 화면 navigate
- 1건(동일 id): detail invalidate만
- 1건(다른 id): hot-marker 세팅 후 setFocus → §6.16 패턴 재사용

> 이전 패턴(`port.list(filter, 1, 2)` LIKE+COUNT 재사용) 폐기. Entry Search는 PK만 fetch하고 Detail 로드는 useQuery `getById`(기존 `findNonBlById`)가 담당. SQL 2회(SELECT+COUNT) → 1회(SELECT pk only)로 단축, JOIN/Projection 매핑 제거.

사례: 0526e83(초기 LIKE+COUNT 구현) → 2026-05-10(EXACT PK 분리, `findNonBlKeysByHblNoExact`).

### 6.21 Entry mutation 후 List 캐시 자동 invalidate 금지

Entry(생성/수정/삭제) `onSuccess`에서 List queryKey를 **자동 invalidate하지 않는다**.

업무 사용자가 List로 돌아왔을 때 직접 Search 버튼을 눌러 재조회하는 흐름이 의도된 동작. 자동 invalidate는 불필요한 백엔드 호출을 유발하고 사용자의 재조회 타이밍 제어를 빼앗는다.

```ts
// ❌ 금지
onSuccess: () => {
  qc.invalidateQueries({ queryKey: ['non-bl', 'list'] }); // 자동 재조회 금지
}

// ✅ 올바른 패턴 — detail invalidate만 (편집 재조회)
onSuccess: () => {
  qc.invalidateQueries({ queryKey: ['non-bl', 'detail', id] });
}
```

### 6.22 Entry plan에 List filter 전용 필드 혼용 금지

`partyKind`, `dateKind`, `portKind` 등 ComboBox 기반 필터 드롭다운은 **List filter 전용** 패턴이다. Entry 화면은 Party 5종 고정 라벨·단일 날짜·단일 포트로 구성되며 이런 kind 필드가 존재하지 않는다.

Entry plan에 `DateKind/PartyKind/PortKind` enum 등록이나 관련 필드 처리를 포함하면 scope 오염.

**올바른 패턴**: Entry 화면과 List filter는 서로 독립된 plan으로 분리.

### 6.23 프로덕트 컴포넌트에 stub 데이터 금지

하드코딩 defaultValue(`"COSCO2404195"`, `"한진무역(주)"` 등), 인라인 fixture 객체는 product 컴포넌트에 잔존해서는 안 된다.

**올바른 패턴**:
- 컴포넌트 내부 `DEFAULTS_*` 값, `defaultValue="실제값"` JSX prop → 모두 `""` 또는 제거
- 시연·개발용 fixture는 `adapter/out/mock/*` 또는 테스트 파일로 이동
- `NEXT_PUBLIC_USE_MOCK=true`일 때만 활성화되는 mock 어댑터는 허용 (dev/test 전용)

### 6.24 Entry form Enter 키 implicit submission 차단

`<form>` 안에서 Enter 키를 누르면 HTML implicit submission이 발동해 의도치 않은 저장이 트리거된다. 두 겹 방어를 모두 적용해야 완전히 차단된다.

```tsx
// 1. form onKeyDown 가드 — TEXTAREA 줄바꿈은 보존
<form
  onSubmit={methods.handleSubmit(handleSubmit)}
  onKeyDown={(e) => {
    if (e.key === "Enter" && (e.target as HTMLElement).tagName !== "TEXTAREA") {
      e.preventDefault();
    }
  }}
>

// 2. Save 버튼 — type="submit" → type="button" + onClick 명시
<button
  type="button"
  onClick={methods.handleSubmit(handleSubmit)}
>
  Save
</button>
```

> **이유**: `type="submit"` 버튼이 form 안에 있으면 Enter만으로 submit이 발화된다. `onKeyDown` 가드만으로는 버튼 포커스 상태에서 Space/Enter로 클릭이 발화되는 케이스를 놓칠 수 있어 이중 방어가 필요.

사례: faad158 — Non B/L Entry 적용.

### 6.25 NonBl Request DTO — UI required 기준 검증 어노테이션 관리 정책

**백엔드 validation SSOT 원칙**: 검증 책임은 백엔드(Request DTO 어노테이션)에 일원화. FE zodResolver/HTML5 required와 공존 금지(관리 포인트 분산 방지).

- **UI required 9개** (`hblNo`, `workDivision`, `polCode`, `podCode`, `etd`, `eta`, `actualCustomerCode`, `operatorCode`, `teamCode`): `@NotBlank` + 형식 어노테이션(`@Size`/`@Pattern`) **유지**. 검증 실패 시 `MethodArgumentNotValidException` → `GlobalExceptionHandler` → ProblemDetail → 프론트 `toast.error` 자동.
- **그 외 모든 필드**: 어노테이션 제거, null 그대로 통과. VO compact constructor 검증(BlDate yyyyMMdd 포맷, Weight ≥ 0 등)은 데이터 형식 무결성이므로 유지.
- DDL NOT NULL 컬럼(`house_bl.bound`, `container_no`, `length_feet` 등)은 별도 마이그레이션으로 nullable로 전환 예정(2/2).

> **다른 Entry 적용 시**: 각 Entry Request DTO도 동일 정책 — 해당 Entry의 UI required 필드는 어노테이션 유지, 그 외만 제거.

사례: 0aafb4d — `CreateNonBlRequest` UI required까지 함께 제거한 over-correction. 후속 plan에서 9개 복원.

### 6.26 List 더블클릭 → Entry 진입 패턴 (PK 단건 조회 모드)

List 행을 더블클릭하면 **PK를 URL로 전달**하고 Entry가 마운트 직후 `useQuery`로 단건 조회한다. List는 데이터를 prefetch/전달하지 않는다.

#### 허용 패턴 A — Path-param + hot-marker (Non B/L, Truck B/L · 정규 패턴)

```tsx
// Grid의 BL-No 셀 onDoubleClick
onDoubleClick={() => {
  sessionStorage.setItem(`${prefix}-entry:hot:${row.id}`, "1");
  router.push(`/fms/${prefix}/entry/${row.id}`);
}}
```

Entry 마운트 시:
- 마커 있으면 제거 후 `hydrateAllowed = true` → `useQuery` 발화 → `form.reset`
- 마커 없으면 (F5/deeplink) `router.replace("/fms/${prefix}/entry")` → 빈 폼

#### 허용 패턴 B — Query-param (Master B/L, House B/L · 부분 마이그레이션 상태)

```tsx
// Grid의 BL-No 셀 onDoubleClick
onDoubleClick={() => router.push(`/fms/.../entry?id=${row.id}`)}
```

Entry가 `searchParams.id`를 읽어 `useQuery` 발화. F5-clear 없음. 후속 마이그레이션에서 패턴 A로 전환 예정.

#### 안티패턴 (버그) ❌

```tsx
// PK 없이 push → 신규 저장 모드로 진입
onDoubleClick={() => router.push("/fms/non-bl/entry")}
```

> **적용 현황** (2026-05-09): Non B/L ✅ 패턴 A, Truck B/L ✅ 패턴 A, Master B/L ✅ 패턴 B, House B/L ✅ 패턴 B.

### 6.27 Entry 화면 비동기 처리 시 ScreenGuard 풀 오버레이 적용

Entry 화면에서 단건 조회·저장·삭제 mutation 진행 중에는 표준 공통 컴포넌트 `<ScreenGuard />`(`@/components/shared/screen-guard`)로 화면 전체를 잠그고 로딩 인디케이터를 표시한다.

```tsx
import { ScreenGuard } from "@/components/shared/screen-guard";

const isLoading =
  isDetailFetching || mutation.isPending || deleteMutation.isPending;
const loadingMessage = deleteMutation.isPending
  ? "삭제 중..."
  : mutation.isPending
    ? "저장 중..."
    : "조회 중...";

return (
  <>
    {/* form ... */}
    <ScreenGuard visible={isLoading} message={loadingMessage} />
  </>
);
```

- **트리거**: react-query의 `isFetching`(useQuery)·`isPending`(useMutation)을 그대로 활용. 신규 `useState` 로딩 변수 만들지 말 것.
- **props**: `visible?(기본 true)`, `size?("sm"|"md"|"lg", 기본 "md")`, `count?(3~7, 기본 5)`, `color?(막대 기본 #38BDF8)`, `message?(기본 "조회 중...")`. 텍스트 색은 `#F1F5F9`로 고정 (가독성 정책).
- **접근성**: `role="status"` + `aria-label={message ?? "Loading"}`, `prefers-reduced-motion: reduce` 시 막대 정적 표시(`@/app/globals.css`의 미디어 쿼리 분기).
- **dev 카탈로그**: `/preview` → "ScreenGuard" 섹션에서 size/count/color/message/reduced-motion variants 시연.

> **List 화면은 적용 금지** — List는 GridList의 스켈레톤 로딩을 사용하며 ScreenGuard와 동작이 충돌. `ScreenGuard` import는 Entry 컴포넌트에만 허용.

사례: (2026-05-10) Non B/L · Truck B/L · House B/L · Master B/L Entry 4개 적용.

### 6.28 자식 collection UPDATE — row id를 PUT 페이로드에 포함 필수

Entry 폼이 자식 collection(예: Dimension·Container·Desc 등)을 가질 때, FE에서 PUT 페이로드의 각 row에 **DB id(row.id)를 그대로 포함**해야 한다. id가 빠지면 백엔드 매퍼가 신규 row로 인식해 UPDATE 분기가 발사되지 않고, 의도와 달리 INSERT/누락이 발생한다.

```ts
// ❌ 함정 — id 탈락
const payload = {
  dimensions: form.dimensions.map((d) => ({ length: d.length, /* ... */ })),
};

// ✅ id 포함
const payload = {
  dimensions: form.dimensions.map((d) => ({ id: d.id, length: d.length, /* ... */ })),
};
```

> **백엔드 어댑터/Service `merge*`도 함께 검증** — 자식 row id 매칭으로 UPDATE 분기되는 경로. 어댑터 테스트는 `merge*` 메서드 호출 검증으로 작성 (사례: 0e55a28).

사례: 67aa1d4 + 2eefab1 — Non B/L Entry 자식(dim/container/desc) UPDATE 누락 → FE id 포함 + 어댑터 fix.

### 6.29 BE 응답 body 시그니처 변경 시 FE adapter zod 스키마 동시 정합 필수

BE 컨트롤러 응답 body를 축소·확장하면 FE adapter의 zod 파싱 스키마와 `Port` 반환 타입을 같은 시그니처로 정합시켜야 한다. zod `.safeParse`는 누락된 필수 필드(`z.string()`/`z.number()` 등)를 감지해 `ResponseParseError`를 throw하므로, BE만 변경하면 mutation `onSuccess`가 발화되지 않고 이후 라우팅·`setFocus` 분기가 깨진다.

컴포넌트가 `saved.id`만 사용한다는 정보만으로는 안전 판단 불가 — adapter 단의 강제 파싱 layer가 별도로 깨질 수 있다.

**올바른 패턴**:
1. BE 응답 시그니처 변경 PR에 FE adapter zod 스키마 + `Port` 인터페이스 반환 타입 동시 갱신
2. 다른 메서드(`getById`, `update` 등)는 기존 detail 시그니처 유지
3. `mutationFn`이 `isEdit` 분기로 union 반환 타입을 가지면 컴포넌트 호출부가 공통 필드(`id` 등)만 접근하는지 재확인

사례: 7e5e41d — Non B/L `createNonBl` 응답이 `NonBlDetailResponse` → `{id}`로 축소된 후 FE adapter zod 미정합으로 신규 저장 시 `405 Method Not Allowed for GET /api/non-bl` 발생(`saved.id` undefined → 후속 GET URL이 `/api/non-bl`로 됨). `apiResponse(z.object({ id: z.number() }))` + `NonBlPort.create: Promise<{id: number}>`로 fix.

### 6.30 bulk DELETE의 `clearAutomatically=true` 부작용

`@Modifying @Query("delete from ...")` JPQL bulk DELETE에 `clearAutomatically=true`를 켜면 1차 캐시가 **전부** 비워진다. 같은 트랜잭션에서 직후 `deleteById`/`findById`/cascade LAZY fetch가 일어나면 모두 캐시 미스로 추가 SELECT 발사 — 의도와 달리 SELECT 수가 오히려 증가한다.

**올바른 패턴**:
- bulk DELETE 직후 트랜잭션이 종료되거나 다른 read/write가 없는 흐름이면 `flushAutomatically=true`만 사용 (`clearAutomatically` 생략)
- bulk DELETE 대상 엔티티가 부모 cascade 컬렉션에 매핑되어 있지 않으면 stale 위험 없음 (1:1 ext, desc 등)
- bulk DELETE 후 같은 entity를 다시 read해야 하는 경우만 `clearAutomatically=true`

사례: d9f9cd4 — HouseBl ext Repository 5개의 `clearAutomatically=true` 제거 후 NON_BL 삭제 SELECT 10→7회 회복(이전 켰을 때 부모 + container/dim 재SELECT 3건 추가 발생).

### 6.31 부모 삭제 흐름 SSOT — DB CASCADE 금지, ID-only + subquery bulk delete

`DDL_RULES.md §5` — DB 레벨 `ON DELETE CASCADE` 사용 금지. 부모 레코드 삭제 시 자식 레코드는 **애플리케이션 레벨에서 명시적으로 삭제**한다. JPA `cascade=ALL`/`orphanRemoval=true`는 ORM 레벨이므로 허용(save 경로의 `sync*`/`merge*` 의존이 있어 유지).

**삭제 흐름 SSOT (옵션 D)**:

1. Service: `findJobDivById(id)` projection 1회로 jobDiv만 가벼이 조회 (도메인 전체 로드 X)
2. Port: `deleteByIdAndJobDiv(Long id, JobDiv jobDiv)` 호출
3. Adapter: `switch(jobDiv)` 분기마다 `자식 명시 정리 → ext bulk DELETE → 부모 bulk DELETE` 순서로 명시 호출
4. 자식 정리는 `deleteByParentXxxBlId(Long parentId)` subquery bulk DELETE — `where child.parent_id in (select ext.id from ext where ext.parent_id = :parentId)`

**Repository 패턴**:
```java
// 부모 (HouseBlRepository / MasterBlRepository)
@Modifying(flushAutomatically = true)
@Query("delete from HouseBlJpaEntity h where h.houseBlId = :id")
void deleteByIdBulk(@Param("id") Long id);

// 자식 (subquery로 ext 거쳐 parent_id로 끌어옴)
@Modifying(flushAutomatically = true)
@Query("delete from HouseBlSeaContainerJpaEntity c " +
       "where c.sea.houseBlSeaId in (" +
       "  select s.houseBlSeaId from HouseBlSeaJpaEntity s where s.houseBl.houseBlId = :parentId)")
void deleteByParentHouseBlId(@Param("parentId") Long parentId);
```

**금지 사항**:
- DDL의 `ON DELETE CASCADE` (DDL_RULES §5 위반)
- JPA `@org.hibernate.annotations.OnDelete(action=OnDeleteAction.CASCADE)` (DDL을 우회로 강제)
- 삭제 사전 `findEntityById`/`loadWithExt` 호출 (도메인 전체 로드 — ID-only 흐름과 충돌)

**성능 (NON_BL 삭제 기준)**: SELECT 4(loadWithExt) + DELETE 4 → SELECT 0 + DELETE 4. `HouseBlService` 경유 시 jobDiv projection SELECT 1회만 추가.

**자식 jobDiv 알고 있는 service의 단축**: `NonBlService.deleteNonBlById`는 자기 jobDiv를 알고 있으므로 `houseBlPort.deleteByIdAndJobDiv(id, JobDiv.NON_BL)` 직접 호출(HouseBlUseCase·jobDiv projection 모두 우회). SELECT 0회.

**역사적 배경 (무효 패턴 폐기)**:
- 직전 ER 재구조화 Phase 2~4 시점에는 5개 desc/ext 컬렉션에 `@OnDelete(CASCADE)` 어노테이션을 추가해 H2 `ddl-auto=create-drop` 재생성 시 마이그레이션 SQL CASCADE 누락을 회피했다(7b6d0b6).
- 그러나 이는 DDL_RULES §5 위반이며, 본 SSOT(2026-05-11) 적용으로 모든 `@OnDelete` 및 DDL `ON DELETE CASCADE`는 제거됐다. 신규 자식 추가/ext 분리 시에도 `@OnDelete` 사용 금지 — 자식 정리는 어댑터에서 명시 bulk delete로 처리.

### 6.32 ER 재구조화 — 단독 자식 vs 공유 자식 처리 원칙 (SSOT)

자식 테이블 FK를 부모(`house_bl`/`master_bl`)에서 ext(`house_bl_<jobdiv>`)로 이전할 때:

**단독 자식** (한 JobDiv만 사용): 테이블명 유지 + FK 컬럼만 ext PK로 이전
- 예: `house_bl_schedule_leg.house_bl_id` → `house_bl_air_id` (AIR 전용)
- 단순 ALTER TABLE 마이그레이션, JPA 부모 참조 변경만

**공유 자식** (여러 JobDiv 사용): 테이블 자체를 ext별로 분리
- 예: `house_bl_desc` → `house_bl_sea_desc` + `house_bl_air_desc` + `house_bl_truck_desc`
- 도메인 객체는 단일(`HouseBlDesc`), JPA·Repository·mapper만 ext별 분기

**원칙**: 단독은 그대로(이후 공유로 확장될 때 분리), 공유는 즉시 분리. 일관성을 위해 단독 자식까지 강제로 prefix(`house_bl_air_schedule_leg`)를 붙이지는 않음 — 테이블명에 ext 의미가 이미 포함되거나(`air_charge`, `truck_order`) prefix 중복(`air_air_charge`)이 어색한 경우 회피.

**적용 흐름** (Phase 1~4):
- Phase 1 (단독 5개): schedule_leg/air_charge/truck_order × HouseBl/MasterBl — FK만 이전
- Phase 2 (공유 desc): HouseBl 3개(sea/air/truck) + MasterBl 2개(sea/air) — 테이블 분리
- Phase 3 (공유 container): HouseBl 2개(sea/nonbl) — 테이블 분리, MasterBl 미사용
- Phase 4 (공유 dim HouseBl + 단독 MasterBl): HouseBl 3개 분리, MasterBl FK만 이전

### 6.33 HouseBl 영속성 어댑터 — jobDiv별 Strategy 분리 SSOT

`HouseBlPersistenceAdapter`가 SEA/AIR/TRUCK/NON_BL 네 jobDiv를 단일 클래스의 switch/instanceof로 분기 처리하면 16+ Repository와 4개 Mapper가 한 어댑터에 주입되어 책임 경계가 흐려진다. jobDiv별 `HouseBl<JobDiv>PersistenceStrategy`로 추출 + dispatcher 패턴.

**구조**:
```
HouseBlPersistenceAdapter (dispatcher, ~150줄)
 ├─ 공통 부모 처리: HouseBlRepository, HouseBlDomainToJpaMapper
 │  (existsById/getReferenceById/applyCommonFields/save)
 └─ jobDiv별 ext는 strategy 위임
       seaStrategy   : HouseBlSeaPersistenceStrategy
       airStrategy   : HouseBlAirPersistenceStrategy
       truckStrategy : HouseBlTruckPersistenceStrategy
       nonBlStrategy : HouseBlNonBlPersistenceStrategy
```

**Strategy 인터페이스** (`adapter/out/persistence/housebl/strategy/HouseBlPersistenceStrategy.java`, package-private):
```java
interface HouseBlPersistenceStrategy<T extends HouseBl> {
    JobDiv jobDiv();
    T saveExt(T domain, HouseBlJpaEntity savedParent);
    HouseBl loadWithExt(HouseBlJpaEntity parent);
    void deleteExt(Long parentId);
}
```

**원칙**:
- Strategy 인터페이스는 adapter 패키지 내부에만 노출 — Application·도메인 계층은 Strategy 존재를 모름
- 트랜잭션은 `HouseBlPersistenceAdapter`의 `saveHouseBl`/`deleteByIdAndJobDiv`에 `@Transactional`(REQUIRED) 유지, Strategy는 어댑터 트랜잭션 안에서 호출 — `@Transactional` 미부착
- Port 시그니처(`HouseBlPort`) / 호출자(NonBlService, HouseBlService 등) 무변경
- jobDiv별 의존성(자식 Repository, mapper)을 strategy로 분산 — 어댑터에는 공통 3개만

**효과**:
- 어댑터 350+ 줄 → 142줄 dispatcher
- 신규 jobDiv 추가/제거가 단일 strategy 클래스 작업으로 축소
- jobDiv 단위 회귀 테스트 가능 — `HouseBl<JobDiv>PersistenceStrategyTest` 4개로 검증 분리

**다른 도메인 적용 시**:
- MasterBl도 동일 패턴: `MasterBlPersistenceAdapter` + 2개 strategy(`MasterBlSea/Air`) — Master 도메인은 NON_BL/TRUCK 없음
- 통합 테스트 `@Import` 블록에 Strategy 클래스 4개 추가 필수(누락 시 Spring 컨텍스트 로드 실패: `NoSuchBeanDefinitionException`)
- 어댑터 직접 mock 검증 테스트는 dispatcher 동작만 검증으로 축소, Repository 호출 순서 검증은 Strategy 단위 테스트로 이전

사례: a8f04c7(메인 5 신규 + 1 수정) + 2d5ba76(테스트 정합 + Strategy별 단위 테스트 4개 신규). 605 → 605 PASS 회귀 없음.

### 6.34 1차 캐시 hit은 PK 기반 조회만 작동

같은 트랜잭션이라도 hibernate 1차 캐시 hit은 **PK 기반 조회**만 보장. JPQL/Criteria/derived 메서드 쿼리는 **항상 DB까지 SELECT**가 발사된다.

| 호출 | 1차 캐시 |
|---|---|
| `findById(pk)` | hit (SELECT 없음) |
| `getReferenceById(pk)` | hit (프록시, 필드 접근 시 hit 검증) |
| `existsById(pk)` | **미스** (count(*) SELECT 발사) |
| `findByXxxId(fk)` derived | **미스** (JPQL SELECT) |
| `@Query("from ...")` 명시 JPQL/QueryDSL | **미스** |

따라서 같은 엔티티를 2회 fetch해도 두 번째가 자동으로 SELECT 절감되는 게 아니다. **PK로 fetch하거나 attached 참조를 직접 사용**해야 절감 가능.

**적용 함정**: NonBl update 1회 분석 시 `existsById`(count SELECT) + `findByHouseBlHouseBlId`(FK 메서드 쿼리)가 모두 1차 캐시 우회로 발사되는 현상 확인. **§6.35의 attached JPA 직접 매핑 패턴**으로 `saveHouseBl` 우회해야 절감.

사례: 3037a44 분석 — 같은 트랜잭션에서 service가 fetch한 후 adapter가 다시 fetch하던 패턴(SELECT 6건)을 attached 직접 매핑으로 4건까지 축소.

### 6.35 Update 흐름 책임 분리 SSOT — 도메인별 Port + Adapter (Consumer 콜백 안티패턴 폐기)

**원칙**: 도메인별 update는 **도메인 전용 Port + Adapter**로 분리해 service는 1줄 위임. adapter가 jobDiv 검증·factory·attached JPA 직접 매핑까지 일괄 처리한다.

**안티패턴 — Consumer 콜백 trampoline (실측상 위임 표면적)**:
```java
// Port (안티패턴)
void update(Long id, Consumer<HouseBl> mutator);

// Service — instanceof 검증·factory 호출이 여전히 남음
houseBlPort.update(id, existing -> {
    if (!(existing instanceof HouseBlNonBl)) throw new ResourceNotFoundException(...);
    houseBlFactory.applyToEntity(command, existing);
});

// Adapter — saveHouseBl을 내부 호출 → existsById + FK 재조회 그대로 발사
public void update(Long id, Consumer<HouseBl> mutator) {
    HouseBl domain = houseBlRepository.findById(id).map(this::loadWithExt).orElseThrow(...);
    mutator.accept(domain);
    saveHouseBl(domain);  // ← 내부 existsById(SELECT count) + strategy.saveExt(FK 재조회)
}
```

결과: SELECT 6 → 6 (절감 0). Service에 `instanceof` 검증과 `factory.applyToEntity` 호출이 그대로 남아 위임 책임이 사실상 service에 잔존.

**올바른 패턴 — 도메인 전용 Port + Adapter**:
```java
// Service — 진짜 1줄 위임
public void updateNonBl(Long id, UpdateHouseBlCommand command) {
    nonBlPersistencePort.update(id, command);
}

// Port (application/<domain>/port/out/) — Command를 받음
public interface NonBlPersistencePort {
    void update(Long id, UpdateHouseBlCommand command);
}

// Adapter (adapter/out/persistence/<domain>/)
@Component
@Transactional
public class NonBlUpdatePersistenceAdapter implements NonBlPersistencePort {
    public void update(Long id, UpdateHouseBlCommand command) {
        // 1) parent PK fetch + jobDiv 검증
        HouseBlJpaEntity parentJpa = houseBlRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));
        if (parentJpa.getJobDiv() != JobDiv.NON_BL) {
            throw new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND);
        }

        // 2) ext fetch (FK 쿼리, attached 보관)
        HouseBlNonBlJpaEntity nonBlJpa = houseBlNonBlRepository.findByHouseBlHouseBlId(id)
            .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));

        // 3) 도메인 변환 (자식 컬렉션 LAZY 트리거)
        HouseBlNonBl domain = jpaToDomainMapper.toNonBlDomain(parentJpa, nonBlJpa);

        // 4) factory로 command 적용 (adapter→application/factory 의존 — 실용적 헥사고널 완화)
        houseBlFactory.applyToEntity(command, domain);

        // 5) attached JPA에 직접 매핑 — saveHouseBl 미호출
        domainToJpaMapper.applyCommonFields(domain, parentJpa);
        domainToJpaMapper.applyNonBlFields(domain, nonBlJpa);

        // 6) 자식 collection은 attached JPA에 merge — LAZY 재트리거 없음(loadWithExt에서 이미 fetch)
        nonBlJpa.mergeContainers(domain.getContainers().stream().map(...).toList());
        nonBlJpa.mergeDims(domain.getDims().stream().map(...).toList());

        // dirty checking이 트랜잭션 종료 시 UPDATE 자동 발사
    }
}
```

**SELECT 절감 (NonBl update 기준)**:
| # | Consumer 패턴 | Port+Adapter 패턴 |
|---|---|---|
| 1 | SELECT house_bl (PK) | SELECT house_bl (PK) |
| 2 | SELECT house_bl_non_bl (FK) | SELECT house_bl_non_bl (FK) |
| 3 | SELECT container | SELECT container |
| 4 | SELECT dim | SELECT dim |
| 5 | SELECT count(*) (saveHouseBl 내 existsById) | **제거** |
| 6 | SELECT house_bl_non_bl (strategy.saveExt FK 재조회) | **제거** |
| **합계** | **6** | **4** |

**컨트롤러 응답 void화 함께 적용**:
FE가 PUT 응답을 받고 `invalidateQueries` + GET refetch 패턴이면 응답 본문은 무용. `ApiResponse<DetailResponse>` → `ApiResponse<Void>` 축소.
- Backend: `UseCase.updateXxx` 반환 `void`, Controller 응답 `ApiResponse<Void>`, Assembler `toDetail` 호출 제거
- FE: `xxxPort.update`/api client 반환 `Promise<void>`, zod schema 검증 제거
- create/update mutation 분리(반환 union 회피) — `useMutation` 제네릭 불일치 회피용 패턴(§6.29 참고)

**다른 도메인 적용 시 체크리스트**:
1. `<Domain>PersistencePort` 신설 — `application/<domain>/port/out/`. Command 직접 받음.
2. `<Domain>UpdatePersistenceAdapter` 신설 — `adapter/out/persistence/<domain>/`. 명명 충돌 회피 위해 `Update` 접미사 사용(기존 `xxxPersistenceAdapter`가 Search 등 다른 책임을 가질 때).
3. Service의 `update<X>` 본문은 `<x>PersistencePort.update(id, command);` 1줄
4. UseCase 반환 `void`로 변경 + Controller 응답 `ApiResponse<Void>`
5. FE adapter `Promise<void>` 정합 — `xxxPort` 인터페이스 + api client + 응답 schema 검증 제거
6. 기존 `HouseBlPort.saveHouseBl`은 다른 호출자(create 등) 호환 위해 유지 — update만 새 Port로 전환
7. 테스트:
   - 신규 Adapter 단위 테스트: happy path(factory·applyCommonFields·applyNonBlFields·mergeContainers·mergeDims 호출 검증), notFound, wrong jobDiv
   - Adapter 테스트의 mock stub은 실제 호출 경로와 정확히 일치(Mockito strict mode `UnnecessaryStubbingException` 회피)
   - Command 레코드 파라미터 수 정확히 일치(인수 누락 시 컴파일 실패)
8. p6spy 실측으로 SELECT 절감 확인

**리스크 — adapter→application/factory 의존**:
정통 헥사고널은 adapter→application 의존 금지지만, factory가 stateless command→도메인 mutator 호출 헬퍼라 실용적으로 허용. 의존 회피하려면 service에서 command→fields 변환 후 fields만 port에 전달하는 패턴(분량 더 큼).

사례: 3037a44(메인 5) + b0a92ad·8b70969(테스트 fix). 613 PASS 회귀 없음. NonBl update SELECT 6→4.

**Truck B/L 적용 완료 (2026-05-12, 8c54dd9·d5bbc99·61f5f87)** — `house_bl_truck_dim` / `house_bl_truck_order` 가 매 update마다 신규 INSERT N건 + 기존 DELETE N건을 발사하던 `syncDims`/`syncTruckOrders`(clear+addAll) 패턴을 `mergeDims`/`mergeTruckOrders`(id 기반 매칭)로 전환. Truck 전용 sub-set 매퍼(`applyTruckCommonFields`/`applyTruckBlFields`) 동반 신설로 form 미보유 필드 NULL 덮어쓰기 차단. UpdateHouseBlCommand.TruckOrderCommand 에 `Long id` 추가 + TruckBlAssembler `toTruckOrderCommandsU` 가 row id 전달. CREATE 경로 호출처 호환 위해 기존 `syncDims`/`syncTruckOrders` 메서드는 유지.

**회귀 회고 (2026-05-12, ed24829 → 8c54dd9)** — Truck B/L Dimension 그리드 신규 도입(commit 6a31ae6) 시 본 §6.35 + §6.28 + §6.37 reference가 누락된 채 NonBl 이전의 sync(clear+addAll) 패턴을 그대로 차용해 매 update마다 자식 그리드 전체 교체 + 부모 NULL 덮어쓰기가 발사됨. **신규 도메인에 child grid를 추가할 때는 본 §6.35(mergeXxx) + §6.28(자식 row id PUT 페이로드 포함) + §6.37(sub-set Request DTO/매퍼) 세 가이드를 동반 적용 의무.** Coder 작업 단위 지시에 항상 본 절을 reference 로 포함할 것.

**후속 정규화 (2026-05-12 후속)** — sub-set 매퍼 도입(8c54dd9) + address 4필드 보강(41205f8) 이후에도 무수정 조회→저장이 여전히 `house_bl` + `house_bl_truck` 두 테이블에 UPDATE를 발사하는 케이스 발견. 원인은 **디폴트 주입 비대칭** 두 군데. (1) FE `use-truck-bl-entry.ts`의 `dimensionDivisor: detail.volumeDivisor ?? "CM6000"` — DB NULL → form "CM6000" 정규화 → payload "CM6000" → entity dirty. (2) BE `applyTruckBlFields`의 `setVesselName(vv != null ? vv.vesselName() : "TRUCK")` — form 미보유인데 vv null일 때 "TRUCK" 강제 set → DB NULL → "TRUCK" dirty. 해결: FE는 `?? ""`로 디폴트 주입 제거(정규화 정렬), BE는 vv null이면 vesselName/voyageNo setter skip(sub-set). 신규 INSERT 경로(`applyTruckFields`)는 그대로 "TRUCK" 고정값 유지 — Update 경로 전용 분기.

**HouseBl Sea/Air · MasterBl · SwitchBl 적용 권장** — 본 SSOT를 그대로 따라 별도 작업으로 진행.

### 6.36 Entry detail useQuery — 화면 재진입 시 자동 refetch 차단

Entry 상세 `useQuery`는 옵션 미지정 시 전역 `QueryClient` 기본값(`staleTime: 30_000`, `refetchOnMount: true`)으로 동작한다. 결과: Entry → 다른 화면 → 30초 후 Entry 재진입 시 **mount 자동 refetch 발생**.

§6.21(mutation 후 List 자동 invalidate 금지)과 동일한 원칙 — **사용자 명시 트리거(Search 버튼 / mutation invalidate) 외에는 백엔드 호출하지 않는다** — 의 detail 측 보완 항목.

**추가 함정 — `gcTime` 동반 명시 필수 (2026-05-12 확인)**: `staleTime: Infinity`만으로는 **`gcTime` 기본 5분(React Query v5 디폴트)** 에 막혀 무력화된다. List → Entry 이동 후 5분 경과 시 inactive 쿼리가 가비지 컬렉트되어 캐시 entry 자체가 사라지고, 재진입 시 `queryFn` 재실행 → 백엔드 재조회. `staleTime`(fresh 유지 시간)과 `gcTime`(비활성 캐시 보존 시간)은 별개 개념이므로 **둘 다 `Infinity`로 명시**해야 "사용자 명시 트리거 외 백엔드 호출 0" 정책이 성립.

```ts
// ❌ 기본 동작 — 30초 stale 후 mount 시 자동 refetch
useQuery({
  queryKey: ['non-bl', 'detail', id],
  queryFn: () => fetchDetail(id),
  enabled: id != null,
});

// ❌ staleTime만 Infinity — gcTime 기본 5분에 막혀 자리 비움 후 재조회 발생
useQuery({
  queryKey: ['non-bl', 'detail', id],
  queryFn: () => fetchDetail(id),
  enabled: id != null,
  staleTime: Infinity,
  refetchOnMount: false,
});

// ✅ Entry 화면 정책 — staleTime + gcTime 모두 Infinity, 명시 invalidate에만 재조회
useQuery({
  queryKey: ['non-bl', 'detail', id],
  queryFn: () => fetchDetail(id),
  enabled: id != null,
  staleTime: Infinity,
  gcTime: Infinity,
  refetchOnMount: false,
  structuralSharing: false,
});
```

**mutation invalidate는 정상 동작**: `invalidateQueries`는 active query를 즉시 stale 표시 + refetch 트리거하므로 `staleTime: Infinity`/`gcTime: Infinity`라도 create/update/delete 직후 detail 재조회는 그대로 작동. 차단되는 것은 **stale time 만료 + mount** 및 **gcTime 만료로 인한 캐시 소멸 후 재조회** 조합으로 발생하는 비명시 refetch뿐이다.

**전역 설정과의 관계**: `QueryProvider`의 `refetchOnWindowFocus: false`는 이미 적용되어 있으나 `refetchOnMount`/`staleTime`/`gcTime`은 글로벌이 30초/true/5분이다. 글로벌 변경 시 List/그리드 등 다른 화면 영향이 크므로 **화면 단위(Entry detail useQuery / Grid list useQuery)에서 옵션 명시**로 한정.

⚠️ **글로벌 `gcTime: Infinity` 절대 금지**: `QueryProvider` defaultOptions에 적용 시 검색 결과·enum·일회성 조회 등 **모든 inactive 쿼리가 영원히 GC되지 않아 메모리 누수**. 캐시 유지가 의도된 화면(Entry detail / Grid list)에만 화면 단위로 적용.

**List `gcTime: Infinity`의 부작용 — 검색 조건 다양화 시 무한 누적**: `gcTime`은 "시간 기반" 만료라 사용자가 검색 조건을 다양하게 바꿔 빠르게 돌리면 시간으로는 못 잡는다. queryKey가 매번 달라지면(예: `[domain, "list", extraFilter1]` → `[..., extraFilter2]` → `[..., extraFilter3]` ...) 이전 entry는 inactive로 전환만 될 뿐 영원히 메모리 잔존. 보완책: **건수 기반 LRU 큐** — `query-provider.tsx`의 `QueryCache.subscribe`에서 화면별 list inactive entry를 5개로 상한. 활성 entry는 카운트 제외, detail은 미적용(BL ID당 1개라 누적 제한).

```ts
// query-provider.tsx — 글로벌 1곳에서 처리, list-client 수정 불필요
const queryCache = client.getQueryCache();
queryCache.subscribe((event) => {
  if (event.type !== 'added' && event.type !== 'observerRemoved') return;
  // 화면별 그룹핑 — [domain, "list", variantKey?] 기준 (variantKey가 string이면 화면 식별자에 포함, 객체면 미포함)
  const groups = new Map<string, { queryKey: unknown[]; updatedAt: number }[]>();
  queryCache.findAll({
    predicate: (q) =>
      Array.isArray(q.queryKey) &&
      q.queryKey[1] === 'list' &&
      q.getObserversCount() === 0,
  }).forEach((q) => {
    const qk = q.queryKey as unknown[];
    const key = [qk[0], qk[1], typeof qk[2] === 'string' ? qk[2] : ''].join('::');
    const arr = groups.get(key) ?? [];
    arr.push({ queryKey: qk, updatedAt: q.state.dataUpdatedAt });
    groups.set(key, arr);
  });
  for (const arr of groups.values()) {
    if (arr.length <= 5) continue;
    arr.sort((a, b) => a.updatedAt - b.updatedAt)
      .slice(0, arr.length - 5)
      .forEach((entry) => queryCache.remove(queryCache.find({ queryKey: entry.queryKey })!));
  }
});
```

**LRU 정책 정량 효과**:
- 활성 화면(현재 grid) → observer ≥ 1 → 카운트 제외, 영향 0
- 같은 화면 inactive entry 5개까지 보존 → 5 × 50KB ≈ 250KB / 화면
- 11~15 화면(variant 포함) × 250KB ≈ **상한 ~3MB로 고정**
- showAll 분기도 자동 LRU 대상 — 5MB × 5 = 25MB 화면당 (단일 화면이라 총량은 동일)

**Trade-off**: 같은 검색 조건에서 페이지만 6+ 넘게 이동 후 첫 페이지 복귀 시 첫 페이지 entry가 밀려나 백엔드 1회 호출. 일반 작업 단위(50 rows × 5 페이지 = 250 rows)에서 실용상 문제 없음.

사례: 2026-05-12 — 사용자 보고 "검색 조건 다양화 시 누적 위험" 분석에서 시작된 후속 정렬.

**그리드(List)와의 분업**:
| 위치 | 옵션 | 근거 |
|---|---|---|
| `<Domain>Grid` (List) | `staleTime: Infinity`, `gcTime: Infinity`, `refetchOnMount: false` | Search 버튼 명시 트리거만 |
| `<Domain>Entry` (Detail) | `staleTime: Infinity`, `gcTime: Infinity`, `refetchOnMount: false` | mutation invalidate만 |

사례:
- 0d5d492 — NonBlEntry detail useQuery에 `staleTime`/`refetchOnMount` 옵션 추가(3 insertions)
- 2026-05-12 — 8개 파일(6 grid + 2 entry 훅) `gcTime: Infinity` 동반 추가. 15분 자리 비움 → list→entry 재진입 시 백엔드 재조회 발생 보고로 시작된 후속 정렬.

**다른 도메인 적용 권장**: Master B/L `<MasterBlEntry>`, House B/L `<HouseBlEntry>`도 동일 패턴(현재 둘 다 옵션 미지정 → 30초 후 mount 자동 refetch 발생). 별도 작업으로 일괄 정렬 — 신규 마이그레이션 시 `staleTime`/`gcTime` 둘 다 Infinity로 동반 명시.

### 6.37 Entry sub-set 화면 — Request DTO·매퍼에서 form 미매핑 필드 sub-set화 필수

화면 form schema가 entity 일부만 다룰 때, 백엔드 Request DTO가 entity 전체 필드를 받으면 **프론트가 안 보내는 필드는 null로 도착 → 도메인/JPA에 null set → DB 기존 값을 null로 덮어씀(데이터 손실)**.

증상: 무수정 저장인데 SQL UPDATE 발생 + 일부 컬럼이 NULL로 덮어써짐. 첫 저장 후 idempotent(두 번째부터 dirty 없음 — 이미 정규화됐기 때문).

```java
// ❌ entity 전체 필드를 받는 Request DTO
public record UpdateXxxRequest(
    String shipperCode, String shipperAddress,  // form엔 코드만 → address null 도착
    String incoterms, String mblNo, ...
) {}

// ✅ 화면 전용 sub-set
public record UpdateXxxRequest(String shipperCode, ...) {}
```

매퍼 단계 분기:

| 매퍼 위치 | 처리 |
|---|---|
| **화면 전용 매퍼/엔티티** (예: `HouseBlNonBlJpaEntity.copyContainerFields`) | 미사용 setter **직접 제거** → DB 기존 값 유지 |
| **공유 매퍼** (예: `HouseBlDomainToJpaMapper.applyCommonFields` — House B/L/Master B/L과 공유) | 공통 setter 제거 금지(타 화면 회귀). **화면 전용 메서드 신설** (예: `applyNonBlCommonFields`) — 공통과 동일하되 미매핑 setter 제외. 어댑터에서 호출 교체 |

**위험 신호**: `setShipperAddress(mapOrNull(domain.getShipperCode(), CustomerCode::address))` 같은 auto-derive setter. `CustomerCode`는 단순 record `(value, address)` — lookup 없이 코드만 들어온 VO는 address가 null → 위 데이터 손실 경로 직격. House B/L/Master B/L은 form에서 address를 명시 받아 정상 동작하지만, NonBl 같이 form에 address 없는 화면은 setter가 그대로 호출되면 데이터 손실.

**점검 순서**:
1. detail 응답·Request DTO와 form schema mismatch 필드 식별
2. 백엔드: Detail/Request/Projection/Command 인자/Assembler 매핑에서 제거(공유 Command 시그니처가 막히면 null 고정)
3. 매퍼: 화면 전용이면 setter 제거 / 공유면 신규 화면 전용 메서드 분기 + 어댑터 호출 교체
4. 프론트: zod schema + 도메인 타입 sub-set 동기화(§6.29 참조 — 응답 schema mismatch 방지)
5. 회귀: 두 번째 저장 UPDATE 미발생 + 다른 화면(공유 매퍼 사용 시) 정상 동작

**유사 증상 분리**: `?? "CM6000"` 디폴트 주입, `trim()` 정규화 등 fetch/payload 비대칭도 첫 저장 dirty + 이후 idempotent 패턴이 동일. **반드시 백엔드 SQL log로 dirty 컬럼 특정 후** sub-set화 vs 정규화 정렬 분기 판단.

사례: f15736e — NonBl Entry 컨테이너 8필드(`lengthFeet, sealNo4-6, netWeightKg, vgmKg, isSoc, seq`) + 메인 11필드(`jobDiv, shipmentType, freightTerm, docPartner*, address×3, incoterms, mbl*×3`) sub-set화 + `applyNonBlCommonFields` 신설 (12 files, 75+/177-).

**다른 도메인 적용 권장**: Master B/L, Truck B/L 등 form에서 address 명시 매핑이 빠진 화면 점검. 같은 데이터 손실 패턴 발생 가능.

### 6.47 List → Entry 진입 시 detail 쿼리 invalidate + draft clear 필수 (SSOT)

§6.36(Entry detail useQuery `staleTime: Infinity, refetchOnMount: false`) + §6.18 갱신(unmount cleanup 제거)의 결합 효과로 **같은 BL 재진입 시 stale 캐시 + 잔존 draft가 화면에 표시**될 수 있다. 방어책: list 더블클릭과 handleSearch에서 setFocus 직전에 `invalidateQueries` + `clearDraft`를 명시 호출.

```tsx
// list grid 더블클릭 핸들러 (4 도메인 동일 패턴)
onDoubleClick={() => {
  sessionStorage.setItem(`<domain>-entry:hot:${row.id}`, "1");
  queryClient.invalidateQueries({ queryKey: ["<domain>", "detail", row.id] });
  clearDraft(`<draftKey-pattern>(row.id)`);
  setFocus(entryFocusKeys.<domain>, row.id);
  addTab(...); router.push(...);
}}
```

```ts
// handleSearch — 같은 id / 다른 id 분기 모두 적용
if (targetId === id) {
  queryClient.invalidateQueries({ queryKey: ["<domain>", "detail", id] });
  clearDraft(<draftKey>(id));
  detailLoadedRef.current = false;
} else {
  queryClient.invalidateQueries({ queryKey: ["<domain>", "detail", targetId] });
  clearDraft(<draftKey>(targetId));
  setFocus(<domain>, targetId);
}
```

도메인별 키 패턴(이번 세션 적용 기준):

| 도메인 | queryKey | draftKey |
|---|---|---|
| Truck B/L | `["truck-bl", "detail", id]` | `` `truck::${id}` `` |
| Non B/L | `["non-bl", "detail", id]` | `` `non::${id}` `` |
| House B/L | `["house-bl", "detail", id]` | `` `house:${variantKey}:${id}` `` |
| Master B/L | `["master-bl", "detail", id]` | `` `master:${variantKey}:${id}` `` |

House/Master B/L은 variant가 draftKey에 포함됨. grid는 prop(`variantKey`), search 훅은 인수(`variant.key` 또는 동등 필드)로 획득.

의도:
- 다른 사용자가 DB 수정한 케이스 → list 더블클릭마다 fresh fetch
- 본인이 entry에서 작업하고 list로 갔다가 같은 BL 재진입 → 저장 안 한 draft 제거

cleanup useEffect로 unmount 시 일괄 제거(§6.18 안티패턴)하는 방식 대신, **진입 시점에 명시 제거 + 메뉴 이동은 보존**의 분리 정책. 신규 도메인 entry 마이그레이션 시 list grid + handleSearch 두 진입 경로 모두 적용.

사례: bab177f — Truck/Non/House/Master 4 도메인 list grid 더블클릭 + search 훅 동시 적용.
