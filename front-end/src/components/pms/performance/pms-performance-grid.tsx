"use client";

import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { usePmsEnumOptions } from "@/application/pms/enums/use-pms-enum";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { pmsPerformancePort } from "@/lib/ports";
import { pmsPerformanceKeys } from "@/application/pms/performance/use-cases";
import { fmtDate, fmtEnum, fmtNumber } from "@/lib/grid-formatters";
import type { PmsPerformanceRow, SearchPmsPerformanceInput } from "@/application/pms/performance/ports";

interface Props {
  searchFilter: SearchPmsPerformanceInput | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  pageSize: number;
  onCyclePageSize: () => void;
}

/**
 * PS-01 실적 그리드 — 36컬럼, 읽기 전용.
 * searchFilter가 null이면 enabled=false로 조회하지 않음 (Reset 정책).
 */
export function PmsPerformanceGrid({
  searchFilter,
  currentPage,
  onPageChange,
  pageSize,
  onCyclePageSize,
}: Props) {
  const { options: jobDivOptions } = usePmsEnumOptions("JobDiv");
  const { options: boundOptions }  = usePmsEnumOptions("Bound");

  const enabled = searchFilter !== null;
  const queryInput = searchFilter
    ? { ...searchFilter, page: currentPage - 1, size: pageSize }
    : null;

  const { data, isFetching } = useQuery({
    queryKey: pmsPerformanceKeys.search(queryInput ?? { basis: "FREIGHT_INPUT", page: 0, size: pageSize }),
    queryFn: () => pmsPerformancePort.search(queryInput!),
    enabled,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages    = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const columns = useMemo<GridColumn<PmsPerformanceRow>[]>(() => [
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
    { key: "etd", label: "ETD", minWidth: 90, align: "center", render: (v) => fmtDate(v) },
    { key: "eta", label: "ETA", minWidth: 90, align: "center", render: (v) => fmtDate(v) },
    { key: "performanceDt", label: "Perf. Date", minWidth: 90, align: "center", render: (v) => fmtDate(v) },

    // col 9-12 Actual Customer / Settle Partner
    { key: "actualCustomerCode", label: "Actual Customer", minWidth: 100, align: "center" },
    { key: "actualCustomerName", label: "Name",            minWidth: 160 },
    { key: "settlePartnerCode",  label: "Settle Partner",  minWidth: 100, align: "center" },
    { key: "settlePartnerName",  label: "Name",            minWidth: 160 },

    // col 13-14 Carrier
    { key: "linerCode", label: "Carrier", minWidth: 80, align: "center" },
    { key: "linerName", label: "Name",    minWidth: 140 },

    // col 15-16 항만
    { key: "polCode", label: "POL", minWidth: 70, align: "center" },
    { key: "podCode", label: "POD", minWidth: 70, align: "center" },

    // col 17 Sales Man
    { key: "salesManName", label: "Sales Man", minWidth: 80 },

    // col 18
    { key: "incoterms", label: "Incoterms", minWidth: 80, align: "center" },

    // col 19-24 화물 수치
    { key: "loadType",       label: "Load Type",  minWidth: 70, align: "center" },
    { key: "pkgQty",         label: "Pkg Qty",    minWidth: 70, align: "right", render: (v) => fmtNumber(v, 0) },
    { key: "rton",           label: "R/Ton",      minWidth: 80, align: "right", render: (v) => fmtNumber(v, 3) },
    { key: "cbm",            label: "CBM",        minWidth: 80, align: "right", render: (v) => fmtNumber(v, 3) },
    { key: "chargeWeightKg", label: "Charge W/T", minWidth: 90, align: "right", render: (v) => fmtNumber(v, 3) },
    { key: "grossWeightKg",  label: "Gross W/T",  minWidth: 90, align: "right", render: (v) => fmtNumber(v, 3) },

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
    { key: "blClosed",      label: "B/L Closed",      minWidth: 90, align: "center" },
    { key: "freightClosed", label: "Freight Closed",  minWidth: 90, align: "center" },
  ], [jobDivOptions, boundOptions]);

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">실적 조회</span>
          <span className="panel__rowcount">{totalElements}</span>
        </div>
        <div className="list-wrap">
          <GridList<PmsPerformanceRow>
            columns={columns}
            data={rows}
            rowKey={(row) => `${row.blType}-${row.blId}`}
            isLoading={isFetching}
            emptyMessage="데이터가 없습니다."
          />
        </div>
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={onPageChange}
          disabled={isFetching}
          pageSize={pageSize}
          onCyclePageSize={onCyclePageSize}
        />
      </div>
    </div>
  );
}
