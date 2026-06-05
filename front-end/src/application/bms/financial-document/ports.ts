/**
 * BMS 금융 서류(Financial Document) 도메인 타입 및 포트 인터페이스.
 * BE DTO: FinancialDocumentResponse, IssuableLineResponse, IssueDocumentRequest, IssueDocumentResponse
 */

// === 도메인 모델 ===

/** BE: FinancialDocumentResponse — Account Documents 패널 목록 항목 */
export interface FinancialDocument {
  financialDocumentId: number;
  documentNo: string;
  documentType: string;
  documentDt: string;
  status: string;
  customerCode: string;
  customerName: string;
  /** BigDecimal → 집계 결과, 라인 없으면 null */
  settleTotalAmount: number | null;
  localTotalAmount: number | null;
  settleTotalVat: number | null;
  localTotalVat: number | null;
  usdTotalAmount: number | null;
  performanceDt: string;
  teamCode: string | null;
  operator: string | null;
  groupFinancialNo: string | null;
}

/** BE: IssuableLineResponse — 발행 가능 운임 라인 (미발행이면 financialDocumentId·documentNo null) */
export interface IssuableLine {
  freightLineId: number;
  freightType: string;
  financialDocType: string;
  freightCode: string;
  customerCode: string;
  customerName: string;
  currency: string;
  /** BigDecimal → null 가능 */
  settleAmount: number | null;
  localAmount: number | null;
  settleTaxAmount: number | null;
  localTaxAmount: number | null;
  usdAmount: number | null;
  performanceDt: string;
  /** 이미 발행된 라인이면 식별자와 번호가 채워짐 */
  financialDocumentId: number | null;
  documentNo: string | null;
}

/** BE: IssueDocumentRequest — 서류 발행 요청 입력 */
export interface IssueDocumentInput {
  blType: string;
  blId: string;
  freightType: string;
  /** BE: List<Long> lineIds */
  lineIds: number[];
  documentDt: string;
  performanceDt: string;
  teamCode: string | null;
  operator: string | null;
}

/** BE: IssueDocumentResponse — 서류 발행 응답 */
export interface IssueDocumentResult {
  financialDocumentId: number;
  documentNo: string;
}

/** BE: AmendDocumentRequest — 서류 편집(amend) 요청 입력 */
export interface AmendDocumentInput {
  documentId: number;
  blType: string;
  blId: string;
  freightType: string;
  /** 최종 라인 상태(선언적) — 서버가 현재 상태와 diff하여 link/unlink 처리 */
  finalLineIds: number[];
  /** 헤더 편집 4필드 — status=CREATED일 때만 BE가 UPDATE, 비CREATED는 무시 */
  documentDt?: string | null;
  performanceDt?: string | null;
  teamCode?: string | null;
  operator?: string | null;
}

/** BE: AmendDocumentResponse — 서류 편집 응답. deleted=true이면 모든 라인 제거로 서류 자동 삭제됨 */
export interface AmendDocumentResult {
  financialDocumentId: number | null;
  documentNo: string;
  deleted: boolean;
}

// === 포트 인터페이스 ===

export interface FinancialDocumentPort {
  /** POST /api/bms/financial-documents/issue */
  issueDocument(req: IssueDocumentInput): Promise<IssueDocumentResult>;
  /** PATCH /api/bms/financial-documents/{id} */
  amendDocument(req: AmendDocumentInput): Promise<AmendDocumentResult>;
  /** DELETE /api/bms/financial-documents/{id} */
  deleteDocument(id: number): Promise<void>;
  /** GET /api/bms/financial-documents?blType=&blId= */
  listByBl(blType: string, blId: string): Promise<FinancialDocument[]>;
  /** GET /api/bms/financial-documents/issuable-lines?blType=&blId=&freightType= */
  findIssuableLines(blType: string, blId: string, freightType: string): Promise<IssuableLine[]>;
}
