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
  // 식별 — 항상 존재
  blType: string;
  blId: number;

  // col 1-2
  houseBlNo: string | null;
  masterBlNo: string | null;

  // col 3 Team
  teamCode: string | null;
  teamName: string | null;

  // col 4-8 B/L 속성
  jobDiv: string | null;
  bound: string | null;
  etd: string | null;
  eta: string | null;
  performanceDt: string | null;

  // col 9-10 Actual Customer
  actualCustomerCode: string | null;
  actualCustomerName: string | null;

  // col 11-12 Settle Partner
  settlePartnerCode: string | null;
  settlePartnerName: string | null;

  // col 13-14 Carrier
  linerCode: string | null;
  linerName: string | null;

  // col 15-16 항만
  polCode: string | null;
  podCode: string | null;

  // col 17 Sales Man
  salesManCode: string | null;
  salesManName: string | null;

  // col 18
  incoterms: string | null;

  // col 19-24 화물 수치
  loadType: string | null;
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

  // col 35-36 마감 (플레이스홀더 — BE는 항상 null)
  blClosed: string | null;
  freightClosed: string | null;
}

/** BE: PmsPerformancePageResponse */
export interface PmsPerformancePage {
  content: PmsPerformanceRow[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/** PS-01 조회 요청 입력 — BE SearchPmsPerformanceRequest 18필드와 1:1 대응 */
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

  // BMS 서류
  documentTypes?: string[] | null;
  documentStatus?: string | null;
  grouped?: string | null;
  issued?: string | null;

  // 총건수 정확도 옵션 — true: 정확치(느릴 수 있음), false/미전송: 근사치(기본)
  exactCount?: boolean | null;

  // Search 클릭 시점 타임스탬프 — FE queryKey 변경(캐시 미스) + BE filterSignature 변경(BE 캐시 미스) 동시 적용.
  // 페이지 이동 시엔 nonce 불변 → count 캐시 재사용(비용 절감).
  searchNonce?: number | null;
}

// === 포트 인터페이스 ===

export interface PmsPerformancePort {
  /** POST /api/pms/performance/search */
  search(input: SearchPmsPerformanceInput, opts?: { signal?: AbortSignal }): Promise<PmsPerformancePage>;
}
