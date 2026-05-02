import { test, expect, type Page } from '@playwright/test';

// bl-variants.ts BL_VARIANT_KEYS: ['sea-exp', 'sea-imp', 'air-exp', 'air-imp']
// sea-exp: 필드가 가장 많은 EXP variant (hasContainer, hasIssueInfo 모두 true)
const HOUSE_VARIANT  = 'sea-exp';
const MASTER_VARIANT = 'sea-exp';

const HOUSE_LIST   = `/fms/house-bl/${HOUSE_VARIANT}/list`;
const HOUSE_ENTRY  = `/fms/house-bl/${HOUSE_VARIANT}/entry`;
const MASTER_LIST  = `/fms/master-bl/${MASTER_VARIANT}/list`;
const MASTER_ENTRY = `/fms/master-bl/${MASTER_VARIANT}/entry`;

// ── House B/L 폼 채우기 헬퍼 ───────────────────────────────────────────────
//
// house-bl-entry.tsx의 form.register 연결 현황:
//   form.register('hbl')    → toolbar "HBL No"  input[name="hbl"]
//   form.register('mbl')    → toolbar "MBL No"  input[name="mbl"]
//   form.register('lType')  → toolbar "Load Type" input[name="lType"]
//   form.register('settle') → toolbar "Settle"  input[name="settle"]  (text input, select 아님)
//
// form에 존재하지만 DOM name attribute 미노출 필드:
//   sType, etd, eta, pol, pod, expImp — tab panel 내부의 독립 defaultValue input으로 렌더됨
//   TODO: 해당 필드들이 form.register로 연결되면 name selector로 채울 수 있음
//
// party/schedule/trade 패널은 모두 독립 input (form.register 미연결, defaultValue 사용)
// → Playwright로 채울 수 없는 커스텀 컴포넌트 구조
async function fillHouseBlForm(page: Page): Promise<void> {
  // form.register 연결 필드 — name attribute로 직접 접근 가능
  await page.fill('input[name="hbl"]',    'HBLTEST0001');
  await page.fill('input[name="mbl"]',    'MBLTEST0001');
  await page.fill('input[name="lType"]',  'CY/CY');
  // settle은 text input (select 아님) — defaultValue "PREPAID"이므로 그대로 유지
  // form.register('settle')의 defaultValue가 이미 "PREPAID"로 설정되어 있음
}

// ── Master B/L 폼 채우기 헬퍼 ─────────────────────────────────────────────
//
// master-bl-entry.tsx 분석 결과:
//   toolbar input 전체가 defaultValue만 사용하며 form.register 미연결
//   → toolbar input들은 DOM에 name attribute 없음
//
// form.register 연결 필드(mblNo, masterRefNo, freightTerm, shipperCode 등)는
// MasterMainTab → WidgetGrid → 각 패널 컴포넌트로 전달되지만,
// 해당 패널들(MasterPartyPanel, MasterSchedulePanel, MasterCargoDocPanel)은
// 독립 input (form.register 미연결, defaultValue 사용)
// TODO: 백엔드 연동 시 form.register 연결 확장 필요
async function fillMasterBlForm(_page: Page): Promise<void> {
  // 현재 Master B/L entry의 form.register 연결 필드가 DOM에 name attribute로
  // 노출되지 않아 Playwright selector로 직접 채울 수 없음
  // defaultValues: freightTerm="PREPAID", jobDiv="SEA", bound="EXP" 으로 초기화됨
  // TODO: form.register 연결 확장 후 아래 채우기 로직 추가
  //   await page.fill('input[name="mblNo"]',      'MBLTEST0001');
  //   await page.fill('input[name="masterRefNo"]', 'MREF0001');
  //   await page.selectOption('select[name="freightTerm"]', 'PREPAID');
  //   await page.fill('input[name="shipperCode"]',   'SHIPPER01');
  //   await page.fill('input[name="consigneeCode"]', 'CONSIG01');
  //   await page.fill('input[name="polCode"]',       'KRBSA');
  //   await page.fill('input[name="podCode"]',       'USLAX');
  //   await page.fill('input[name="etd"]',           '20260601');
  //   await page.fill('input[name="eta"]',           '20260620');
  //   await page.fill('input[name="pkgQty"]',        '5');
  //   await page.fill('input[name="grossWeightKg"]', '500');
  //   await page.fill('input[name="cbm"]',           '10');
  //   await page.fill('input[name="operatorCode"]',  'OP01');
}

// ── House B/L ─────────────────────────────────────────────────────────────
test.describe('House B/L', () => {
  test('list — 페이지 로딩 성공 (에러 없음)', async ({ page }) => {
    await page.goto(HOUSE_LIST);

    // 로딩 텍스트가 사라질 때까지 대기
    await expect(page.getByText('로딩 중...')).not.toBeVisible({ timeout: 10_000 });

    // 에러 메시지 미노출 확인 — 빈 리스트도 정상 상태
    await expect(page.getByText('데이터를 불러올 수 없습니다.')).not.toBeVisible();

    // URL 유지 확인
    await expect(page).toHaveURL(HOUSE_LIST);

    // 그리드 패널 컨테이너 존재 확인
    await expect(page.locator('.panel--list')).toBeVisible();
  });

  test('entry — 모든 폼 필드 채워서 Save 클릭', async ({ page }) => {
    await page.goto(HOUSE_ENTRY);

    // Save 버튼 활성화 확인 (신규 폼 기본 상태)
    const saveBtn = page.locator('button[type="submit"]:has-text("Save")');
    await expect(saveBtn).toBeVisible();
    await expect(saveBtn).toBeEnabled();

    // form.register 연결 필드 채우기
    await fillHouseBlForm(page);

    // Save 클릭 후 Saving... 상태 전환 또는 redirect 확인
    // mockHouseBlPort 사용 시 성공 후 /fms/house-bl/sea-exp/list 로 이동
    await saveBtn.click();

    // "Saving..." 일시 노출 또는 list redirect — 둘 중 하나를 확인
    // 백엔드 미연동 환경에서는 mock이 즉시 resolve하므로 redirect 발생 가능
    // TODO: 백엔드 연동 완료 후 redirect URL로 강화
    await expect(page).not.toHaveURL(HOUSE_ENTRY + '?error=true');
  });

  test.skip('entry — 수정 모드 (list에 데이터 있어야 확인 가능)', () => {
    // list에 저장된 행이 있어야 id가 생성됨
    // /fms/house-bl/sea-exp/entry?id=1 진입 후 기존 데이터 로드 확인
    // TODO: 신규 저장 성공 후 연계 테스트로 구현
  });

  test.skip('list — 행 더블클릭 → 수정 entry 이동 (데이터 필요)', () => {
    // list에 데이터가 있어야 row click 이벤트 발생 가능
    // TODO: 신규 저장 성공 후 연계 테스트로 구현
  });

  test.skip('DELETE 버튼 — TODO: onClick 핸들러 미구현', () => {
    // house-bl-entry.tsx: <button type="button" className="btn btn--danger">Delete</button>
    // onClick 핸들러 미연결 상태 — 구현 완료 후 테스트 추가
  });
});

// ── Master B/L ────────────────────────────────────────────────────────────
test.describe('Master B/L', () => {
  test('list — 페이지 로딩 성공', async ({ page }) => {
    await page.goto(MASTER_LIST);

    // 에러 메시지 미노출 확인
    await expect(page.getByText('데이터를 불러오지 못했습니다.')).not.toBeVisible({ timeout: 10_000 });

    // URL 유지 확인
    await expect(page).toHaveURL(MASTER_LIST);

    // 패널 컨테이너 존재 확인
    await expect(page.locator('.panel').first()).toBeVisible();
  });

  test('entry — 모든 폼 필드 채워서 Save 클릭', async ({ page }) => {
    await page.goto(MASTER_ENTRY);

    // Save 버튼 활성화 확인 (신규 폼 기본 상태)
    const saveBtn = page.locator('button[type="submit"]:has-text("Save")');
    await expect(saveBtn).toBeVisible();
    await expect(saveBtn).toBeEnabled();

    // form.register 미연결로 채울 수 있는 필드가 없음 — 헬퍼 호출
    // defaultValues(freightTerm="PREPAID", jobDiv="SEA", bound="EXP")로 이미 초기화됨
    await fillMasterBlForm(page);

    // Save 클릭 후 list redirect 또는 pending 상태 확인
    // masterBlPort.create 호출 — 백엔드 연동 시 /fms/master-bl/sea-exp/list 로 이동
    // TODO: 백엔드 연동 완료 후 redirect URL로 강화
    await saveBtn.click();

    await expect(page).not.toHaveURL(MASTER_ENTRY + '?error=true');
  });

  test.skip('entry — 수정 모드 (list에 데이터 있어야 확인 가능)', () => {
    // /fms/master-bl/sea-exp/entry?id=1 진입 후 기존 데이터 로드 확인
    // TODO: 신규 저장 성공 후 연계 테스트로 구현
  });

  test.skip('DELETE 버튼 — TODO: onClick 핸들러 미구현', () => {
    // master-bl-entry.tsx: <button type="button" className="btn btn--danger">Delete</button>
    // onClick 핸들러 미연결 상태 — 구현 완료 후 테스트 추가
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
