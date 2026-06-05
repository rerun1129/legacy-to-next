"use client";

import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { useEnumOptions } from "@/application/enums/use-enum";
import { financialDocumentKeys } from "@/application/bms/financial-document/use-cases";
import { financialDocumentPort } from "@/lib/ports";
import { fmtDate, fmtEnum, fmtNumber } from "@/lib/grid-formatters";
import type { LabelOption } from "@/components/shared/inputs/_types";
import type { SearchFinancialDocumentInput, FinancialDocumentSearchRow } from "@/application/bms/financial-document/ports";

interface Props {
  searchFilter: SearchFinancialDocumentInput | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  pageSize: number;
  onCyclePageSize: () => void;
  selectedId: number | null;
  onSelectRow: (row: FinancialDocumentSearchRow | null) => void;
  /** 체크박스 다중선택 활성화 여부 */
  selectable?: boolean;
  /** 선택된 row key Set (number) */
  selectedKeys?: ReadonlySet<number>;
  /** 선택 변경 콜백 */
  onSelectionChange?: (next: Set<number>) => void;
}

export function FinancialDocumentMasterGrid({
  searchFilter,
  currentPage,
  onPageChange,
  pageSize,
  onCyclePageSize,
  selectedId,
  onSelectRow,
  selectable,
  selectedKeys,
  onSelectionChange,
}: Props) {
  const t = useTranslations("bms.list.masterCols");
  const tBmsCommon = useTranslations("bms.list.common");
  const tFilter = useTranslations("bms.list.filter");

  // status는 BE에 enum 미등록 → 정적 라벨 사용
  const statusOptions = useMemo<LabelOption[]>(() => [
    { value: "CREATED", label: tFilter("statusCreated") },
    { value: "GROUPED", label: tFilter("statusGrouped") },
    { value: "TAX",     label: tFilter("statusTax") },
    { value: "SLIP",    label: tFilter("statusSlip") },
    { value: "CLEAR",   label: tFilter("statusClear") },
  ], [tFilter]);

  const { options: jobDivOptions }  = useEnumOptions("housebl.JobDiv");
  const { options: boundOptions }   = useEnumOptions("Bound");

  const enabled = searchFilter !== null;
  const { data, isFetching } = useQuery({
    queryKey: financialDocumentKeys.search(
      searchFilter ?? { documentTypes: [] },
      currentPage - 1,
      pageSize
    ),
    // BE는 0-based page
    queryFn: () => financialDocumentPort.search(searchFilter!, currentPage - 1, pageSize),
    enabled,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  // 컬럼 배열 — useMemo로 옵션/번역 변경 시에만 재계산 (렌더 루프 방지)
  const columns = useMemo<GridColumn<FinancialDocumentSearchRow>[]>(() => [
    {
      key: "documentStatus",
      label: t("status"),
      minWidth: 80,
      align: "center",
      render: (v) => fmtEnum(v, statusOptions),
    },
    { key: "documentNo",        label: t("documentNo"),   minWidth: 140 },
    { key: "groupFinancialNo",  label: t("groupNo"),      minWidth: 120 },
    { key: "customerCode",      label: t("customerCode"), minWidth: 80,  align: "center" },
    { key: "customerName",      label: t("customerName"), minWidth: 240 },
    {
      key: "documentDt",
      label: t("documentDt"),
      minWidth: 90,
      align: "center",
      render: (v) => fmtDate(v),
    },
    {
      key: "performanceDt",
      label: t("performanceDt"),
      minWidth: 90,
      align: "center",
      render: (v) => fmtDate(v),
    },
    {
      key: "jobDiv",
      label: t("jobDiv"),
      minWidth: 70,
      align: "center",
      render: (v) => fmtEnum(v, jobDivOptions),
    },
    {
      key: "bound",
      label: t("bound"),
      minWidth: 60,
      align: "center",
      render: (v) => fmtEnum(v, boundOptions),
    },
    { key: "blNo",         label: t("blNo"),         minWidth: 140 },
    { key: "teamName",     label: t("teamName"),     minWidth: 80 },
    { key: "operatorName", label: t("operatorName"), minWidth: 80 },
    {
      key: "etd",
      label: t("etd"),
      minWidth: 90,
      align: "center",
      render: (v) => fmtDate(v),
    },
    {
      key: "eta",
      label: t("eta"),
      minWidth: 90,
      align: "center",
      render: (v) => fmtDate(v),
    },
    {
      key: "usdTotalAmount",
      label: t("usdAmount"),
      minWidth: 120,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      key: "localTotalAmount",
      label: t("localAmount"),
      minWidth: 120,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      key: "settleTotalVat",
      label: t("localVat"),
      minWidth: 120,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
  ], [t, statusOptions, jobDivOptions, boundOptions]);

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{t("panelTitle")}</span>
          <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        </div>
        <div className="list-wrap">
          <GridList<FinancialDocumentSearchRow>
            columns={columns}
            data={rows}
            rowKey={(row) => row.financialDocumentId}
            onRowClick={(row) => onSelectRow(row)}
            rowClassName={(row) =>
              row.financialDocumentId === selectedId ? "grid-row--selected" : undefined
            }
            isLoading={isFetching}
            emptyMessage={tBmsCommon("noData")}
            selectable={selectable}
            selectedKeys={selectedKeys}
            onSelectionChange={onSelectionChange as ((next: Set<string | number>) => void) | undefined}
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
