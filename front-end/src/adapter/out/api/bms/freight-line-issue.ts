import { z } from "zod";
import type { FreightLineIssuePort } from "@/application/bms/freight-line-issue/ports";
import type {
  FreightLineIssuePage,
  SearchFreightLineInput,
  IssueFreightLineInput,
  IssueFreightLineResult,
  CancelFreightLineInput,
  CancelFreightLineResult,
} from "@/application/bms/freight-line-issue/ports";
import { bmsFetchJson } from "../bms-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/bms/freight-line-issues";

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

// BE: FreightLineIssueRowResponse — 조회 결과 단건
// 금액 필드·B/L 관련 필드·발급번호 필드는 null 가능
const FREIGHT_LINE_ISSUE_ROW_SCHEMA = z.object({
  freightLineId: z.number(),
  freightHeaderId: z.number(),
  blType: z.string().nullable(),
  blId: z.number().nullable(),
  blNo: z.string().nullable(),
  jobDiv: z.string().nullable(),
  bound: z.string().nullable(),
  etd: z.string().nullable(),
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
  documentStatus: z.string().nullable(),
  taxNo: z.string().nullable(),
  taxDt: z.string().nullable(),
  slipNo: z.string().nullable(),
  slipDt: z.string().nullable(),
});

// BE: FreightLineIssuePageResponse — 페이지 응답 래퍼
const FREIGHT_LINE_ISSUE_PAGE_SCHEMA = z.object({
  content: z.array(FREIGHT_LINE_ISSUE_ROW_SCHEMA),
  totalElements: z.number(),
  totalPages: z.number(),
  pageNumber: z.number(),
  pageSize: z.number(),
}) satisfies z.ZodType<FreightLineIssuePage>;

// BE: IssueFreightLineResponse — 발급 응답
// statuses 키는 documentId.toString()인 숫자 string
const ISSUE_FREIGHT_LINE_RESPONSE_SCHEMA = z.object({
  issueNo: z.string(),
  affectedDocumentIds: z.array(z.number()),
  statuses: z.record(z.string(), z.string()),
}) satisfies z.ZodType<IssueFreightLineResult>;

// BE: CancelFreightLineResponse — 발급취소 응답
const CANCEL_FREIGHT_LINE_RESPONSE_SCHEMA = z.object({
  affectedDocumentIds: z.array(z.number()),
  statuses: z.record(z.string(), z.string()),
}) satisfies z.ZodType<CancelFreightLineResult>;

export const API_FREIGHT_LINE_ISSUE_PORT: FreightLineIssuePort = {
  async search(
    filter: SearchFreightLineInput,
    page: number,
    size: number,
  ): Promise<FreightLineIssuePage> {
    const json = await bmsFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify({
        customerCode: filter.customerCode ?? null,
        financialDocType: filter.financialDocType ?? null,
        jobDiv: filter.jobDiv ?? null,
        bound: filter.bound ?? null,
        performanceDtFrom: filter.performanceDtFrom ?? null,
        performanceDtTo: filter.performanceDtTo ?? null,
        issuedStatus: filter.issuedStatus ?? null,
        page,
        size,
      }),
    });
    const parsed = apiResponse(FREIGHT_LINE_ISSUE_PAGE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid freight-line-issue search response: ${parsed.error.message}`,
      );
    }
    return parsed.data.data;
  },

  async issueTax(req: IssueFreightLineInput): Promise<IssueFreightLineResult> {
    const json = await bmsFetchJson(`${BASE}/tax`, {
      method: "POST",
      body: JSON.stringify({
        issueDt: req.issueDt,
        lineIds: req.lineIds,
      }),
    });
    const parsed = apiResponse(ISSUE_FREIGHT_LINE_RESPONSE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid issue-tax response: ${parsed.error.message}`,
      );
    }
    return parsed.data.data;
  },

  async issueSlip(req: IssueFreightLineInput): Promise<IssueFreightLineResult> {
    const json = await bmsFetchJson(`${BASE}/slip`, {
      method: "POST",
      body: JSON.stringify({
        issueDt: req.issueDt,
        lineIds: req.lineIds,
      }),
    });
    const parsed = apiResponse(ISSUE_FREIGHT_LINE_RESPONSE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid issue-slip response: ${parsed.error.message}`,
      );
    }
    return parsed.data.data;
  },

  async cancelTax(req: CancelFreightLineInput): Promise<CancelFreightLineResult> {
    const json = await bmsFetchJson(`${BASE}/tax/cancel`, {
      method: "POST",
      body: JSON.stringify({ lineIds: req.lineIds }),
    });
    const parsed = apiResponse(CANCEL_FREIGHT_LINE_RESPONSE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid cancel-tax response: ${parsed.error.message}`,
      );
    }
    return parsed.data.data;
  },

  async cancelSlip(req: CancelFreightLineInput): Promise<CancelFreightLineResult> {
    const json = await bmsFetchJson(`${BASE}/slip/cancel`, {
      method: "POST",
      body: JSON.stringify({ lineIds: req.lineIds }),
    });
    const parsed = apiResponse(CANCEL_FREIGHT_LINE_RESPONSE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid cancel-slip response: ${parsed.error.message}`,
      );
    }
    return parsed.data.data;
  },
};
