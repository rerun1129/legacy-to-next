/**
 * BS-01 Invoice / BS-02 Payment / BS-03 D/C Note 화면별 config.
 * 공용 Shell(financial-document-list-client)이 이 config를 주입받아 렌더한다.
 */

export interface FinancialDocumentListConfig {
  /** BE SearchFinancialDocumentRequest.documentTypes 값 */
  documentTypes: string[];
  /** i18n titleKey (bms.list.title.*) */
  titleKey: string;
  /** 권한 게이트용 메뉴 코드 */
  menuCode: string;
  /** Search ActionButton 코드 */
  searchButtonCode: string;
  /** Reset ActionButton 코드 */
  resetButtonCode: string;
  /** listFilterStore scope key용 라우트 경로 */
  routeKey: string;
}

export const INVOICE_LIST_CONFIG: FinancialDocumentListConfig = {
  documentTypes: ["INVOICE"],
  titleKey: "invoice",
  menuCode: "MENU_BMS_INVOICE",
  searchButtonCode: "BTN_BMS_INVOICE_SEARCH",
  resetButtonCode: "BTN_BMS_INVOICE_RESET",
  routeKey: "/bms/invoice/list",
};

export const PAYMENT_LIST_CONFIG: FinancialDocumentListConfig = {
  documentTypes: ["PAYMENT"],
  titleKey: "payment",
  menuCode: "MENU_BMS_PAYMENT",
  searchButtonCode: "BTN_BMS_PAYMENT_SEARCH",
  resetButtonCode: "BTN_BMS_PAYMENT_RESET",
  routeKey: "/bms/payment/list",
};

export const DC_NOTE_LIST_CONFIG: FinancialDocumentListConfig = {
  // D/C Note = Debit + Credit 합본
  documentTypes: ["DEBIT", "CREDIT"],
  titleKey: "dcNote",
  menuCode: "MENU_BMS_DC_NOTE",
  searchButtonCode: "BTN_BMS_DC_NOTE_SEARCH",
  resetButtonCode: "BTN_BMS_DC_NOTE_RESET",
  routeKey: "/bms/dc-note/list",
};
