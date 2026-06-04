import { z } from "zod";
import type { FinancialDocumentPort } from "@/application/bms/financial-document/ports";
import type { FinancialDocument, IssuableLine, IssueDocumentInput, IssueDocumentResult } from "@/application/bms/financial-document/ports";
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

export const API_FINANCIAL_DOCUMENT_PORT: FinancialDocumentPort = {
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

  async listByBl(blType: string, blId: string): Promise<FinancialDocument[]> {
    const params = new URLSearchParams({ blType, blId });
    const json = await bmsFetchJson(`${BASE}?${params}`);
    const parsed = apiResponse(z.array(FINANCIAL_DOCUMENT_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid financial-document list response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },

  async findIssuableLines(blType: string, blId: string, freightType: string): Promise<IssuableLine[]> {
    const params = new URLSearchParams({ blType, blId, freightType });
    const json = await bmsFetchJson(`${BASE}/issuable-lines?${params}`);
    const parsed = apiResponse(z.array(ISSUABLE_LINE_SCHEMA)).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(`Invalid issuable-lines response: ${parsed.error.message}`);
    }
    return parsed.data.data;
  },
};
