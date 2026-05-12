# Entry 화면 마이그레이션 가이드

> Non B/L Entry를 기준으로 다른 엔티티 화면(Sea/Air House, Sea/Air Master, Truck B/L 등) Entry 화면을 카탈로그 컴포넌트로 통일하는 작업의 패턴·함정·체크리스트.
> Entry 마이그레이션이 완료될 때까지 살아있는 문서.

---

## 1. 작업 흐름 요약

1. 대상 Entry 화면의 모든 native `<input>`/`<select>`/로컬 wrapper 컴포넌트 식별
2. 카탈로그 표준 컴포넌트(TextBox/NumberBox/CodeBox/ComboBox/DateBox)로 1:1 매핑
3. enum 드롭다운은 `useEnumOptions(name)` 훅으로 옵션 로딩
4. form schema/defaults에 신규 필드(register 미연결되어 있던 것 포함) 보정
5. 인라인 `style={{...}}` 일괄 제거 (카탈로그 컴포넌트가 토큰 디자인 책임)
6. 로컬 wrapper 컴포넌트(예: 패널 내부 `PartyBlock` 같은 정의) 제거, CodeBox 등 표준으로 교체
7. 카탈로그 페이지(`app/(dev)/preview/sections/inputs-section.tsx`)에 신규 표준 컴포넌트 노출 추가
8. 그리드 패널: GridList 표준 사용, `onClearRow` 등 selection 격리 prop 연결

---

## 2. 카탈로그 컴포넌트 매핑 표

| 기존 마크업 | 표준 컴포넌트 | 핵심 prop |
|---|---|---|
| `<input type="text">` | `TextBox` | `variant="panel"`, `{...register("name")}` |
| `<input type="number">` | `NumberBox` | `decimalPlaces={0|3}`, `{...register("name", { valueAsNumber: true })}` |
| `<input type="date">` / 직접 마스킹 | `DateBox` | RHF `Controller` 사용 (value/onChange/onBlur forwarding) |
| 코드+이름 2-input (코드 lookup 아이콘) | `CodeBox kind="lcn"` | `codeProps={...register("Xcode")}`, `nameProps={...register("Xname")}`, `onLookup` |
| Party 코드+이름 (라벨 우측 일렬) | `CodeBox kind="party-cn"` | 동일 prop, `kind="party-cn"` |
| `<select>` (정적 옵션) | `ComboBox` | `options=[{value,label}, …]`, RHF Controller 필수 |
| `<select>` (enum 기반) | `ComboBox` + `useEnumOptions("EnumName")` | `options={enumOptions}`, `placeholder={enumPlaceholder}`, RHF Controller 필수 |

위치: 모두 `front-end/src/components/shared/inputs/`

### BoxBaseProps 공통 prop (모든 표준 컴포넌트)

`variant?: "panel" | "cell"` · `required?` · `readOnly?` · `disabled?` · `className?` · `style?` · `label?`

### Variant 사용 기준

- `panel`: 패널(섹션) 내부 입력 필드
- `cell`: 그리드 셀 내부 입력 (`grid__cell-input`과 통합)
- `label`: 라벨 자리에 ComboBox를 임베드 (LcnLabel 라벨 셀렉터에 적용, 2026-05-08)

---

## 3. Form 통합 (react-hook-form)

### Spread 패턴

```tsx
// TextBox / NumberBox: register 결과 자체를 spread
<TextBox variant="panel" {...register("nonBlNo")} />
<NumberBox variant="panel" decimalPlaces={3} {...register("grossWt", { valueAsNumber: true })} />

// CodeBox: codeProps / nameProps 분리 spread
<CodeBox
  variant="panel"
  kind="lcn"
  label="Liner"
  codeProps={{...register("linerCode"), placeholder: "Code"}}
  nameProps={{...register("linerName")}}
  onLookup={() => {/* lookup 모달 */}}
/>

// DateBox: Controller 사용
<Controller
  control={control}
  name="etd"
  render={({ field }) => (
    <DateBox
      variant="panel"
      value={field.value}
      onChange={field.onChange}
      onBlur={field.onBlur}
      name={field.name}
    />
  )}
/>

// ComboBox: 반드시 Controller 사용 — register spread 불가
// register({...register("name")}) spread 시 값 미반영 버그 발생 (66a217c 사례)
<Controller
  name="workDiv"
  control={control}
  render={({ field }) => (
    <ComboBox
      variant="panel"
      options={workDivOptions}
      placeholder={workDivPlaceholder}
      value={field.value}
      onChange={field.onChange}
    />
  )}
/>
```

### Schema/Defaults 동시 갱신 필수

신규 필드 추가 시 두 파일 동시 업데이트:

- `xxx-schema.ts`: `dimensionDivisor: z.string().optional()` 같이 z 타입 추가
- `xxx-defaults.ts`: `dimensionDivisor: "CM/6000"` 같이 초기값 추가

> **함정**: 한쪽만 갱신하면 register는 동작하지만 form 초기값/검증이 어긋나 저장 시 누락 발생.
> **사례**: 2846fd7 — Non B/L FE submit 시 `salesClass`·`originalBlRef` 누락. schema/defaults 어느 한쪽에만 추가되어 있던 필드가 submit 페이로드에서 빠진 것이 원인.

---

## 4. Enum 드롭다운 패턴

### useEnumOptions 훅

위치: `front-end/src/application/enums/use-enum.ts`

```tsx
const { options, placeholder, isLoading } = useEnumOptions("WorkDivision");
// options: Array<{ value: string; label: string }>
```

### 등록된 enum (백엔드 EnumRegistryFactory)

- `WorkDivision` (Sea/Air/Warehouse/Trucking)
- `Bound` (EXP/IMP)
- `SalesClass` (S/N)
- `WeightUnit` (KGS/LBS)
- `VolumeDivisor` (CM/6000, CM/5000, IN/366)

> **새 enum 필요 시**: 백엔드 `domain/.../enums/`에 enum 추가 + `EnumRegistryFactory`에 등록 → 프론트는 `useEnumOptions("NewName")` 만으로 즉시 사용. 한글 라벨 보강은 별도 작업.

---

## 5. 그리드 패널 패턴

### GridList 컴포넌트 분기

`@/components/shared/grid-list`의 `GridList`:
- `gridId` **없음** → PlainGridList (Entry용 - Dimension/Container Info/카탈로그 등)
- `gridId` **있음** → ManagedGridList (List 화면용 - localStorage persist + 컬럼 visibility 메뉴)

### Entry 그리드 표준 props

```tsx
const [selectedKey, setSelectedKey] = useState<string | null>(null);

<GridList
  columns={cols}
  data={fields}
  rowKey={(r) => r.id}
  onRowClick={(row) => setSelectedKey(String(row.id))}
  rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}
  onClearRow={() => setSelectedKey("")}        // 필수 - 외부 클릭 시 자동 해제
  style={{ flex: 1, minHeight: 0 }}
/>
```

### 셀 input (표준 컴포넌트 권장)

> **중요**: 그리드 셀 input은 **표준 컴포넌트의 cell variant 사용 필수**. native `<input className="grid__cell-input">` 직접 사용 금지(autoComplete 등 default 속성 누락 위험). 셀 컴포넌트 내부에서 `autoComplete="off"`, focus 처리, required 표시 등 일관 처리됨.

```tsx
// ✅ 권장: 표준 컴포넌트 cell variant
<TextBox variant="cell" {...register(`array.${i}.field`)} />

// NumberBox cell — decimalPlaces 기준:
//   정수(pkg, qty 등) → decimalPlaces={0} + valueAsNumber:true (schema: z.number())
<NumberBox variant="cell" decimalPlaces={0} {...register(`array.${i}.pkg`, { valueAsNumber: true })} />
//   소수 3자리(grossWt, cbm 등) → decimalPlaces={3} + valueAsNumber:true
<NumberBox variant="cell" decimalPlaces={3} {...register(`array.${i}.grossWt`, { valueAsNumber: true })} />
//   schema가 string일 때 → decimalPlaces 생략 (blur normalize 없이 string 그대로)
<NumberBox variant="cell" {...register(`dims.${i}.length`)} />

<DateBox variant="cell" /* Controller 사용 */ />
<ComboBox variant="cell" options={enumOptions} {...register(`array.${i}.kind`)} />

// ⚠️ 부득이 native input 사용 시 (legacy 영역만)
<input autoComplete="off" className="grid__cell-input" {...register(`array.${i}.field`)} />
<input autoComplete="off" type="number" className="grid__cell-input is-num" {...register(...)} />
```

> **컬럼 리사이즈**: PlainGridList는 session-local로 자동 동작 (구현됨). 새로고침 시 초기화. localStorage 영속이 필요하면 `gridId` 부여하여 ManagedGridList 분기로 전환.

---

## 6. 재발 방지 체크리스트 (자주 하는 실수)

### 6.1 CodeBox 외부 wrapper 중첩 금지

`<div className="lcn">` 같은 외부 wrapper 안에 CodeBox를 넣으면 CodeBox 자체 `.lcn` 렌더와 중첩되어 grid-template-columns가 깨진다.

**올바른 패턴**: CodeBox만 단독 렌더. 외부 wrapper 제거.

### 6.2 DateBox에 className/style 전달 금지

`PanelDateInput`의 `DateInputBaseProps`는 `className`/`style`을 `Omit`으로 제외하고 있다. DateBox에서 받은 className/style을 PanelDateInput으로 그대로 전달하면 타입 오류 발생.

**올바른 패턴**: DateBox 내부에서 className/style을 PanelDateInput에 전달하지 않음. (BoxBaseProps의 className/style은 받되 실제로는 사용 안 함)

### 6.3 새 form 필드 추가 시 schema + defaults 동시 갱신

위 §3 참고. 한 쪽만 갱신하면 register 미연결 또는 초기값 누락.

### 6.4 카탈로그 페이지 등록 필수 + 300줄 초과 분리 검토

신규 표준 컴포넌트 추가 시 `front-end/src/app/(dev)/preview/sections/inputs/` 아래 해당 sub-section 파일에 추가. inputs-section.tsx는 49줄 오케스트레이터로 분리 완료(2026-05). 서브 섹션 파일 목록: `text-section.tsx` / `code-section.tsx` / `number-section.tsx` / `combo-section.tsx` / `date-section.tsx` / `time-section.tsx` / `link-radio-section.tsx`.

### 6.5 useCallback / useEffect deps 배열 갱신

ref나 새 변수를 hook 본문에 추가할 때 deps 배열 갱신 필수. lint `react-hooks/exhaustive-deps` 경고로 잡힘. (`useGridCellDrag` 사례: `onActiveRowChangeRef` 추가 시 deps 누락으로 lint FAIL).

### 6.6 로컬 wrapper 컴포넌트 제거 시 정의도 함께 삭제

표준 컴포넌트로 교체할 때 패널 내부에 정의된 로컬 wrapper(예: `PartyBlock`) 정의도 함께 삭제. 두 구현 공존 시 디자인 불일치.

### 6.7 인라인 style 일괄 제거

카탈로그 컴포넌트가 토큰 디자인 책임. 잔존 style은:
- BoxBaseProps `className`로 이전
- 또는 CSS 모듈/유틸 클래스로 이전
- gridTemplateColumns 같은 레이아웃 1-2건은 예외 허용 (주석 명시)

### 6.8 register spread + ref 충돌 검증

CodeBox는 `forwardRef`를 받고 codeProps를 spread하는 구조. `codeProps={...register("X")}` spread 안 ref가 input에 정상 연결되는지 빌드 후 실제 폼 submit 시 값 도달 확인 필요.

### 6.9 row.id 비교 시 String 변환

`onRowClick={(row) => setSelectedKey(String(row.id))}` + `rowClassName={(row) => String(row.id) === selectedKey ? "is-selected" : undefined}`

> **이유**: id가 number일 수 있는데 selectedKey state는 string. 명시적 String() 변환 누락 시 비교 항상 false.

### 6.10 GridList `onClearRow` 누락 금지

외부 클릭 시 행 강조 자동 해제 기능. 누락하면 한 화면에 여러 그리드가 있을 때 이전 그리드 강조가 잔존. `onClearRow={() => setSelectedKey("")}` 항상 추가.

### 6.11 Coder가 plan 모드 진입 시 메인에 보고 의무

서브 에이전트가 자체 plan mode에 진입하면 메인 에이전트는 즉시 사용자에게 알려야 함 (재호출 루프 금지). 메모리 규칙.

### 6.12 그리드 셀 native input 직접 사용 금지

`<input className="grid__cell-input">` 직접 사용 시 `autoComplete="off"`, focus 처리, required 표시 등을 매번 inline으로 챙겨야 함 → 누락 위험.

**올바른 패턴**: 표준 컴포넌트 cell variant 사용
- `<TextBox variant="cell" {...register(...)} />`
- `<NumberBox variant="cell" decimalPlaces={n} {...register(..., { valueAsNumber: true })} />`
- `<DateBox variant="cell" />` (Controller 사용 — DateBox에 cell variant 흡수됨, 2026-05)
- `<ComboBox variant="cell" options={...} {...register(...)} />`

> **SSOT 결정 (2026-05)**: `NumberBox/TextBox/DateBox/ComboBox/TimeBox variant="cell"` 통일. `TextCell`/`NumericCell`/`DateCell`(`shared/grid-cell-inputs.tsx`)은 `@deprecated` — House-BL·Master-BL 레거시 호환 유지용. 신규 작업에서 사용 금지. 단계적 마이그레이션 후 제거 예정.

기존 native 사용처(legacy)는 점진 마이그레이션 대상. 새 Entry 작업 시 처음부터 표준 컴포넌트 사용.

### 6.13 백엔드 관리 필드(status 등) hidden register

UI에 노출하지 않는 필드라도 schema/defaults에 있으면 `register("fieldName")` 단순 호출로 RHF tracking 등록. hidden input에 연결하거나 spread 불필요.

```tsx
// non-bl-entry.tsx 예시
register("status"); // 백엔드 관리 필드 — badge로만 시각화, form submit에 포함
```

### 6.15 ComboBox는 register spread 금지 — Controller 필수

`{...register("name")}` spread로 ComboBox에 직접 연결하면 onChange가 RHF에 연결되지 않아 **값이 form에 반영되지 않는 버그** 발생 (66a217c — Non B/L Entry/Cargo/Container/Document 전체 교체 사례).

**올바른 패턴**: §3의 ComboBox Controller 예제 참고.

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

### 6.20 위젯 편집 버튼 `type="button"` 명시

`field-item-grid`, `field-widget-list`, `field-widget-container` 등 위젯 내부 편집/토글 버튼에 `type="button"`을 명시하지 않으면 form 내부에 있을 때 **의도치 않은 submit이 트리거**됨.

사례: 66a217c — Non B/L Entry 위젯 토글 버그 수정.

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

### 6.14 Container pkgUnit — 자유 텍스트 정책

Container Info 그리드의 `pkgUnit` 컬럼은 비표준 단위(CTN/PCS/BAG 등) 가능성으로 **자유 텍스트 유지**. Cargo 패널의 `cargoUnit`(WeightUnit enum)과 다름. House-BL과 동일 정책.

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

**HouseBl/MasterBl/SwitchBl 적용 권장** — 본 SSOT를 그대로 따라 별도 작업으로 진행.

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

---

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

### 6.38 그리드 +/- 버튼 클래스 SSOT — `btn--icon btn--success` / `btn--icon btn--danger`

카탈로그 표준(`grid-preview-panel.tsx:221-231`) 정합 클래스:
- Plus(+): `<button className="btn btn--sm btn--icon btn--success">`
- Minus(-): `<button className="btn btn--sm btn--icon btn--danger">`

`btn btn--sm` 단독 또는 `btn btn--sm btn--ghost`는 카탈로그와 불일치 — 28px 정사각 + 성공/위험색 미적용.

신규 그리드 마이그레이션 시 즉시 적용. 사례: Truck Information(86e6493), Non B/L Dimension/Container Info(056b648).

### 6.39 그리드 행 삭제 — 포커싱 셀 행 우선 패턴 SSOT

**사용자 정의 "선택한 행" = 마우스/Tab으로 셀에 포커싱 중인 행**. 외부 `selectedKey` state만으로는 행 클릭 → setSelectedKey 외부 sync까지 도달하는 흐름이 신뢰성 부족(셀 input 없는 컬럼 클릭/외부 callback stale 등). mousedown 시점 `document.activeElement.closest("td[data-row-key]")`로 직접 capture.

```tsx
const focusedRowKeyRef = useRef<string | null>(null);

function captureFocusedRow() {
  const activeEl = document.activeElement as HTMLElement | null;
  const td = activeEl?.closest("td[data-row-key]") as HTMLElement | null;
  focusedRowKeyRef.current = td?.dataset.rowKey ?? null;
}

function handleRemove() {
  if (fields.length === 0) return;
  const focused = focusedRowKeyRef.current;
  let targetIdx = -1;
  if (focused !== null) {
    targetIdx = fields.findIndex(f => String((f as { id: number | string }).id) === focused);
  }
  if (targetIdx === -1 && selectedKey !== null && selectedIdx !== -1) {
    targetIdx = selectedIdx;
  }
  if (targetIdx === -1) targetIdx = fields.length - 1;
  remove(targetIdx);
  setSelectedKey(null);
  focusedRowKeyRef.current = null;
}

<button type="button" className="btn btn--sm btn--icon btn--danger" onMouseDown={captureFocusedRow} onClick={handleRemove} disabled={fields.length === 0}>
  <Minus size={12} />
</button>
```

우선순위: **포커싱된 td의 row → selectedKey state → 마지막 행**. 모든 그리드 패널 + 카탈로그(`grid-preview-panel.tsx`)에 일관 적용. 사례: aefe8e9·746a304.

### 6.40 GridList outside-click 가드 — 같은 패널 내 클릭은 selection 유지

`use-grid-cell-selection.ts`의 `handleOutsideClick`이 테이블 element 바깥의 모든 mousedown을 outside로 처리해 `onClearActiveRow`로 selection을 즉시 풀어버리는 문제. panel head의 +/- 버튼 클릭 시에도 mousedown으로 selection이 풀려, 같은 mousedown으로 발생한 handleRemove는 항상 마지막 행만 삭제하던 버그.

해결: `e.target.closest(".panel") === tableEl.closest(".panel")`이면 outside로 처리하지 않음.

```ts
function handleOutsideClick(e: MouseEvent) {
  const tableEl = getTableRef.current();
  if (tableEl?.contains(e.target as Node)) return;
  // 같은 패널 안의 외부(예: panel__head의 +/- 버튼) 클릭은 outside로 보지 않음
  const targetEl = e.target as HTMLElement | null;
  const samePanel = targetEl?.closest(".panel");
  const tablePanel = tableEl?.closest(".panel");
  if (samePanel && tablePanel && samePanel === tablePanel) return;
  selectedRangeRef.current = null;
  copiedRangeRef.current = null;
  applyOverlay();
  applyCopiedOverlay();
  onClearActiveRowRef.current?.();
}
```

사례: 5f48185.

### 6.41 useVirtualizer `getItemKey` row.id 기반 — 행 삭제 후 stale input value 방지

TanStack `useVirtualizer`의 default `getItemKey`는 **index 기반**. 행 삭제 시 같은 dataIndex 위치에 들어온 새 row를 React가 같은 key로 인식해 `<input>` element를 **재사용** → `register()` uncontrolled로 RHF가 DOM input.value를 sync하지 않아 **이전 row의 값이 셀에 그대로 남음**.

해결: `useVirtualizer`에 `getItemKey` 추가(PlainGridList + ManagedGridList 둘 다):

```ts
const rowVirtualizer = useVirtualizer({
  count: data.length,
  getScrollElement: () => scrollRef.current,
  estimateSize: () => ROW_HEIGHT_PX,
  overscan: 30,
  measureElement: (el) => el?.getBoundingClientRect().height ?? ROW_HEIGHT_PX,
  getItemKey: (index) => {
    if (rowKey) {
      try { return String(rowKey(data[index], index)); } catch { return index; }
    }
    const idVal = (data[index] as Record<string, unknown> | undefined)?.id;
    return idVal != null ? String(idVal) : index;
  },
});
```

row identity가 바뀌면 React가 element를 unmount/remount → register는 mount 시 RHF의 latest value를 input.defaultValue로 적용 → 정상 표시. GridList에 적용 완료 — 모든 호출처 자동 정상화. 사례: 12e3047.

### 6.42 `.li__input--tight` 자식 width 고정 — inline `flex: "0 0 80px"` 덮어쓰기

`forms.css`의 `.li__input--tight > * { flex: 1 1 0; min-width: 0; }`이 자식 모두에 동일 비율을 강제. 특정 자식만 width 80px 고정하려면 인라인 `flex: "0 0 80px"`로 덮어써야 함.

- **ComboBox**: 외곽 `.combo` div에 style prop 적용 → 직접 전달
  ```tsx
  <ComboBox variant="panel" style={{ flex: "0 0 80px" }} options={opts} {...} />
  ```
- **CodeBox**: 외곽 `.lcn`/`.party-block`에 style 미적용 → 외부 wrapper div 필요
  ```tsx
  <div style={{ flex: "0 0 80px" }}>
    <CodeBox kind="code-only" variant="panel" codeProps={...} />
  </div>
  ```

사례: Truck Cargo Package/G/W 우측 박스 80px 고정 (91bcd90).

### 6.43 CodeBox `kind="lcn"`/`code-only` width 좁힐 때 `.lcn` grid 해제 필요

`.lcn`(`forms.css:202`)은 `display: grid; grid-template-columns: 110px 120px minmax(0, 1fr);`. wrapper width가 80px이어도 grid 첫 column 110px이 강제되어 시각적으로 깨짐.

해결: 패널 외곽 div에 식별 클래스 부여 + 해당 스코프 안 `.lcn` grid 해제. CodeBox 자체는 수정 금지(Non B/L 호환 유지).

```tsx
// truck-cargo-panel.tsx
<div className="panel truck-cargo-panel" ...>
```

```css
/* widgets.css */
.truck-cargo-panel .li__input--tight > div > .lcn { display: block; width: 100%; padding: 0; gap: 0; }
```

사례: 0710cb7.

### 6.44 CodeBox kind 명칭 컨벤션 — LCN / code-only / party-cn

사용자가 코드박스 형태를 부를 때 사용하는 표준 명칭은 컴포넌트의 `kind` prop과 1:1 매칭:

| 형태 | 부르는 말 | CodeBox kind |
|---|---|---|
| Label + Code + Name 한 줄 | **LCN** (lcn 박스, lcn) | `kind="lcn"` |
| Code 한 칸만 | **code-only** (코드 온리) | `kind="code-only"` |
| Party 행 전체(외곽 .party-block까지 자체 렌더) | **party-cn** | `kind="party-cn"` |

사용자가 "이 필드를 LCN으로 만들어줘"라고 하면 `<CodeBox kind="lcn" label="..." codeProps={...} nameProps={...} />` 패턴. LCN의 Name 필드가 schema에 없으면 nameProps 미지정으로 빈 input 유지(추후 master 데이터 join 정책 — Truck Performance customerPic 사례, 22a2886).

메모리: `feedback_codebox_terms.md`.

### 6.45 enum option label이 description인 경우 → 프론트에서 `label = value` 재매핑

`EnumRegistryFactory`의 매핑이 `e -> new EnumOption(e.getCode(), e.getDescription(), e.getDescription())` 형태인 enum(예: `ContainerType`, `CargoType`, `Per`, `Fhd`, `FlightType`, `FreightCondition`, `HandlingInfoCode`)은 옵션 label에 긴 설명이 들어옴. UI에서 짧은 코드만 표시하려면 프론트에서 재매핑:

```tsx
const { options: rawOptions } = useEnumOptions("ContainerType");
const containerTypeOptions = useMemo(
  () => rawOptions.map(o => ({ value: o.value, label: o.value })),
  [rawOptions]
);
```

사례: Non B/L Container Info `contType`, Truck Information `containerType`(d0dcf1b).

### 6.46 ComboBox cell variant도 register spread 금지 — Controller 필수

§6.15(패널 variant)와 동일하게 **cell variant도 Controller 필수**. 그리드 셀 ComboBox에 `register()` spread 시 외부 `value` prop이 주입되지 않아 ComboBox 내부 `strValue`가 항상 `""` → 선택해도 표시·반영 안 됨.

```tsx
// ❌ register spread — value 미반영
<ComboBox variant="cell" options={opts} {...register(`array.${i}.field`)} />

// ✅ Controller — value/onChange 직접 전달
<Controller
  name={`array.${i}.field`}
  control={control}
  render={({ field }) => (
    <ComboBox variant="cell" options={opts} value={field.value} onChange={field.onChange} />
  )}
/>
```

> **함정**: 카탈로그(`grid-preview-panel.tsx:117`)는 register spread 형태로 쓰여있지만 실제 form 바인딩 검증 없음. 카탈로그 코드를 복사할 때는 Controller 패턴으로 교체.

사례: Truck Information TruckType/ContainerType ComboBox(d0dcf1b).

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

---

## 7. CSS 토큰화 디자인 (참고 위치)

- `front-end/src/styles/forms.css` — `.lcn`, `.lcn__label`, `.lcn__code`, `.lcn__name`
  - `.lcn`: grid 3열(110px / 120px / 1fr), gap 8px
  - `.lcn__label`: text-align right, padding-right 8px
- `front-end/src/styles/components.css` — `.party-block`, `.party-cn`
  - `.party-block__head > span:first-child`: min-width 110px, flex-shrink 0
- `front-end/src/styles/grids.css` — `.grid__cell-input`, `.grid-selection-overlay`, `.grid__resize-handle`
  - 셀 input focus 시 background만 (inset ring 제거됨)
  - is-required focus 시 inset 좌측 bar만 (외곽 ring 제거됨)
- `front-end/src/styles/forms.css` — `.li__input--tight`
  - 자식 요소 flex:1 분배 적용. NumberBox·ComboBox가 같은 row에 있을 때 겹침 해소 (66a217c)
- `front-end/src/app/globals.css` — Tailwind v4 `@theme` 블록 + `--animate-wave-bar` + `@keyframes waveBar`
  - ScreenGuard 막대 wave 애니메이션 (1s ease-in-out, scaleY 0.35 ↔ 1, 막대당 0.12s delay)
  - `@media (prefers-reduced-motion: reduce)` 분기에서 `animation: none` + `--static-h` 변수 기반 정적 높이 표시 (§6.27)

---

## 8. 검증 절차

```powershell
npm --prefix front-end run lint
npm --prefix front-end run build
```

### 빌드 통과 후 확인

- `/(dev)/preview` Inputs/Grid 섹션 — 신규/변경 컴포넌트 회귀
- 대상 Entry 화면 실 화면 회귀:
  - 모든 panel 필드 panel variant 표시 일관성
  - 날짜 캘린더 picker + yyyyMMdd 마스킹
  - ComboBox enum 로딩 (네트워크 탭 확인)
  - Party / LCN 코드 입력 → name 자동 채움 동작 (lookup 모달 wire 시)
  - NumberBox 소수점 포맷 (0/3)
  - 그리드: 셀 클릭 → overlay/행 강조 → 다른 그리드 클릭 → 해제 확인
  - 그리드: 컬럼 헤더 경계 드래그 → 너비 조정 → 셀 overlay 추적 확인
  - Form submit 시 모든 필드 값 도달 확인 (개발자 도구 Network)

### 시각 회귀

이미 마이그레이션 완료된 sea-house / air-house Entry와 토큰 일관성 비교.

---

## 9. 커밋 메시지 작성 시

PowerShell heredoc 사용 시 한글 메시지에 backtick(`) 포함하지 말 것 → PS 변수 expansion 오류. Bash heredoc 또는 일반 -m 사용 권장.

```bash
git commit -m "feat: <요약>

<상세>

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## 10. 이미 완료된 공통 작업 (다른 Entry에서 재호출 가능)

다음은 이미 적용되어 있어 다른 Entry 마이그레이션 시 재구현 불필요:

- DateBox 카탈로그 컴포넌트 (`shared/inputs/date-box.tsx`) — **`variant="cell"` 지원 추가됨 (2026-05)**
- BoxBaseProps 표준 (`shared/inputs/_types.ts`)
- **표준 입력 컴포넌트 default `autoComplete="off"`** (TextBox/NumberBox/CodeBox/DateBox + grid-cell-inputs.tsx의 TextCell/NumericCell/DateInputBase) — cell variant 사용 시 자동 적용. Native `grid__cell-input` 직접 사용 시 §6.12 참고
- **그리드 셀 SSOT 결정**: TextBox/NumberBox/DateBox/ComboBox/TimeBox `variant="cell"`. TextCell/NumericCell/DateCell은 `@deprecated` (§6.12 참고)
- 그리드 셀 편집 모드 단일 outline 처리 (`grids.css`)
- 그리드 외부 클릭 selection 자동 해제 (`use-grid-cell-selection.ts`)
- 그리드 외부 클릭 행 highlight 해제 (`onClearRow` prop)
- PlainGridList 컬럼 리사이즈 (session-local)
- selection overlay td DOM 실측 보정
- 컬럼 리사이즈 후 overlay 자동 갱신 (ResizeObserver)
- CodeBox lcn / party-cn 라벨 너비 통일
- **ComboBox `variant="label"` 지원** — LcnLabel 라벨 셀렉터 위임 (2026-05-08, 00a9a16)
- **BoxVariant 타입 확장** — "panel" / "cell" / "label" 3종 (2026-05-08, 3c78d06)
- **ComboBox RHF Controller 패턴 SSOT** — register spread 금지, Controller 필수 (2026-05-08, 66a217c)
- **Non B/L Entry hot-marker 라우팅 표준화** — F5 클리어 / 저장 직후 재조회 / New URL 리셋 / Search hot-marker 재사용 / unmount clearDraft (2026-05-09, c918324·0526e83·4572a33)
- **Entry form Enter 키 implicit submission 차단** — `form onKeyDown` 가드(TEXTAREA 제외) + Save 버튼 `type="button"·onClick` 이중 방어 (2026-05-09, faad158)
- **CreateNonBlRequest validation 어노테이션 UI required 외 제거** — UI required 아닌 필드는 어노테이션 없이 null 통과. VO 검증 유지. ⚠️ 0aafb4d는 UI required 9개도 함께 제거한 over-correction. (2026-05-09, 0aafb4d)
- **CreateNonBlRequest UI required 9개 어노테이션 복원** — §6.25 정책 교정. `hblNo`/`workDivision`/`polCode`/`podCode`/`etd`/`eta`/`actualCustomerCode`/`operatorCode`/`teamCode`에 `@NotBlank`+형식 어노테이션 재부착. (2026-05-09)
- **List 더블클릭 → Entry 단건 조회 모드 진입 (§6.26)** — Non B/L List·Truck B/L List 더블클릭 시 hot-marker SET 후 path-param push. Truck B/L 백엔드 `GET /api/truck-bl/{id}` 신규 추가(`TruckBlUseCase`, `TruckBlService`, `TruckBlFactory`, `TruckBlDetailResult`, `TruckBlDetailResponse`). Truck Entry edit-mode 결선(id prop, hydrateAllowed, useQuery, form.reset, F5-redirect, clearDraft). (2026-05-09)
- **Non B/L Entry body 콤보박스 4종 + Ref.No 저장 누락 보정 (2026-05-09)** — `volumeDivisor` Non B/L 한정 도입(`HouseBlNonBl`), `salesClass`·`pkgUnit`·`containerType` projection 복원, `mblNo`/`masterRefNo` `assignMasterReference` 호출 보강, `dimensionDivisor` FE Controller 전환. §12.1에 항공 공통화 검토 메모 추가.
- **Non B/L 단건 조회 desc LEFT JOIN 제거 + remark 필드 이전 (2026-05-10, f632ee9)** — `HouseBlJpaEntity.desc` `@OneToOne mappedBy` 의 EAGER 한계로 `findById` 시 `house_bl_desc` LEFT JOIN이 자동 발사되던 현상. NON_BL 화면이 desc(marks/description/descClause1/descClause2) 미사용 사실 확인 후, NON_BL 단건 조회를 `NonBlSearchPort.findNonBlById` QueryDSL projection 으로 분리하고 `house_bl_desc` JOIN 차단. NON_BL 의 `remark` 필드는 `house_bl_desc.remark` 대신 `house_bl_non_bl.remark` 로 이전 (DDL `V_add_house_bl_non_bl_remark.sql` 신설). 응답·요청 DTO(`NonBlDetailResponse`/`CreateNonBlRequest`/`UpdateNonBlRequest`)에서 desc 객체 제거하고 본체에 `remark` 평탄화. SEA/AIR/TRUCK 무영향(`HouseBlSubFactory.applyDesc` 에 `instanceof HouseBlNonBl` 가드 추가). FE 도메인 타입·`buildNonBlRequest`·Entry `methods.reset`·`adapter/out/api/non-bl.ts` 매핑 동기화.
- **Non B/L Entry remark 바인딩 후속 fix (2026-05-10, 16753a4·93f7e68)** — f632ee9 후 zod schema에 `remark` 누락 + Entry 화면 `methods.reset` 매핑 미정합으로 저장 후 조회 시 remark 미표시. zod schema에 `remark` 추가 + desc 잔재 제거 + 화면 설정 값 정합 보정.
- **Non B/L Entry 자식 collection UPDATE 쿼리 누락 fix (2026-05-09, 67aa1d4·2eefab1·0e55a28)** — Dimension·Container·Desc 자식 row의 PUT 페이로드에 DB id가 빠져 백엔드 매퍼가 신규로 인식 → UPDATE 미발사. FE 자식 행에 `id` 포함 + 백엔드 어댑터 매퍼 보정 + 어댑터 테스트를 `merge*` 메서드 호출 검증으로 갱신. (§6.28 SSOT 함정으로 등재)
- **Entry 화면 house_bl_id URL 노출 제거 (2026-05-09, 1482b65)** — Entry path-param에 내부 PK(`house_bl_id`)가 노출되던 동작을 `hbl_no` 기반 식별로 통일. List 더블클릭 → Entry 진입(§6.26) 패턴과 정합.
- **Entry 라우팅·사이드바·탭 stable 동작 fix (2026-05-09, d084934·14250d7·b393795)** — (1) 사이드바 메뉴 이동 시 Entry store 유지·탭 닫기 시 store clear, (2) List 더블클릭 Entry 이동 시 List 탭이 소멸하던 버그, (3) 동일 케이스에서 사이드바 섹션이 닫히던 버그 모두 수정. §6.26 보강.
- **§6.25 SSOT DDL 정합 후속 (2026-05-09, dd3f3b2·3510adc)** — Non B/L Entry 저장 시 백엔드 검증 SSOT 처리 + `house_bl_container.container_no` JPA `nullable=false` 제거 (DDL 마이그레이션과 정합).
- **ScreenGuard 공통 컴포넌트 도입 + Entry 4개 적용 (2026-05-10)** — 비동기 처리 중 화면 잠금 + bar wave 인디케이터 표준화. `@/components/shared/screen-guard` 신규, Tailwind v4 `globals.css` `@theme` 블록에 `waveBar` keyframes/animation 추가, `/preview` 카탈로그에 ScreenGuard 섹션 등록. Non B/L · Truck B/L · House B/L · Master B/L Entry 4개에 `<ScreenGuard visible={isLoading} message={loadingMessage} />` 패턴으로 일괄 적용. **List 화면 적용 금지** (스켈레톤 로딩 사용). 막대 색 `#38BDF8`, 텍스트 색 `#F1F5F9` 가독성 fix. (§6.27 SSOT)
- **House B/L 계열 Entry hbl_no Update 차단 (2026-05-10)** — 모든 House B/L 계열(House BL Sea/Air/Truck, Non B/L)의 toolbar `hbl_no`는 **Insert·조회 입력 전용**이며 update 시 변경 불가. 차단은 **백엔드 SSOT**: `HouseBl.update()` 의 hblNo PATCH 라인 제거 + `HouseBlUpdateFields`·`UpdateHouseBlCommand`·`UpdateHouseBlRequest`·`UpdateNonBlRequest`·`HouseBlFactory`·`HouseBlAssembler`·`NonBlAssembler` 의 hblNo 필드/매핑 제거. FE toolbar `hbl_no`(`hbl`/`nonBlNo`) TextBox는 readOnly 금지(조회 키 입력 + 신규 저장 용도). FE submit은 update 전용 빌더(`buildHouseBlUpdateRequest`/`buildNonBlUpdateRequest`)에서 destructure로 hblNo를 제외해 payload에서 빠진 채 전송 — `isEdit` 인자 패턴 금지(분기 책임은 entry mutation이 가진다). 회귀 보호로 `HouseBlTest.update_doesNotChangeHblNo` 추가.
- **Non B/L Entry Change BL No 모달 + 전용 엔드포인트 (2026-05-10)** — 일반 Update 경로는 hbl_no 변경 차단(위 항목)을 유지한 채, 사용자 명시 변경 액션을 위한 **별도 격리 엔드포인트** `PUT /api/non-bl/{id}/hbl-no` body `{hblNo}` → `ApiResponse<Void>` 신설. 도메인은 `HouseBl.changeHblNo(BlNumber)` 추상 부모에 두어 재사용 가능, 본 작업은 `NonBlController`만 노출(다른 모드는 application 레벨에서 미노출). `NonBlService.changeNonBlHblNo`가 `findNonBlDomainById`로 NON_BL jobDiv 보장 후 `HouseBlService.changeHblNo`에 위임. validation BE SSOT — `ChangeNonBlHblNoRequest` `@NotBlank`+`@Size(max=50)`. **PK(`house_bl_id`) 단일 행 select & update**로 동일 hbl_no가 여러 행에 존재해도 다른 행 무영향(통합 테스트 `NonBlChangeHblNoIntegrationTest`로 회귀 보호). FE 신규 컴포넌트 `change-bl-no-modal.tsx`(현재 hbl_no readOnly + 신 hbl_no 입력 + Update/Close), `nonBlPort.changeHblNo` 어댑터, Entry toolbar 버튼 가드 — 미조회 상태 클릭 시 `toast.info("먼저 Non B/L을 조회해주세요.")` 후 차단(disabled 속성 미사용, 조회된 경우만 모달 오픈). 모달 입력은 zodResolver/required 금지(BE SSOT). 성공 시 `["non-bl","detail",id]` + `["non-bl","list"]` invalidate.
- **HouseBl desc cascade 분리 — Repository 명시 save/delete 전환 (2026-05-10, 701b5e2)** — §587(NON_BL 한정)을 SEA/AIR까지 확장. `HouseBlJpaEntity`의 `@OneToOne(cascade=ALL, orphanRemoval=true) desc` 매핑과 `mergeDesc`/`replaceDesc` 메서드 제거. `HouseBlDescRepository` 신설(`findByHouseBl_HouseBlId`/`deleteByHouseBl_HouseBlId`). `HouseBlPersistenceAdapter`는 SEA/AIR 단건 조회 시 desc 1회 별도 SELECT, NON_BL/TRUCK은 desc 미사용이므로 조회 생략. 저장 경로는 `saveOrDeleteDesc(domainDesc, parentJpa)` private 메서드로 통일 — 도메인 desc=null이면 row 삭제, 있으면 기존 row UPDATE 또는 신규 INSERT. 삭제 경로는 FK ON DELETE CASCADE 미설정으로 `deleteByHouseBl_HouseBlId` 명시 호출. `HouseBlJpaToDomainMapper.toSeaDomain/toAirDomain` 시그니처에 `HouseBlDescJpaEntity descJpa` 인자 추가.
- **Change B/L No 흐름 QueryDSL 부분 UPDATE 단축 + form stale fix (2026-05-10, 86d4406·d7eb0b9)** — §595 후속. (BE) `HouseBlPort.updateHblNoById` 신설, `HouseBlRepositoryImpl`에 QueryDSL `update().set(hblNo).where(houseBlId=:id AND jobDiv=:div).execute()` 첫 도입. `HouseBlService.changeHblNo`·`NonBlService.changeNonBlHblNo` 본문을 도메인 hydrate→full save에서 port 직접 호출로 교체. jobDiv 검증을 UPDATE WHERE 절에 포함하고 affected=0이면 `ResourceNotFoundException`. bulk UPDATE 1차 캐시 stale 방지 위해 `EntityManager.flush()/clear()` 호출. NON_BL Change BL No 기준 SELECT 7회 → 0회, UPDATE 1회. (FE) `ChangeBlNoModal`에 `onChanged?: () => void` 콜백 prop 추가, `NonBlEntry`에서 `onChanged`로 `detailLoadedRef.current=false` 트리거 → invalidate 후 refetch된 detail이 `form.reset`에 반영. list 캐시 자동 invalidate 라인 제거(§6.21 준수).
- **Non B/L Entry update 누락 필드 후속 fix (2026-05-10, a71bb0d·55b53e5)** — §588 후속. (a71bb0d) `updateNonBl` 경로에 `bound`/`workDivision`이 빠져 변경이 사라지던 현상 — `HouseBlUpdateFields` record + `HouseBl.update()`에 bound PATCH 분기 추가, `HouseBlNonBl.updateWorkDivision()` 신설, `HouseBlFactory.toUpdateFields()` bound 매핑 + `applyNonBlUpdate()` workDivision 갱신 호출 보강. (55b53e5) FE Non B/L Entry `form.reset`에 `refNo: detail.originalBlRef` 매핑 추가(저장 후 화면 미반영 수정). `toBeRequest`의 cargo `volumeWtKg` JSON 키를 BE 명세 `volumeWeightKg`로 정합(BE Jackson 역직렬화 누락 → DB null 덮어쓰기 현상 해소).
- **Non B/L Entry Search EXACT PK 조회 endpoint 분리 (2026-05-10)** — Entry Search가 List용 `searchNonBlSummaries`(LIKE+COUNT, JOIN+22 projection)를 재사용하던 흐름을 폐기하고 `POST /api/non-bl/find-by-hbl-no` 신설. UseCase/Port/Repository에 `findNonBlKeysByHblNoExact(String): List<Long>` 추가, JOIN/projection/COUNT 모두 제거하고 `select house_bl_id where job_div='NON_BL' and hbl_no=? order by created_at desc limit 2`만 발사. FE `nonBlPort.findByHblNo` 어댑터 추가, Entry `handleSearch`의 30개 dummy filter 객체 제거. Detail 로드는 기존 useQuery `getById` 흐름이 그대로 담당. SQL 2회 → 1회. (§6.19 갱신)
- **Non B/L Entry Update 응답용 중복 reload 제거 (2026-05-10)** — Non B/L Update 1회당 p6spy SELECT 6회 발생 분석. dirty checking 사전 fetch(parent + ext entity, line 46/189/126)는 필수, containers/dims lazy(line 81/85)는 응답에 필요하므로 유지. 진짜 redundant한 두 곳만 제거: (A) `HouseBlPersistenceAdapter.saveHouseBl` 본문 끝의 `return loadWithExt(savedJpa)` — NON_BL case 한정으로 `return nonBl;` (in-memory 직접 반환)으로 단축, `BaseEntity.assignIdentity`로 parent id + 감사 필드 sync, 신규 private `syncChildIds`/`syncDimIds`로 cascade flush 후 자식 PK를 도메인 자식에 역방향 인덱스 매핑 sync. SEA/AIR/TRUCK case는 기존대로 `return loadWithExt(savedJpa)` 경로 유지(회귀 차단). (B) `NonBlService.updateNonBl`의 `houseBlUseCase.updateHouseBl` 호출 + `findNonBlDomainById` 응답 재조회 둘 다 제거 — `NonBlService`가 `HouseBlPort` + `HouseBlFactory`를 직접 사용해 `findHouseBlById → applyToEntity → saveHouseBl`을 수행하고 반환 도메인을 `(HouseBlNonBl)` 캐스팅해서 `NonBlDetailResult.from(...)` 호출. ARCH1 — `HouseBlUseCase`에 도메인 노출용 메서드 추가는 금지(application↔application 참조는 허용). 결과: **SELECT 6 → 5회 (실측)**. line 141의 `loadWithExt` 자체는 자체 SELECT를 만들지만 그 내부 라인(189/81/85)은 사전 로드 시 이미 1차 캐시·lazy 초기화 완료되어 두 번째 호출에서 SELECT 미발사 → 실제 절감은 NonBlService 응답 재조회(#6, line 157의 QueryDSL fetchOne) 1건. dirty checking 사전 fetch(#5, line 126)는 영속성 컨텍스트 managed 상태 확보 목적이라 변경 데이터 유무와 무관하게 발사되며, 본 작업에서는 의도적으로 유지(추가 제거는 옵션 3 — 사전 로드된 ext JPA를 saveHouseBl 시그니처로 전달해 1차 캐시 히트 유도, 별도 plan). UPDATE 변경 없음(house_bl + house_bl_non_bl + container/dim merge). 테스트 정리 — `HouseBlPersistenceAdapterTest`의 NON_BL save 3개에서 `jpaToDomainMapper.toNonBlDomain` stubbing 라인 제거(`UnnecessaryStubbingException` 해소). `loadWithExt` 메서드 자체는 SEA/AIR/TRUCK 및 `findHouseBlById` 경로에서 그대로 사용.
- **Non B/L Entry 신규 저장 응답 detail 재조회 제거 (2026-05-10, 608d0df·1be98fb·7e5e41d)** — POST `/api/non-bl` 응답 body를 `ApiResponse<NonBlDetailResponse>` → `ApiResponse<{id}>`로 축소, `nonBlUseCase.findNonBlById(id)` 재조회 호출 제거 → BE 응답 흐름 SELECT 3건(house_bl×non_bl join + containers LAZY + dims LAZY) 절감. 화면 detail 갱신은 컴포넌트 `onSuccess`의 `setFocus("nonBl", saved.id)` → useQuery `getById` refetch가 담당. FE adapter `non-bl.ts` `create` zod 파싱 스키마 `NON_BL_DETAIL_SCHEMA` → `apiResponse(z.object({ id: z.number() }))`, `NonBlPort.create` 반환 `Promise<NonBlDetail>` → `Promise<{id: number}>` 동시 정합. `non-bl-entry.tsx`는 `saved.id`만 사용하므로 무수정. web layer 테스트 1건(`createNonBl_happyPath_returns201WithIdAndLocation`) 추가 — Mockito `any(Class)` 매처는 null 미매칭 함정이 있어 `any()` 사용. (§6.29 SSOT 함정으로 등재)
- **HouseBl 삭제 SELECT 최적화 시리즈 (2026-05-10, 2920bf9·07dd2dd·d9f9cd4)** — NON_BL 삭제 SQL 분석 후 단계 최적화. (1) 2920bf9: `deleteHouseBl`에 JobDiv 분기 도입 + ext bulk delete (work_division 무관 4개 ext 모두 SELECT-then-delete 패턴 폐기, NON_BL 삭제 SELECT 12→8회). (2) 07dd2dd: 5개 ext Repository(sea/air/truck/non_bl/desc)의 derived `deleteByXxx`를 `@Modifying @Query` JPQL bulk DELETE로 교체 — derived deleteBy의 SELECT-then-delete 패턴 회피. (3) d9f9cd4: bulk DELETE의 `clearAutomatically=true` 옵션 제거 — 1차 캐시 stale 회복으로 부모 deleteById의 cascade fetch가 캐시 hit (8→7회). (§6.30 SSOT 함정 등재)
- **ER 재구조화 Phase 1 — 단독 자식 ext FK 이전 (2026-05-10·11)** — HouseBl/MasterBl의 1:N 자식이 부모 FK로 매달려 있어 cascade orphanRemoval로 JobDiv 무관 모든 자식 LAZY fetch가 트리거되는 근본 문제 해결의 첫 단계. 단독 사용 자식 5개를 ext PK FK로 이전(테이블명 유지). Step 1.1 schedule_leg→AIR (9bb5436·1a4207f), 1.2 air_charge→AIR (7cb1281), 1.3 truck_order→TRUCK (3335073), 1.4 master_schedule_leg→AIR (3884e83), 1.5 master_air_charge→AIR (14beff9·b9491b8). 각 step 마이그레이션은 컬럼 추가/백필/NOT NULL/FK CASCADE/기존 FK·컬럼 제거 5단계. AIR/TRUCK 분기 호출 순서: ext save → sync* (PK 확보 후, `savedAirJpa`/`savedTruckJpa` 변수 도입). (§6.32 SSOT 등재)
- **ER 재구조화 Phase 2 — desc 분리 (2026-05-10·11, c07f701·b0ebc0f·7b6d0b6)** — desc는 여러 JobDiv 공유 자식이라 테이블 자체를 ext별로 분리. Step 2.1: HouseBl `house_bl_desc` → `sea_desc`+`air_desc`+`truck_desc` 3개. Step 2.2: MasterBl `master_bl_desc` → `sea_desc`+`air_desc` 2개. NON_BL은 desc 미사용. 도메인 `HouseBlDesc`/`MasterBlDesc` 그대로, adapter/mapper만 ext별 분기. `deleteHouseBl`에서 desc 명시 호출은 ext bulk DELETE에 위임. 7b6d0b6에서 5개 desc JPA에 `@OnDelete(CASCADE)` 추가는 H2 `ddl-auto` 재생성 회피 목적이었으나, 후속 옵션 D 작업에서 DDL_RULES §5 위반으로 모두 제거됨(§6.31 갱신 SSOT 참고). (§6.32 SSOT 등재)
- **ER 재구조화 Phase 3 — container 분리 (2026-05-10·11, 7d61e4b·ec6d06f)** — container는 SEA + NON_BL 공유 자식. `house_bl_container` → `house_bl_sea_container`+`house_bl_nonbl_container` 2개로 분리. SEA는 sync 패턴(`syncContainers`), NON_BL은 merge 패턴(`mergeContainers`, id 매칭 update + 신규 insert) 보존. SeaHouseRepositoryImpl QueryDSL Q타입/FK 경로 변경(`QHouseBlContainerJpaEntity` → `QHouseBlSeaContainerJpaEntity`, `houseBlId` → `houseBlSeaId`). ec6d06f: 신규 container entity 2개의 `@NoArgsConstructor` public 접근성 fix(PROTECTED → public, 다른 패키지 mapper의 `new` 호출 컴파일 오류 회피). MasterBl은 container 미사용으로 영향 없음.
- **ER 재구조화 Phase 4 — dim 분리 (2026-05-11, 2b7f35e·a3e3a6c·afb0066)** — dim 처리: HouseBl(공유)는 분리, MasterBl(단독)은 FK만 이전. Step 4.1: HouseBl `house_bl_dim` → `air_dim`+`truck_dim`+`nonbl_dim` 3개로 분리, AIR/TRUCK은 sync 패턴, NON_BL은 merge 패턴. Step 4.2: MasterBl `master_bl_dim` 테이블 유지 + FK만 `master_bl_id` → `master_bl_air_id` 이전(AIR 단독, Phase 1 패턴). 모든 신규 ext 컬렉션 매핑에 `cascade=ALL`+`orphanRemoval=true` 적용(ORM 레벨, save 경로 의존). DB `ON DELETE CASCADE`/JPA `@OnDelete`는 후속 옵션 D 작업에서 모두 제거됨(§6.31 갱신 SSOT). 도메인 `HouseBlDim`/`MasterBlDim` 그대로.
- **ER 재구조화 최종 상태 (2026-05-11, Phase 1~4 완료)** — 모든 1:N/1:1 자식이 의미상 속한 ext의 PK FK로 매달림. 부모 `house_bl`/`master_bl`의 cascade 컬렉션 매핑 0개 → JobDiv 무관 LAZY fetch 0건. **자식 정리는 후속 옵션 D 작업에서 어댑터 명시 bulk DELETE로 전환**(§6.31 갱신 SSOT). **기술 부채 보고**: `HouseBlPersistenceAdapter.java` 347줄, `MasterBlMapper.java` 338줄 — 300줄 임계 초과(500 미만으로 강제 분리 X). 후속으로 `HouseBlPersistenceAdapter`의 `saveOrDelete<Sea/Air/Truck>Desc` 추출 + `MasterBlMapper` → `MasterBlDocMapper` 분리 검토 권장.

- **DDL_RULES §5 정합화 + ID-only 삭제 흐름(옵션 D) (2026-05-11)** — ER 재구조화 Phase 2~4가 DB `ON DELETE CASCADE`+JPA `@OnDelete`에 의존하던 흐름을 §5 준수로 전환. 16개 ext FK의 `ON DELETE CASCADE` 제거(`schema/V1__fms_initial_schema.sql` 통합본으로 14개 마이그레이션 파일 정리). 10개 JPA(desc 5 + ext 5)의 `@OnDelete` 어노테이션·import 제거. `HouseBlPort`/`MasterBlPort`의 `delete<X>Bl(<X>Bl)` 시그니처 폐기 → `deleteByIdAndJobDiv(Long id, JobDiv jobDiv)`+`findJobDivById(Long): Optional<JobDiv>` 신설. Adapter는 `switch(jobDiv)` 분기마다 자식 subquery bulk DELETE → ext bulk DELETE → 부모 bulk DELETE 순서로 명시 호출. Service 계층은 `findJobDivById` projection 1회 후 port 직접 호출(기존 `findEntityById`/`loadWithExt` 호출 제거). `NonBlService.deleteNonBlById`는 자기 jobDiv 알므로 `houseBlPort.deleteByIdAndJobDiv(id, JobDiv.NON_BL)` 직접 호출(HouseBlUseCase 우회). 자식 Repository 13곳에 `deleteByParentHouseBlId`/`deleteByParentMasterBlId` subquery bulk DELETE 메서드 추가(11개 신규 Repository + 5 desc Repository 메서드 추가). 테스트는 cascade 검증을 어댑터 명시 호출 검증으로 일괄 교체(`HouseBlPersistenceAdapterTest`/`MasterBlPersistenceAdapterTest`/`HouseBlServiceTest`/`MasterBlServiceTest`/`MasterBlMappingIntegrationTest`+신규 `NonBlServiceTest`). 성능: NON_BL 삭제 기준 SELECT 4 + DELETE 4 → SELECT 0 + DELETE 4(`HouseBlService` 경유 시 jobDiv projection 1회만 추가). (§6.31 SSOT 갱신)

- **pkgUnit/weightUnit 필드 분리 + Non-BL Cargo UI 재배치 시리즈 (2026-05-11, f6c3657·4423db9·9f88b76)** — `CargoSummary.packageUnit` 단일 필드가 포장 단위(자유텍스트, 'CTN' 등)와 중량 단위(`WeightUnit` enum, KGS/LBS)의 의미를 혼재시키던 문제를 3-step 으로 해소. (1) f6c3657: `WeightUnit.fromCodeOrDefault(String)` 신설(unknown/null/blank → KGS 기본값 반환, `IllegalArgumentException` 제거) + `CargoSummary.packageUnit` → `weightUnit` 리네임(타입-필드명 정합), HouseBl/MasterBl 매퍼·팩토리 5곳의 `fromCode` 호출을 `fromCodeOrDefault` 로 교체 — Non B/L List 더블클릭으로 Entry 진입 시 invalid pkg_unit('CTN' 등)이 던지던 500 에러 즉시 해소. (2) 4423db9: 의미적 분리 완성 — `CargoSummary`/`HouseBl`/`MasterBl` 도메인에 `pkgUnit(String, 자유텍스트)` + `weightUnit(WeightUnit enum)` 두 필드로 분리, `HouseBlSea`/`MasterBlSea`에 있던 `weightUnit`을 **부모 BL로 승격**(Air/Truck/NonBl도 모두 weightUnit 보유 — 모드 무관 공통 필드). DDL: `house_bl`/`master_bl`에 `weight_unit` 컬럼 추가, `house_bl_sea`/`master_bl_sea`에서 제거. BE 어셈블러·매퍼·DTO(Create/Update Request, Detail Response)·Command·Projection·Factory + FE 도메인·schema·api 어댑터·Entry 컴포넌트 일괄 정합화. (3) 9f88b76: Non-BL Cargo 패널 UI 재배치 — `CodeBox` 에 `kind="code-only"` variant 추가(코드 한 칸 + 검색 아이콘, name 영역 미렌더), `non-bl-schema` 의 `cargoUnit` → `weightUnit` 리네임 + `pkgUnit` 신규 필드, `non-bl-submit`/`non-bl-entry`/`non-bl-defaults` 의 pkgUnit·weightUnit 분리 매핑(기존 pkgUnit=undefined 손실 수정), Cargo 패널에서 Package 옆 ComboBox 제거 → `CodeBox kind="code-only"` (pkgUnit), Gross W/T 옆 ComboBox 신설(weightUnit). `CodeBox` 테스트 케이스 추가.

- **HouseBl Bound 필드 NON_BL Entry 필수화 (2026-05-11)** — `CreateNonBlRequest`/`UpdateNonBlRequest`의 `bound`에 `@NotBlank` 추가(DB는 이미 NOT NULL이나 API 검증 누락 NPE 위험). FE `non-bl-schema` `bound: z.string().min(1, "Bound를 선택하세요.")` + 툴바 `is-required` 클래스/라벨 + ComboBox `rules.required` 연동. 신규 의존성 `@hookform/resolvers ^5.2.2` 추가(react-hook-form v7 호환, zodResolver). `NonBlBoundValidationTest` 4 케이스 신규(bound null/blank × create/update). 기존 `NonBlControllerTest.VALID_CREATE_JSON`에 `"bound": "EXP"` 추가(사용자 명시 승인). §6.25 정책 적용 — UI required 필드에 @NotBlank 유지.

- **HouseBlPersistenceAdapter Strategy 분리 (2026-05-11, a8f04c7·2d5ba76)** — 352줄 어댑터의 SEA/AIR/TRUCK/NON_BL switch 분기 3곳(saveHouseBl 96줄·deleteByIdAndJobDiv 28줄·loadWithExt 27줄)을 4개 `HouseBl<JobDiv>PersistenceStrategy`로 추출 + dispatcher 패턴. Strategy 인터페이스(`adapter/out/persistence/housebl/strategy/`)는 package-private로 adapter 외부 노출 차단. 어댑터는 142줄로 축약(공통 `HouseBlRepository`+`HouseBlDomainToJpaMapper`+4 Strategy만 주입), 기존 16+ Repository는 strategy로 분산. 트랜잭션은 어댑터에 `@Transactional` 유지, Strategy는 미부착. Port 시그니처/호출자(NonBlService, HouseBlService 등) 무변경. 통합 테스트 `NonBlChangeHblNoIntegrationTest`/`NonBlFindByHblNoIntegrationTest` `@Import` 블록에 Strategy 4개 추가 필수(누락 시 `NoSuchBeanDefinitionException`). `HouseBlPersistenceAdapterTest`(644→369줄, dispatcher 검증만)+신규 4개 `HouseBl<JobDiv>PersistenceStrategyTest`(각 ~140줄)로 검증 분리. 동작 동등성: p6spy SQL 횟수/순서 변화 없음. (§6.33 SSOT 등재)

- **NonBl update 1차 위임 시도 + 응답 void화 (Phase 2, 2026-05-11, a6d96cd·4165b8a·db98df4)** — `HouseBlPort.update(Long id, Consumer<HouseBl> mutator)` 추가하여 Service의 fetch+mutate+save 3단계를 1줄 위임으로 축약 시도. Controller 응답 `ApiResponse<NonBlDetailResponse>` → `ApiResponse<Void>`(FE invalidate+refetch라 영향 없음). FE `nonBlPort.update`/api client 반환 `Promise<void>`, zod 응답 검증 제거. `non-bl-entry.tsx`의 create(Promise<{id}>)+update(Promise<void>) 단일 `useMutation` 삼항 패턴이 TS2322 제네릭 불일치 → create/update mutation 분리. **함정**: 1차 캐시 hit이 PK 기반 조회만 작동(§6.34)하므로 adapter의 `update`가 내부적으로 `saveHouseBl`을 호출하면 `existsById`(count SELECT)+`strategy.saveExt`의 `findByHouseBlHouseBlId`(FK 메서드 쿼리) 2건이 그대로 발사 → SELECT 절감 0. Service에 `instanceof HouseBlNonBl` 검증과 `factory.applyToEntity` 호출이 남아 위임도 표면적. **Phase 2 보강 작업으로 폐기**됨.

- **NonBl update 진짜 위임 + SELECT 절감 6→4 (Phase 2 보강, 2026-05-11, 3037a44·b0a92ad·8b70969)** — Phase 2의 Consumer trampoline 안티패턴을 폐기하고 **NonBl 전용 Port + Adapter** 신설. `application/nonbl/port/out/NonBlPersistencePort.update(Long id, UpdateHouseBlCommand command)` + `adapter/out/persistence/nonbl/NonBlUpdatePersistenceAdapter`(명명 충돌 회피 — 같은 패키지의 `NonBlPersistenceAdapter`가 `NonBlSearchPort` 구현 중). Service는 `nonBlPersistencePort.update(id, command);` 1줄 위임 + `houseBlFactory` 의존 제거. Adapter가 jobDiv 검증·factory.applyToEntity·attached JPA 직접 매핑까지 일괄 처리하며 `saveHouseBl` 미호출 — dirty checking이 트랜잭션 종료 시 UPDATE 자동 발사. `existsById`(count SELECT) + `findByHouseBlHouseBlId`(FK 재조회) 2건 제거. `HouseBlPort.update(Consumer)` 및 어댑터 구현은 미사용으로 제거(`HouseBlPersistenceAdapterTest`의 update 2 케이스도 제거 — 메인 메서드 제거에 따른 자연 정리, 사용자 승인됨). 신규 `NonBlPersistenceAdapterTest` 6 케이스(happy path·containers·empty collections·notFound·ext notFound·wrong jobDiv). 함정: `emptyCommand()` 헬퍼의 `UpdateHouseBlCommand` 인수 수 정확히 일치 필요(50/53 → 컴파일 실패), Mockito strict mode에서 미사용 stub `UnnecessaryStubbingException` 제거. mergeContainers/mergeDims LAZY 재트리거 없음(`toNonBlDomain`이 이미 1차 캐시 적재). **adapter→application/factory 의존**은 실용적 헥사고널 완화로 사용자 합의. 헥사고널 정통 회피하려면 service에서 command→fields 변환 후 fields만 port에 전달하는 패턴(분량 더 큼, 별도 작업). (§6.33·§6.34·§6.35 SSOT 등재)

  **다른 도메인(HouseBl Sea/Air/Truck · MasterBl · SwitchBl) 적용 권장 — 별도 작업으로 진행. 체크리스트는 §6.35.**

- **Non B/L Entry 공용 confirm 모달 적용 + Copy 제거 + 조회 전 Save 가드 (2026-05-11, 16dbc0b)** — `e55af73` 에서 도입된 `@/components/confirm` 의 **product 첫 적용 사례**. (1) `use-non-bl-entry-mutations.ts` 의 `handleSubmit`/`handleDelete` 를 async 화하고 `await confirm({...})` Promise API 호출 — Save `variant: "default"` + 기본 메시지, Delete `variant: "destructive" + confirmText: "삭제"` + "삭제된 데이터는 복구할 수 없습니다." description. 기존 `window.confirm` 폐기. 반환 타입 시그니처 `handleSubmit`/`handleDelete` → `Promise<void>` 로 정합. (2) `non-bl-entry-header.tsx` 의 onClick 미정의 Copy 버튼(스텁) + lucide-react `Copy` import 제거 — props 인터페이스 변경 없음(`onCopy` 원래 미존재). Copy 자리는 추후 다른 기능으로 대체 예정. (3) `non-bl-entry.tsx` 의 `onSave` prop 을 inline 가드 함수로 교체 — `!isEdit` 시 `toast.info("먼저 Non B/L을 조회해주세요.")` 후 early return, isEdit 일 때만 `methods.handleSubmit(handleSubmit)()` 호출(`handleChangeBlNo` 와 동일 가드 패턴, toast import 기존 재사용). `useNonBlEntryMutations` 의 `createMutation` 코드는 dead code 화되지만 추후 신규 생성 워크플로우 재활성화 가능성으로 **의도적 유지**. ScreenGuard(§6.27) 와 충돌 없음 — confirm 모달은 mutate 외부에서 해소 후 모달 닫힘 → 기존 `isSavePending`/`deleteMutation.isPending` 로딩 표시 흐름 그대로 동작. 다른 Entry(Sea/Air HBL, Master BL, Truck BL, SwitchBlModal) 의 `window.confirm` 잔존 — 동일 패턴 후속 적용 권장.

- **Truck B/L Entry 패널 디테일 정합화 시리즈 (2026-05-12)** — Phase A 풀 마이그레이션 후 패널/그리드 디테일 정합. (1) **Remark 패널 분리 + LineNumberTextarea 통일** (b942e34·d4fb855) — Description 패널에서 Remark 필드 분리해 신규 `truck-remark-panel.tsx`(2x2, Performance 아래 col:4 row:4)로 추출, 처음 TextArea 평문 사용했다가 Description/Marks/Party Address와 동일하게 `Controller + LineNumberTextarea`로 통일. (2) **Party 패널 Non B/L 패턴 채택** (6a1b49c·d1844ed·3df603a·69d8eea) — CodeBox `kind="party-cn"`이 자체 `.party-block`+`.party-block__head`+라벨까지 렌더하므로 외부 wrapper 제거(라벨 중복 해소), Clear 버튼 제거, 잔존 액션 버튼(To Order/Same as Cne.)을 CodeBox 옆 같은 행에 인접 배치(flex gap:4) + Address LineNumberTextarea와의 사이 marginTop:4 추가, `.truck-party-panel .field-widget-item + .field-widget-item { padding-top: 4px }` 스코프 CSS 신설. (3) **Performance 패널 6필드 모두 LCN 통일** (22a2886) — `<div className="li">+TextBox` → `<CodeBox kind="lcn" label="..." required? ...>` 단독 호출 6개, `customerPic`은 schema에 Name 없어 nameProps 미지정(빈 Name input 유지, 추후 master 데이터 join 정책). (4) **Cargo Package/G/W 우측 박스 80px 고정** (91bcd90·0710cb7, §6.42·§6.43 등재) — `.li__input--tight > *` 강제 flex 덮어쓰기 + `.truck-cargo-panel` 스코프 CSS로 `.lcn` grid 해제. (5) **Truck Information 그리드 카탈로그 정합** (86e6493, §6.38) — +/- 버튼 `btn--icon btn--success`/`btn--icon btn--danger`, GridList `onClearRow={() => setSelectedKey(null)}` 추가. (6) **truckType/containerType ComboBox(cell) + BE enum 바인딩** (39469e5·d0dcf1b, §6.45·§6.46) — `useEnumOptions("TruckType")`/`useEnumOptions("ContainerType")` 호출, ContainerType label=description이라 `rawOptions.map(o => ({ value: o.value, label: o.value }))` 재매핑, register spread는 ComboBox value 미주입 → Controller 패턴으로 교체.

- **GridList SSOT 보강 시리즈 (2026-05-12, 5f48185·12e3047·aefe8e9·746a304·056b648)** — Truck B/L Entry 디테일 정합 중 발견한 GridList 공통 버그 4건 일괄 해소. (1) **outside-click 같은 패널 액션 버튼까지 outside 처리** (5f48185, §6.40) — `handleOutsideClick` 가드 추가, `e.target`과 tableEl 둘 다 `closest(".panel")`로 동일 panel인지 확인 후 outside 제외. (2) **useVirtualizer default getItemKey index 기반으로 인한 행 삭제 후 stale input value 표시** (12e3047, §6.41) — `getItemKey` 옵션 추가(rowKey > row.id > index 폴백), row identity 변화 시 React가 element를 unmount/remount → register가 mount 시 RHF latest value를 defaultValue로 적용. PlainGridList + ManagedGridList 둘 다 수정 → 모든 호출처 자동 정상화. (3) **포커싱 셀 행 우선 삭제 패턴 8 그리드 + 카탈로그 일관 적용** (aefe8e9·746a304, §6.39) — Truck Information / Non B/L Dimension·Container Info + House B/L Container·Dimension·Item HS + Master House B/L + 카탈로그 `grid-preview-panel`에 `focusedRowKeyRef` + `captureFocusedRow()` + - 버튼 `onMouseDown` 패턴 일관 적용. handleRemove 우선순위: 포커싱된 td의 row → selectedKey state → 마지막 행. Master House B/L Grid는 커스텀 `<table>` 구조라 `<tr data-row-key={field.rhfKey}>` 마크업도 함께 추가. (4) **Non B/L Dimension/Container Info 그리드 +/- 버튼 카탈로그 정합** (056b648, §6.38) — `btn btn--sm btn--ghost` → `btn btn--sm btn--icon btn--success`/`btn--icon btn--danger`.

- **CodeBox kind 명칭 컨벤션 등재 (2026-05-12, §6.44)** — 사용자가 코드박스 형태를 호명할 때 사용하는 표준 명칭(LCN / code-only / party-cn)을 CodeBox `kind` prop과 1:1 매칭. 메모리 `feedback_codebox_terms.md` 신설.

- **Truck B/L Entry Dimension 그리드 + Volume Divisor 추가 (2026-05-12)** — Truck B/L Entry 풀 마이그레이션 직후 후속. Performance 패널 아래에 Non B/L `nonbl-dimension-panel.tsx`와 동일 구성의 `truck-dimension-panel.tsx`(7컬럼: #/Length/Width/Height/Qty/CBM/Volume Wt., Volume Divisor ComboBox + Plus/Minus toolbar, GridList variant=cell) 2x2 신규(`col:4 row:4`). 기존 Remark는 row:4→6 이동(크기 유지), 다른 패널은 col 영역이 달라 무영향. **(BE)** 인프라(`house_bl_truck_dim` 테이블·`HouseBlTruckDimJpaEntity`·`HouseBlCargoMapper.toTruckDimDomain/toTruckDimJpa`·`HouseBlTruckJpaEntity.@OneToMany dims`+`syncDims`·`HouseBlTruckPersistenceStrategy.saveExt`·`TruckBlUpdatePersistenceAdapter.syncDims`)는 ER 재구조화 Phase 4(§ER §1246)에서 이미 완성되어 있었고, `TruckBlAssembler.toCreate/toUpdateCommand`에서 `null, // dims`/`null, // volumeDivisor` 하드코딩 라인만 풀어주면 데이터 흐름이 살아남. `CreateTruckBlRequest`/`UpdateTruckBlRequest`에 `String volumeDivisor` + `List<DimRequest> dims` + nested `DimRequest` record(Update 버전은 `Long id` 포함, NonBl §6.28 패턴) 추가. `TruckBlDetailResult`/`TruckBlDetailResponse`에 `String volumeDivisor` + `List<DimView> dims` + nested `DimView` record 추가, `TruckBlDetailResponse.from()`/`TruckBlFactory.toDetailResult()` 매핑 연결. `HouseBlTruck` 도메인에 `private VolumeDivisor volumeDivisor` 필드 + `assignVolumeDivisor` 메서드 추가(**dims 필드는 추가하지 않음** — `HouseBl` 부모의 dims 컬렉션 재사용, Non B/L과 동일 SSOT). `HouseBlTruckSubFactory.applyTruckVolumeDivisor(HouseBl, String)` 별도 메서드 신설(기존 `applyTruckCreate`/`applyTruckUpdate` 시그니처 무변경 — ripple 최소). `HouseBlFactory.toEntity`/`applyToEntity`에서 호출 1줄 추가. JPA Entity `HouseBlTruckJpaEntity`에 `@Column(name="volume_divisor", length=10) @Enumerated(STRING)` 필드 + setter, 양방향 매퍼(`HouseBlDomainToJpaMapper.applyTruckFields`/`HouseBlJpaToDomainMapper.copyTruckFields`)에 각 1줄. **DDL**: `house_bl_truck.volume_divisor VARCHAR(10)` 컬럼 추가, `schema/manual_migration/add_truck_volume_divisor_2026-05-12.sql` 운영 마이그레이션 신설(`ALTER TABLE ... ADD COLUMN IF NOT EXISTS`). NULLABLE이며 기존 row backfill 정책은 FE가 `?? "CM6000"` 디폴트 표시 후 다음 Save 시 자연 채움(명시적 UPDATE 미실행). **(FE)** `front-end/src/domain/truck-bl/index.ts`에 `TruckBlDimView`/`TruckBlDimRequest` 인터페이스 + `TruckBlDetail`/`CreateTruckBlRequest`에 `volumeDivisor?`/`dims?` 추가(`UpdateTruckBlRequest`는 `Omit`/`Pick` 자동 반영). `adapter/out/api/truck-bl.ts`에 `TRUCK_BL_DIM_SCHEMA`(zod) + `TRUCK_BL_DETAIL_SCHEMA`에 `volumeDivisor`/`dims` 필드. `truck-bl-schema.ts`에 `TRUCK_DIM_SCHEMA`(string 6필드 + id) + `TRUCK_BL_SCHEMA`에 `dimensionDivisor`/`dimensions` + `EMPTY_TRUCK_DIM_ROW` export. `truck-bl-defaults.ts`에 `dimensionDivisor: "CM6000"`/`dimensions: []`. `use-truck-bl-entry.ts` `form.reset` 매핑(BE→Form: `lengthCm→length string` 등, `detail.volumeDivisor ?? "CM6000"`). `truck-bl-submit.ts`에 `buildTruckDimCreateRows`/`buildTruckDimUpdateRows` 헬퍼 + Create/Update 빌더에 `volumeDivisor`+`dims` 포함. `truck-panels.tsx` 배럴에 export 1줄. **레이아웃**: `main-truck.tsx` TRUCK_REGISTRY에 `dimension-truck { col:4, row:4, colSpan:2, rowSpan:2 }` 신규 + `remark-truck` row:4→6. BE 전체 빌드/테스트 PASS, FE lint 0 error/build PASS. 회귀 검증: `TruckBlFactoryTest`/`TruckBlControllerTest` 등 record 시그니처 변경 컴파일 영향 0건(record 생성자 직접 호출 없음). **§12.1 결정 갱신**: VolumeDivisor가 이제 Non B/L + Truck B/L 2개 도메인에 도입됨 — 항공 마이그레이션 시 `HouseBl` 부모로의 공통 승격 검토 가치 강화. **§12.2 결정 갱신**: `TruckBlDetailResponse`에 `dims`/`volumeDivisor`는 이제 포함됨, 자식 컬렉션 `truckOrders` + `desc`(marks/description/clauses)는 여전히 미포함(별개 후속 작업으로 남음).

- **Truck B/L Entry 풀 마이그레이션 (2026-05-12)** — Non B/L Entry 패턴을 그대로 적용한 두 번째 풀 마이그레이션 사례. **(BE)** `TruckBlController`에 `POST /api/truck-bl`(create→`{id}`, §6.29)·`PUT /{id}`(update→`Void`, §6.35)·`DELETE /{id}`·`POST /find-by-hbl-no`(EXACT PK, §6.19)·`PUT /{id}/hbl-no`(Change BL No, §930) 5개 endpoint 신설. DTO 4종(`CreateTruckBlRequest`/`UpdateTruckBlRequest`/`ChangeTruckBlHblNoRequest`/`FindTruckBlByHblNoRequest`) §6.25 BE SSOT 적용(UI required `hblNo`/`bound`/`polCode`/`podCode`/`etd`/`eta`/`actualCustomerCode`/`operatorCode`/`teamCode`/`salesManCode` 10개 `@NotBlank` 유지, `UpdateTruckBlRequest`에 `hblNo` 자체 제외 §10 SSOT). `TruckBlPersistencePort` + `TruckBlUpdatePersistenceAdapter` 신설(§6.35 패턴). `TruckBlService`는 자기 jobDiv(TRUCK) 직접 알아 `houseBlPort.deleteByIdAndJobDiv(id, JobDiv.TRUCK)` 호출. `HouseBlFactory`에 `applyTruckCreate/applyTruckUpdate` 메서드 추가(305→335줄, 300줄 분리 검토 대상으로 `HouseBlTruckSubFactory` 향후 분리 권장). `Create/UpdateHouseBlCommand` record에 `TruckDetailCommand` nested record 추가 — 함정: `NonBlPersistenceAdapterTest.emptyCommand()` 파라미터 53→54 컴파일 오류 발생, null 추가로 수정(사용자 사후 보고). 신규 테스트 5종 29 케이스 전원 PASS, 전체 642 테스트 그린. **(FE)** `truck-bl-schema.ts`/`truck-bl-defaults.ts` 신설(Non B/L 패턴, `HouseBlFormValues` 의존 완전 제거). Toolbar 5→4 재구성(`Truck B/L No`/`Bound`/`Load Type`/`Service Term`, 기존 `Settle`은 Performance 패널의 `settlePartnerCode`로 입력 경로 유지, `Incoterms`/`Freight Term`/`Status`는 truck b/l 도메인 미사용으로 단순 제거). 작업 중 `truck-panels.tsx` 411줄로 증가 → 5개 패널 파일(party/schedule/cargo/document/performance)로 강제 분리, 기존 파일은 7줄 배럴로 축소. `truck-marks-panel.tsx`/`truck-description-panel.tsx` truck 전용 신설(House-BL 공유에서 분리). `pkgUnit`은 §10 Non B/L 정책 따라 `CodeBox kind="code-only"` 자유 텍스트(기존 `<select>` 7개 옵션 사라짐). `truck-order-grid-panel` cell SSOT(§6.12) `TextBox/NumberBox variant="cell"`로 교체. `use-truck-bl-entry-mutations.ts`(88줄) 신설(create/update mutation 분리 §6.29, 공용 confirm 모달 §959). `truck-bl-submit.ts`(114줄) `buildTruckBlCreateRequest`/`buildTruckBlUpdateRequest`(hblNo destructure 제외 §10 SSOT, 자식 row `id` 포함 §6.28). `truck-change-bl-no-modal.tsx` truck 전용 신설 — Non B/L `change-bl-no-modal.tsx`는 `nonBlPort`/`["non-bl",...]` queryKey 하드코딩이라 prop 분기 불가, 동일 구조로 truck 전용 신설. Entry 결선: hot-marker `truck-bl-entry:hot:${id}`(§6.16), F5 redirect, EXACT Search(§6.19), onSave 미조회 가드 toast.info, Enter implicit submit 차단(§6.24), detail useQuery `staleTime:Infinity`+`refetchOnMount:false`(§6.36). `truck-bl-grid.tsx` 더블클릭 hot-marker 추가 — §10 메모리상 "이미 적용"으로 표시됐으나 실제 미적용 상태였음, Phase C에서 보정. FE lint 0 error/build PASS.

---

## 11. 본 가이드의 후속 갱신 시점

- 다른 Entry 마이그레이션 중 **새로운 함정** 발견 시 §6에 추가
- **공통 컴포넌트 신설** 시 §2 표 + §10 목록 갱신
- **새 enum 등록** 시 §4 목록 갱신
- **CSS 토큰 변경** 시 §7 갱신
- **새 패턴 확립** 시 해당 섹션에 예시 추가

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

Truck B/L Phase A에서 `TruckBlDetailResponse`에 자식 컬렉션 일부가 누락되어 있었다. **2026-05-12 Dimension 추가 작업에서 `dims`/`volumeDivisor`는 응답 시그니처에 포함됨**(§10 Truck Dimension 항목 참조). 여전히 누락된 자식:

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

- House-BL `page.tsx`가 `searchParams`를 수신하지 않는 라우팅 결함 정리
- query-param 라우팅(`?id={row.id}`)을 받도록 page.tsx 시그니처 보정 또는 path-param(§6.26 패턴 A)으로 전환

### 13.6 권장 진행 순서

1. Sea House Entry (사용 빈도 최고, 패턴 정립 대상)
2. Air House Entry
3. Sea Master Entry — §13.3 ChangeBlNoModal 공통화 검토 시점
4. Air Master Entry
5. SwitchBlModal 일괄 정리 (`window.confirm` 잔존 3 파일)

> **참고**: 본 §13은 핸드오프 2026-05-12 미해결 이슈(§7 / §8 / §9 / §11)를 단독 작업이 아닌 도메인별 PR로 분산 흡수한 결과. Phase 4 마이그레이션 진행 시 항상 본 체크리스트를 기준으로 PR 작성.
