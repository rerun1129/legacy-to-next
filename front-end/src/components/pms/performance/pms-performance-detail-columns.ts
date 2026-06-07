/**
 * PS-01 상세 보기 36컬럼 정의.
 * pms-performance-grid.tsx에서 분리(300줄 규칙 대응).
 */

import type { GridColumn } from "@/components/shared/grid-list";
import { fmtDate, fmtEnum, fmtNumber } from "@/lib/grid-formatters";
import type { PmsPerformanceRow } from "@/application/pms/performance/ports";

export function buildDetailColumns(
  jobDivOptions: ReadonlyArray<{ value: string; label: string }>,
  boundOptions:  ReadonlyArray<{ value: string; label: string }>,
): GridColumn<PmsPerformanceRow>[] {
  return [
    // col 1-2
    { key: "houseBlNo",  label: "House B/L No",  minWidth: 150 },
    { key: "masterBlNo", label: "Master B/L No", minWidth: 150 },

    // col 3 Team
    { key: "teamName", label: "Team", minWidth: 80 },

    // col 4-8
    {
      key: "jobDiv",
      label: "Job Div",
      minWidth: 70,
      align: "center",
      render: (v) => fmtEnum(v, jobDivOptions),
    },
    {
      key: "bound",
      label: "Bound",
      minWidth: 60,
      align: "center",
      render: (v) => fmtEnum(v, boundOptions),
    },
    { key: "etd",           label: "ETD",        minWidth: 90,  align: "center", render: (v) => fmtDate(v) },
    { key: "eta",           label: "ETA",        minWidth: 90,  align: "center", render: (v) => fmtDate(v) },
    { key: "performanceDt", label: "Perf. Date", minWidth: 90,  align: "center", render: (v) => fmtDate(v) },

    // col 9-12 Actual Customer / Settle Partner
    { key: "actualCustomerCode", label: "Actual Customer", minWidth: 100, align: "center" },
    { key: "actualCustomerName", label: "Name",            minWidth: 160 },
    { key: "settlePartnerCode",  label: "Settle Partner",  minWidth: 100, align: "center" },
    { key: "settlePartnerName",  label: "Name",            minWidth: 160 },

    // col 13-14 Carrier
    { key: "linerCode", label: "Carrier", minWidth: 80,  align: "center" },
    { key: "linerName", label: "Name",    minWidth: 140 },

    // col 15-16 항만
    { key: "polCode", label: "POL", minWidth: 70, align: "center" },
    { key: "podCode", label: "POD", minWidth: 70, align: "center" },

    // col 17 Sales Man
    { key: "salesManName", label: "Sales Man", minWidth: 80 },

    // col 18
    { key: "incoterms", label: "Incoterms", minWidth: 80, align: "center" },

    // col 19-24 화물 수치
    { key: "loadType",       label: "Load Type",  minWidth: 70,  align: "center" },
    { key: "pkgQty",         label: "Pkg Qty",    minWidth: 70,  align: "right", render: (v) => fmtNumber(v, 0) },
    { key: "rton",           label: "R/Ton",      minWidth: 80,  align: "right", render: (v) => fmtNumber(v, 3) },
    { key: "cbm",            label: "CBM",        minWidth: 80,  align: "right", render: (v) => fmtNumber(v, 3) },
    { key: "chargeWeightKg", label: "Charge W/T", minWidth: 90,  align: "right", render: (v) => fmtNumber(v, 3) },
    { key: "grossWeightKg",  label: "Gross W/T",  minWidth: 90,  align: "right", render: (v) => fmtNumber(v, 3) },

    // col 25-29 Local 금액
    { key: "invoiceLocalAmt",  label: "Invoice Local Amt",  minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "debitLocalAmt",    label: "Debit Local Amt",    minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "paymentLocalAmt",  label: "Payment Local Amt",  minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "creditLocalAmt",   label: "Credit Local Amt",   minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "localProfit",      label: "Local Profit",       minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },

    // col 30-34 USD 금액
    { key: "invoiceUsdAmt",  label: "Invoice USD Amt",  minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "debitUsdAmt",    label: "Debit USD Amt",    minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "paymentUsdAmt",  label: "Payment USD Amt",  minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "creditUsdAmt",   label: "Credit USD Amt",   minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },
    { key: "usdProfit",      label: "USD Profit",       minWidth: 120, align: "right", aggregate: "sum", aggregateDecimals: 2, render: (v) => fmtNumber(v, 2) },

    // col 35-36 마감 (공란)
    { key: "blClosed",      label: "B/L Closed",     minWidth: 90, align: "center" },
    { key: "freightClosed", label: "Freight Closed", minWidth: 90, align: "center" },
  ];
}
