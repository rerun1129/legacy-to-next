import { z } from "zod";
import type { FinancialDocumentPort } from "@/application/bms/financial-document/ports";
import type {
  FinancialDocument,
  IssuableLine,
  IssueDocumentInput,
  IssueDocumentResult,
  AmendDocumentInput,
  AmendDocumentResult,
  SearchFinancialDocumentInput,
  FinancialDocumentPage,
  FreightLineDetail,
} from "@/application/bms/financial-document/ports";
import { bmsFetchJson } from "../bms-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/bms/financial-documents";

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

// BE: FinancialDocumentResponse
// BigDecimal 금액 필드는 집계 계산 결과로 null 가능성 있음
const FINANCIAL_DOCUMENT_SCHEMA = z.object({
  financialDocumentId: z.number(),
  documentNo: z.string(),
  documentType: z.string(),
  documentDt: z.string(),
  status: z.string(),
  customerCode: z.string(),
  customerName: z.string(),
  settleTotalAmount: z.number().nullable(),
  localTotalAmount: z.number().nullable(),
  settleTotalVat: z.number().nullable(),
  localTotalVat: z.number().nullable(),
  usdTotalAmount: z.number().nullable(),
  performanceDt: z.string(),
  teamCode: z.string().nullable(),
  operator: z.string().nullable(),
  groupFinancialNo: z.string().nullable(),
}) satisfies z.ZodType<FinancialDocument>;

// BE: IssuableLineResponse
// financialDocumentId·documentNo: 미발행 상태일 때 null (BE DTO 주석 명시)
const ISSUABLE_LINE_SCHEMA = z.object({
  freightLineId: z.number(),
  freightType: z.string(),
  financialDocType: z.string(),
  freightCode: z.string(),
  customerCode: z.string(),
  customerName: z.string(),
  currency: z.string(),
  settleAmount: z.number().nullable(),
  localAmount: z.number().nullable(),
  settleTaxAmount: z.number().nullable(),
  localTaxAmount: z.number().nullable(),
  usdAmount: z.number().nullable(),
  performanceDt: z.string(),
  financialDocumentId: z.number().nullable(),
  documentNo: z.string().nullable(),
}) satisfies z.ZodType<IssuableLine>;

// BE: IssueDocumentResponse
const ISSUE_DOCUMENT_RESPONSE_SCHEMA = z.object({
  financialDocumentId: z.number(),
  documentNo: z.string(),
}) satisfies z.ZodType<IssueDocumentResult>;

// BE: AmendDocumentResponse
const AMEND_DOCUMENT_RESPONSE_SCHEMA = z.object({
  financialDocumentId: z.number().nullable(),
  documentNo: z.string(),
  deleted: z.boolean(),
}) satisfies z.ZodType<AmendDocumentResult>;

// BE: FinancialDocumentSearchResponse — 전역 검색 결과 단건
// BigDecimal 금액 필드는 null 가능, 파생 B/L 필드도 null 가능
const FINANCIAL_DOCUMENT_SEARCH_ROW_SCHEMA = z.object({
  financialDocumentId: z.number(),
  documentNo: z.string(),
  documentType: z.string(),
  documentDt: z.string(),
  documentStatus: z.string(),
  customerCode: z.string(),
  customerName: z.string(),
  settleTotalAmount: z.number().nullable(),
  localTotalAmount: z.number().nullable(),
  settleTotalVat: z.number().nullable(),
  localTotalVat: z.number().nullable(),
  usdTotalAmount: z.number().nullable(),
  performanceDt: z.string(),
  teamCode: z.string().nullable(),
  teamName: z.string().nullable(),
  operator: z.string().nullable(),
  operatorName: z.string().nullable(),
  groupFinancialNo: z.string().nullable(),
  blType: z.string().nullable(),
  blId: z.number().nullable(),
  jobDiv: z.string().nullable(),
  bound: z.string().nullable(),
  blNo: z.string().nullable(),
  etd: z.string().nullable(),
  eta: z.string().nullable(),
}) satisfies z.ZodType<FinancialDocumentPage["content"][number]>;

// BE: FinancialDocumentPageResponse — 페이지 응답 래퍼
const FINANCIAL_DOCUMENT_PAGE_SCHEMA = z.object({
  content: z.array(FINANCIAL_DOCUMENT_SEARCH_ROW_SCHEMA),
  totalElements: z.number(),
  totalPages: z.number(),
  page: z.number(),
  size: z.number(),
}) satisfies z.ZodType<FinancialDocumentPage>;

// BE: FreightLineDetailResponse — 운임 라인 디테일
const FREIGHT_LINE_DETAIL_SCHEMA = z.object({
  freightLineId: z.number(),
  freightHeaderId: z.number(),
  freightType: z.string(),
  financialDocType: z.string(),
  freightCode: z.string(),
  freightName: z.string(),
  unitQuantity: z.number().nullable(),
  unitPrice: z.number().nullable(),
  per: z.string().nullable(),
  currency: z.string(),
  exchangeRate: z.number().nullable(),
  settleAmount: z.number().nullable(),
  localAmount: z.number().nullable(),
  settleTaxAmount: z.number().nullable(),
  localTaxAmount: z.number().nullable(),
  usdExchangeRate: z.number().nullable(),
  usdAmount: z.number().nullable(),
  customerCode: z.string(),
  customerName: z.string(),
  taxType: z.string().nullable(),
  taxNo: z.string().nullable(),
  taxDt: z.string().nullable(),
  slipNo: z.string().nullable(),
  slipDt: z.string().nullable(),
  performanceDt: z.string(),
  financialDocumentId: z.number(),
}) satisfies z.ZodType<FreightLineDetail>;

export const API_FINANCIAL_DOCUMENT_PORT: FinancialDocumentPort = {
  async amendDocument(req: AmendDocumentInput): Promise<AmendDocumentResult> {
    const json = await bmsFetchJson(`${BASE}/${req.documentId}`, {
      method: "PATCH",
      body: JSON.stringify({
        blType: req.blType,
        blId: req.blId,
        freightType: req.freightType,
        finalLineIds: req.finalLineIds,
        documentDt: req.documentDt ?? null,
        performanceDt: req.performanceDt ?? null,
        teamCode: req.teamCode ?? null,
        operator: req.operator ?? null,
      }),
    });
    const parsed = apiResponse(AMEND_DOCUMENT_RESPONSE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid amend-document response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async issueDocument(req: IssueDocumentInput): Promise<IssueDocumentResult> {
    const json = await bmsFetchJson(BASE + "/issue", {
      method: "POST",
      body: JSON.stringify({
        blType: req.blType,
        blId: req.blId,
        freightType: req.freightType,
        lineIds: req.lineIds,
        documentDt: req.documentDt,
        performanceDt: req.performanceDt,
        teamCode: req.teamCode,
        operator: req.operator,
      }),
    });
    const parsed = apiResponse(ISSUE_DOCUMENT_RESPONSE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid issue-document response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async deleteDocument(id: number): Promise<void> {
    await bmsFetchJson(`${BASE}/${id}`, { method: "DELETE" });
  },

  async listByBl(blType: string, blId: number): Promise<FinancialDocument[]> {
    const params = new URLSearchParams({ blType, blId: String(blId) });
    const json = await bmsFetchJson(`${BASE}?${params}`);
    const parsed = apiResponse(z.array(FINANCIAL_DOCUMENT_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid financial-document list response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async findIssuableLines(blType: string, blId: number, freightType: string): Promise<IssuableLine[]> {
    const params = new URLSearchParams({ blType, blId: String(blId), freightType });
    const json = await bmsFetchJson(`${BASE}/issuable-lines?${params}`);
    const parsed = apiResponse(z.array(ISSUABLE_LINE_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid issuable-lines response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async search(filter: SearchFinancialDocumentInput, page: number, size: number): Promise<FinancialDocumentPage> {
    const json = await bmsFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({
        documentTypes: filter.documentTypes,
        documentStatus: filter.documentStatus ?? null,
        customerCode: filter.customerCode ?? null,
        documentNoLike: filter.documentNoLike ?? null,
        teamCode: filter.teamCode ?? null,
        operator: filter.operator ?? null,
        documentDtFrom: filter.documentDtFrom ?? null,
        documentDtTo: filter.documentDtTo ?? null,
        performanceDtFrom: filter.performanceDtFrom ?? null,
        performanceDtTo: filter.performanceDtTo ?? null,
        etdFrom: filter.etdFrom ?? null,
        etdTo: filter.etdTo ?? null,
        etaFrom: filter.etaFrom ?? null,
        etaTo: filter.etaTo ?? null,
        jobDiv: filter.jobDiv ?? null,
        bound: filter.bound ?? null,
        page,
        size,
      }),
    });
    const parsed = apiResponse(FINANCIAL_DOCUMENT_PAGE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid financial-document search response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async findLines(documentId: number): Promise<FreightLineDetail[]> {
    const json = await bmsFetchJson(`${BASE}/${documentId}/lines`);
    const parsed = apiResponse(z.array(FREIGHT_LINE_DETAIL_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid freight-line-detail response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },
};
