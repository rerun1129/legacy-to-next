# Phase 1 — Plan/Setup

> **언제 read**: 새 도메인 Entry 마이그레이션의 plan 단계에서 작업 범위 결정 시.

본 phase는 **Plan 단계에서 매번 봐야 하는 의무 체크리스트**를 모은다. §6.48 / §6.49 매트릭스는 신규 도메인 마이그레이션 시 누락하면 회귀가 거의 확실하므로 plan 작성 전 반드시 read.

---

## 6.11 Coder가 plan 모드 진입 시 메인에 보고 의무

서브 에이전트가 자체 plan mode에 진입하면 메인 에이전트는 즉시 사용자에게 알려야 함 (재호출 루프 금지). 메모리 규칙.

---

## 6.48 ⚠️ 신규 도메인 마이그레이션 의무 체크리스트 — Non B/L 발견 패턴이 Truck B/L에서 재발한 케이스

> ## ⚠️ 회귀 위험 핫스팟 (READ FIRST)
>
> 2026-05 Truck B/L 마이그레이션 과정에서 **Non B/L에서 이미 발견·수정된 6종의 동일 패턴이 그대로 재발**했다. 신규 B/L 도메인(현 미진행분 — Sea Master / Air Master / 잔여 Sea/Air House variant 등) 추가 시 **본 체크리스트를 거치지 않으면 같은 회귀가 거의 확실**하다. Plan 단계에서 본 절 reference 의무, Coder 작업 단위 지시에 본 절 포함 의무.

### 재발 패턴 매트릭스 (Non B/L → Truck B/L)

| # | 패턴 | Non B/L 해결 | Truck 재발 → 수정 | 가이드 절 |
|---|---|---|---|---|
| ① | form 미보유 필드 sub-set 매퍼 | `applyNonBlCommonFields` (f15736e) | `applyTruckCommonFields`/`applyTruckBlFields` (8c54dd9) | §6.37 |
| ② | child grid merge-by-id (clear+addAll 금지) | `mergeContainers`/`mergeDims` (3037a44) | `mergeTruckOrders`/`mergeDims` (8c54dd9) | §6.35, §6.28 |
| ③ | address 4필드 풀스택(FE submit · BE Request · Command · Mapper) | NonBl address 정합 완료 | Truck address 4필드 풀스택 보강 (41205f8) | §6.37 |
| ④ | 디폴트 주입 비대칭(`?? "CM6000"`, vv null→"TRUCK") | NonBl `volumeDivisor` 정규화 정렬 | Truck `dimensionDivisor`/`vesselName` 정규화 (23852d8) | §6.37 유사 증상 |
| ⑤ | enum detail 매핑 — `getCode()` vs `Enum::name` 정합 | `NonBlDetailResult.java:136` `getCode` | `TruckBlFactory.java:126` `Enum::name` → `getCode` (e32816f) | §6.45 |
| ⑥ | 공유 VO — code 없이 address만 저장 (legacy free-text) | (미발견) | `CustomerCode` invariant 완화 (18f767f) — 모든 도메인 자동 적용 | 신설 (본 절) |

추가: Non B/L 비교 없이 Truck B/L 단독으로 발견된 케이스도 본 절에 포함해 차기 도메인에 반영.

| ⑦ | `onSave` 핸들러 `!isEdit` 차단 — 잘못된 가드 | (Non B/L 동등 코드 무) | `truck-bl-entry.tsx` 가드 3줄 제거 (eff46c4) | 본 절 |
| ⑧ | Detail Response 필드 노출 누락 + FE schema/form.reset 동기 누락 | NonBl "body 콤보박스 4종 + Ref.No"(2026-05-09) — §6.37 사례 인용 | Truck Party address/docPartner/vesselName BE response 보강(`ff0ff68`) + FE 응답스키마·도메인·form.reset 7필드 바인딩(`519177c`) | §6.29, §6.37 |
| ②-보강 | 자식 collection 영속 분기 안전성 | NonBl 자식 UPDATE 쿼리 누락 수정(`67aa1d4`) | Truck dim detached entity persist 에러(`ed24829`) + 부모 어댑터 분기(`a8f04c7`) | §6.35, §6.33 |

> 본 매트릭스는 **최소 식별 집합**이다. Truck B/L 마이그레이션 전체 commit log(약 60+ commits, `git log --grep=truck -i`)를 grep해 본 결과 위 8 + 1패턴 외에도 ComboBox cell variant(`d0dcf1b`, §6.46 신설 계기)·draft sync 분기(`56f8f1d`, §6.18 갱신 계기)·outside-click(`5f48185`, §6.40 신설 계기) 등 **Truck 작업 중 신설된 가이드 절이 다수** 존재한다. 이들은 Non B/L 시점엔 미발견된 신규 패턴이라 "재발"은 아니지만, **차기 도메인에서는 동일 위치에서 재발할 가능성**이 있으므로 본 절의 "점검 항목"으로 동등 취급. 신규 도메인 작업자는 본 매트릭스 외에 §6.18·§6.40·§6.45·§6.46·§6.47까지 함께 점검 의무.

### 신규 도메인 추가 시 의무 점검 8항목

1. **§6.37 sub-set 매퍼** — form 미보유 필드 setter 호출 제거. `apply<Domain>CommonFields` + `apply<Domain>Fields` 신설. 공통 `applyCommonFields` 직접 호출 금지. 검증: 무수정 조회→저장 시 UPDATE 미발사.
2. **§6.37 정규화 정렬** — fetch 응답·payload·DB 디폴트 일관성. `?? "default"` 디폴트 주입은 `createEmpty<Domain>FormValues()`에만 한정, `form.reset(detail)` 분기에서는 빈 값 유지. 검증: legacy NULL row 무수정 저장 시 UPDATE 미발사.
3. **§6.35 child grid merge-by-id** — sync(clear+addAll) 패턴 금지. 자식 row id를 PUT 페이로드(§6.28) · `UpdateRequest` DTO · `Update<Domain>Command` record · Assembler `to<Child>CommandsU` · `merge<Children>(...)` 어댑터 호출 5단계 모두 적용. 검증: 자식 그리드 단일 행 수정 시 해당 행만 UPDATE.
4. **§6.45 enum 매핑 양방향 정합** — BE detail 응답이 enum→String 변환할 때 반드시 `EnumRegistryFactory` 등록과 같은 변환기 사용. `e -> new EnumOption(e.getCode(), ...)` 등록은 detail에도 `<Enum>::getCode`, `e -> new EnumOption(e.name(), ...)` 등록은 detail에도 `Enum::name`. **Non B/L 동등 코드(`NonBlDetailResult` ↔ `<Domain>BlFactory.toXxxView`)와 라인 단위 1:1 비교 의무.** 검증: 조회→저장 round-trip에서 `IllegalArgumentException` 미발생.
5. **address 4필드 풀스택** — `shipperAddress`/`consigneeAddress`/`notifyAddress`/`docPartnerAddress` 모두 (a) FE submit 빌더 (b) `Create<Domain>BlRequest`/`Update<Domain>BlRequest` (c) `Create/UpdateHouseBlCommand` 인자 위치 (d) Assembler 매핑 (e) factory `entity.assignParties(CustomerCode.of(code, addr), ...)` (f) `HouseBlDomainToJpaMapper` setter 호출 6단계 일관 점검. 어느 한 단계라도 빠지면 address 손실.
6. **공유 VO 동작 가정 점검** — `CustomerCode.of(code, address)`는 **code blank이어도 address가 있으면 VO 생성** (legacy free-text address 지원). VO `.value()`/`.address()` 사용처에서 null 체크 의무. 신규 VO 추가 시 동일 invariant 적용. 매퍼는 `mapOrNull(domain.getXxxCode(), CustomerCode::value/::address)` null-safe 패턴 사용.
7. **`onSave` 핸들러 가드 금지** — `<Domain>BLEntry.tsx`의 `onSave={() => { if (!entry.isEdit) { toast.info(...); return; } ... }}` 패턴 금지. `handleSubmit` 내부의 `isEdit` 분기가 create/update mutation 발사를 책임지므로 onClick에 별도 차단 가드는 잘못된 패턴(신규 작성 모드 차단). 동등 패턴 `handleChangeBlNo`(B/L 번호 변경 — 신규엔 endpoint 없음)는 그대로 유효 — 혼동 주의.
8. **Detail Response ↔ FE form.reset 3축 동기** — BE `<Domain>DetailResponse` record · FE 도메인 응답 타입(`*Detail` 인터페이스) · FE `form.reset(detail)` 매핑 — 세 곳이 정확히 같은 필드 집합을 가져야 한다. 한 축이라도 누락되면 detail에서 NULL → form 빈 값 → 저장 시 BE 매퍼가 null로 덮어쓰기(데이터 손실). 점검: form schema의 모든 키가 `form.reset(detail)`에서 detail 필드와 매핑되는지 1:1로 확인. §6.29(zod schema 동시 정합)와 함께 적용.

### Plan/Coder 지시 의무

- **Plan 단계**: plan 본문 첫 줄에 `본 가이드 §6.48 체크리스트를 따른다.` 명시.
- **Coder 작업 단위 지시**: 각 변경 단위마다 위 7항목 중 해당 절 reference를 인용. 완료 보고에 `Non B/L 동등 코드와 1:1 비교 결과: 일치/차이 N건` 포함을 요구.
- **검토 단계**: 신규 도메인 INSERT 1회 + UPDATE 무수정 1회 + 자식 그리드 1행 수정 1회 + enum 옵션 round-trip 1회 — 4종 회귀 시나리오를 p6spy 로그로 검증.

### 사례

- **2026-05-12 본 세션** — Non B/L에서 이미 해결된 패턴 ④⑤⑥과 단독 패턴 ⑦이 Truck B/L에서 재발 확인. 본 절 신설.
- **2026-05-12 회귀 회고(§6.35)** — Truck B/L Dimension 그리드 도입(6a31ae6) 시 §6.35 + §6.28 + §6.37 reference 누락으로 ①②③ 동시 회귀. 본 절은 §6.35 회귀 회고의 일반화·체크리스트화.
- **2026-05-12 보강** — 사용자 지적("저것들만 재발한게 아닐텐데 git log 제대로 확인해서 작성한거 맞아?")에 따라 Truck B/L 마이그레이션 commit log 전체(`git log --grep=truck -i`, 약 60+ commits) 재점검. ⑧(detail response 필드 노출 ↔ FE schema/form.reset 동기 — `ff0ff68`+`519177c`) 추가, ②-보강(자식 collection 영속 분기 — `ed24829`) 사례 추가. 본 절은 살아있는 문서 — 차기 도메인에서 새 재발 패턴 발견 시 본 매트릭스 보강 의무.

---

## 6.49 🚨 FE 카탈로그 마이그레이션 사전 점검 매트릭스 — Sea House 회귀 회고 (READ FIRST)

> # 🚨🚨🚨 STOP — 마이그레이션 시작 전 반드시 읽고 체크 의무 🚨🚨🚨
>
> §6.48이 풀스택 BE+FE 회귀 패턴이라면, **본 절은 FE 카탈로그 마이그레이션 작업 범위 책정 단계에서 빠뜨리기 쉬운 7가지 사전 점검 항목**이다. Sea House B/L 마이그레이션(2026-05-13)에서 Round 1 작업 단위 5의 범위를 좁게 잡은 결과, **HOUSE_BL_SEA_REGISTRY 9개 패널 중 5개 누락 + toolbar 영역 미점검 + 카탈로그 Button 컴포넌트 미적용 + rowKey 안티패턴 잔존**이 사용자 검수 단계에서 한꺼번에 회귀로 드러나 Round 2(단위 7~10)로 재작업했다. 가이드 본문에 SSOT가 있었으나 **plan 단계에서 점검 의무로 명시되지 않아 답습**된 사례 — Plan/Coder 작업 단위 지시 시 본 절을 reference로 인용 의무.

### 🔴 7대 누락 패턴 매트릭스

| # | 누락 패턴 | 회귀 결과 | SSOT 위치 | **사전 점검 의무 (plan 단계)** |
|---|---|---|---|---|
| ① | **Registry 등록 패널 전수 점검** | Sea House: schedule/document/cargo/marks/remark 5개 패널 미점검 → native input 8건+ 잔존 | `main-<jobDiv>.tsx`의 `HOUSE_BL_<JobDiv>_REGISTRY` 배열 | Registry 배열의 **모든 entry를 grep 후 read** 의무. 작업 범위에 N/9 식으로 명시. 본 세션 Round 1: 6/9 → Round 2: 9/9. |
| ② | **toolbar 영역 점검** | `<Domain>Entry.tsx`의 `.toolbar` map 콜백이 패널 디렉터리 밖이라 사각지대. Sea House: Load Type/Service Term/B/L Type native input 3건 | `<Domain>Entry.tsx`의 `TOOLBAR_LABEL_TO_FIELD` 매핑 | `toolbar` map + `.page-head__actions` + `.page-head__meta` 영역 grep 의무. native `<input>`/`<select>`/`<button>` 잔존 식별. |
| ③ | **page-head Button 컴포넌트 SSOT** | Save/Search/Delete/Copy/Export/Print/Switch/New 등 page-head 9개 native `<button className="btn ...">` 잔존 | `@/components/shared/button` + `(dev)/preview/sections/buttons/_bundles-data.ts`·`_actions-data.ts` | 카탈로그 `_bundles-data.ts`에서 페이지별 액션 묶음 견본의 `initialVariant` 라인 단위 인용 의무. **추측 매핑 금지**. |
| ④ | **Button variant 카탈로그 매핑** | `btn--success`/`btn--secondary`/`btn--primary` 같은 임의 클래스 → 카탈로그 variant(default/danger/search/transaction/normal)와 1:1 매핑 누락 | 동일 (`_bundles-data.ts`) | 임의 클래스 발견 시 카탈로그 SSOT에 대응 variant 있는지 grep. 없으면 SSOT 확장 또는 사용자 의사결정 요청. |
| ⑤ | **rowKey 인덱스 안티패턴** (§6.41-보강) | `(_, i) => fields[i].id` 패턴 → RHF `append` 직후 `fields[i]` undefined → `Cannot read properties of undefined (reading 'id')` throw | §6.41-보강 + Truck `truck-order-grid-panel:105-107` 모범 | 모든 그리드 패널에서 `rowKey={(_, i)` / `onRowClick={(_, i)` / `rowClassName={(_, i)` 패턴 grep 의무. 발견 시 `(r) => String(r.id)` 정정. |
| ⑥ | **detail mapping ↔ UI 라벨 ↔ BE field ↔ BE enum 4축 정합** | Sea House: toolbar UI "Load Type" → `lType` 필드 → ComboBox는 `LoadType` enum, 그러나 `form.reset(detail)`은 `lType: detail.blType` → BE/UI 미스매치 | (본 절 신설) | toolbar/패널 라벨, form schema 키, BE detail 필드, EnumRegistry 등록 4축을 표로 정렬해 점검. 어느 한 축이라도 불일치 시 product 의문 보고. |
| ⑦ | **dev server 캐시 stale로 인한 검증 실패** | fix 후 사용자가 화면에서 "그대로"라고 보고 → 실제로는 dev `.next` 캐시 stale | (본 절 신설) | fix 후 사용자 검증 안내에 **`Remove-Item -Recurse -Force front-end\.next` + dev server 재시작** 명시 의무. |
| ⑧ | **BE detail 응답 ↔ FE form.reset nested 매핑 정합 (Read flow critical)** | House B/L 후속: form schema에 `seaDetail.*` 28개 키 정의되어 있고 panel 컴포넌트들이 그 키를 `register`로 참조 중이었으나, **BE `HouseBlDetailResponse`에 SEA 본체 nested 필드는 `loadType`/`blType`/`remark` 3개만 노출** + **`house-bl-entry.tsx` `form.reset` 객체에 `seaDetail: {...}` block 자체가 누락** → list 더블클릭 시 trade/schedule/cargo 패널이 모두 빈 상태 (Read 완전 불능). a8ae1c1/075e2b4/a8ee607/f89011a/d245854 누적 commits에서 사용자 검수 안 한 채 진행되어 표면화. **2026-05-14 추가 사례 (77341a8)**: `masterRefNo`가 BE 저장 경로(Request/Assembler/Domain/Mapper/DB column) 모두 정상이었으나 `HouseBlDetailResult`/`HouseBlDetailResponse`/`HouseBlFactory.toDetailResult` 3축 + **FE `adapter/out/api/house-bl.ts`의 `HOUSE_BL_DETAIL_SCHEMA` zod 파싱 라인** + `HouseBlDetail` interface + `map-house-bl-detail.ts` 5축 모두 비어 있어 toolbar 영원히 빈 값. zod schema에 키 없으면 BE가 보내도 파싱 시점에 소실. | (본 절 + §6.50 신설) | **신규 form 매핑/필드 추가/변경 직후 즉시**: (a) BE detail response DTO record에 해당 필드 존재 여부 grep — 없으면 BE 작업 단위 추가 (b) FE `form.reset({...})` 객체에 nested mapping(`seaDetail: { ... }` 등) block 존재 여부 read (c) `HOUSE_BL_DETAIL_SCHEMA`(zod) 키 ⊆ `form.reset` 매핑 키 정합 표 작성 **(d) FE adapter zod schema (`adapter/out/api/<domain>.ts` 의 `*_DETAIL_SCHEMA`)에도 신규 필드 존재 여부 grep** — 누락 시 BE 응답이 zod 파싱 시점에 소실되어 form에 절대 도달 못 함 **(e) FE 도메인 타입 (`domain/<domain>/index.ts` 의 `XxxDetail` interface)에도 신규 필드 선언** — adapter schema가 통과시켜도 TypeScript 타입에 없으면 mapper에서 컴파일 오류. 1축이라도 누락 시 plan에 BE/FE 양쪽 작업 단위 추가. |
| ⑨ | **`useBlDraftSync` 반환 `didRestoreFromDraftRef` 가드 미적용** | House B/L entry가 반환값 무시(`useBlDraftSync(form, key);`) → draft race로 detail이 form에 일시 덮어쓰일 위험. Truck `use-truck-bl-entry.ts:62, 132` / Non `use-non-bl-entry.ts:65, 67`은 가드 적용 중. **House만 비대칭**. | §6.18 + 본 절 | 모든 BL Entry는 `const { didRestoreFromDraftRef } = useBlDraftSync(...)` 반환 수신 + form.reset useEffect에 `if (didRestoreFromDraftRef.current) return;` 가드 + deps에 ref 포함 의무. Truck/Non 패턴: `detailLoadedRef.current = true;` **다음**에 가드 배치. |
| ⑩ | **mutation onSuccess Truck/Non SSOT 비대칭 + §6.21 List 자동 invalidate 금지 위반** | House B/L: `queryClient.invalidateQueries({ queryKey: ["house-bl", "list"] })` 호출(§6.21 위반) + create/update detail invalidate · sessionStorage hot-marker · clearDraft · `detailLoadedRef=false` 누락 + update 시 `router.push(list)`로 자동 이탈해 form refetch 검증 불가. Truck `use-truck-bl-entry-mutations.ts:30~48` 패턴과 비대칭. | §6.16 + §6.21 + 본 절 | create/update onSuccess SSOT: (a) `invalidateQueries(["{domain}", "detail", id])` (b) `sessionStorage.setItem("{domain}-entry:hot:${id}", "1")` (c) `setFocus(focusKey, id)` (d) `clearDraft(key)` (e) `detailLoadedRef.current = false`. List 자동 invalidate 금지(§6.21). update 시 router.push 자동 이동 금지. |
| ⑪ | **`useSearchParams` 사용 시 page.tsx Suspense 경계 누락** | URL `?id` → store 동기 effect 추가하면서 `useSearchParams()` 도입 → lint PASS / **build FAIL** `useSearchParams() should be wrapped in a suspense boundary at page "/fms/house-bl/[variant]/entry"` (Next.js 14 App Router SSG 통과 조건). lint만 보면 놓치는 회귀. | 본 절 + Next.js 14 docs | `useSearchParams()` 추가 시 page.tsx에서 해당 컴포넌트를 `<Suspense fallback={...}>`로 감싸기 동반 의무. **lint는 통과하고 build에서만 잡힌다 — FE-QA는 lint+build 모두 실행 필수**. |
| ⑫ | **URL `?id` ↔ zustand focus store 동기 부재** | entry가 `useEntryFocusStore`만 읽고 URL `?id` 무시 → 사용자가 URL 직접 진입(북마크/리프레시/외부 링크) 시 store empty → isEdit=false → 빈 form. list `router.push`는 path만 사용(?id 안 붙임)이라 더블클릭 자체는 영향 없지만 다른 진입 경로 전부 깨짐. | §13.5 + 본 절 | entry mount 시 `useSearchParams()` `?id` 파싱 후 `useEntryFocusStore.getState().focus[key] !== parsed`이면 `setFocus(key, parsed)` + `detailLoadedRef.current = false`. ⑪의 Suspense 의무 동반. |
| ⑬ | **BE Request DTO·Assembler ↔ DetailResponse 1:1 정합 점검 (Read flow 결정)** | Sea House: `CreateHouseBlRequest`/`UpdateHouseBlRequest`/`HouseBlAssembler`는 `incoterms`/`salesClass`/`mblNo`/`settlePartnerCode`를 받아 도메인에 저장하나 `HouseBlDetailResponse`/`HouseBlDetailResult`에 미노출 → form 필드가 영원히 비어있음(submit은 잘 되지만 round-trip 실패). 사용자 검수에서 "값이 안 채워짐" 보고로 표면화. (`e0d13c4`·`04a8127`) | 본 절 신설 | **Request DTO 필드 ⊂ DetailResponse 필드** 정합표 작성 의무. plan 단계에서 `grep "String|Long" CreateXxxRequest` ↔ `grep XxxDetailResponse` 두 record 필드 set 비교. 도메인 getter 존재 확인 후 누락 시 BE 작업 단위 추가. |
| ⑭ | **EnumRegistryFactory value SSOT — `e.name()` vs `e.getCode()` 일관성** | ContainerType만 `e -> new EnumOption(e.getCode(), ...)`로 등록(value=`"20GP"`)되어 있어 DB 저장값/응답 직렬화값(`@Enumerated(STRING)` → `name()`=`"T20GP"`)과 mismatch. ComboBox 옵션 value(`"20GP"`)와 form value(`"T20GP"`)가 매치 안 되어 표시 깨짐. **Sea House + Truck B/L 두 화면 동시 회귀**. 다른 enum(TruckType/SalesClass/WorkDivision)은 `e.name()` 등록이라 정상. (`e0d13c4`) | 본 절 신설 + §6.45 보강 | EnumRegistryFactory 등록 시 **value=`e.name()` SSOT 의무**. label은 `e.getCode()` 또는 `e.getLabel()` 사용자 친화. `fromCode` 메서드는 dual 매칭(valueOf 우선 + getCode fallback)으로 외부 입력 호환. plan 단계에서 새 enum 등록 시 `register(map, "Xxx", ..., e -> new EnumOption(e.name(), ..., ...))` 형식 강제. |
| ⑮ | **FE zod `.optional()`은 `undefined`만 허용 — `null` 거부 함정** | BE Jackson 기본 직렬화는 null 필드를 `"key": null`로 명시 출력. FE zod `z.string().optional()`이 받으면 `expected string, received null` parse 실패 → `ResponseParseError` → 토스트. Sea House의 9 NonBl 본체 필드(linerCode/linerName/vesselName/voyageNo/finalDestCode/finalDestName/finalEta/volumeWeightKg/rton)가 SEA 응답에서 null로 도착하던 오랜 잠재 결함이 본 세션 검수에서 표면화. (`e0d13c4`) | §6.29 보강 + 본 절 | BE Request/Response가 잠재 null인 모든 필드는 zod에서 `.nullable().optional().transform((v) => v ?? undefined)` 패턴 의무. plan 단계에서 zod schema의 `.optional()` 단독 사용 grep → BE 응답 null 가능 여부 검증 후 `.nullable()` 추가. |
| ⑯ | **FE form key 4축 정합 — schema ↔ panel register ↔ submit 빌더 ↔ mapper** | Sea House 7건 mismatch 사례: party-panel `*Addr` vs schema `*Address`, schedule-panel 본체 `vesselName`/`voyNo` vs BE seaDetail nested `vesselName`/`voyageNo`, marks-panel `marksAndNumbers` vs BE `desc.marks`, description-panel `descriptionOfGoods` vs BE `desc.description`, sea-remark-panel `desc.remark` vs BE 본체 `remark`, toolbar mbl 필드가 mapper에서 `String(detail.masterBlId)` PK 받음 vs `detail.mblNo`. submit 빌더와 mapper가 다른 key 사용해도 zod·lint 통과 → 사용자 검수에서만 표면화. (`e0d13c4`·`04a8127`) | 본 절 신설 + §6.49 ⑥ 보강 | plan 단계에서 **각 form 필드마다 4축 매핑표** 작성 의무: (a) `house-bl-schema.ts` key (b) 패널 `register(...)` / `Controller name=` (c) submit 빌더 BE 키 (d) mapper 매핑 키. 한 축이라도 다르면 작업 단위로 정정. 중복 schema 키(`shipperAddr` vs `shipperAddress` 등)는 제거. |
| ⑰ | **FE 도메인 enum literal strict union 회피** | `HouseBlDetail.blType: 'OBL'\|'SWB'\|'SURRENDER'\|null` strict union이 BE 실 enum(ORIGINAL/SURRENDER/SEAWAY/NORMAL/EXPRESS) 5상수와 mismatch. `z.enum([...]).nullable()` strict 검증이 BE의 `SEAWAY` 값을 즉시 거부. ComboBox 옵션이 useEnumOptions로 동적 fetch면 strict 검증 가치가 없으므로 완화하는 게 SSOT. (`e0d13c4`) | 본 절 신설 | enum 필드 zod 정의 시 **`useEnumOptions("EnumName")`으로 ComboBox 옵션 동적 fetch하는 필드는 `z.string().nullable()` 완화** + 도메인 타입 `string \| null`. strict literal union은 BE enum이 변경될 때마다 FE 즉시 거부되는 fragile pattern. 단 close-set이고 화면 분기 로직이 strict 비교를 필요로 하는 경우만 예외 유지. |

### 🔴 신규 도메인 마이그레이션 시 의무 사전 점검 (plan 단계 체크리스트)

다음 16가지를 **plan 단계에서 명시적으로 작업 범위에 포함**하라. 누락 시 사용자 검수 단계에서 회귀가 드러나 재작업한다.

1. **Registry 전수 read** — `main-<jobDiv>.tsx`의 등록 배열을 read하여 모든 패널 파일 식별. native `<input>`/`<select>`/`<button>`/code+name pair grep 후 일람을 plan에 첨부.
2. **toolbar/page-head 영역 read** — `<Domain>Entry.tsx`의 `.toolbar` map 콜백 + `.page-head__actions` 버튼 영역 + `.page-head__meta` badge 영역을 plan 범위에 명시 포함. 패널 밖 사각지대 차단.
3. **카탈로그 SSOT pre-read** — `@/components/shared/button` 시그니처 + `(dev)/preview/sections/buttons/_bundles-data.ts` + `(dev)/preview/sections/inputs/*` 파일을 plan 작성 전 read. 적용 대상 액션·필드 매트릭스를 plan에 첨부.
4. **enum 매핑 후보 list-up** — `form schema`의 `z.string()` 필드 중 `EnumRegistryFactory`에 등록된 23개 enum과 매핑 가능한 후보를 사전에 list-up. ComboBox 적용 대상으로 plan에 명시.
5. **rowKey 패턴 grep** — 모든 그리드 패널의 `rowKey={(_, i)`/`onRowClick={(_, i)`/`rowClassName={(_, i)` 패턴 grep. 발견 즉시 정정 단위에 포함.
6. **4축 정합 점검** — `form.reset(detail)` 매핑이 (a) form 필드명 ↔ (b) UI 라벨 ↔ (c) BE detail 필드명 ↔ (d) BE EnumRegistry 등록과 일치하는지 표로 정렬. 1축이라도 어긋나면 product 의문 보고.
7. **검증 절차에 캐시 정리 포함** — fix 후 사용자 검증 안내에 `.next` 삭제 + dev server 재시작 명시. 사용자가 "그대로"라고 보고하면 추측 fix 전에 cache clear 재검증 요청.
8. **BE detail response ↔ FE 5축 매핑 정합 (Read flow 결정)** — 신규/변경 form 필드 추가 직후 (a) BE Detail Response/Result record에 필드 존재 grep (b) BE Factory(`toDetailResult`)·Assembler(`toDetail`)에 매핑 grep (c) FE adapter zod schema(`*_DETAIL_SCHEMA`)에 키 존재 grep — **누락 시 BE 응답이 zod 파싱 시점에 소실되어 form 도달 0%** (d) FE 도메인 타입(`XxxDetail` interface)에 필드 선언 (e) FE `form.reset({...})` 객체의 매핑(nested block 포함). 5축 한 줄이라도 누락 시 BE/FE 풀스택 작업 단위 추가. (사례: 77341a8 `masterRefNo`)
9. **`useBlDraftSync` 가드 적용** — entry 컴포넌트는 `const { didRestoreFromDraftRef } = useBlDraftSync(...)` 반환 수신 + form.reset useEffect 가드. 신규 도메인 마이그레이션 시 Truck/Non 패턴 카피.
10. **mutation onSuccess SSOT 적용** — create/update onSuccess 5요소(detail invalidate · hot-marker · setFocus · clearDraft · `detailLoadedRef=false`) + §6.21 List 자동 invalidate 금지 + update 시 router.push 금지. Truck `use-truck-bl-entry-mutations.ts` 패턴 카피.
11. **URL ?id 동기 + Suspense 경계** — entry 컴포넌트에 `useSearchParams()` 도입해 URL → store 동기 effect 추가. 동시에 page.tsx에서 해당 컴포넌트를 `<Suspense fallback={...}>`로 감싸기. **lint PASS만으로는 빠지는 회귀이므로 FE-QA에서 build 반드시 동반 실행**.
12. **BE Request DTO ↔ DetailResponse 1:1 정합 (Read flow)** — `CreateXxxRequest`/`UpdateXxxRequest`/`Assembler`가 받는 필드 set이 `XxxDetailResponse`/`XxxDetailResult` 노출 필드 set의 부분집합인지 grep 비교. 누락 발견 시 BE 작업 단위 추가. submit은 잘 되지만 round-trip 깨지는 패턴.
13. **EnumRegistryFactory value SSOT 점검** — 신규/수정 enum 등록 시 `e -> new EnumOption(e.name(), ..., ...)` 형식 강제. value=`getCode()`는 DB 저장값(`@Enumerated(STRING)` = `name()`)과 mismatch 야기. `fromCode` 메서드 dual 매칭(valueOf + getCode) 동반.
14. **FE zod `.optional()` null 처리 점검** — BE 잠재 null 직렬화 필드는 `.nullable().optional().transform((v) => v ?? undefined)` 패턴 의무. plan 단계에서 zod schema의 `.optional()` 단독 사용 grep + BE 응답 null 가능 여부 검증.
15. **FE form key 4축 정합** — 각 form 필드마다 (a) schema key / (b) panel register / (c) submit 빌더 BE 키 / (d) mapper 매핑 키 표 작성. 중복 schema 키 제거(`*Addr` vs `*Address` 등). 한 축이라도 다르면 정정 단위 추가.
16. **FE strict enum literal 회피** — useEnumOptions로 ComboBox 옵션 동적 fetch하는 필드는 zod `z.string().nullable()` + 도메인 `string | null` 완화. strict literal union은 BE enum 추가 시 fragile.

### 본 세션 사례 (2026-05-13)

- **Round 1 (commits 860abfb~bdc757f, c481ac2)** — 작업 단위 5(카탈로그 교체) 범위를 6개 패널(party/sea-trade/description/freight/container-grid/item-hs)로 좁게 잡음. HOUSE_BL_SEA_REGISTRY 9개 중 5개(schedule/document/cargo/marks/remark) 누락. toolbar 영역·page-head Button 영역 미점검. rowKey 인덱스 안티패턴 잔존. Frontend-QA build PASS이지만 사용자 검수에서 회귀 4건 발견.
- **Round 2 (commits 8e792dd, 60694a9, 5320d0b, 4bd1a0c)** — 단위 7(누락 5개 패널) + 단위 8(ComboBox 교정 + Container useMemo) + 단위 9(rowKey 안티패턴 + 가이드 §6.41-보강) + 단위 10(toolbar ComboBox + page-head Button)으로 보강. **본 절 매트릭스가 plan 단계에 명시되어 있었다면 Round 1에서 일괄 처리 가능**.
- **Round 3 / CRUD 복원 (commits c8fc52a, a8ae1c1, 075e2b4, 9495316, a8ee607, f89011a, d9d185d, 5ac78ec, d245854, f560236, d3cdde1, 9448dbb, 886b3a7, 806aff6, 293e21c) — 누적 회귀의 표면화 + Phase A~D 복원**. Round 2 종료 후 사용자 수동 회귀 검증 8종을 끝까지 통과시키지 못한 상태에서 후속 카탈로그·정합화 commits가 누적. **list → entry 더블클릭 시 form이 빈 상태**(Read 완전 불능)로 사용자가 표면화. Phase 1 Explore 병렬 3개로 원인 4가지(BE detail 응답 누락, FE form.reset nested 매핑 누락, useBlDraftSync 가드 미사용, mutation onSuccess Truck 비대칭)와 부수 1가지(URL ?id 동기 부재)를 식별. 4 Phase 풀스택 복원 후 build FAIL(Suspense 누락)을 또 한 commit으로 해소. **매트릭스 항목 ⑧~⑫는 본 라운드에서 신설 — Read/Write/Delete 흐름의 BE↔FE 결합 표면적이 가장 깊은 회귀 패턴**. 본 라운드 모든 작업 단위마다 BE-QA / FE-QA 분리 실행으로 검증.
- **본 절은 살아있는 문서** — 차기 도메인에서 새로운 누락 패턴 발견 시 매트릭스에 추가 의무.

---

## 12. 후속 결정 대기 사항

항공·그 외 Entry 마이그레이션 시 검토할 결정 사항.

### 12.1 VolumeDivisor 도메인 위치 — 항공 Entry 마이그레이션 시 공통화 검토

현재 `volumeDivisor`(부피 환산 divisor)는 **두 도메인**에 도입되어 있다:
- Non B/L 확장 필드(`HouseBlNonBl`, `house_bl_non_bl.volume_divisor`)
- Truck B/L 확장 필드(`HouseBlTruck`, `house_bl_truck.volume_divisor`, 2026-05-12 추가)

도입 패턴은 동일(`VolumeDivisor` enum 재사용, `HouseBl<X>SubFactory.applyXxxVolumeDivisor` 별도 메서드)이지만 컬럼이 각 ext 테이블에 중복 존재한다.

항공 Entry(Air House / Air Master) 마이그레이션 시 동일 개념이 필요해지면 다음 선택지 중 채택:

- **`HouseBl` 공통 필드로 일괄 승격** (권장) — Non B/L + Truck B/L의 기존 컬럼을 `house_bl.volume_divisor`로 통합 마이그레이션 + 도메인 필드를 부모로 이동. 3 도메인 동시 정합. nullable이라 SEA 회귀 안전.
- 각 확장에 별도 보유 — 3개 컬럼 일관성 떨어지고 마이그레이션 비용 누적

> **결정 시점**: 항공 Entry 마이그레이션 plan 수립 단계. 이미 2 도메인이 동일 패턴이므로 공통화 가치가 강화되었음.

### 12.2 TruckBlDetailResponse 자식 응답 확장 (Truck B/L 마이그레이션 후속)

Truck B/L Phase A에서 `TruckBlDetailResponse`에 자식 컬렉션 일부가 누락되어 있었다. **2026-05-12 Dimension 추가 작업에서 `dims`/`volumeDivisor`는 응답 시그니처에 포함됨**(history.md Truck Dimension 항목 참조). 여전히 누락된 자식:

- `truck_order` 그리드 14컬럼(Truck Information 그리드 행) — `TruckBlDetailResponse.truckOrders` 부재
- `house_bl_truck_desc`의 marks/description/descClause1/descClause2 — `TruckBlDetailResponse.desc` 부재

> **결정 시점**: Truck B/L Entry 첫 실 데이터 저장 회귀 시점 또는 사용자 보고 시. 응답 시그니처 확장 시 §6.29(BE 응답 body 변경 시 FE adapter zod 스키마 동시 정합 필수)를 반드시 적용.

### 12.3 ChangeBlNoModal 도메인 공통화 검토

Non B/L(`change-bl-no-modal.tsx`)과 Truck B/L(`truck-change-bl-no-modal.tsx`)이 동일 구조(현재 hbl_no readOnly + 신 hbl_no 입력 + Update/Close)이지만 `port`/`queryKey` 하드코딩 때문에 도메인 prop 분기 불가로 신설되어 있다.

향후 House B/L Sea/Air/Master B/L 등 동일 패턴 적용 시 다음을 고려:

- 도메인 prop(`{ port, queryKey, label }`)을 받는 단일 컴포넌트로 리팩토링
- `useMutation`을 컴포넌트 외부에서 주입(`{ mutation: UseMutationResult }`)하는 패턴

> **결정 시점**: 세 번째 도메인 ChangeBlNoModal 적용 시.

---

## 13. 도메인별 엔트리 마이그레이션 시 리마인드 체크리스트

핸드오프 2026-05-12 §7 / §8 / §9 / §11 항목을 도메인별 마이그레이션 PR에 분산 흡수하기 위한 체크리스트. 잔여 4 도메인(Sea House / Air House / Sea Master / Air Master) + SwitchBlModal 마이그레이션 진행 시 각 도메인 PR마다 본 체크리스트로 점검한다. 단독 작업으로 처리하지 않음.

### 13.1 모든 도메인 PR 공통 적용

| 출처 | 항목 | 비고 |
|------|------|------|
| §6.35 | 도메인 전용 Port+Adapter (UseCase + Controller CRUD endpoint + DetailResponse 신설 + UseCase 분리) | 현재 HouseBlController / MasterBlController 통합 단일 CRUD 상태 |
| §6.19 | Entry Search EXACT PK 단건 조회 + hot-marker | — |
| 핸드오프 §930 | ChangeBlNo 흐름 (truck-change-bl-no-modal / change-bl-no-modal 패턴) | — |
| §6.16 | List 그리드 더블클릭 시 hot-marker `sessionStorage.setItem` — **2026-05-12 grep 기준 4 도메인 모두 미적용 상태** | grep 검증 의무 |
| 핸드오프 §959 | 공용 confirm 모달 적용 — `window.confirm` 잔존 3 파일: `switch-bl-modal.tsx` / `house-bl-entry.tsx` / `master-bl-entry.tsx` | 해당 파일 변경 PR에 동반 일괄 |
| §6.36 | detail useQuery `staleTime:Infinity` + `gcTime:Infinity` (둘 다 동반) | List grid `useQuery` 도 동일 — `gcTime` 미지정 시 5분 후 캐시 GC되어 staleTime 무력화. 신규 도메인 마이그레이션 시 grep 검증 의무 |
| §6.18 | unmount cleanup으로 `clearDraft` 호출 금지 (정정) | `useBlDraftSync` 반환 `didRestoreFromDraftRef`로 detail useEffect 분기 |
| §6.47 | List 더블클릭 + handleSearch에 `invalidateQueries` + `clearDraft` 명시 호출 | 4 도메인 적용 완료(bab177f) — 신규 도메인 entry 마이그레이션 시 동일 패턴 |

### 13.2 Detail Response / Controller CRUD 분리 (핸드오프 §9 흡수)

| 항목 | 적용 시점 |
|------|----------|
| `HouseBlDetailResponse` 단일 통합 → SeaHouse/AirHouseDetailResponse 분리 | Sea House / Air House PR |
| `MasterBlDetailResponse` 단일 통합 → SeaMaster/AirMasterDetailResponse 분리 | Sea Master / Air Master PR |
| SeaHouse/AirHouse/SeaMaster/AirMasterController에 CRUD endpoint 추가 | 해당 도메인 PR |
| `HouseBlController` / `MasterBlController` 통합 CRUD endpoint 점진 deprecation | 모든 도메인 분리 완료 시점 |

### 13.3 ChangeBlNoModal 도메인 공통화 (핸드오프 §7 = 이 가이드 §12.3 흡수)

- **세 번째 도메인 적용 시점**에 prop 인터페이스 결정 (추측 기반 설계 회피)
- 두 도메인(Non B/L / Truck B/L) 실 사용 패턴 + 세 번째 도메인(Sea House 등)의 사용 패턴 종합 후 §12.3 두 옵션 중 채택

### 13.4 가이드/메모리 stale 점검 의무 (핸드오프 §11 흡수)

- 각 도메인 grid 파일의 hot-marker(`sessionStorage.setItem`) / `window.confirm` / §13.1 5 패턴 적용 여부를 **grep으로 실 검증** (선언된 사실 ≠ 현재 코드 상태)
- 메모리 룰: "Before recommending from memory ... verify first."

### 13.5 선결 과제 (Sea House 첫 PR)

- ~~House-BL `page.tsx`가 `searchParams`를 수신하지 않는 라우팅 결함 정리~~ — **해소 (806aff6 + 293e21c, 2026-05-13 Round 3)**. entry 컴포넌트 내부에서 `useSearchParams()`로 URL `?id`를 읽고 `useEntryFocusStore` 동기. page.tsx에서 `<Suspense>` 경계 추가로 SSG 통과. 잔여 도메인(Air House / Sea Master / Air Master)에도 동일 패턴 의무.
- ~~query-param 라우팅(`?id={row.id}`)을 받도록 page.tsx 시그니처 보정 또는 path-param(§6.26 패턴 A)으로 전환~~ — entry self-sync 방식(§6.49 ⑫)으로 갈음. path-param 전환은 더 큰 라우팅 리팩토링이라 별도 작업.
- **Round 3 후속 결함 (open)**:
  - ~~SEA `containers` + `desc`(marks/description/descClause1/descClause2) BE detail 응답 노출 누락~~ — **해소 (2026-05-13, 54e61fb)**.
  - ~~SEA `incoterms`/`salesClass`/`mblNo`/`settlePartnerCode` BE detail 응답 노출 누락~~ — **해소 (2026-05-13, e0d13c4·04a8127)**. Create/Update Request·Assembler엔 있었으나 detail 응답만 빠져 form 화면에 미바인딩이었음. `HouseBlDetailResult`/`HouseBlDetailResponse`에 4 필드 추가 + `HouseBlFactory.toDetailResult` enum/VO 매핑 보강. FE 도메인·zod·`map-house-bl-detail` 동기.
  - ~~`ContainerType` ComboBox 표시 깨짐 (BE EnumRegistryFactory value=getCode vs DB/응답=name 불일치)~~ — **해소 (2026-05-13, e0d13c4)**. EnumRegistryFactory ContainerType 등록을 `e -> new EnumOption(e.name(), e.getCode(), e.getDescription())`로 변경(다른 enum과 SSOT). `ContainerType.fromCode` dual 매칭(valueOf + getCode fallback)으로 호환. FE `label: o.label`로 짧은 코드 표시. **Sea House Container Grid + Truck Information 그리드 동시 영향**.
  - ~~FE zod `.optional()`이 null 거부로 detail parse 실패~~ — **해소 (2026-05-13, e0d13c4)**. 9 NonBl 본체 필드를 `.nullable().optional().transform(v ?? undefined)` 통일.
  - ~~`HouseBlDetail.blType` strict enum(`'OBL'|'SWB'|'SURRENDER'`)이 BE 실 상수(ORIGINAL/SEAWAY/...)와 mismatch~~ — **해소 (2026-05-13, e0d13c4)**. `string|null` + `z.string().nullable()` 완화. ComboBox 옵션은 `useEnumOptions("BlType")`로 동적 fetch.
  - ~~FE form key 정합 7건 (party `*Addr`/`*Address`, SEA `vesselName`/`voyNo` 본체 vs seaDetail, `marksAndNumbers`/`descriptionOfGoods`/`desc.remark` 키 불일치)~~ — **해소 (2026-05-13, e0d13c4)**. panel register key를 BE 매핑 SSOT로 통일, 중복 schema 키 7종 제거.
  - ~~`house-bl-entry.tsx` 500줄 분리~~ — **해소 (2026-05-13, e0d13c4)**. `form.reset` 매핑 블록을 `map-house-bl-detail.ts`로 추출 (507→410줄, mapper 107줄).
  - AIR/TRUCK 자식 컬렉션(scheduleLegs/airCharges/dims/truckOrders) BE detail 응답 노출 누락 — 그리드 패널 빈 상태. AIR/TRUCK 도메인 별도 작업.
  - AIR 본체 issue 필드(`HouseBlAir.issuePlace` 등) nested 응답 노출 — Phase A-1과 동일 패턴으로 추가.
  - `HouseBlFactory.java` 340줄 (300 초과, 500 미만) — `HouseBlSeaSubFactory`로 SEA projection 헬퍼(`toSeaContainerProjections`/`toSeaDescProjection`) 이전 권장.
  - `map-non-bl-detail.test.ts:8` `TS2353 'jobDiv' does not exist in type 'NonBlDetail'` — Next build에서는 PASS이나 `tsc --noEmit`에서 표면화되는 기존 타입 불일치. 별도 정리.
  - `master-bl-entry.tsx` / `switch-bl-modal.tsx` `window.confirm` 잔존 (§959) — 후속 일괄.

### 13.6 권장 진행 순서

1. Sea House Entry (사용 빈도 최고, 패턴 정립 대상)
2. Air House Entry
3. Sea Master Entry — §13.3 ChangeBlNoModal 공통화 검토 시점
4. Air Master Entry
5. SwitchBlModal 일괄 정리 (`window.confirm` 잔존 3 파일)

> **참고**: 본 §13은 핸드오프 2026-05-12 미해결 이슈(§7 / §8 / §9 / §11)를 단독 작업이 아닌 도메인별 PR로 분산 흡수한 결과. Phase 4 마이그레이션 진행 시 항상 본 체크리스트를 기준으로 PR 작성.
