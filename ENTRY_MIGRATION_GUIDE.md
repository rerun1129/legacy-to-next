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

현재 `volumeDivisor`(부피 환산 divisor)는 Non B/L 전용 확장 필드(`HouseBlNonBl`)로 도입되어 있다.
항공 Entry(Air House / Air Master) 마이그레이션 시 동일 개념이 필요해지면 다음 선택지를 검토:

- `HouseBl` 공통 필드로 이전 — SEA/AIR/Master 모드도 새 컬럼 존재 (nullable로 회귀 안전)
- 각 확장(`HouseBlNonBl`, `HouseBlAirHouse` 등)에 별도 보유 — 일관성 떨어짐, 이전 비용 발생

> **결정 시점**: 항공 Entry 마이그레이션 plan 수립 단계.
