/**
 * 세금계산서 발급 / 전표 발급 화면별 config.
 * 공용 Shell(freight-line-issue-list-client)이 이 config를 주입받아 렌더한다.
 * V65 menu_code: BMS_TAX_INVOICE, BMS_SLIP (prefix 없이 시드 → FE에서 MENU_/BTN_ 부착)
 */

/** 발급 종류 — BE IssueType enum과 1:1 */
export type IssueType = "TAX" | "SLIP";

export interface FreightLineIssueListConfig {
  /** 발급 종류 (URL 경로에 따라 결정) */
  issueType: IssueType;
  /** Search ActionButton 코드 (BTN_ prefix 포함) */
  searchButtonCode: string;
  /** Reset ActionButton 코드 (BTN_ prefix 포함) */
  resetButtonCode: string;
  /** Issue ActionButton 코드 (BTN_ prefix 포함) */
  issueButtonCode: string;
  /** listFilterStore scope key용 라우트 경로 */
  routeKey: string;
}

/** BS-E1 세금계산서 발급 화면 config */
export const TAX_INVOICE_ISSUE_CONFIG: FreightLineIssueListConfig = {
  issueType: "TAX",
  searchButtonCode: "BTN_BMS_TAX_INVOICE_SEARCH",
  resetButtonCode: "BTN_BMS_TAX_INVOICE_RESET",
  issueButtonCode: "BTN_BMS_TAX_INVOICE_ISSUE",
  routeKey: "/bms/tax-invoice/issue",
};

/** BS-E2 전표 발급 화면 config */
export const SLIP_ISSUE_CONFIG: FreightLineIssueListConfig = {
  issueType: "SLIP",
  searchButtonCode: "BTN_BMS_SLIP_SEARCH",
  resetButtonCode: "BTN_BMS_SLIP_RESET",
  issueButtonCode: "BTN_BMS_SLIP_ISSUE",
  routeKey: "/bms/slip/issue",
};
