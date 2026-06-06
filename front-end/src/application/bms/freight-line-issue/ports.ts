/**
 * BMS 운임 행 발급(세금계산서·전표) 도메인 타입 및 포트 인터페이스.
 * BE DTO: SearchFreightLineRequest, FreightLineIssueRowResponse,
 *          IssueFreightLineRequest, IssueFreightLineResponse
 */

// === 도메인 모델 ===

/** BE: FreightLineIssueRowResponse — 조회 결과 단건 */
export interface FreightLineIssueRow {
  freightLineId: number;
  freightHeaderId: number;
  blType: string | null;
  blId: number | null;
  blNo: string | null;
  jobDiv: string | null;
  bound: string | null;
  etd: string | null;
  freightType: string;
  financialDocType: string;
  freightCode: string;
  customerCode: string;
  customerName: string;
  currency: string;
  settleAmount: number | null;
  localAmount: number | null;
  settleTaxAmount: number | null;
  localTaxAmount: number | null;
  usdAmount: number | null;
  performanceDt: string;
  financialDocumentId: number | null;
  documentNo: string | null;
  documentStatus: string | null;
  /** 세금계산서 번호 — 미발급 시 null */
  taxNo: string | null;
  /** 세금계산서 발급일 — 미발급 시 null */
  taxDt: string | null;
  /** 전표 번호 — 미발급 시 null */
  slipNo: string | null;
  /** 전표 발급일 — 미발급 시 null */
  slipDt: string | null;
}

/** BE: FreightLineIssuePageResponse — 페이지 응답 래퍼 */
export interface FreightLineIssuePage {
  content: FreightLineIssueRow[];
  totalElements: number;
  totalPages: number;
  pageNumber: number;
  pageSize: number;
}

/** BE: SearchFreightLineRequest — 조회 요청 입력 */
export interface SearchFreightLineInput {
  customerCode?: string | null;
  financialDocType?: string | null;
  jobDiv?: string | null;
  bound?: string | null;
  performanceDtFrom?: string | null;
  performanceDtTo?: string | null;
  issuedStatus?: string | null;
  page?: number;
  size?: number;
}

/** BE: IssueFreightLineRequest — 발급 요청 입력 */
export interface IssueFreightLineInput {
  /** 발급일 (yyyyMMdd) */
  issueDt: string;
  /** 발급 대상 운임 행 ID 목록 */
  lineIds: number[];
}

/** BE: IssueFreightLineResponse — 발급 응답 */
export interface IssueFreightLineResult {
  /** 채번된 발급 번호 (T/S + YYMM + seq5) */
  issueNo: string;
  /** 상태가 승급된 서류 ID 목록 */
  affectedDocumentIds: number[];
  /** 서류별 새 상태 (key: documentId.toString()) */
  statuses: Record<string, string>;
}

/** BE: CancelFreightLineRequest — 발급취소 요청 입력 */
export interface CancelFreightLineInput {
  lineIds: number[];
}

/** BE: CancelFreightLineResponse — 발급취소 응답 */
export interface CancelFreightLineResult {
  affectedDocumentIds: number[];
  statuses: Record<string, string>;
}

// === 포트 인터페이스 ===

export interface FreightLineIssuePort {
  /** POST /api/bms/freight-line-issues/search */
  search(filter: SearchFreightLineInput, page: number, size: number): Promise<FreightLineIssuePage>;
  /** POST /api/bms/freight-line-issues/tax */
  issueTax(req: IssueFreightLineInput): Promise<IssueFreightLineResult>;
  /** POST /api/bms/freight-line-issues/slip */
  issueSlip(req: IssueFreightLineInput): Promise<IssueFreightLineResult>;
  /** POST /api/bms/freight-line-issues/tax/cancel */
  cancelTax(req: CancelFreightLineInput): Promise<CancelFreightLineResult>;
  /** POST /api/bms/freight-line-issues/slip/cancel */
  cancelSlip(req: CancelFreightLineInput): Promise<CancelFreightLineResult>;
}
