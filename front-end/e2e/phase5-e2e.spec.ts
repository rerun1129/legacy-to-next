import { test, expect, type Page } from '@playwright/test';

// bl-variants.ts BL_VARIANT_KEYS: ['sea-exp', 'sea-imp', 'air-exp', 'air-imp']
// sea-exp: hasContainer·hasIssueInfo 모두 true, 입력 필드가 가장 풍부한 variant
const HOUSE_VARIANT  = 'sea-exp';
const MASTER_VARIANT = 'sea-exp';

const HOUSE_LIST   = `/fms/house-bl/${HOUSE_VARIANT}/list`;
const HOUSE_ENTRY  = `/fms/house-bl/${HOUSE_VARIANT}/entry`;
const MASTER_LIST  = `/fms/master-bl/${MASTER_VARIANT}/list`;
const MASTER_ENTRY = `/fms/master-bl/${MASTER_VARIANT}/entry`;

// ── House B/L 폼 채우기 헬퍼 ───────────────────────────────────────────────
//
// house-bl-entry.tsx 조사 결과:
//
// [toolbar — form.register 연결 필드 → name attribute 있음]
//   form.register('hbl')    → input placeholder="HBL No",   name="hbl"
//   form.register('mbl')    → input placeholder="MBL No",   name="mbl"
//   form.register('lType')  → input placeholder="Load Type", name="lType"
//   form.register('settle') → input placeholder="Settle",    name="settle" (text input)
//
// [toolbar — form.register 미연결 → defaultValue only, name attribute 없음]
//   "Shipment Type", "Service Term", "B/L Type", "Master Ref"
//   → getByPlaceholder 로 접근 가능
//
// [Main 탭 — PartyPanel (party-panel.tsx)]
//   PartyBlock 마다: input placeholder="Code", input placeholder="Company Name"
//   textarea placeholder="Address (free text)"
//   → 동일 placeholder 다수 존재 → .nth(n) 로케이터 필요
//   SHIPPER=index0, CONSIGNEE=index1, NOTIFY=index2, DOC PARTNER=index3
//
// [Main 탭 — SchedulePanel (schedule-panel.tsx)]
//   Liner Code: input placeholder="Code" (party Code와 같은 placeholder, 패널이 다름)
//   Liner Name: input (placeholder 없음, defaultValue="COSCO SHIPPING")
//   Vessel: input (defaultValue 사용, placeholder 없음)
//   Voyage: input (defaultValue 사용, placeholder 없음)
//   ETD/ETA: PanelDateInput → DateInputBase → input placeholder="yyyyMMdd"
//     → 동일 placeholder 다수 존재 → .nth(n) 로케이터 필요
//   POL/POD: input placeholder="UNLOC" / input placeholder="Port Name"
//     → 동일 placeholder 다수 존재 → .nth(n) 로케이터 필요
//
// [Main 탭 — TradePanel (trade-panel.tsx)]
//   Incoterms/Freight Term/Payable At/Co-Load: defaultValue only, placeholder 없음
//   Actual Customer/Sales Man/Operator/Team: defaultValue only, placeholder 없음
//   → 채울 수 없음: placeholder 없고 label 텍스트와 input이 직접 연결된 label 요소 없음
//
// [WidgetGrid 주의사항]
//   WidgetGrid는 ResizeObserver로 containerWidth 측정 후 렌더
//   → page.waitForSelector로 패널 내 input이 실제 DOM에 나타날 때까지 대기 필요
//
async function fillHouseBlForm(page: Page): Promise<void> {
  // ── Toolbar (form.register 연결 필드) ──────────────────────────────────────
  await page.fill('input[name="hbl"]', 'HBLTEST0001');
  await page.fill('input[name="mbl"]', 'MBLTEST0001');
  await page.fill('input[name="lType"]', 'CY/CY');
  // settle: form.register 연결 text input — defaultValue "PREPAID" 이미 설정됨
  // clearFirst로 기존값 지우고 새 값 입력
  await page.fill('input[name="settle"]', 'PREPAID');

  // ── Toolbar (form.register 미연결, getByPlaceholder 접근) ──────────────────
  // "Shipment Type": defaultValue="FCL", form 미연결
  await page.getByPlaceholder('Shipment Type').fill('HOUSE');
  // "Service Term": defaultValue="FCL", form 미연결
  // 채울 수 없음: placeholder="Service Term" 이나 DOM에 렌더된 값은 defaultValue 기반으로 표시만 됨
  // → getByPlaceholder로 덮어쓰기는 가능 (defaultValue 있어도 fill은 작동)
  await page.getByPlaceholder('B/L Type').fill('OBL');

  // ── Main 탭 — PartyPanel ─────────────────────────────────────────────────
  // WidgetGrid가 ResizeObserver 완료 후 렌더하므로 party Code input 나타날 때까지 대기
  // party-panel.tsx: SHIPPER(0), CONSIGNEE(1), NOTIFY(2), DOC PARTNER(3) 순서
  await page.waitForSelector('input[placeholder="Code"]', { timeout: 5_000 });

  // SHIPPER Code
  await page.getByPlaceholder('Code').nth(0).fill('SHIPPER01');
  // CONSIGNEE Code
  await page.getByPlaceholder('Code').nth(1).fill('CONSIG01');
  // NOTIFY Code
  await page.getByPlaceholder('Code').nth(2).fill('NOTIFY01');

  // Company Name (동일 placeholder 순서: SHIPPER=0, CONSIGNEE=1, NOTIFY=2, DOC PARTNER=3)
  await page.getByPlaceholder('Company Name').nth(0).fill('SHIPPER CO LTD');
  await page.getByPlaceholder('Company Name').nth(1).fill('CONSIG CO LTD');

  // ── Main 탭 — SchedulePanel ─────────────────────────────────────────────
  // schedule-panel.tsx: PanelDateInput → DateInputBase → placeholder="yyyyMMdd"
  // ETD(index 0), ETA(index 1)
  await page.getByPlaceholder('yyyyMMdd').nth(0).fill('20260601');
  await page.getByPlaceholder('yyyyMMdd').nth(1).fill('20260620');

  // POL/POD: input placeholder="UNLOC"
  // schedule-panel.tsx LcnField: POL(index 0), POD(index 1), Delivery(index 2)
  await page.getByPlaceholder('UNLOC').nth(0).fill('KRBSA');
  await page.getByPlaceholder('UNLOC').nth(1).fill('USLAX');

  // ── Main 탭 — TradePanel ────────────────────────────────────────────────
  // 채울 수 없음: trade-panel.tsx LiField/LcnField 모두 placeholder 없고
  //   label은 <span> 요소로 label[for] 연결 없음 → Playwright getByLabel 미작동
  // 채울 수 없음: Incoterms, Freight Term, Payable At, Co-Load, Operator, Team, Sales Man
}

// ── Master B/L 폼 채우기 헬퍼 ─────────────────────────────────────────────
//
// master-bl-entry.tsx 조사 결과:
//
// [toolbar — 전체 form.register 미연결]
//   master-bl-entry.tsx toolbar: 모든 input이 defaultValue only, name attribute 없음
//   placeholder={f || ""} 로 렌더됨
//   "Master Ref"    → placeholder="Master Ref"
//   "MBL No"        → placeholder="MBL No"
//   "Line Bkg. No"  → placeholder="Line Bkg. No"
//   "Load Type"     → placeholder="Load Type"
//   "Service Term"  → placeholder="Service Term"
//   "B/L Type"      → placeholder="B/L Type"
//   "Shipment Type" → placeholder="Shipment Type"
//   "Status"        → placeholder="Status"
//
// [Main 탭 — MasterPartyPanel (master-panels.tsx)]
//   PartyBlock 마다: input placeholder="Code", input placeholder="Company Name"
//   textarea placeholder="Address (free text)"
//   SHIPPER=index0, CONSIGNEE=index1, NOTIFY=index2
//
// [Main 탭 — MasterSchedulePanel (master-schedule-panel.tsx, sea-exp)]
//   buildSeaFields: Liner Code(no placeholder, defaultValue), Vessel(no placeholder), Voyage(no placeholder)
//   ETD/ETA: PanelDateInput → placeholder="yyyyMMdd"
//   POL/POD: input placeholder="UNLOC"
//
// [Main 탭 — MasterCargoDocPanel (master-cargo-doc-panel.tsx)]
//   Main Item/HS Code/CBM/R-Ton: defaultValue only, placeholder 없음
//   PackageField → input type="number" (placeholder 없음), select
//   G/W → input type="number" (placeholder 없음)
//   Settle Partner/Co-Load Agent/Operator/Team: defaultValue only, placeholder 없음
//   → 채울 수 없음: placeholder 없고 label 연결 없음
//
async function fillMasterBlForm(page: Page): Promise<void> {
  // ── Toolbar (form.register 미연결, getByPlaceholder 접근) ──────────────────
  await page.getByPlaceholder('Master Ref').fill('MREF0001');
  await page.getByPlaceholder('MBL No').fill('MBLTEST0001');
  await page.getByPlaceholder('Line Bkg. No').fill('BKG-TEST-001');
  await page.getByPlaceholder('Shipment Type').fill('FCL');

  // ── Main 탭 — MasterPartyPanel ──────────────────────────────────────────
  // WidgetGrid ResizeObserver 완료 후 렌더 대기
  await page.waitForSelector('input[placeholder="Code"]', { timeout: 5_000 });

  // SHIPPER Code(index 0), CONSIGNEE Code(index 1)
  await page.getByPlaceholder('Code').nth(0).fill('SHIPPER01');
  await page.getByPlaceholder('Code').nth(1).fill('CONSIG01');

  await page.getByPlaceholder('Company Name').nth(0).fill('SHIPPER CO LTD');
  await page.getByPlaceholder('Company Name').nth(1).fill('CONSIG CO LTD');

  // ── Main 탭 — MasterSchedulePanel (sea-exp, isExp=true) ─────────────────
  // ETD(index 0), ETA(index 1)
  await page.getByPlaceholder('yyyyMMdd').nth(0).fill('20260601');
  await page.getByPlaceholder('yyyyMMdd').nth(1).fill('20260620');

  // POL(index 0), POD(index 1)
  await page.getByPlaceholder('UNLOC').nth(0).fill('KRBSA');
  await page.getByPlaceholder('UNLOC').nth(1).fill('USLAX');

  // ── Main 탭 — MasterCargoDocPanel ─────────────────────────────────────
  // 채울 수 없음: PackageField의 qty input은 type="number" placeholder 없음
  // 채울 수 없음: G/W input은 type="number" placeholder 없음
  // 채울 수 없음: Operator/Team/Settle Partner 등 LiField — placeholder 없고 label 연결 없음
}

// ── House B/L CRUD (serial) ───────────────────────────────────────────────
// createdId: CREATE 결과 id를 UPDATE/DELETE에서 공유
let houseBlCreatedId: number;

test.describe.serial('House B/L CRUD', () => {
  test('C — 신규 등록 후 id 추출', async ({ page }) => {
    // 백엔드 API를 직접 호출해서 HBL 생성 (UI form submit 우회 — E2E C단계 격리)
    const res = await page.request.post('http://localhost:8080/api/house-bl', {
      data: {
        jobDiv: 'SEA', bound: 'EXP', hblNo: 'HBLTEST0001',
        shipmentType: 'HOUSE', freightTerm: 'PREPAID',
        shipperCode: 'SHIPPER01', consigneeCode: 'CONSIG01',
        polCode: 'KRBSA', podCode: 'USLAX',
        etd: '20260601', eta: '20260620',
      },
      headers: { 'Content-Type': 'application/json' },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    houseBlCreatedId = body.data.id;
    expect(houseBlCreatedId).toBeGreaterThan(0);

    // list 페이지에서 데이터 로딩 확인
    await page.goto(HOUSE_LIST);
    await expect(page.getByText('로딩 중...')).not.toBeVisible({ timeout: 10_000 });
    await expect(page.getByText('데이터를 불러올 수 없습니다.')).not.toBeVisible();
  });

  test('R — list 조회 및 상세 확인', async ({ page }) => {
    await page.goto(HOUSE_LIST);

    // 로딩 완료 대기
    await expect(page.getByText('로딩 중...')).not.toBeVisible({ timeout: 10_000 });

    // 에러 없음 확인
    await expect(page.getByText('데이터를 불러올 수 없습니다.')).not.toBeVisible();

    // list에 행이 있음 (CREATE 이후)
    const firstRow = page.locator('table.grid--list tbody tr').first();
    await expect(firstRow).toBeVisible();

    // 상세 페이지 직접 진입 후 Save 버튼 존재 확인
    await page.goto(`${HOUSE_ENTRY}?id=${houseBlCreatedId}`);
    await expect(page.locator('button[type="submit"]:has-text("Save")')).toBeVisible({ timeout: 10_000 });
  });

  test('U — 수정', async ({ page }) => {
    await page.goto(`${HOUSE_ENTRY}?id=${houseBlCreatedId}`);

    // hbl 필드에 기존 데이터가 로드될 때까지 대기
    await page.waitForFunction(
      () => (document.querySelector('input[name="hbl"]') as HTMLInputElement | null)?.value?.length ?? 0 > 0,
      { timeout: 10_000 },
    );

    // HBL No 수정
    await page.fill('input[name="hbl"]', 'HBLTEST0002');
    await page.click('button[type="submit"]');

    // list로 redirect 확인
    await page.waitForURL(`**${HOUSE_LIST}`, { timeout: 15_000 });
  });

  test('D — 삭제 후 빈 entry 폼으로 리셋', async ({ page }) => {
    // window.confirm을 항상 true로 override (dialog 이벤트 타이밍 문제 우회)
    await page.addInitScript(() => { window.confirm = () => true; });
    await page.goto(`${HOUSE_ENTRY}?id=${houseBlCreatedId}`);
    await page.waitForSelector('button.btn--danger:not([disabled])');
    await page.click('button.btn--danger');

    // 삭제 후 ?id 없는 신규 entry로 replace (빈 폼)
    await page.waitForURL(`**${HOUSE_ENTRY}`, { timeout: 15_000 });
    await expect(page.url()).not.toContain('?id=');
    await expect(page.locator('input[name="hbl"]')).toHaveValue('');
  });

  test('C(마지막) — 삭제 후 빈 폼에서 재등록 (DB에 레코드 유지)', async ({ page }) => {
    await page.goto(HOUSE_ENTRY);
    await fillHouseBlForm(page);
    await page.click('button[type="submit"]');
    await page.waitForURL(`**${HOUSE_LIST}`, { timeout: 15_000 });
    await expect(page.getByText('로딩 중...')).not.toBeVisible({ timeout: 10_000 });
  });
});

// ── Master B/L CRUD (serial) ─────────────────────────────────────────────
// masterBLCreatedId: CREATE 결과 id를 UPDATE/DELETE에서 공유
let masterBLCreatedId: number;

test.describe.serial('Master B/L CRUD', () => {
  test('C — 신규 등록 후 id 추출', async ({ page }) => {
    // 매 실행마다 고유한 mblNo 생성 (중복 방지)
    const uniqueMbl = `MBL${Date.now()}`;
    // 백엔드 API 직접 호출로 MBL 생성
    const res = await page.request.post('http://localhost:8080/api/master-bl', {
      data: {
        jobDiv: 'SEA', bound: 'EXP', mblNo: uniqueMbl,
        masterRefNo: 'MREF0001', freightTerm: 'PREPAID',
        shipperCode: 'SHIPPER01', consigneeCode: 'CONSIG01',
        polCode: 'KRBSA', podCode: 'USLAX',
        etd: '20260601', eta: '20260620',
      },
      headers: { 'Content-Type': 'application/json' },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    masterBLCreatedId = body.data.id;
    expect(masterBLCreatedId).toBeGreaterThan(0);

    // list 페이지에서 데이터 로딩 확인
    await page.goto(MASTER_LIST);
    await expect(page.getByText('Loading...')).not.toBeVisible({ timeout: 10_000 });
    await expect(page.getByText('데이터를 불러오지 못했습니다.')).not.toBeVisible();
  });

  test('R — list 조회 및 상세 확인', async ({ page }) => {
    await page.goto(MASTER_LIST);

    // 로딩 완료 대기
    await expect(page.getByText('Loading...')).not.toBeVisible({ timeout: 10_000 });

    // 에러 없음 확인
    await expect(page.getByText('데이터를 불러오지 못했습니다.')).not.toBeVisible();

    // list에 행이 있음 (CREATE 이후)
    const firstRow = page.locator('table.grid--list tbody tr').first();
    await expect(firstRow).toBeVisible();

    // 상세 페이지 직접 진입 후 Save 버튼 존재 확인
    await page.goto(`${MASTER_ENTRY}?id=${masterBLCreatedId}`);
    await expect(page.locator('button[type="submit"]:has-text("Save")')).toBeVisible({ timeout: 10_000 });
  });

  test('U — 수정', async ({ page }) => {
    await page.goto(`${MASTER_ENTRY}?id=${masterBLCreatedId}`);

    // MBL No 필드에 기존 데이터가 로드될 때까지 대기
    // master-bl-entry.tsx toolbar: getByPlaceholder('MBL No') — form.register 미연결이므로
    // defaultValue 로드 완료를 value 존재로 확인
    await page.waitForFunction(
      () => {
        const inputs = Array.from(document.querySelectorAll('input'));
        const mblInput = inputs.find(
          (el) => (el as HTMLInputElement).placeholder === 'MBL No',
        ) as HTMLInputElement | undefined;
        return (mblInput?.value?.length ?? 0) > 0;
      },
      { timeout: 10_000 },
    );

    // MBL No 수정
    await page.getByPlaceholder('MBL No').fill('MBLTEST0002');
    await page.click('button[type="submit"]');

    // list로 redirect 확인
    await page.waitForURL(`**${MASTER_LIST}`, { timeout: 15_000 });
  });

  test('D — 삭제 후 빈 entry 폼으로 리셋', async ({ page }) => {
    await page.addInitScript(() => { window.confirm = () => true; });
    await page.goto(`${MASTER_ENTRY}?id=${masterBLCreatedId}`);
    await page.waitForSelector('button.btn--danger:not([disabled])');
    await page.click('button.btn--danger');

    // 삭제 후 ?id 없는 신규 entry로 replace (빈 폼)
    await page.waitForURL(`**${MASTER_ENTRY}`, { timeout: 15_000 });
    await expect(page.url()).not.toContain('?id=');
  });

  test('C(마지막) — 삭제 후 빈 폼에서 재등록 (DB에 레코드 유지)', async ({ page }) => {
    await page.goto(MASTER_ENTRY);
    await fillMasterBlForm(page);
    await page.click('button[type="submit"]');
    await page.waitForURL(`**${MASTER_LIST}`, { timeout: 15_000 });
    await expect(page.getByText('Loading...')).not.toBeVisible({ timeout: 10_000 });
  });
});

// ── 미구현 도메인 — stub 유지 확인 ──────────────────────────────────────────
test.describe('미구현 도메인 — stub 유지 확인', () => {
  test('Non B/L list 진입 시 에러 없음', async ({ page }) => {
    await page.goto('/fms/non-bl/list');

    // 루트(/) 또는 에러 페이지로 이동하지 않아야 함
    await expect(page).toHaveURL('/fms/non-bl/list');

    // 페이지 헤더 텍스트 존재 확인
    await expect(page.getByText('Non B/L List')).toBeVisible();
  });

  test('Truck B/L list 진입 시 에러 없음', async ({ page }) => {
    await page.goto('/fms/truck-bl/list');

    // 에러 페이지로 이동하지 않아야 함
    await expect(page).toHaveURL('/fms/truck-bl/list');

    // 페이지 헤더 텍스트 존재 확인 (truck-bl/list/page.tsx: "Truck B/L List")
    await expect(page.getByText('Truck B/L List')).toBeVisible();
  });

  test('Dashboard 진입 시 에러 없음', async ({ page }) => {
    await page.goto('/dashboard');

    // 에러 페이지로 이동하지 않아야 함
    await expect(page).toHaveURL('/dashboard');

    // dashboard/page.tsx: KpiStrip, ShipmentPipeline 등 — 컨테이너 클래스 확인
    await expect(page.locator('.app__main--dash')).toBeVisible();
  });
});
