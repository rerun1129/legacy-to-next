import { z } from "zod";
import type { PmsPerformancePort } from "@/application/pms/performance/ports";
import type { PmsPerformancePage, SearchPmsPerformanceInput } from "@/application/pms/performance/ports";
import { pmsFetchJson } from "../pms-fetch";
import { ResponseParseError } from "../errors";

const BASE = "/api/pms/performance";

const apiResponse = <T extends z.ZodTypeAny>(schema: T) =>
  z.object({ data: schema, message: z.string().optional() });

// BE: PmsPerformanceRowResponse — 36컬럼.
// BigDecimal → JSON number or string → z.coerce.number().nullable() で수신 안전.
// null 가능 여부는 BE DTO 주석 기준 (수치·금액은 null 가능, 식별·코드는 string으로 non-null).
const PMS_PERFORMANCE_ROW_SCHEMA = z.object({
  // 식별
  blType: z.string(),
  blId: z.number(),

  // col 1-2
  houseBlNo: z.string(),
  masterBlNo: z.string(),

  // col 3 Team
  teamCode: z.string(),
  teamName: z.string(),

  // col 4-8 B/L 속성
  jobDiv: z.string(),
  bound: z.string(),
  etd: z.string(),
  eta: z.string(),
  performanceDt: z.string(),

  // col 9-10 Actual Customer
  actualCustomerCode: z.string(),
  actualCustomerName: z.string(),

  // col 11-12 Settle Partner
  settlePartnerCode: z.string(),
  settlePartnerName: z.string(),

  // col 13-14 Carrier
  linerCode: z.string(),
  linerName: z.string(),

  // col 15-16 항만
  polCode: z.string(),
  podCode: z.string(),

  // col 17 Sales Man
  salesManCode: z.string(),
  salesManName: z.string(),

  // col 18
  incoterms: z.string(),

  // col 19-24 화물 수치 (null 가능 — 모드별 미지원 필드)
  loadType: z.string(),
  pkgQty: z.number().nullable(),
  rton: z.coerce.number().nullable(),
  cbm: z.coerce.number().nullable(),
  chargeWeightKg: z.coerce.number().nullable(),
  grossWeightKg: z.coerce.number().nullable(),

  // col 25-29 Local 금액 (BigDecimal → null 가능)
  invoiceLocalAmt: z.coerce.number().nullable(),
  debitLocalAmt: z.coerce.number().nullable(),
  paymentLocalAmt: z.coerce.number().nullable(),
  creditLocalAmt: z.coerce.number().nullable(),
  localProfit: z.coerce.number().nullable(),

  // col 30-34 USD 금액 (BigDecimal → null 가능)
  invoiceUsdAmt: z.coerce.number().nullable(),
  debitUsdAmt: z.coerce.number().nullable(),
  paymentUsdAmt: z.coerce.number().nullable(),
  creditUsdAmt: z.coerce.number().nullable(),
  usdProfit: z.coerce.number().nullable(),

  // col 35-36 마감 (공란 — BE는 null/빈문자열 가능성 있음)
  blClosed: z.string(),
  freightClosed: z.string(),
}) satisfies z.ZodType<import("@/application/pms/performance/ports").PmsPerformanceRow>;

// BE: PmsPerformancePageResponse
const PMS_PERFORMANCE_PAGE_SCHEMA = z.object({
  content: z.array(PMS_PERFORMANCE_ROW_SCHEMA),
  totalElements: z.number(),
  totalPages: z.number(),
  page: z.number(),
  size: z.number(),
}) satisfies z.ZodType<PmsPerformancePage>;

export const API_PMS_PERFORMANCE_PORT: PmsPerformancePort = {
  async search(input: SearchPmsPerformanceInput): Promise<PmsPerformancePage> {
    // 단일 dateKind 콤보 → 백엔드 날짜 필드 분기 매핑
    // ETD/ETA: dateKind + dateFrom/dateTo 전송
    // PERF: performanceDtFrom/To 전송 (dateKind null)
    // DOC: documentDtFrom/To 전송 (dateKind null)
    const isEtdEta        = input.dateKind === "ETD" || input.dateKind === "ETA";
    const dateKindForBe   = isEtdEta ? (input.dateKind ?? null) : null;
    const dateFromForBe   = isEtdEta ? (input.dateFrom ?? null) : null;
    const dateToForBe     = isEtdEta ? (input.dateTo   ?? null) : null;
    const performanceDtFrom = input.dateKind === "PERF" ? (input.dateFrom ?? null) : null;
    const performanceDtTo   = input.dateKind === "PERF" ? (input.dateTo   ?? null) : null;
    const documentDtFrom    = input.dateKind === "DOC"  ? (input.dateFrom ?? null) : null;
    const documentDtTo      = input.dateKind === "DOC"  ? (input.dateTo   ?? null) : null;

    const body: Record<string, unknown> = {
      basis: input.basis,
      page: input.page,
      size: input.size,
      jobDiv: input.jobDiv ?? null,
      bound: input.bound ?? null,
      dateKind: dateKindForBe,
      dateFrom: dateFromForBe,
      dateTo: dateToForBe,
      performanceDtFrom,
      performanceDtTo,
      documentDtFrom,
      documentDtTo,
      hblNo: input.hblNo ?? null,
      mblNo: input.mblNo ?? null,
      actualCustomerCode: input.actualCustomerCode ?? null,
      settlePartnerCode: input.settlePartnerCode ?? null,
      carrierCode: input.carrierCode ?? null,
      portKind: input.portKind ?? null,
      portCode: input.portCode ?? null,
      salesManCode: input.salesManCode ?? null,
      salesClass: input.salesClass ?? null,
      incoterms: input.incoterms ?? null,
      loadType: input.loadType ?? null,
      teamCode: input.teamCode ?? null,
      operator: input.operator ?? null,
      documentTypes: input.documentTypes ?? null,
      documentStatus: input.documentStatus ?? null,
      documentNoLike: input.documentNoLike ?? null,
      groupFinancialNo: input.groupFinancialNo ?? null,
      grouped: input.grouped ?? null,
      issued: input.issued ?? null,
      financialDocType: input.financialDocType ?? null,
      taxType: input.taxType ?? null,
    };

    const json = await pmsFetchJson(`${BASE}/search`, {
      method: "POST",
      body: JSON.stringify(body),
    });

    const parsed = apiResponse(PMS_PERFORMANCE_PAGE_SCHEMA).safeParse(json);
    if (!parsed.success) {
      throw new ResponseParseError(
        `Invalid pms-performance search response: ${parsed.error.message}`
      );
    }
    return parsed.data.data;
  },
};
