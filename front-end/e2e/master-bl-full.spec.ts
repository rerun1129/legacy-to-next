import { test, expect } from '@playwright/test';
import { fillMasterBlForm, type MasterBlVariant } from './helpers/master-bl-form';

const API_BASE = 'http://localhost:8080/api';

// ── 공유 상태: serial 테스트 내에서 CREATE → GET 간 id 전달 ──────────────
let seaExpId: number;
let seaImpId: number;
let airExpId: number;
let airImpId: number;

// ── variant별 공통 흐름: UI로 CREATE → GET으로 검증 ──────────────────────
//
// variant별 진입 URL:
//   sea-exp → /fms/master-bl/sea-exp/entry (jobDiv=SEA, bound=EXP)
//   sea-imp → /fms/master-bl/sea-imp/entry (jobDiv=SEA, bound=IMP)
//   air-exp → /fms/master-bl/air-exp/entry (jobDiv=AIR, bound=EXP)
//   air-imp → /fms/master-bl/air-imp/entry (jobDiv=AIR, bound=IMP)
//
// Save 후 list URL로 redirect됨 → 마지막에 등록된 레코드를 list API에서 추출
async function getLastCreatedIdFromApi(
  page: Parameters<typeof fillMasterBlForm>[0],
  variant: MasterBlVariant,
  ts: string,
): Promise<number> {
  // mblNo / mawbNo로 검색할 수 없으므로 list를 조회해 최신 id를 추출
  // master-bl list API: GET /api/master-bl?jobDiv=SEA&bound=EXP&size=1&page=0 (최신순)
  const isSea = variant.startsWith('sea');
  const isExp = variant.endsWith('exp');
  const jobDiv  = isSea ? 'SEA' : 'AIR';
  const bound   = isExp ? 'EXP' : 'IMP';

  const res = await page.request.get(`${API_BASE}/master-bl`, {
    params: { jobDiv, bound, size: '1', page: '0' },
  });
  expect(res.ok()).toBeTruthy();
  const body = await res.json();

  // 응답 형식: { data: { content: [{ id, mblNo, ... }], ... } }
  const content: Array<{ id: number; mblNo: string | null }> = body.data?.content ?? body.data ?? [];
  const latest = content[0];
  expect(latest, `list가 비어 있어 ${variant}:MBL${ts} 생성 확인 불가`).toBeTruthy();

  // 생성한 mblNo 또는 mawbNo가 ts를 포함하는 첫 번째 항목 검증
  const mblKey = isSea ? 'mblNo' : 'mblNo';
  expect(
    (latest as Record<string, unknown>)[mblKey],
    `최신 list 항목의 mblNo가 MBL${ts}가 아님`,
  ).toBe(`MBL${ts}`);

  return latest.id;
}

test.describe.serial('Master B/L 전수 채움 E2E', () => {

  // ── sea-exp ──────────────────────────────────────────────────────────────
  test('sea-exp: UI 폼 입력으로 CREATE', async ({ page }) => {
    const variant: MasterBlVariant = 'sea-exp';
    const ts = Date.now().toString();

    await page.goto(`/fms/master-bl/${variant}/entry`);

    // WidgetGrid ResizeObserver 렌더 완료 대기
    await page.waitForLoadState('networkidle');

    await fillMasterBlForm(page, { variant, ts });

    // Save 버튼 클릭
    await page.click('button[type="submit"]:has-text("Save")');

    // list URL로 redirect 확인
    await page.waitForURL(`**/fms/master-bl/${variant}/list`, { timeout: 20_000 });

    seaExpId = await getLastCreatedIdFromApi(page, variant, ts);
    expect(seaExpId).toBeGreaterThan(0);
  });

  test('sea-exp: GET /api/master-bl/{id} 응답 검증', async ({ page }) => {
    const res = await page.request.get(`${API_BASE}/master-bl/${seaExpId}`);
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    const d = body.data;

    expect(d.id).toBe(seaExpId);
    expect(d.mblNo).toBeTruthy();
    expect(d.jobDiv).toBe('SEA');
    expect(d.bound).toBe('EXP');
    expect(d.shipperCode).toBe('SHIPPER01');
    expect(d.consigneeCode).toBe('CONSIG01');
    expect(d.polCode).toBe('KRBSA');
    expect(d.podCode).toBe('USLAX');
  });

  // ── sea-imp ──────────────────────────────────────────────────────────────
  test('sea-imp: UI 폼 입력으로 CREATE', async ({ page }) => {
    const variant: MasterBlVariant = 'sea-imp';
    const ts = Date.now().toString();

    await page.goto(`/fms/master-bl/${variant}/entry`);
    await page.waitForLoadState('networkidle');

    await fillMasterBlForm(page, { variant, ts });

    await page.click('button[type="submit"]:has-text("Save")');
    await page.waitForURL(`**/fms/master-bl/${variant}/list`, { timeout: 20_000 });

    seaImpId = await getLastCreatedIdFromApi(page, variant, ts);
    expect(seaImpId).toBeGreaterThan(0);
  });

  test('sea-imp: GET /api/master-bl/{id} 응답 검증', async ({ page }) => {
    const res = await page.request.get(`${API_BASE}/master-bl/${seaImpId}`);
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    const d = body.data;

    expect(d.id).toBe(seaImpId);
    expect(d.mblNo).toBeTruthy();
    expect(d.jobDiv).toBe('SEA');
    expect(d.bound).toBe('IMP');
    expect(d.shipperCode).toBe('SHIPPER01');
    expect(d.consigneeCode).toBe('CONSIG01');
  });

  // ── air-exp ──────────────────────────────────────────────────────────────
  test('air-exp: UI 폼 입력으로 CREATE', async ({ page }) => {
    const variant: MasterBlVariant = 'air-exp';
    const ts = Date.now().toString();

    await page.goto(`/fms/master-bl/${variant}/entry`);
    await page.waitForLoadState('networkidle');

    await fillMasterBlForm(page, { variant, ts });

    await page.click('button[type="submit"]:has-text("Save")');
    await page.waitForURL(`**/fms/master-bl/${variant}/list`, { timeout: 20_000 });

    airExpId = await getLastCreatedIdFromApi(page, variant, ts);
    expect(airExpId).toBeGreaterThan(0);
  });

  test('air-exp: GET /api/master-bl/{id} 응답 검증', async ({ page }) => {
    const res = await page.request.get(`${API_BASE}/master-bl/${airExpId}`);
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    const d = body.data;

    expect(d.id).toBe(airExpId);
    expect(d.mblNo).toBeTruthy();
    expect(d.jobDiv).toBe('AIR');
    expect(d.bound).toBe('EXP');
    expect(d.shipperCode).toBe('SHIPPER01');
    expect(d.consigneeCode).toBe('CONSIG01');
  });

  // ── air-imp ──────────────────────────────────────────────────────────────
  test('air-imp: UI 폼 입력으로 CREATE', async ({ page }) => {
    const variant: MasterBlVariant = 'air-imp';
    const ts = Date.now().toString();

    await page.goto(`/fms/master-bl/${variant}/entry`);
    await page.waitForLoadState('networkidle');

    await fillMasterBlForm(page, { variant, ts });

    await page.click('button[type="submit"]:has-text("Save")');
    await page.waitForURL(`**/fms/master-bl/${variant}/list`, { timeout: 20_000 });

    airImpId = await getLastCreatedIdFromApi(page, variant, ts);
    expect(airImpId).toBeGreaterThan(0);
  });

  test('air-imp: GET /api/master-bl/{id} 응답 검증', async ({ page }) => {
    const res = await page.request.get(`${API_BASE}/master-bl/${airImpId}`);
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    const d = body.data;

    expect(d.id).toBe(airImpId);
    expect(d.mblNo).toBeTruthy();
    expect(d.jobDiv).toBe('AIR');
    expect(d.bound).toBe('IMP');
    expect(d.shipperCode).toBe('SHIPPER01');
    expect(d.consigneeCode).toBe('CONSIG01');
  });

});
