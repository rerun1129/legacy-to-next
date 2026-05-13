# 다음 세션 이어서 작업할 항목

마지막 작업일: 2026-05-13 (House B/L CRUD 복원 Round 3 종료)

## 현재 master HEAD

```
293e21c fix(FE): entry page에 Suspense 경계 추가 (useSearchParams SSG 대응)
```

Round 3 commit chain (CRUD 복원, 위→아래 시간 역순):
```
293e21c FE Suspense 경계
806aff6 FE URL ?id → store 동기
886b3a7 FE mutation onSuccess Truck 정합 (§6.21)
9448dbb FE useBlDraftSync didRestoreFromDraftRef 가드
d3cdde1 FE HouseBlDetail seaDetail + form.reset nested 매핑
f560236 BE HouseBlDetailResponse nested seaDetail 18필드 (Phase A-1)
```

미커밋:
- `ENTRY_MIGRATION_GUIDE.md` — §6.49 매트릭스 ⑧~⑫ 추가 + Round 3 사례 단락 + §13.5 갱신. **commit 결정 필요**.

---

## 1. 다른 PC 환경 셋업

```powershell
# Node 20.20.2 (프로젝트는 fnm 사용 — nvm 금지)
eval "$(fnm env --use-on-cd --shell bash)" && fnm use 20.20.2

# 의존성
npm --prefix front-end install

# BE 빌드/기동 (IntelliJ에서 띄우는 게 일반적)
back-end/java-spring/gradlew.bat -p back-end/java-spring build
# 또는 IntelliJ에서 LegacyToNextApplication bootRun

# FE dev (Sea House Entry 검증용 — 캐시 정리 필수)
Remove-Item -Recurse -Force front-end\.next
npm --prefix front-end run dev
```

---

## 2. 사용자 수동 회귀 검증 (Round 3 결과 점검)

`http://localhost:3000/fms/house-bl/sea-exp/entry` + `/sea-imp/entry`에서 다음 시나리오 — 각 항목 PASS/FAIL 확인.

### Read 흐름 (이번 복원 핵심)
- [ ] **list 더블클릭 → entry** — toolbar/party/trade/schedule 모든 필드 채워짐
- [ ] **URL 직접 진입** (`?id=198344` 같은 형태) — 동일하게 form 채워짐
- [ ] **재진입** (entry → 메뉴 이동 → 5분 자리 비움 → 재진입) — detail 재조회 없이 캐시 표시 (§6.36)
- [ ] **다른 row 더블클릭 후 재진입** — 새 id의 detail로 form 갱신

### 채워져야 할 필드 (Phase A-1/A-2/A-3 효과)
- [ ] toolbar: HBL, MBL, Load Type, Shipment Type, Service Term, B/L Type, ETD/ETA, POL/POD
- [ ] Party 4종: shipper/consignee/notify/docPartner code + address
- [ ] Trade & Performance: Incoterms, Freight Term, **Payable At code**, Actual Customer, Operator/Team/Sales Man
- [ ] Schedule: Liner, Vessel, Voyage, Issue Date, **No. of B/L**, **Issue Place code**, D/O Date
- [ ] 자식 그리드(Container 등) — **빈 상태 확인 (후속 결함 #1 영역)**

### Write 흐름
- [ ] **Sea EXP 신규 INSERT** — 모든 필드 입력 + Container 3행 + 저장 → 성공 + form이 새 detail로 갱신
- [ ] **Sea IMP 신규 INSERT** — 동일
- [ ] **무수정 조회→저장 UPDATE** — p6spy 로그에서 UPDATE 0건 (§6.37)
- [ ] **Container 단일 행 수정 후 저장** — 해당 row만 UPDATE 발생 (§6.28 mergeContainers)
- [ ] **저장 후 router.push 자동 이동 없음** — entry에 머물러 refetch된 detail 확인 가능 (Phase C)
- [ ] **List 자동 invalidate 없음** — §6.21 준수 (저장 후 list 캐시 그대로)

### Change B/L No 모달
- [ ] **Change B/L No 클릭** — 모달 열림 (only isEdit)
- [ ] **새 B/L No 입력 후 Update** — toast 성공 + detail invalidate + form 갱신

### Delete
- [ ] **Delete 클릭 → 확인 → 삭제** — 정상

### 회귀 검증 (이전 작업 영향)
- [ ] enum round-trip — Load Type/Service Term/B/L Type/Shipment Type/Freight Term/Incoterms/NoOfBl ComboBox 선택 → 저장 → 재조회 시 동일 값
- [ ] address 4필드 round-trip — shipper/consignee/notify/docPartner address 저장/재조회 일치
- [ ] Party Clear 버튼 부재 (Truck 정합)
- [ ] page-head 버튼 배치: EXP는 New|Search|Save|Delete|Print|Switch B/L|Change B/L No, IMP는 Print/Switch B/L 숨김

---

## 3. 후속 open 결함 (Round 3 종료 시점)

**우선순위 순**:

### 3-A. 자식 컬렉션 BE detail 응답 노출 누락 (Critical)
- 영향: Sea Container 그리드 등 자식 컬렉션 모든 패널 빈 상태
- 대상 컬렉션: `containers` / `scheduleLegs` / `licenses` / `airCharges` / `dims` / `truckOrders`
- 작업 패턴: Phase A-1과 동일 — `HouseBlDetailResponse`/`HouseBlDetailResult`에 nested record 추가 + `HouseBlFactory.toDetailResult`에서 추출 + FE zod schema + form.reset nested 매핑
- 시작점: `HouseBlSea`에 자식 컬렉션 어떤 게 있는지 도메인 모델 확인 → BE 응답 record 설계

### 3-B. AIR 본체 issue 필드 nested 응답 노출
- 영향: Air House Entry에서 Issue Place 등 채워지지 않음 (Sea와 동일 회귀)
- 작업: Phase A-1 패턴으로 `AirDetailResponse` nested record 신설 → Factory 추출 → FE 매핑
- BE 도메인: `HouseBlAir.java` — `issuePlace` 등 필드 보유

### 3-C. `house-bl-entry.tsx` 464줄 분리 검토
- 300줄 초과, 500줄 미만 (강제 분리 아님)
- 분리 후보: page-head buttons / form.reset 매핑 / mutation 핸들러 / toolbar render
- Truck/Non은 `use-truck-bl-entry.ts` 같은 hook으로 핸들러 분리 — 동일 패턴 검토

### 3-D. `map-non-bl-detail.test.ts:8` `TS2353 'jobDiv' does not exist in type 'NonBlDetail'`
- Next build PASS / `tsc --noEmit`에서 표면화
- 기존 잠재 이슈, 이번 복원과 무관
- `NonBlDetail` 타입에 `jobDiv` 추가 또는 fixture에서 제거 (기존 테스트 수정이므로 사용자 승인 필요)

---

## 4. 가이드/메모리 참조

- **§6.49 매트릭스** (ENTRY_MIGRATION_GUIDE.md:1219~) — Round 3에서 누락 패턴 ⑧~⑫ 추가됨. 차기 도메인(Air House / Sea Master / Air Master) 마이그레이션 plan 단계에서 11개 사전 점검 의무.
- **§6.50 / §6.51 / §6.52 미신설** — 매트릭스 ⑧~⑫는 별도 절 신설 없이 §6.49 매트릭스에 통합. 추후 신설 결정 시 §6.50부터 신규 번호.
- **§13.5 선결 과제** — 라우팅 결함 해소 표시 + Round 3 후속 4건(open).

---

## 5. 다른 PC에서 빠르게 이어가기

1. `git pull origin master`
2. `git log --oneline -20` 확인 — 마지막 commit 293e21c 또는 그 이후
3. `ENTRY_MIGRATION_GUIDE.md` 미커밋 변경 있는지 확인:
   - 있으면: 내용 확인 후 `git add ENTRY_MIGRATION_GUIDE.md && git commit -m "docs(guide): §6.49 매트릭스 ⑧~⑫ + Round 3 사례 + §13.5 갱신"`
4. 본 RESUME.md의 **§2 수동 검증** 진행 → PASS/FAIL 기록
5. FAIL 발생 시 본 RESUME.md의 **§3 후속 결함**과 맞춰 작업 단위 결정
6. 우선순위: **3-A (자식 컬렉션) → 3-B (AIR issue) → 3-C (분리) → 3-D (TS2353)**
