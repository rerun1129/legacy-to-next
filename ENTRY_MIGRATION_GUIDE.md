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

### 6.18 useBlDraftSync 사용 시 unmount cleanup으로 clearDraft

`useBlDraftSync`는 `form.watch()`로 변경마다 draft를 zustand에 저장하지만, **unmount 시 자동 clear하지 않음**. 메뉴 닫고 재진입 시 mount 복원 로직이 이전 값을 부활시킴.

Entry 컴포넌트에 cleanup useEffect를 추가해야 함:

```ts
useEffect(() => {
  const draftKey = `non::${id ?? "new"}`;
  return () => { clearDraft(draftKey); };
}, [clearDraft, id]);
```

사례: 4572a33 — Non B/L Entry nonBlNo 잔류 버그 수정.

### 6.19 Entry Search 버튼 — port.list + hot-marker 재사용 패턴

Entry 화면의 Search는 `port.list(filter, 1, 2)`로 0건/다건/단건 분기:
- 빈 입력: toast.info
- 0건: toast.info
- 다건(>1): toast.info + List 화면 navigate
- 1건(동일 id): detail invalidate만
- 1건(다른 id): hot-marker 세팅 후 `router.replace(/entry/${id})` → §6.16 패턴 재사용

사례: 0526e83 — Non B/L Entry Search 구현.

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

---

## 11. 본 가이드의 후속 갱신 시점

- 다른 Entry 마이그레이션 중 **새로운 함정** 발견 시 §6에 추가
- **공통 컴포넌트 신설** 시 §2 표 + §10 목록 갱신
- **새 enum 등록** 시 §4 목록 갱신
- **CSS 토큰 변경** 시 §7 갱신
- **새 패턴 확립** 시 해당 섹션에 예시 추가
