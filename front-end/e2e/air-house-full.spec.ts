import { test, expect } from '@playwright/test';
import { makeTs } from './helpers/house-bl-form';
import {
  buildAirExpHousePayload,
  buildAirImpHousePayload,
} from './helpers/house-bl-payloads';
import { BACKEND_BASE } from './config';

// ── 공통 상수 ─────────────────────────────────────────────────────────────
const API_BASE = `${BACKEND_BASE}/api/house-bl`;
const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ── 공통 검증 헬퍼 ────────────────────────────────────────────────────────

// 생성 응답에서 id를 추출하고 양수임을 확인한다.
async function assertCreatedId(
  page: import('@playwright/test').Page,
  payload: object,
): Promise<number> {
  const res = await page.request.post(API_BASE, { data: payload, headers: JSON_HEADERS });
  expect(res.ok(), `POST ${API_BASE} failed: ${res.status()}`).toBeTruthy();
  const body = await res.json();
  const id: number = body.data.id;
  expect(id).toBeGreaterThan(0);
  return id;
}

// 생성된 id로 GET 호출 후 지정 필드를 검증한다.
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

// airDetail nested 필드 검증
async function assertAirDetail(
  page: import('@playwright/test').Page,
  id: number,
  checks: Record<string, unknown>,
): Promise<void> {
  const res = await page.request.get(`${API_BASE}/${id}`);
  expect(res.ok(), `GET ${API_BASE}/${id} failed: ${res.status()}`).toBeTruthy();
  const body = await res.json();
  const airDetail = (body.data as Record<string, unknown>).airDetail as Record<string, unknown> | null;
  expect(airDetail, 'airDetail should not be null').not.toBeNull();
  for (const [field, expected] of Object.entries(checks)) {
    expect(airDetail![field], `airDetail.${field} mismatch`).toBe(expected);
  }
}

// scheduleLeg 컬렉션 길이 및 특정 인덱스 필드 검증
async function assertScheduleLegs(
  page: import('@playwright/test').Page,
  id: number,
  expectedLength: number,
  rowIdx: number,
  fieldChecks: Record<string, unknown>,
): Promise<void> {
  const res = await page.request.get(`${API_BASE}/${id}`);
  expect(res.ok()).toBeTruthy();
  const body = await res.json();
  const airDetail = (body.data as Record<string, unknown>).airDetail as Record<string, unknown> | null;
  expect(airDetail).not.toBeNull();
  const legs = airDetail!.scheduleLegs as Array<Record<string, unknown>>;
  expect(legs, 'scheduleLegs should be array').toBeDefined();
  expect(legs.length, `scheduleLegs length mismatch`).toBe(expectedLength);
  const row = legs[rowIdx];
  for (const [field, expected] of Object.entries(fieldChecks)) {
    expect(row[field], `scheduleLegs[${rowIdx}].${field} mismatch`).toBe(expected);
  }
}

// ── AIR House B/L 회귀 E2E ───────────────────────────────────────────────
test.describe.serial('AIR House B/L 회귀 E2E', () => {

  // ── air-exp describe ────────────────────────────────────────────────────
  test.describe.serial('air-exp — 항공 수출 회귀 4종', () => {
    let createdId: number;
    const ts = makeTs();
    const payload = buildAirExpHousePayload(ts);

    // 시나리오 1 — INSERT 1회: 빈 폼 → AIR EXP 페이로드 입력 → Save → GET 일치
    test('INSERT — air-exp 생성 후 GET form.reset 전 필드 일치', async ({ page }) => {
      createdId = await assertCreatedId(page, payload);
      // 본체 필드 검증
      await assertGetById(page, createdId, {
        hblNo:  `HAWB${ts}`,
        jobDiv: 'AIR',
        bound:  'EXP',
      });
      // airDetail nested 필드 검증
      await assertAirDetail(page, createdId, {
        airlineCode: 'KE',
        rateClass:   'Q',
        currencyCode: 'USD',
        issuePlace:  'SEL',
      });
    });

    // 시나리오 2 — UPDATE 무수정: GET 후 dirty 없이 Save → 상태 코드 200
    test('UPDATE 무수정 — air-exp 조회 후 무변경 Save status 200', async ({ page }) => {
      // 이미 생성된 id를 PUT로 호출. 본체 필드만 포함한 최소 payload 전송.
      // (FE 화면 없이 API 수준 검증 — status code 200이 핵심)
      const updatePayload = {
        jobDiv: 'AIR', bound: 'EXP',
        hblNo: `HAWB${ts}`,
        shipmentType: 'HOUSE', freightTerm: 'PREPAID',
        polCode: 'KRICN', podCode: 'USLAX',
      };
      const res = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updatePayload,
        headers: JSON_HEADERS,
      });
      expect(res.ok(), `PUT ${API_BASE}/${createdId} failed: ${res.status()}`).toBeTruthy();
      expect(res.status()).toBe(200);
    });

    // 시나리오 3 — 자식 그리드 row 수정: scheduleLeg 1행 변경 → 해당 row 검증
    test('자식 그리드 row 수정 — air-exp scheduleLeg 1행 변경 후 해당 row 확인', async ({ page }) => {
      // GET으로 현재 scheduleLegs 조회 후 행 id 추출
      const getRes = await page.request.get(`${API_BASE}/${createdId}`);
      expect(getRes.ok()).toBeTruthy();
      const getBody = await getRes.json();
      const airDetail = (getBody.data as Record<string, unknown>).airDetail as Record<string, unknown>;
      const legs = airDetail.scheduleLegs as Array<Record<string, unknown>>;
      expect(legs.length).toBeGreaterThan(0);
      const firstLegId = legs[0].id as number;

      // 1행만 flightNo 변경하여 PUT
      const updatedLegs = [
        { id: firstLegId, toCode: 'PVG', byCarrier: 'KE', flightNo: 'KE999', onBoardDt: '20260601' },
        { toCode: 'LAX', byCarrier: 'KE', flightNo: 'KE017', onBoardDt: '20260602' },
      ];
      const updateWithLegs = {
        jobDiv: 'AIR', bound: 'EXP',
        hblNo: `HAWB${ts}`,
        shipmentType: 'HOUSE', freightTerm: 'PREPAID',
        polCode: 'KRICN', podCode: 'USLAX',
        scheduleLegs: updatedLegs,
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updateWithLegs,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT scheduleLeg update failed: ${putRes.status()}`).toBeTruthy();

      // 변경된 row만 확인 (인덱스 0이 id=firstLegId에 해당)
      await assertScheduleLegs(page, createdId, 2, 0, { flightNo: 'KE999' });
    });

    // 시나리오 4 — enum round-trip: rateClass 값 save → fetch 동일
    test('enum round-trip — air-exp rateClass M 저장 후 동일 값 조회', async ({ page }) => {
      // rateClass 를 'M'으로 변경하여 저장
      const enumPayload = {
        jobDiv: 'AIR', bound: 'EXP',
        hblNo: `HAWB${ts}`,
        shipmentType: 'HOUSE', freightTerm: 'PREPAID',
        polCode: 'KRICN', podCode: 'USLAX',
        airDetail: {
          airlineCode: 'KE',
          rateClass: 'M',
          currencyCode: 'USD',
        },
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: enumPayload,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT enum round-trip failed: ${putRes.status()}`).toBeTruthy();

      // GET 후 rateClass 동일 값 확인
      await assertAirDetail(page, createdId, { rateClass: 'M' });
    });

    // 정리 — 생성한 HAWB 삭제
    test('D — air-exp HAWB 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

  // ── air-imp describe ────────────────────────────────────────────────────
  test.describe.serial('air-imp — 항공 수입 회귀 4종', () => {
    let createdId: number;
    const ts = makeTs();
    const payload = buildAirImpHousePayload(ts);

    // 시나리오 1 — INSERT 1회
    test('INSERT — air-imp 생성 후 GET form.reset 전 필드 일치', async ({ page }) => {
      createdId = await assertCreatedId(page, payload);
      await assertGetById(page, createdId, {
        hblNo:  `HAWB${ts}`,
        jobDiv: 'AIR',
        bound:  'IMP',
      });
      await assertAirDetail(page, createdId, {
        airlineCode:  'OZ',
        rateClass:    'M',
        currencyCode: 'KRW',
        issuePlace:   'LAX',
      });
    });

    // 시나리오 2 — UPDATE 무수정
    test('UPDATE 무수정 — air-imp 조회 후 무변경 Save status 200', async ({ page }) => {
      const updatePayload = {
        jobDiv: 'AIR', bound: 'IMP',
        hblNo: `HAWB${ts}`,
        shipmentType: 'HOUSE', freightTerm: 'PREPAID',
        polCode: 'USLAX', podCode: 'KRICN',
      };
      const res = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updatePayload,
        headers: JSON_HEADERS,
      });
      expect(res.ok(), `PUT ${API_BASE}/${createdId} failed: ${res.status()}`).toBeTruthy();
      expect(res.status()).toBe(200);
    });

    // 시나리오 3 — 자식 그리드 row 수정
    test('자식 그리드 row 수정 — air-imp scheduleLeg 1행 변경 후 해당 row 확인', async ({ page }) => {
      const getRes = await page.request.get(`${API_BASE}/${createdId}`);
      expect(getRes.ok()).toBeTruthy();
      const getBody = await getRes.json();
      const airDetail = (getBody.data as Record<string, unknown>).airDetail as Record<string, unknown>;
      const legs = airDetail.scheduleLegs as Array<Record<string, unknown>>;
      expect(legs.length).toBeGreaterThan(0);
      const firstLegId = legs[0].id as number;

      const updatedLegs = [
        { id: firstLegId, toCode: 'ICN', byCarrier: 'OZ', flightNo: 'OZ999', onBoardDt: '20260610' },
        { toCode: 'GMP', byCarrier: 'OZ', flightNo: 'OZ202', onBoardDt: '20260611' },
      ];
      const updateWithLegs = {
        jobDiv: 'AIR', bound: 'IMP',
        hblNo: `HAWB${ts}`,
        shipmentType: 'HOUSE', freightTerm: 'PREPAID',
        polCode: 'USLAX', podCode: 'KRICN',
        scheduleLegs: updatedLegs,
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updateWithLegs,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT scheduleLeg update failed: ${putRes.status()}`).toBeTruthy();

      await assertScheduleLegs(page, createdId, 2, 0, { flightNo: 'OZ999' });
    });

    // 시나리오 4 — enum round-trip
    test('enum round-trip — air-imp rateClass Q 저장 후 동일 값 조회', async ({ page }) => {
      const enumPayload = {
        jobDiv: 'AIR', bound: 'IMP',
        hblNo: `HAWB${ts}`,
        shipmentType: 'HOUSE', freightTerm: 'PREPAID',
        polCode: 'USLAX', podCode: 'KRICN',
        airDetail: {
          airlineCode: 'OZ',
          rateClass: 'Q',
          currencyCode: 'KRW',
        },
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: enumPayload,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT enum round-trip failed: ${putRes.status()}`).toBeTruthy();

      await assertAirDetail(page, createdId, { rateClass: 'Q' });
    });

    // 정리
    test('D — air-imp HAWB 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

});
