import { test, expect } from '@playwright/test';
import {
  makeTs,
  fillSeaExpForm, fillSeaImpForm,
  fillAirExpForm,  fillAirImpForm,
  fillTruckForm,   fillNonBlForm,
  buildSeaExpPayload, buildSeaImpPayload,
  buildAirExpPayload, buildAirImpPayload,
  buildTruckPayload,  buildNonBlPayload,
} from './helpers/house-bl-form';

// ── 공통 상수 ─────────────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api/house-bl';
const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ── 공통 검증 헬퍼 ────────────────────────────────────────────────────────
// 생성 응답에서 id를 추출하고 양수임을 확인한다.
async function assertCreatedId(page: import('@playwright/test').Page, payload: object): Promise<number> {
  const res = await page.request.post(API_BASE, { data: payload, headers: JSON_HEADERS });
  expect(res.ok(), `POST ${API_BASE} failed: ${res.status()}`).toBeTruthy();
  const body = await res.json();
  const id: number = body.data.id;
  expect(id).toBeGreaterThan(0);
  return id;
}

// 생성된 id로 GET 호출 후 핵심 필드가 null이 아님을 확인한다.
async function assertGetById(
  page: import('@playwright/test').Page,
  id: number,
  checks: Record<string, unknown>,
): Promise<void> {
  const res = await page.request.get(`${API_BASE}/${id}`);
  expect(res.ok(), `GET ${API_BASE}/${id} failed: ${res.status()}`).toBeTruthy();
  const body = await res.json();
  const data: Record<string, unknown> = body.data;
  for (const [field, expected] of Object.entries(checks)) {
    if (expected === 'not-null') {
      expect(data[field], `field "${field}" should not be null`).not.toBeNull();
    } else {
      expect(data[field], `field "${field}" mismatch`).toBe(expected);
    }
  }
}

// ── House B/L 전수 채움 E2E ──────────────────────────────────────────────
test.describe.serial('House B/L 전수 채움 E2E', () => {

  // ── sea-exp ──────────────────────────────────────────────────────────────
  test.describe.serial('sea-exp — 해상 수출 전수 채움', () => {
    let createdId: number;
    const ts = makeTs();

    test('C — API로 sea-exp HBL 생성', async ({ page }) => {
      createdId = await assertCreatedId(page, buildSeaExpPayload(ts));
    });

    test('R — sea-exp 상세 조회: hblNo·seaDetail null 아님 확인', async ({ page }) => {
      await assertGetById(page, createdId, {
        hblNo:    `HBL${ts}`,
        jobDiv:   'SEA',
        bound:    'EXP',
      });
    });

    test('UI — sea-exp entry 폼 진입 후 HBL No 입력', async ({ page }) => {
      await page.goto(`/fms/house-bl/sea-exp/entry`);
      await expect(page.locator('button[type="submit"]')).toBeVisible({ timeout: 10_000 });

      // 폼 입력 후 Save
      await fillSeaExpForm(page, `${ts}ui`);
      await page.click('button[type="submit"]');

      // Save 후 list로 redirect
      await page.waitForURL('**/fms/house-bl/sea-exp/list', { timeout: 15_000 });
    });

    test('D — sea-exp HBL 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

  // ── sea-imp ──────────────────────────────────────────────────────────────
  test.describe.serial('sea-imp — 해상 수입 전수 채움', () => {
    let createdId: number;
    const ts = makeTs();

    test('C — API로 sea-imp HBL 생성', async ({ page }) => {
      createdId = await assertCreatedId(page, buildSeaImpPayload(ts));
    });

    test('R — sea-imp 상세 조회: hblNo·bound 확인', async ({ page }) => {
      await assertGetById(page, createdId, {
        hblNo:  `HBL${ts}`,
        jobDiv: 'SEA',
        bound:  'IMP',
      });
    });

    test('UI — sea-imp entry 폼 진입 후 HBL No 입력', async ({ page }) => {
      await page.goto(`/fms/house-bl/sea-imp/entry`);
      await expect(page.locator('button[type="submit"]')).toBeVisible({ timeout: 10_000 });

      await fillSeaImpForm(page, `${ts}ui`);
      await page.click('button[type="submit"]');

      await page.waitForURL('**/fms/house-bl/sea-imp/list', { timeout: 15_000 });
    });

    test('D — sea-imp HBL 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

  // ── air-exp ──────────────────────────────────────────────────────────────
  test.describe.serial('air-exp — 항공 수출 전수 채움', () => {
    let createdId: number;
    const ts = makeTs();

    test('C — API로 air-exp HAWB 생성', async ({ page }) => {
      createdId = await assertCreatedId(page, buildAirExpPayload(ts));
    });

    test('R — air-exp 상세 조회: hblNo·jobDiv 확인', async ({ page }) => {
      await assertGetById(page, createdId, {
        hblNo:  `HAWB${ts}`,
        jobDiv: 'AIR',
        bound:  'EXP',
      });
    });

    test('UI — air-exp entry 폼 진입 후 HAWB No 입력', async ({ page }) => {
      await page.goto(`/fms/house-bl/air-exp/entry`);
      await expect(page.locator('button[type="submit"]')).toBeVisible({ timeout: 10_000 });

      await fillAirExpForm(page, `${ts}ui`);
      await page.click('button[type="submit"]');

      await page.waitForURL('**/fms/house-bl/air-exp/list', { timeout: 15_000 });
    });

    test('D — air-exp HAWB 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

  // ── air-imp ──────────────────────────────────────────────────────────────
  test.describe.serial('air-imp — 항공 수입 전수 채움', () => {
    let createdId: number;
    const ts = makeTs();

    test('C — API로 air-imp HAWB 생성', async ({ page }) => {
      createdId = await assertCreatedId(page, buildAirImpPayload(ts));
    });

    test('R — air-imp 상세 조회: hblNo·bound 확인', async ({ page }) => {
      await assertGetById(page, createdId, {
        hblNo:  `HAWB${ts}`,
        jobDiv: 'AIR',
        bound:  'IMP',
      });
    });

    test('UI — air-imp entry 폼 진입 후 HAWB No 입력', async ({ page }) => {
      await page.goto(`/fms/house-bl/air-imp/entry`);
      await expect(page.locator('button[type="submit"]')).toBeVisible({ timeout: 10_000 });

      await fillAirImpForm(page, `${ts}ui`);
      await page.click('button[type="submit"]');

      await page.waitForURL('**/fms/house-bl/air-imp/list', { timeout: 15_000 });
    });

    test('D — air-imp HAWB 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

  // ── truck ─────────────────────────────────────────────────────────────────
  test.describe.serial('truck — 트럭 전수 채움', () => {
    let createdId: number;
    const ts = makeTs();

    test('C — API로 truck HBL 생성', async ({ page }) => {
      createdId = await assertCreatedId(page, buildTruckPayload(ts));
    });

    test('R — truck 상세 조회: hblNo·jobDiv 확인', async ({ page }) => {
      await assertGetById(page, createdId, {
        hblNo:  `TRK${ts}`,
        jobDiv: 'TRUCK',
      });
    });

    test('UI — truck entry 폼 진입 후 Truck B/L No 입력', async ({ page }) => {
      await page.goto(`/fms/house-bl/truck/entry`);
      await expect(page.locator('button[type="submit"]')).toBeVisible({ timeout: 10_000 });

      await fillTruckForm(page, `${ts}ui`);
      await page.click('button[type="submit"]');

      await page.waitForURL('**/fms/house-bl/truck/list', { timeout: 15_000 });
    });

    test('D — truck HBL 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

  // ── non-bl ───────────────────────────────────────────────────────────────
  test.describe.serial('non-bl — Non B/L 전수 채움', () => {
    let createdId: number;
    const ts = makeTs();

    test('C — API로 non-bl HBL 생성', async ({ page }) => {
      createdId = await assertCreatedId(page, buildNonBlPayload(ts));
    });

    test('R — non-bl 상세 조회: hblNo·jobDiv 확인', async ({ page }) => {
      await assertGetById(page, createdId, {
        hblNo:  `NONBL${ts}`,
        jobDiv: 'NON_BL',
      });
    });

    test('UI — non-bl entry 폼 진입 후 Non B/L No 입력', async ({ page }) => {
      await page.goto(`/fms/house-bl/non-bl/entry`);
      await expect(page.locator('button[type="submit"]')).toBeVisible({ timeout: 10_000 });

      await fillNonBlForm(page, `${ts}ui`);
      await page.click('button[type="submit"]');

      await page.waitForURL('**/fms/house-bl/non-bl/list', { timeout: 15_000 });
    });

    test('D — non-bl HBL 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

});
