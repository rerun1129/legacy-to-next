"use client";

import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { useEnumOptions } from "@/application/enums/use-enum";
import { freightLineIssueKeys } from "@/application/bms/freight-line-issue/use-cases";
import { freightLineIssuePort } from "@/lib/ports";
import { fmtDate, fmtEnum, fmtNumber } from "@/lib/grid-formatters";
import type { SearchFreightLineInput, FreightLineIssueRow } from "@/application/bms/freight-line-issue/ports";
import type { IssueType } from "./freight-line-issue-list-config";

interface Props {
  searchFilter: SearchFreightLineInput | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  pageSize: number;
  onPageSizeChange: (size: number) => void;
  selectedKeys: ReadonlySet<number>;
  onSelectionChange: (next: Set<number>) => void;
  issueType: IssueType;
}

export function FreightLineIssueGrid({
  searchFilter,
  currentPage,
  onPageChange,
  pageSize,
  onPageSizeChange,
  selectedKeys,
  onSelectionChange,
  issueType,
}: Props) {
  const t = useTranslations("bms.issue.cols");
  const tCommon = useTranslations("bms.issue.common");

  const { options: jobDivOptions } = useEnumOptions("housebl.JobDiv");
  const { options: boundOptions }  = useEnumOptions("Bound");

  const enabled = searchFilter !== null;
  const { data, isFetching } = useQuery({
    queryKey: freightLineIssueKeys.search(
      searchFilter ?? {},
      currentPage - 1,
      pageSize,
    ),
    queryFn: () => freightLineIssuePort.search(searchFilter!, currentPage - 1, pageSize),
    enabled,
    staleTime: Infinity,
    gcTime: Infinity,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  const columns = useMemo<GridColumn<FreightLineIssueRow>[]>(() => {
    const issueNoCols: GridColumn<FreightLineIssueRow>[] = issueType === "TAX"
      ? [
          {
            key: "taxNo",
            label: t("taxNo"),
            minWidth: 120,
            render: (v) => (v as string | null | undefined) ?? "—",
          },
          {
            key: "taxDt",
            label: t("taxDt"),
            minWidth: 90,
            align: "center",
            render: (v) => fmtDate(v),
          },
        ]
      : [
          {
            key: "slipNo",
            label: t("slipNo"),
            minWidth: 120,
            render: (v) => (v as string | null | undefined) ?? "—",
          },
          {
            key: "slipDt",
            label: t("slipDt"),
            minWidth: 90,
            align: "center",
            render: (v) => fmtDate(v),
          },
        ];

    return [
      {
        key: "documentStatus",
        label: t("documentStatus"),
        minWidth: 80,
        align: "center",
        render: (v) => (v as string | null | undefined) ?? "—",
      },
      { key: "documentNo",      label: t("documentNo"),    minWidth: 140 },
      { key: "customerCode",    label: t("customerCode"),  minWidth: 80, align: "center" },
      { key: "customerName",    label: t("customerName"),  minWidth: 200 },
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
      { key: "blNo",          label: t("blNo"),          minWidth: 140 },
      {
        key: "etd",
        label: t("etd"),
        minWidth: 90,
        align: "center",
        render: (v) => fmtDate(v as string | null),
      },
      { key: "freightCode",   label: t("freightCode"),   minWidth: 100 },
      { key: "financialDocType", label: t("financialDocType"), minWidth: 80, align: "center" },
      { key: "currency",      label: t("currency"),      minWidth: 60, align: "center" },
      {
        key: "settleAmount",
        label: t("settleAmount"),
        minWidth: 110,
        align: "right",
        render: (v) => fmtNumber(v, 2),
      },
      {
        key: "settleTaxAmount",
        label: t("settleTaxAmount"),
        minWidth: 110,
        align: "right",
        render: (v) => fmtNumber(v, 2),
      },
      {
        key: "usdAmount",
        label: t("usdAmount"),
        minWidth: 110,
        align: "right",
        render: (v) => fmtNumber(v, 2),
      },
      {
        key: "performanceDt",
        label: t("performanceDt"),
        minWidth: 90,
        align: "center",
        render: (v) => fmtDate(v as string | null),
      },
      ...issueNoCols,
    ];
  }, [t, jobDivOptions, boundOptions, issueType]);

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tCommon("panelTitle")}</span>
          <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        </div>
        <div className="list-wrap">
          <GridList<FreightLineIssueRow>
            columns={columns}
            data={rows}
            rowKey={(row) => row.freightLineId}
            isLoading={isFetching}
            emptyMessage={tCommon("noData")}
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={onSelectionChange as ((next: Set<string | number>) => void)}
          />
        </div>
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={onPageChange}
          disabled={isFetching}
          pageSize={pageSize}
          onPageSizeChange={onPageSizeChange}
        />
      </div>
    </div>
  );
}
