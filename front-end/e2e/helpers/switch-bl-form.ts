import type { Page } from '@playwright/test';

// switch-bl-modal.tsx / switch-bl-party-panel.tsx selector 분석 결과:
//
// [Toolbar — register 연결 필드]
//   register("switchBlNo")  → input placeholder="Switch B/L No"
//   input readOnly           → House B/L No (수정 불가)
//
// [SwitchBlPartyPanel]
//   register("shipperCode")   → input placeholder="Shipper Code"
//   register("consigneeCode") → input placeholder="Consignee Code"
//   register("notifyCode")    → input placeholder="Notify Code"
//
// [SwitchBlDescPanel]
//   register("marks")           → textarea placeholder="Marks and Numbers"
//   register("natureQuantity")  → textarea placeholder="Nature & Quantity of Goods"
//
// [Save 버튼]
//   .btn.btn--sm.btn--primary — 텍스트 "Save" (Saving... 로 바뀌는 pending 상태 있음)

export async function fillSwitchBlForm(page: Page, ts: number): Promise<void> {
  // 모달 body 내 form이 렌더될 때까지 대기 (isLoading 해제 후 form 표시)
  await page.waitForSelector('input[placeholder="Switch B/L No"]', { timeout: 8_000 });

  // Toolbar 필드
  await page.fill('input[placeholder="Switch B/L No"]', `SBL${ts}`);

  // Party 코드
  await page.fill('input[placeholder="Shipper Code"]', 'SHIPPER01');
  await page.fill('input[placeholder="Consignee Code"]', 'CONSIG01');
  await page.fill('input[placeholder="Notify Code"]', 'NOTIFY01');

  // Marks & Nature textarea
  await page.fill('textarea[placeholder="Marks and Numbers"]', `MARKS-${ts}`);
  await page.fill('textarea[placeholder="Nature & Quantity of Goods"]', `CARGO-${ts}`);
}
