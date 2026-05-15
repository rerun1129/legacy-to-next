# Phase 2 — UI/카탈로그 교체

> **언제 read**: UI 컴포넌트를 카탈로그로 1:1 교체할 때.

본 phase는 native `<input>`/`<select>`/로컬 wrapper 제거 → 카탈로그 표준 컴포넌트 적용 + 그리드/셀 패턴 정합화 시 read한다. CRUD/캐시/라우팅 등 풀스택 정합은 phase3-data-flow.md.

---

## 2. 카탈로그 컴포넌트 매핑 표 (README에서 복제)

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

## 6. UI/컴포넌트 안티패턴 (19개)

### 6.1 CodeBox 외부 wrapper 중첩 금지

`<div className="lcn">` 같은 외부 wrapper 안에 CodeBox를 넣으면 CodeBox 자체 `.lcn` 렌더와 중첩되어 grid-template-columns가 깨진다.

**올바른 패턴**: CodeBox만 단독 렌더. 외부 wrapper 제거.

### 6.2 DateBox에 className/style 전달 금지

`PanelDateInput`의 `DateInputBaseProps`는 `className`/`style`을 `Omit`으로 제외하고 있다. DateBox에서 받은 className/style을 PanelDateInput으로 그대로 전달하면 타입 오류 발생.

**올바른 패턴**: DateBox 내부에서 className/style을 PanelDateInput에 전달하지 않음. (BoxBaseProps의 className/style은 받되 실제로는 사용 안 함)

### 6.4 카탈로그 페이지 등록 필수 + 300줄 초과 분리 검토

신규 표준 컴포넌트 추가 시 `front-end/src/app/(dev)/preview/sections/inputs/` 아래 해당 sub-section 파일에 추가. inputs-section.tsx는 49줄 오케스트레이터로 분리 완료(2026-05). 서브 섹션 파일 목록: `text-section.tsx` / `code-section.tsx` / `number-section.tsx` / `combo-section.tsx` / `date-section.tsx` / `time-section.tsx` / `link-radio-section.tsx`.

### 6.6 로컬 wrapper 컴포넌트 제거 시 정의도 함께 삭제

표준 컴포넌트로 교체할 때 패널 내부에 정의된 로컬 wrapper(예: `PartyBlock`) 정의도 함께 삭제. 두 구현 공존 시 디자인 불일치.

### 6.7 인라인 style 일괄 제거

카탈로그 컴포넌트가 토큰 디자인 책임. 잔존 style은:
- BoxBaseProps `className`로 이전
- 또는 CSS 모듈/유틸 클래스로 이전
- gridTemplateColumns 같은 레이아웃 1-2건은 예외 허용 (주석 명시)

### 6.10 GridList `onClearRow` 누락 금지

외부 클릭 시 행 강조 자동 해제 기능. 누락하면 한 화면에 여러 그리드가 있을 때 이전 그리드 강조가 잔존. `onClearRow={() => setSelectedKey("")}` 항상 추가.

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

### 6.20 위젯 편집 버튼 `type="button"` 명시

`field-item-grid`, `field-widget-list`, `field-widget-container` 등 위젯 내부 편집/토글 버튼에 `type="button"`을 명시하지 않으면 form 내부에 있을 때 **의도치 않은 submit이 트리거**됨.

사례: 66a217c — Non B/L Entry 위젯 토글 버그 수정.

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

#### 6.41-보강 (호출처) `rowKey/onRowClick/rowClassName` — 인덱스 기반 콜백 안티패턴 금지

GridList 내부의 `getItemKey`는 `try/catch`로 stale index 호출을 방어하지만, **호출처(product 컴포넌트)의 `rowKey` 콜백이 throw하는 패턴은 catch 밖에서 호출되는 `onRowClick`/`rowClassName` 등에서 그대로 런타임 에러로 노출**된다. RHF `useFieldArray`의 `append`/`remove` 직후 한 frame 동안 GridList의 `data` prop과 useVirtualizer 캐시가 sync되기 전 stale index로 콜백이 발화되면 `fields[i]`가 `undefined` → `.id` 접근에서 `Cannot read properties of undefined (reading 'id')` throw.

```tsx
// ❌ 안티패턴 — append 직후 fields[i] undefined throw
rowKey={(_, i) => fields[i].id}
onRowClick={(_, i) => setSelectedKey(fields[i].id)}
rowClassName={(_, i) => fields[i]?.id === selectedKey ? "is-selected" : undefined}

// ✅ 올바른 패턴 — row 객체에서 직접 추출 + String() 변환(§6.9)
rowKey={(r) => String(r.id)}
onRowClick={(r) => setSelectedKey(String(r.id))}
rowClassName={(r) => String(r.id) === selectedKey ? "is-selected" : undefined}
```

**Truck B/L 모범**: `truck-order-grid-panel.tsx:105-107`, `truck-dimension-panel.tsx:89-91` — 모두 row 객체 기반 + `String()` 변환.

**점검 의무**: 신규/기존 그리드 패널 마이그레이션 시 `rowKey={(_, i) => ...}` / `onRowClick={(_, i) => ...}` / `rowClassName={(_, i) => ...}` 형태 발견 시 즉시 row 객체 기반으로 정정. lint로 잡히지 않으므로 grep 의무.

사례: Sea House Entry container-grid/item-hs/dimension/air-charge-info 4개 패널에서 본 안티패턴이 잔존해 행 추가 시 동일 에러 발생. Truck B/L 마이그레이션 시 이미 모범으로 row 객체 기반을 채택했으나 본 절(§6.41-보강) 명시가 없어 후속 도메인에 답습됨.

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

사례: 0710cb7 (truck-cargo-panel), `a551176f` (master-cargo-doc-panel scope class + `.master-cargo-doc-panel .li__input--tight > div > .lcn { display: block; ... }` override).

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
