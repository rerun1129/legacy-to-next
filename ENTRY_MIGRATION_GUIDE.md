# Entry 화면 마이그레이션 가이드

> Non B/L Entry를 기준으로 다른 엔티티 화면(Sea/Air House, Sea/Air Master, Truck B/L 등) Entry 화면을 카탈로그 컴포넌트로 통일하는 작업의 패턴·함정·체크리스트.
> Entry 마이그레이션이 완료될 때까지 살아있는 문서.

---

## 1. 작업 흐름 요약

1. 대상 Entry 화면의 모든 native `<input>`/`<select>`/로컬 wrapper 컴포넌트 식별
2. 카탈로그 표준 컴포넌트(TextBox/NumberBox/CodeBox/DropBox/DateBox)로 1:1 매핑
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
| `<select>` (정적 옵션) | `DropBox` | `options=[{value,label}, …]` |
| `<select>` (enum 기반) | `DropBox` + `useEnumOptions("EnumName")` | `options={enumOptions}`, `placeholder={enumPlaceholder}` |

위치: 모두 `front-end/src/components/shared/inputs/`

### BoxBaseProps 공통 prop (모든 표준 컴포넌트)

`variant?: "panel" | "cell"` · `required?` · `readOnly?` · `disabled?` · `className?` · `style?` · `label?`

### Variant 사용 기준

- `panel`: 패널(섹션) 내부 입력 필드
- `cell`: 그리드 셀 내부 입력 (`grid__cell-input`과 통합)

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
```

### Schema/Defaults 동시 갱신 필수

신규 필드 추가 시 두 파일 동시 업데이트:

- `xxx-schema.ts`: `dimensionDivisor: z.string().optional()` 같이 z 타입 추가
- `xxx-defaults.ts`: `dimensionDivisor: "CM/6000"` 같이 초기값 추가

> **함정**: 한쪽만 갱신하면 register는 동작하지만 form 초기값/검증이 어긋나 저장 시 누락 발생.

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

### 셀 input

```tsx
<input className="grid__cell-input" {...register(`array.${i}.field`)} />
// 또는 숫자형
<input type="number" className="grid__cell-input is-num" {...register(...)} />
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

신규 표준 컴포넌트 추가 시 `front-end/src/app/(dev)/preview/sections/inputs-section.tsx`에 섹션 추가. CLAUDE.md 규칙: 300줄 초과 시 분리 검토. 현재 inputs-section.tsx는 355줄(분리 검토 대상).

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
  - DropBox enum 로딩 (네트워크 탭 확인)
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

- DateBox 카탈로그 컴포넌트 (`shared/inputs/date-box.tsx`)
- BoxBaseProps 표준 (`shared/inputs/_types.ts`)
- 그리드 셀 편집 모드 단일 outline 처리 (`grids.css`)
- 그리드 외부 클릭 selection 자동 해제 (`use-grid-cell-selection.ts`)
- 그리드 외부 클릭 행 highlight 해제 (`onClearRow` prop)
- PlainGridList 컬럼 리사이즈 (session-local)
- selection overlay td DOM 실측 보정
- 컬럼 리사이즈 후 overlay 자동 갱신 (ResizeObserver)
- CodeBox lcn / party-cn 라벨 너비 통일

---

## 11. 본 가이드의 후속 갱신 시점

- 다른 Entry 마이그레이션 중 **새로운 함정** 발견 시 §6에 추가
- **공통 컴포넌트 신설** 시 §2 표 + §10 목록 갱신
- **새 enum 등록** 시 §4 목록 갱신
- **CSS 토큰 변경** 시 §7 갱신
- **새 패턴 확립** 시 해당 섹션에 예시 추가
