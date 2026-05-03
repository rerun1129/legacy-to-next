import type { Page } from '@playwright/test';

// 페이로드 타입·빌더는 house-bl-payloads.ts에서 관리.
// spec 파일이 두 헬퍼를 모두 가져올 수 있도록 re-export.
export {
  buildSeaExpPayload, buildSeaImpPayload,
  buildAirExpPayload, buildAirImpPayload,
  buildTruckPayload,  buildNonBlPayload,
} from './house-bl-payloads';

// ── 타임스탬프 기반 고유 번호 ─────────────────────────────────────────────
// 테스트 실행마다 HBL 번호 충돌을 막기 위해 호출 시점 타임스탬프를 suffix로 사용.
export function makeTs(): string {
  return String(Date.now());
}

// ── SEA 공통 UI 채우기 ────────────────────────────────────────────────────
// house-bl-entry.tsx 조사 결과:
//   form.register 연결: hbl(name="hbl"), mbl(name="mbl"), lType(name="lType"), settle(name="settle")
//   form 미연결(placeholder only): "Shipment Type", "B/L Type", "Service Term"
//   PartyBlock: placeholder="Code" (.nth 순서: SHIPPER=0, CONSIGNEE=1, NOTIFY=2, DOC PARTNER=3)
//   SchedulePanel: placeholder="yyyyMMdd"(ETD=0, ETA=1), placeholder="UNLOC"(POL=0, POD=1)
async function fillSeaCommon(page: Page, ts: string): Promise<void> {
  // toolbar — form.register 연결 필드
  await page.fill('input[name="hbl"]', `HBL${ts}`);
  await page.fill('input[name="settle"]', 'PREPAID');

  // toolbar — form 미연결 필드
  await page.getByPlaceholder('Shipment Type').fill('HOUSE');
  await page.getByPlaceholder('B/L Type').fill('OBL');

  // PartyPanel: WidgetGrid ResizeObserver 완료 후 렌더 대기
  await page.waitForSelector('input[placeholder="Code"]', { timeout: 5_000 });

  await page.getByPlaceholder('Code').nth(0).fill('SHIPPER01');
  await page.getByPlaceholder('Code').nth(1).fill('CONSIG01');
  await page.getByPlaceholder('Company Name').nth(0).fill('SHIPPER CO LTD');
  await page.getByPlaceholder('Company Name').nth(1).fill('CONSIG CO LTD');

  // SchedulePanel
  await page.getByPlaceholder('yyyyMMdd').nth(0).fill('20260601');
  await page.getByPlaceholder('yyyyMMdd').nth(1).fill('20260620');
  await page.getByPlaceholder('UNLOC').nth(0).fill('KRBSA');
  await page.getByPlaceholder('UNLOC').nth(1).fill('USLAX');
}

// ── SEA EXP UI 채우기 (공통 + license 행 추가) ───────────────────────────
// LicensePanel은 SEA EXP 전용 (house-bl-submit.ts: variant.direction === 'EXP' 조건).
// license rows는 form.register(`licenses.${idx}.licenseNo`) 방식으로 name attribute 존재.
export async function fillSeaExpForm(page: Page, ts: string): Promise<void> {
  await fillSeaCommon(page, ts);

  // LicensePanel의 + 버튼 — panel__title "License" 패널 내 첫 btn--sm
  const licenseSection = page.locator('.panel').filter({
    has: page.locator('.panel__title:has-text("License")'),
  });
  const licensePlusBtn = licenseSection.locator('button.btn--sm').first();

  await licensePlusBtn.click();
  await licensePlusBtn.click();

  await page.fill('input[name="licenses.0.licenseNo"]', 'LIC-001');
  await page.fill('input[name="licenses.1.licenseNo"]', 'LIC-002');

  // ContainerPanel은 현재 stub(defaultValue 기반)이라 form.register 미연결.
  // 백엔드 검증은 API 직접 호출로 수행하므로 UI 입력 불필요.
}

// ── SEA IMP UI 채우기 (공통 — license 없음) ──────────────────────────────
export async function fillSeaImpForm(page: Page, ts: string): Promise<void> {
  await fillSeaCommon(page, ts);
  // IMP는 D/O Date 패널이 있으나 placeholder="yyyyMMdd"가 이미 3번째 index로 추가됨.
  // SchedulePanel ETD(0), ETA(1)은 이미 채워졌으므로 추가 처리 불필요.
}

// ── AIR 공통 UI 채우기 ────────────────────────────────────────────────────
// main-air.tsx 조사:
//   HAWB No: input[name="hbl"], MAWB No: input[name="mbl"]
//   Rate Class: input[name="lType"]
//   AirSchedulePanel: LEG_DATA가 defaultValue로 pre-populate → 행 수정 불필요
//   placeholder="Code": SHIPPER(0), CONSIGNEE(1)
async function fillAirCommon(page: Page, ts: string): Promise<void> {
  // toolbar — form.register 연결 필드
  await page.fill('input[name="hbl"]', `HAWB${ts}`);
  await page.fill('input[name="settle"]', 'PREPAID');

  // PartyPanel 렌더 대기
  await page.waitForSelector('input[placeholder="Code"]', { timeout: 5_000 });

  await page.getByPlaceholder('Code').nth(0).fill('SHIPPER01');
  await page.getByPlaceholder('Code').nth(1).fill('CONSIG01');
  await page.getByPlaceholder('Company Name').nth(0).fill('SHIPPER CO LTD');
  await page.getByPlaceholder('Company Name').nth(1).fill('CONSIG CO LTD');
}

// ── AIR EXP UI 채우기 ────────────────────────────────────────────────────
export async function fillAirExpForm(page: Page, ts: string): Promise<void> {
  await fillAirCommon(page, ts);
  // AirSchedulePanel에 LEG_DATA default rows가 이미 있으므로 추가 입력 불필요.
  // Issue Information 필드는 defaultValue로 pre-populate되어 있음.
}

// ── AIR IMP UI 채우기 ────────────────────────────────────────────────────
export async function fillAirImpForm(page: Page, ts: string): Promise<void> {
  await fillAirCommon(page, ts);
}

// ── TRUCK UI 채우기 ───────────────────────────────────────────────────────
// truck-order-panel.tsx: form.register(`truckOrders.${idx}.truckOrderNo`) 등
// house-bl-entry.tsx TOOLBAR_FIELDS_TRUCK: "Truck B/L No"(name="hbl"), "Settle"(name="settle")
// TRUCK variant는 MainTabSea → TruckOrderPanel 렌더 (WidgetGrid 사용 안 함).
export async function fillTruckForm(page: Page, ts: string): Promise<void> {
  await page.fill('input[name="hbl"]', `TRK${ts}`);
  await page.fill('input[name="settle"]', 'PREPAID');

  // TruckOrderPanel은 tab-content 안에서 바로 렌더 (WidgetGrid 아님)
  await page.waitForSelector('.panel__title:has-text("Truck Order")', { timeout: 5_000 });

  const truckSection = page.locator('.panel').filter({
    has: page.locator('.panel__title:has-text("Truck Order")'),
  });
  const truckPlusBtn = truckSection.locator('button.btn--sm').first();

  await truckPlusBtn.click();
  await truckPlusBtn.click();

  await page.fill('input[name="truckOrders.0.truckOrderNo"]', 'TO-001');
  await page.fill('input[name="truckOrders.0.truckNo"]', 'TRUCK-A1');
  await page.fill('input[name="truckOrders.0.driver"]', 'Kim Driver');
  await page.fill('input[name="truckOrders.0.mobileNo"]', '010-1234-5678');

  await page.fill('input[name="truckOrders.1.truckOrderNo"]', 'TO-002');
  await page.fill('input[name="truckOrders.1.truckNo"]', 'TRUCK-B2');
  await page.fill('input[name="truckOrders.1.driver"]', 'Lee Driver');
  await page.fill('input[name="truckOrders.1.mobileNo"]', '010-9876-5432');
}

// ── NON_BL UI 채우기 ─────────────────────────────────────────────────────
// TOOLBAR_FIELDS_NON_BL: "Non B/L No"(name="hbl"), "Settle"(name="settle")
// NON_BL은 MainTabSea → WidgetGrid 렌더 (기본 SEA 위젯 재활용).
export async function fillNonBlForm(page: Page, ts: string): Promise<void> {
  await page.fill('input[name="hbl"]', `NONBL${ts}`);
  await page.fill('input[name="settle"]', 'PREPAID');

  // PartyPanel 렌더 대기 — NON_BL도 WidgetGrid 사용
  await page.waitForSelector('input[placeholder="Code"]', { timeout: 5_000 });

  await page.getByPlaceholder('Code').nth(0).fill('SHIPPER01');
  await page.getByPlaceholder('Code').nth(1).fill('CONSIG01');
}
