import { test, expect } from '@playwright/test';
import { makeTs } from './helpers/house-bl-form';
import {
  buildSeaExpMasterPayload,
  buildSeaImpMasterPayload,
} from './helpers/master-bl-payloads';
import { BACKEND_BASE } from './config';

// ── 공통 상수 ─────────────────────────────────────────────────────────────
const API_BASE = `${BACKEND_BASE}/api/master-bl`;
const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ── 공통 검증 헬퍼 ────────────────────────────────────────────────────────

/**
 * POST /api/master-bl 호출 후 ID-only 응답(§6.54)에서 id를 추출.
 * body.data.id > 0 검증 포함.
 */
async function assertCreatedId(
  page: import('@playwright/test').Page,
  payload: object,
): Promise<number> {
  const res = await page.request.post(API_BASE, { data: payload, headers: JSON_HEADERS });
  expect(res.ok(), `POST ${API_BASE} failed: ${res.status()}`).toBeTruthy();
  const body = await res.json();
  const id: number = body.data.id;
  expect(id, 'POST 응답 id는 양수여야 한다 (§6.54)').toBeGreaterThan(0);
  return id;
}

/**
 * GET /api/master-bl/{id} 후 root 본체 필드 검증.
 * checks 키는 detailResponse 최상위 필드명과 일치해야 한다.
 */
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

/**
 * seaDetail nested 필드 검증 (Phase 2 §6.49 ⑬ 정합).
 * BE MasterBlDetailResponse.seaDetail(SeaDetailResponse) 구조에 매핑.
 */
async function assertSeaDetail(
  page: import('@playwright/test').Page,
  id: number,
  checks: Record<string, unknown>,
): Promise<void> {
  const res = await page.request.get(`${API_BASE}/${id}`);
  expect(res.ok(), `GET ${API_BASE}/${id} failed: ${res.status()}`).toBeTruthy();
  const body = await res.json();
  const seaDetail = (body.data as Record<string, unknown>).seaDetail as Record<string, unknown> | null;
  expect(seaDetail, 'seaDetail should not be null for SEA jobDiv').not.toBeNull();
  for (const [field, expected] of Object.entries(checks)) {
    expect(seaDetail![field], `seaDetail.${field} mismatch`).toBe(expected);
  }
}

/**
 * seaDetail.desc nested 필드 검증 (desc 1:1 round-trip).
 * BE SeaDetailResponse.SeaDescView 구조에 매핑.
 */
async function assertSeaDesc(
  page: import('@playwright/test').Page,
  id: number,
  checks: Record<string, unknown>,
): Promise<void> {
  const res = await page.request.get(`${API_BASE}/${id}`);
  expect(res.ok(), `GET ${API_BASE}/${id} failed: ${res.status()}`).toBeTruthy();
  const body = await res.json();
  const seaDetail = (body.data as Record<string, unknown>).seaDetail as Record<string, unknown> | null;
  expect(seaDetail, 'seaDetail should not be null').not.toBeNull();
  const desc = seaDetail!.desc as Record<string, unknown> | null;
  // desc는 null일 수도 있으나, Phase 8 §6.55 DescView.empty() 적용으로 null 또는 빈 객체.
  // 본 검증은 desc가 non-null인 상태만 호출하는 시나리오에서 사용한다.
  expect(desc, 'seaDetail.desc should not be null').not.toBeNull();
  for (const [field, expected] of Object.entries(checks)) {
    expect(desc![field], `seaDetail.desc.${field} mismatch`).toBe(expected);
  }
}

// ── Sea Master B/L 회귀 E2E ──────────────────────────────────────────────
test.describe.serial('Sea Master B/L 회귀 E2E', () => {

  // ── sea-exp describe ────────────────────────────────────────────────────
  test.describe.serial('sea-exp — 해상 수출 회귀 4종', () => {
    let createdId: number;
    const ts = makeTs();
    const payload = buildSeaExpMasterPayload(ts);

    // 시나리오 1 — INSERT: 모든 필수 필드 + seaDetail nested + party 3종 + desc 채움 → POST → ID-only 검증 → GET 일치
    test('INSERT — sea-exp 생성 후 GET 본체 + seaDetail + desc 일치', async ({ page }) => {
      createdId = await assertCreatedId(page, payload);

      // 본체 root 필드 검증
      await assertGetById(page, createdId, {
        mblNo: `MBL${ts}`,
        jobDiv: 'SEA',
        bound: 'EXP',
        shipperCode: 'SHIPPER01',
        consigneeCode: 'CONSIG01',
        notifyCode: 'NOTIFY01',
        polCode: 'KRBSA',
        podCode: 'USLAX',
      });

      // seaDetail nested 필드 검증 (§6.49 ⑯ c — nested 위치 정합)
      await assertSeaDetail(page, createdId, {
        linerCode: 'COSCO',
        vesselName: 'COSCO PACIFIC',
        voyageNo: '0412N',
        blType: 'OBL',
        serviceTerm: 'CY/CY',
      });

      // desc 1:1 round-trip 검증
      await assertSeaDesc(page, createdId, {
        marks: 'MARK-EXP-001',
        description: 'ELECTRONIC COMPONENTS',
      });
    });

    // 시나리오 2 — UPDATE 무수정: 조회 후 최소 payload로 PUT → status 200
    // §6.37 PATCH 의미론 — master 본체 한정 필드만 포함, seaDetail null은 기존 값 유지
    test('UPDATE 무수정 — sea-exp 조회 후 무변경 Save status 200', async ({ page }) => {
      // 최소 payload: 필수 필드(mblNo, masterRefNo, polCode, podCode, etd, eta, teamCode)만 포함.
      // seaDetail은 null(미전달)로 기존 값 유지. §6.37 PATCH 의미론 검증.
      const updatePayload = {
        jobDiv: 'SEA',
        bound: 'EXP',
        mblNo: `MBL${ts}`,
        masterRefNo: `MREF${ts}`,
        freightTerm: 'PREPAID',
        polCode: 'KRBSA',
        podCode: 'USLAX',
        etd: '20260601',
        eta: '20260620',
        shipmentType: 'FCL',
        teamCode: 'TEAM01',
      };
      const res = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updatePayload,
        headers: JSON_HEADERS,
      });
      expect(res.ok(), `PUT ${API_BASE}/${createdId} failed: ${res.status()}`).toBeTruthy();
      expect(res.status()).toBe(200);
    });

    // 시나리오 3 — desc 1:1 수정: marks 단일 필드 변경 → PUT → GET round-trip 검증
    // SEA Master는 scheduleLegs/dims/airCharges 없이 desc 1:1만 존재 (자식 그리드 역할)
    test('desc 1:1 수정 — sea-exp marks 변경 후 round-trip 확인', async ({ page }) => {
      const updateWithDesc = {
        jobDiv: 'SEA',
        bound: 'EXP',
        mblNo: `MBL${ts}`,
        masterRefNo: `MREF${ts}`,
        freightTerm: 'PREPAID',
        polCode: 'KRBSA',
        podCode: 'USLAX',
        etd: '20260601',
        eta: '20260620',
        shipmentType: 'FCL',
        teamCode: 'TEAM01',
        desc: {
          marks: 'MARK-EXP-UPDATED',
          description: 'ELECTRONIC COMPONENTS UPDATED',
          descClause1: 'SAID TO CONTAIN',
          descClause2: 'SHIPPER LOAD AND COUNT',
        },
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updateWithDesc,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT desc update failed: ${putRes.status()}`).toBeTruthy();

      // marks 변경 결과 GET 검증
      await assertSeaDesc(page, createdId, {
        marks: 'MARK-EXP-UPDATED',
        description: 'ELECTRONIC COMPONENTS UPDATED',
      });
    });

    // 시나리오 4 — enum round-trip: blType / serviceTerm / loadType 값 변경 → PUT → GET 동일
    // §6.49 ⑭ — ComboBox enum 값 round-trip 검증
    test('enum round-trip — sea-exp blType/serviceTerm 변경 후 동일 값 조회', async ({ page }) => {
      const enumPayload = {
        jobDiv: 'SEA',
        bound: 'EXP',
        mblNo: `MBL${ts}`,
        masterRefNo: `MREF${ts}`,
        freightTerm: 'PREPAID',
        polCode: 'KRBSA',
        podCode: 'USLAX',
        etd: '20260601',
        eta: '20260620',
        shipmentType: 'FCL',
        teamCode: 'TEAM01',
        seaDetail: {
          linerCode: 'COSCO',
          vesselName: 'COSCO PACIFIC',
          voyageNo: '0412N',
          loadType: 'LCL',
          serviceTerm: 'CFS/CFS',
          blType: 'SWB',
        },
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: enumPayload,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT enum round-trip failed: ${putRes.status()}`).toBeTruthy();

      // 변경된 enum 값 GET 검증
      await assertSeaDetail(page, createdId, {
        loadType: 'LCL',
        serviceTerm: 'CFS/CFS',
        blType: 'SWB',
      });
    });

    // 정리 — 생성한 Sea Master 삭제
    test('D — sea-exp MBL 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

  // ── sea-imp describe ────────────────────────────────────────────────────
  test.describe.serial('sea-imp — 해상 수입 회귀 4종', () => {
    let createdId: number;
    const ts = makeTs();
    const payload = buildSeaImpMasterPayload(ts);

    // 시나리오 1 — INSERT: IMP 필수 분기 (consigneeCode @NotBlank SeaImpMasterGroup) 포함
    test('INSERT — sea-imp 생성 후 GET 본체 + seaDetail + desc 일치', async ({ page }) => {
      createdId = await assertCreatedId(page, payload);

      // 본체 root 필드 검증
      await assertGetById(page, createdId, {
        mblNo: `MBL${ts}`,
        jobDiv: 'SEA',
        bound: 'IMP',
        shipperCode: 'SHIPPER02',
        consigneeCode: 'CONSIG02',
        polCode: 'CNSHA',
        podCode: 'KRBSA',
      });

      // seaDetail nested 필드 검증 (IMP 특유 데이터)
      await assertSeaDetail(page, createdId, {
        linerCode: 'EVERGREEN',
        vesselName: 'EVER GIVEN',
        voyageNo: '0518W',
        blType: 'SWB',
        serviceTerm: 'CY/CY',
      });

      // desc round-trip 검증
      await assertSeaDesc(page, createdId, {
        marks: 'MARK-IMP-001',
        description: 'CLOTHING ACCESSORIES',
      });
    });

    // 시나리오 2 — UPDATE 무수정
    test('UPDATE 무수정 — sea-imp 조회 후 무변경 Save status 200', async ({ page }) => {
      const updatePayload = {
        jobDiv: 'SEA',
        bound: 'IMP',
        mblNo: `MBL${ts}`,
        masterRefNo: `MREF${ts}`,
        freightTerm: 'COLLECT',
        polCode: 'CNSHA',
        podCode: 'KRBSA',
        etd: '20260515',
        eta: '20260601',
        shipmentType: 'FCL',
        teamCode: 'TEAM02',
      };
      const res = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updatePayload,
        headers: JSON_HEADERS,
      });
      expect(res.ok(), `PUT ${API_BASE}/${createdId} failed: ${res.status()}`).toBeTruthy();
      expect(res.status()).toBe(200);
    });

    // 시나리오 3 — desc 1:1 수정: IMP marks 변경 → round-trip
    test('desc 1:1 수정 — sea-imp marks 변경 후 round-trip 확인', async ({ page }) => {
      const updateWithDesc = {
        jobDiv: 'SEA',
        bound: 'IMP',
        mblNo: `MBL${ts}`,
        masterRefNo: `MREF${ts}`,
        freightTerm: 'COLLECT',
        polCode: 'CNSHA',
        podCode: 'KRBSA',
        etd: '20260515',
        eta: '20260601',
        shipmentType: 'FCL',
        teamCode: 'TEAM02',
        desc: {
          marks: 'MARK-IMP-UPDATED',
          description: 'CLOTHING ACCESSORIES UPDATED',
          descClause1: 'SAID TO CONTAIN',
          descClause2: 'SHIPPER LOAD AND COUNT',
        },
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: updateWithDesc,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT desc update failed: ${putRes.status()}`).toBeTruthy();

      await assertSeaDesc(page, createdId, {
        marks: 'MARK-IMP-UPDATED',
        description: 'CLOTHING ACCESSORIES UPDATED',
      });
    });

    // 시나리오 4 — enum round-trip: IMP loadType / blType 변경
    test('enum round-trip — sea-imp loadType/blType 변경 후 동일 값 조회', async ({ page }) => {
      const enumPayload = {
        jobDiv: 'SEA',
        bound: 'IMP',
        mblNo: `MBL${ts}`,
        masterRefNo: `MREF${ts}`,
        freightTerm: 'COLLECT',
        polCode: 'CNSHA',
        podCode: 'KRBSA',
        etd: '20260515',
        eta: '20260601',
        shipmentType: 'FCL',
        teamCode: 'TEAM02',
        seaDetail: {
          linerCode: 'EVERGREEN',
          vesselName: 'EVER GIVEN',
          voyageNo: '0518W',
          loadType: 'LCL',
          serviceTerm: 'CFS/CY',
          blType: 'OBL',
        },
      };
      const putRes = await page.request.put(`${API_BASE}/${createdId}`, {
        data: enumPayload,
        headers: JSON_HEADERS,
      });
      expect(putRes.ok(), `PUT enum round-trip failed: ${putRes.status()}`).toBeTruthy();

      await assertSeaDetail(page, createdId, {
        loadType: 'LCL',
        serviceTerm: 'CFS/CY',
        blType: 'OBL',
      });
    });

    // 정리
    test('D — sea-imp MBL 삭제', async ({ page }) => {
      const res = await page.request.delete(`${API_BASE}/${createdId}`);
      expect(res.ok()).toBeTruthy();
    });
  });

});
