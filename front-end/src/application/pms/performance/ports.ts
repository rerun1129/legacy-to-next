/**
 * PMS 실적 조회(PS-01) 도메인 타입 및 포트 인터페이스.
 * BE DTO: PmsPerformanceRowResponse, PmsPerformancePageResponse, SearchPmsPerformanceRequest
 */

// === 집계 기준 ===

/** AggregationBasis enum — BE: FREIGHT_INPUT / DOCUMENT_CREATED / TAX_ISSUED / SLIP_ISSUED */
export type AggregationBasis =
  | "FREIGHT_INPUT"
  | "DOCUMENT_CREATED"
  | "TAX_ISSUED"
  | "SLIP_ISSUED";

// === 도메인 모델 ===

/** BE: PmsPerformanceRowResponse — 36컬럼 실적 그리드 한 행 */
export interface PmsPerformanceRow {
  // 식별
  blType: string;
  blId: number;

  // col 1-2
  houseBlNo: string;
  masterBlNo: string;

  // col 3 Team
  teamCode: string;
  teamName: string;

  // col 4-8 B/L 속성
  jobDiv: string;
  bound: string;
  etd: string;
  eta: string;
  performanceDt: string;

  // col 9-10 Actual Customer
  actualCustomerCode: string;
  actualCustomerName: string;

  // col 11-12 Settle Partner
  settlePartnerCode: string;
  settlePartnerName: string;

  // col 13-14 Carrier
  linerCode: string;
  linerName: string;

  // col 15-16 항만
  polCode: string;
  podCode: string;

  // col 17 Sales Man
  salesManCode: string;
  salesManName: string;

  // col 18
  incoterms: string;

  // col 19-24 화물 수치
  loadType: string;
  pkgQty: number | null;
  rton: number | null;
  cbm: number | null;
  chargeWeightKg: number | null;
  grossWeightKg: number | null;

  // col 25-29 Local 금액
  invoiceLocalAmt: number | null;
  debitLocalAmt: number | null;
  paymentLocalAmt: number | null;
  creditLocalAmt: number | null;
  localProfit: number | null;

  // col 30-34 USD 금액
  invoiceUsdAmt: number | null;
  debitUsdAmt: number | null;
  paymentUsdAmt: number | null;
  creditUsdAmt: number | null;
  usdProfit: number | null;

  // col 35-36 마감 (공란)
  blClosed: string;
  freightClosed: string;
}

/** BE: PmsPerformancePageResponse */
export interface PmsPerformancePage {
  content: PmsPerformanceRow[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/** PS-01 조회 요청 입력 */
export interface SearchPmsPerformanceInput {
  basis: AggregationBasis;
  page: number;
  size: number;

  // B/L 공통
  jobDiv?: string | null;
  bound?: string | null;
  dateKind?: string | null;
  dateFrom?: string | null;
  dateTo?: string | null;
  /** dateKind=PERF 선택 시 dateFrom/dateTo에서 파생하여 전송 */
  performanceDtFrom?: string | null;
  performanceDtTo?: string | null;
  /** dateKind=DOC 선택 시 dateFrom/dateTo에서 파생하여 전송 */
  documentDtFrom?: string | null;
  documentDtTo?: string | null;
  hblNo?: string | null;
  mblNo?: string | null;

  // 거래처
  actualCustomerCode?: string | null;
  settlePartnerCode?: string | null;

  // 운송사
  carrierCode?: string | null;

  // 항만
  portKind?: string | null;
  portCode?: string | null;

  // 영업
  salesManCode?: string | null;
  salesClass?: string | null;
  incoterms?: string | null;
  loadType?: string | null;
  teamCode?: string | null;
  operator?: string | null;

  // BMS 서류
  documentTypes?: string[] | null;
  documentStatus?: string | null;
  documentNoLike?: string | null;
  groupFinancialNo?: string | null;
  grouped?: string | null;
  issued?: string | null;

  // BMS 운임행
  financialDocType?: string | null;
  taxType?: string | null;
}

// === 포트 인터페이스 ===

export interface PmsPerformancePort {
  /** POST /api/pms/performance/search */
  search(input: SearchPmsPerformanceInput): Promise<PmsPerformancePage>;
}
