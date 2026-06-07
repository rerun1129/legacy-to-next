/**
 * PMS 실적 집계 모델 — dim 카탈로그 + measure 상수.
 * 순수 데이터, React·RQ 무의존.
 */

import type { AggregateDimension, AggregateMeasure } from "@/lib/grid-aggregate";
import { fmtEnum } from "@/lib/grid-formatters";
import type { PmsPerformanceRow } from "@/application/pms/performance/ports";

// ── 타입 보조 ─────────────────────────────────────────────────

interface EnumCtx {
  jobDivOptions: ReadonlyArray<{ value: string; label: string }>;
  boundOptions:  ReadonlyArray<{ value: string; label: string }>;
}

// ── dim 헬퍼 ─────────────────────────────────────────────────

/** null/빈값은 버킷키 "", display "(미지정)" */
function codeDim<T>(
  key: string,
  label: string,
  codeField: keyof T,
): AggregateDimension<T> {
  return {
    key,
    label,
    bucketKey: (r) => (r[codeField] as string | null) ?? "",
    display:   (r) => (r[codeField] as string | null) || "(미지정)",
  };
}

/** 코드+이름 쌍: 버킷=코드, 표시=이름 */
function codeNameDim<T>(
  key: string,
  label: string,
  codeField: keyof T,
  nameField: keyof T,
): AggregateDimension<T> {
  return {
    key,
    label,
    bucketKey: (r) => (r[codeField] as string | null) ?? "",
    display:   (r) => (r[nameField] as string | null) || (r[codeField] as string | null) || "(미지정)",
  };
}

// ── 공개 API ─────────────────────────────────────────────────

/**
 * 모든 가능 dim의 카탈로그.
 * 순서가 UI 드롭다운에 표시되는 순서.
 * enum dim은 enumCtx에서 options를 받아야 label이 정확히 표시됨.
 */
export function buildPmsDimensionCatalog(
  enumCtx: EnumCtx,
): AggregateDimension<PmsPerformanceRow>[] {
  return [
    // 코드+이름
    codeNameDim<PmsPerformanceRow>("team",           "Team",           "teamCode",            "teamName"),
    codeNameDim<PmsPerformanceRow>("actualCustomer", "Actual Customer","actualCustomerCode",  "actualCustomerName"),
    codeNameDim<PmsPerformanceRow>("settlePartner",  "Settle Partner", "settlePartnerCode",   "settlePartnerName"),
    codeNameDim<PmsPerformanceRow>("carrier",        "Carrier",        "linerCode",           "linerName"),
    codeNameDim<PmsPerformanceRow>("salesMan",       "Sales Man",      "salesManCode",        "salesManName"),

    // enum
    {
      key:       "jobDiv",
      label:     "Job Div",
      bucketKey: (r) => r.jobDiv ?? "",
      display:   (r) =>
        r.jobDiv
          ? fmtEnum(r.jobDiv, enumCtx.jobDivOptions)
          : "(미지정)",
    },
    {
      key:       "bound",
      label:     "Bound",
      bucketKey: (r) => r.bound ?? "",
      display:   (r) =>
        r.bound
          ? fmtEnum(r.bound, enumCtx.boundOptions)
          : "(미지정)",
    },

    // 코드-only
    codeDim<PmsPerformanceRow>("polCode",   "POL",       "polCode"),
    codeDim<PmsPerformanceRow>("podCode",   "POD",       "podCode"),
    codeDim<PmsPerformanceRow>("incoterms", "Incoterms", "incoterms"),
    codeDim<PmsPerformanceRow>("loadType",  "Load Type", "loadType"),
  ];
}

/** 집계 대상 measure 목록. 건수(count)는 AggregateRow.count 전용으로 별도 처리. */
export const PMS_MEASURES: AggregateMeasure<PmsPerformanceRow>[] = [
  // 금액 10 (decimals 2)
  { key: "invoiceLocalAmt",  field: "invoiceLocalAmt",  decimals: 2 },
  { key: "debitLocalAmt",    field: "debitLocalAmt",    decimals: 2 },
  { key: "paymentLocalAmt",  field: "paymentLocalAmt",  decimals: 2 },
  { key: "creditLocalAmt",   field: "creditLocalAmt",   decimals: 2 },
  { key: "localProfit",      field: "localProfit",      decimals: 2 },
  { key: "invoiceUsdAmt",    field: "invoiceUsdAmt",    decimals: 2 },
  { key: "debitUsdAmt",      field: "debitUsdAmt",      decimals: 2 },
  { key: "paymentUsdAmt",    field: "paymentUsdAmt",    decimals: 2 },
  { key: "creditUsdAmt",     field: "creditUsdAmt",     decimals: 2 },
  { key: "usdProfit",        field: "usdProfit",        decimals: 2 },
  // 수량·중량
  { key: "pkgQty",           field: "pkgQty",           decimals: 0 },
  { key: "rton",             field: "rton",             decimals: 3 },
  { key: "cbm",              field: "cbm",              decimals: 3 },
  { key: "chargeWeightKg",   field: "chargeWeightKg",   decimals: 3 },
  { key: "grossWeightKg",    field: "grossWeightKg",    decimals: 3 },
];
