# Entry 화면 마이그레이션 가이드 — 인덱스

> Non B/L Entry를 기준으로 다른 엔티티 화면(Sea/Air House, Sea/Air Master, Truck B/L 등) Entry 화면을 카탈로그 컴포넌트로 통일하는 작업의 패턴·함정·체크리스트.
> Entry 마이그레이션이 완료될 때까지 살아있는 문서.

본 디렉토리는 `ENTRY_MIGRATION_GUIDE.md`(1504줄)를 phase 단위로 분할한 결과다. 각 phase 시작 시 해당 phase 파일 **1개만 read**하여 작업하고, 다음 phase로 넘어가면 해당 phase 파일을 다시 read한다.

---

## 분할 정책

**에이전트가 마이그레이션을 수행할 때 phase별로 하나의 문서만 컨텍스트에 담아 작업 → 다음 phase 문서 로드 → 반복**하는 워크플로우를 위해 가이드를 사용 시점별로 분할한다.

- 각 phase 파일은 **자기 단독으로 완결**되어야 한다 (cross-file 참조 최소화)
- 한 phase 분량은 단일 컨텍스트로 충분히 담을 수 있는 크기 (~300~600줄)
- 차기 회귀 패턴 추가 시 어느 파일에 들어가는지 명확히 분류

---

## 사이클 안내 (가장 중요)

1. **새 도메인 마이그레이션 시작** → `phase1-setup.md` read → Plan 수립
2. **UI 카탈로그 작업** → `phase2-ui.md` read → 구현
3. **풀스택 정합 작업** → `phase3-data-flow.md` read → 구현
4. **검증·커밋** → `phase4-qa.md` read → 마무리
5. **회귀 발견 또는 패턴 추가** 시 → 해당 phase 파일에 추가 (아래 "패턴 추가 시 어느 파일에?" 참조)

> 진행 중 다른 phase의 SSOT 참조가 필요할 경우(예: phase3 작업 중 §2 매핑 표가 필요)에만 해당 phase 파일을 부분 read.

---

## 5개 phase 파일 인덱스

| 파일 | 언제 read | 분량 |
|---|---|---|
| [phase1-setup.md](phase1-setup.md) | Plan 단계: 작업 범위 결정 + 사전 점검 매트릭스 + 도메인 리마인드 | ~280줄 |
| [phase2-ui.md](phase2-ui.md) | UI 컴포넌트를 카탈로그로 1:1 교체할 때 | ~500줄 |
| [phase3-data-flow.md](phase3-data-flow.md) | form↔BE 매핑·CRUD mutation·라우팅·캐시 흐름 작업 시 | ~600줄 |
| [phase4-qa.md](phase4-qa.md) | 빌드·검증·커밋 단계 | ~60줄 |
| [history.md](history.md) | "이미 완료된 작업" 확인이 필요할 때 (일반 사이클에선 read 불필요) | ~80줄 |

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

> **위 §2 매핑 표는 phase2-ui.md에도 복제 게재**한다 (phase2 작업 중 cross-file read 회피). phase3 작업 중 매핑이 필요하면 본 README 참조.

---

## 운영 정책

### 패턴 추가 시 어느 파일에?

1. **Plan/사전 점검 패턴** (작업 시작 전에 매번 봐야 하는 의무 체크) → `phase1-setup.md`
2. **UI/컴포넌트 안티패턴** (TextBox/ComboBox/CodeBox/GridList 등 사용 규칙) → `phase2-ui.md`
3. **CRUD/캐시/라우팅/풀스택 정합** (form↔BE, mutation, useQuery, 캐시) → `phase3-data-flow.md`
4. **빌드/검증/CSS/commit** (작업 종료 직전) → `phase4-qa.md`
5. **완료된 작업 기록** (변경 history) → `history.md`

### cross-file 참조 정책

- 각 phase 파일은 **자기 단독으로 완결**되어야 한다. 다른 phase 파일을 read하지 않아도 그 phase 작업 완수 가능.
- 예외:
  - phase2-ui.md / phase3-data-flow.md 둘 다 §2 매핑 표를 참조하므로 phase2에 복제 게재(README 게재 + phase2 복제). phase3에서는 §2 필요 시 README read.
  - phase별 cross-reference가 있는 경우 짧은 "see §6.XX in phase3-data-flow.md" 형태로 명시.

### 기존 SSOT 보존

- 49개 sub-section의 번호(§6.1~§6.49)와 본문은 **그대로 보존**. 분할은 단순 재배치.

---

## 11. 본 가이드의 후속 갱신 시점

- 다른 Entry 마이그레이션 중 **새로운 함정** 발견 시 §6에 추가
- **공통 컴포넌트 신설** 시 §2 표 + history.md 목록 갱신
- **새 enum 등록** 시 phase2-ui.md §4 목록 갱신
- **CSS 토큰 변경** 시 phase4-qa.md §7 갱신
- **새 패턴 확립** 시 해당 섹션에 예시 추가
