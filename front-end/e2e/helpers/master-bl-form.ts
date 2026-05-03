import type { Page } from '@playwright/test';

// SEA variant: toolbar placeholder 필드명
// master-bl-entry.tsx: TOOLBAR_SEA = ["Master Ref", "MBL No", "Line Bkg. No", "Load Type", "Service Term", "B/L Type", "Shipment Type", "Status"]
// AIR variant: TOOLBAR_AIR = ["Master Ref", "MAWB No", "Shipment Type", "Status"]

export type MasterBlVariant = 'sea-exp' | 'sea-imp' | 'air-exp' | 'air-imp';

interface FillOptions {
  variant: MasterBlVariant;
  ts: string;
}

// ── 파티 패널 ──────────────────────────────────────────────────────────────
// master-panels.tsx MasterPartyPanel: PARTIES = ["SHIPPER", "CONSIGNEE", "NOTIFY"]
// PartyBlock마다 placeholder="Code" input이 순서대로 렌더됨 (index 0, 1, 2)
async function fillPartyPanel(page: Page): Promise<void> {
  await page.waitForSelector('input[placeholder="Code"]', { timeout: 8_000 });
  await page.getByPlaceholder('Code').nth(0).fill('SHIPPER01');
  await page.getByPlaceholder('Code').nth(1).fill('CONSIG01');
}

// ── SEA Schedule 패널 ──────────────────────────────────────────────────────
// master-schedule-panel.tsx SeaScheduleSection:
//   form.register("seaDetail.linerCode")  → name="seaDetail.linerCode"
//   form.register("seaDetail.vesselCode") → name="seaDetail.vesselCode"
//   form.register("seaDetail.vesselName") → name="seaDetail.vesselName"
//   form.register("seaDetail.voyageNo")   → name="seaDetail.voyageNo"
//   form.register("polCode")              → name="polCode"
//   form.register("podCode")              → name="podCode"
//   form.register("seaDetail.issueDate")  → name="seaDetail.issueDate", placeholder="yyyyMMdd"
async function fillSeaSchedulePanel(page: Page): Promise<void> {
  await page.fill('input[name="seaDetail.linerCode"]', 'COSCO');
  await page.fill('input[name="seaDetail.vesselCode"]', 'VSLC01');
  await page.fill('input[name="seaDetail.vesselName"]', 'COSCO PACIFIC');
  await page.fill('input[name="seaDetail.voyageNo"]',   '0412N');
  await page.fill('input[name="polCode"]', 'KRBSA');
  await page.fill('input[name="podCode"]', 'USLAX');
  // issueDate: placeholder="yyyyMMdd" (name으로 직접 선택)
  await page.fill('input[name="seaDetail.issueDate"]', '20260601');
}

// ── AIR Schedule 패널 (scheduleLegs useFieldArray) ─────────────────────────
// master-schedule-panel.tsx AirLegsSection:
//   Add 버튼 클릭 → append({ toCode, onBoardDt, arrivalDt })
//   form.register(`scheduleLegs.${i}.toCode`)    → name="scheduleLegs.0.toCode"
//   form.register(`scheduleLegs.${i}.byCarrier`) → name="scheduleLegs.0.byCarrier"
//   form.register(`scheduleLegs.${i}.flightNo`)  → name="scheduleLegs.0.flightNo"
//   form.register(`scheduleLegs.${i}.onBoardDt`) → name="scheduleLegs.0.onBoardDt"
//   form.register(`scheduleLegs.${i}.arrivalDt`) → name="scheduleLegs.0.arrivalDt"
async function fillAirScheduleLegs(page: Page): Promise<void> {
  // 패널 Add 버튼: "Schedule Legs" subhead 옆 btn
  const addBtn = page.locator('button:has-text("Add")').first();

  // row 0
  await addBtn.click();
  await page.fill('input[name="scheduleLegs.0.toCode"]',    'INICE');
  await page.fill('input[name="scheduleLegs.0.byCarrier"]', 'KE');
  await page.fill('input[name="scheduleLegs.0.flightNo"]',  'KE001');
  await page.fill('input[name="scheduleLegs.0.onBoardDt"]', '20260601');
  await page.fill('input[name="scheduleLegs.0.arrivalDt"]', '20260602');

  // row 1
  await addBtn.click();
  await page.fill('input[name="scheduleLegs.1.toCode"]',    'USLAX');
  await page.fill('input[name="scheduleLegs.1.byCarrier"]', 'KE');
  await page.fill('input[name="scheduleLegs.1.flightNo"]',  'KE002');
  await page.fill('input[name="scheduleLegs.1.onBoardDt"]', '20260602');
  await page.fill('input[name="scheduleLegs.1.arrivalDt"]', '20260603');
}

// ── Cargo & Document 패널 ──────────────────────────────────────────────────
// master-cargo-doc-panel.tsx (form 연결 필드):
//   form.register("mainItemName")        → name="mainItemName"
//   form.register("hsCode")              → name="hsCode"
//   form.register("pkgQty")              → name="pkgQty", type="number"
//   form.register("pkgUnit")             → name="pkgUnit" (select)
//   form.register("grossWeightKg")       → name="grossWeightKg", type="number"
//   form.register("cbm")                 → name="cbm", type="number"
//   SEA only: form.register("seaDetail.rton") → name="seaDetail.rton", type="number"
//   form.register("settlePartnerCode")   → name="settlePartnerCode"
//   form.register("operatorCode")        → name="operatorCode"
async function fillCargoDocPanel(page: Page, isSea: boolean): Promise<void> {
  await page.fill('input[name="mainItemName"]', 'ELECTRONIC GOODS');
  await page.fill('input[name="hsCode"]',       '8517.13');
  await page.fill('input[name="pkgQty"]',       '100');
  await page.selectOption('select[name="pkgUnit"]', 'CTN');
  await page.fill('input[name="grossWeightKg"]', '2500');
  await page.fill('input[name="cbm"]',           '14.5');

  if (isSea) {
    await page.fill('input[name="seaDetail.rton"]', '14.5');
  }

  await page.fill('input[name="settlePartnerCode"]', 'SETTLE01');
  await page.fill('input[name="operatorCode"]',      'OPR01');
}

// ── Marks & Numbers 패널 ──────────────────────────────────────────────────
// master-panels.tsx MasterMarksPanel:
//   Controller name="desc.marks" → LineNumberTextarea
//   LineNumberTextarea 내부: <textarea> 렌더됨
async function fillMarksPanel(page: Page): Promise<void> {
  // LineNumberTextarea는 textarea를 내부에서 렌더함
  // desc.marks Controller로 연결 — placeholder 없음, 순서 기반 (marks=0번째)
  await page.locator('textarea').nth(0).fill('MARK-001\nMADE IN KOREA');
}

// ── Goods Description 패널 ────────────────────────────────────────────────
// master-panels.tsx MasterGoodsDescPanel:
//   SEA: form.register("desc.descClause1") → select
//   Controller name="desc.description" → LineNumberTextarea (textarea, nth 1)
async function fillGoodsDescPanel(page: Page, isSea: boolean): Promise<void> {
  if (isSea) {
    await page.selectOption('select[name="desc.descClause1"]', 'SAID TO CONTAIN');
  }
  // description textarea: marks(0)에 이어 두 번째 textarea
  await page.locator('textarea').nth(1).fill('SAID TO CONTAIN\nELECTRONIC GOODS');
}

// ── AIR Dims 패널 ─────────────────────────────────────────────────────────
// master-container-dim-panel.tsx AirDimsGrid:
//   Add 버튼(Plus icon only): class="btn btn--sm" 내 Plus
//   form.register(`dims.${i}.lengthCm`)   → name="dims.0.lengthCm"
//   form.register(`dims.${i}.widthCm`)    → name="dims.0.widthCm"
//   form.register(`dims.${i}.heightCm`)   → name="dims.0.heightCm"
//   form.register(`dims.${i}.quantity`)   → name="dims.0.quantity"
async function fillAirDimsPanel(page: Page): Promise<void> {
  // AIR Dims 패널의 Add 버튼은 Plus icon만 있는 btn (텍스트 없음)
  // "Load Dimension" 버튼 다음에 위치
  const dimsAddBtn = page.locator('button:has-text(""):near(button:has-text("Load Dimension"))').first();
  // 더 안정적인 방법: dims 그리드 내 Plus 아이콘 버튼
  const dimsPlusBtn = page.locator('.panel__body button.btn--sm').filter({ hasNotText: /\w/ }).first();

  await dimsPlusBtn.click();
  await page.fill('input[name="dims.0.lengthCm"]', '120');
  await page.fill('input[name="dims.0.widthCm"]',  '80');
  await page.fill('input[name="dims.0.heightCm"]', '60');
  await page.fill('input[name="dims.0.quantity"]', '10');

  await dimsPlusBtn.click();
  await page.fill('input[name="dims.1.lengthCm"]', '100');
  await page.fill('input[name="dims.1.widthCm"]',  '70');
  await page.fill('input[name="dims.1.heightCm"]', '50');
  await page.fill('input[name="dims.1.quantity"]', '5');
}

// ── AIR Charges 패널 ─────────────────────────────────────────────────────
// master-air-charges-panel.tsx AirChargesGrid:
//   Add 버튼: btn btn--sm inside panel__actions
//   form.register(`airCharges.${i}.freightCode`)  → name="airCharges.0.freightCode"
//   form.register(`airCharges.${i}.currencyCode`) → name="airCharges.0.currencyCode"
//   form.register(`airCharges.${i}.rate`)         → name="airCharges.0.rate", type="number"
async function fillAirChargesPanel(page: Page): Promise<void> {
  // Air Charges 패널의 Add 버튼 — panel__actions 내 "Add" 텍스트
  const chargesAddBtn = page.locator('.panel__actions button:has-text("Add")').last();

  await chargesAddBtn.click();
  await page.fill('input[name="airCharges.0.freightCode"]',    'F');
  await page.fill('input[name="airCharges.0.currencyCode"]',   'USD');
  await page.fill('input[name="airCharges.0.grossWeightKg"]',  '2500');
  await page.fill('input[name="airCharges.0.rate"]',           '3.5');

  await chargesAddBtn.click();
  await page.fill('input[name="airCharges.1.freightCode"]',    'S');
  await page.fill('input[name="airCharges.1.currencyCode"]',   'USD');
  await page.fill('input[name="airCharges.1.grossWeightKg"]',  '500');
  await page.fill('input[name="airCharges.1.rate"]',           '1.2');
}

// ── 메인 헬퍼 진입점 ──────────────────────────────────────────────────────
export async function fillMasterBlForm(page: Page, { variant, ts }: FillOptions): Promise<void> {
  const isSea = variant.startsWith('sea');
  const isExp = variant.endsWith('exp');

  // ── 툴바 입력 ─────────────────────────────────────────────────────────────
  // master-bl-entry.tsx: 모든 toolbar input은 defaultValue only (form.register 미연결)
  // name attribute 없음 → placeholder 로 접근
  if (isSea) {
    await page.getByPlaceholder('MBL No').fill(`MBL${ts}`);
    await page.getByPlaceholder('Master Ref').fill(`MREF${ts}`);
    await page.getByPlaceholder('Line Bkg. No').fill(`BKG${ts}`);
    await page.getByPlaceholder('Load Type').fill('FCL');
    await page.getByPlaceholder('Service Term').fill('CY/CY');
    await page.getByPlaceholder('B/L Type').fill('OBL');
  } else {
    // AIR: TOOLBAR_AIR = ["Master Ref", "MAWB No", "Shipment Type", "Status"]
    await page.getByPlaceholder('MAWB No').fill(`MBL${ts}`);
    await page.getByPlaceholder('Master Ref').fill(`MREF${ts}`);
  }

  // ── Main 탭 — Party 패널 ───────────────────────────────────────────────
  await fillPartyPanel(page);

  // ── Main 탭 — Schedule 패널 ───────────────────────────────────────────
  if (isSea) {
    await fillSeaSchedulePanel(page);
  } else {
    await fillAirScheduleLegs(page);
  }

  // ── Main 탭 — Cargo & Document 패널 ───────────────────────────────────
  await fillCargoDocPanel(page, isSea);

  // ── Main 탭 — Marks 패널 ──────────────────────────────────────────────
  await fillMarksPanel(page);

  // ── Main 탭 — Goods Description 패널 ─────────────────────────────────
  await fillGoodsDescPanel(page, isSea);

  // ── AIR 전용 추가 패널 ────────────────────────────────────────────────
  if (!isSea) {
    await fillAirDimsPanel(page);
    await fillAirChargesPanel(page);
  }

  void isExp; // direction별 처리 예약 (현재 동일 입력)
}
