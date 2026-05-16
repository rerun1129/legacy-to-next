# Phase 4 — QA·검증·커밋

> **언제 read**: 빌드/검증/커밋 단계.

본 phase는 lint·build 명령, 시각 회귀 점검 항목, CSS 토큰 위치, 커밋 메시지 컨벤션을 모은다. 풀스택 안티패턴은 phase3-data-flow.md, UI 안티패턴은 phase2-ui.md.

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
  - **무수정 조회→저장 시 p6spy update 미발사 확인** — List 더블클릭 → Entry → 수정 없이 Save → BE p6spy 로그에 UPDATE 0건 (§6.62). dirty 있으면 BE Factory 복합 VO method reference 1-arg 안티패턴 또는 FE Entry detail/draft race 점검

### 시각 회귀

이미 마이그레이션 완료된 sea-house / air-house Entry와 토큰 일관성 비교.

### 트러블슈팅 — localStorage layout state stale

widget 구조 변경(예: `MASTER_BL_SEA_REGISTRY`의 widget key/defaultPosition 수정, `buildSeaFields`의 widget 분할 재구성) 시 `useFieldLayout`/`useWidgetLayout`이 저장한 localStorage 키(`fms.fieldLayouts.v1`, 위젯 layout 키)의 stale state로 빈 행 또는 잘못된 위치 잔여 가능.

- 증상: 코드 변경 후 화면이 새 구조 미반영, 빈 행 추가 노출, 사라진 widget이 hidden bar로 잔여
- 우선 진단: DevTools → Application → Local Storage → 해당 키 삭제 또는 시크릿 창에서 재확인
- 후속 phase: `defaultOrder` 변경 감지 시 자동 invalidate 또는 version bump(`v1` → `v2`)로 강제 리셋 검토 (`OPEN_ITEMS.md` §1.6)

사례: `2a86c919` (Container 패널 신규 + main-sea layout 재배치 시 사용자 캐시 클리어 안내 필요).

---

## 9. 커밋 메시지 작성 시

PowerShell heredoc 사용 시 한글 메시지에 backtick(`) 포함하지 말 것 → PS 변수 expansion 오류. Bash heredoc 또는 일반 -m 사용 권장.

```bash
git commit -m "feat: <요약>

<상세>

Co-Authored-By: Claude <noreply@anthropic.com>"
```
