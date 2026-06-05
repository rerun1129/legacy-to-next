"use client";

import { useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { GridList, type GridColumn } from "@/components/shared/grid-list";
import { useEnumOptions } from "@/application/enums/use-enum";
import { financialDocumentKeys } from "@/application/bms/financial-document/use-cases";
import { financialDocumentPort } from "@/lib/ports";
import { fmtDate, fmtEnum, fmtNumber } from "@/lib/grid-formatters";
import type { FreightLineDetail } from "@/application/bms/financial-document/ports";

interface Props {
  selectedDocumentId: number | null;
}

export function FinancialDocumentDetailGrid({ selectedDocumentId }: Props) {
  const t = useTranslations("bms.list.detailCols");
  const tCommon = useTranslations("bms.list.common");

  const { options: taxTypeOptions } = useEnumOptions("TaxType");

  const { data, isFetching } = useQuery({
    queryKey: financialDocumentKeys.lines(selectedDocumentId ?? 0),
    queryFn: () => financialDocumentPort.findLines(selectedDocumentId!),
    enabled: selectedDocumentId !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data ?? [];

  const columns = useMemo<GridColumn<FreightLineDetail>[]>(() => [
    { key: "customerCode",  label: t("customerCode"),  minWidth: 80,  align: "center" },
    { key: "customerName",  label: t("customerName"),  minWidth: 110 },
    { key: "freightCode",   label: t("freightCode"),   minWidth: 80,  align: "center" },
    { key: "freightName",   label: t("freightName"),   minWidth: 110 },
    { key: "currency",      label: t("currency"),      minWidth: 60,  align: "center" },
    {
      key: "exchangeRate",
      label: t("exchangeRate"),
      minWidth: 80,
      align: "right",
      render: (v) => fmtNumber(v, 4),
    },
    { key: "per",           label: t("per"),           minWidth: 50,  align: "center" },
    {
      key: "unitQuantity",
      label: t("unitQuantity"),
      minWidth: 70,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      key: "unitPrice",
      label: t("unitPrice"),
      minWidth: 80,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      key: "settleAmount",
      label: t("settleAmount"),
      minWidth: 90,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      key: "localAmount",
      label: t("localAmount"),
      minWidth: 90,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      key: "taxType",
      label: t("taxType"),
      minWidth: 70,
      align: "center",
      render: (v) => fmtEnum(v, taxTypeOptions),
    },
    {
      key: "localTaxAmount",
      label: t("localTaxAmount"),
      minWidth: 80,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      // 총금액 = localAmount + localTaxAmount (계산 표시)
      key: "totalAmount" as keyof FreightLineDetail,
      label: t("totalAmount"),
      minWidth: 90,
      align: "right",
      render: (_v, row) => {
        const local = Number(row.localAmount ?? 0);
        const tax   = Number(row.localTaxAmount ?? 0);
        return fmtNumber(local + tax, 2);
      },
    },
    { key: "slipNo",  label: t("slipNo"),  minWidth: 100 },
    { key: "taxNo",   label: t("taxNo"),   minWidth: 100 },
    {
      key: "performanceDt",
      label: t("performanceDt"),
      minWidth: 90,
      align: "center",
      render: (v) => fmtDate(v),
    },
  ], [t, taxTypeOptions]);

  if (selectedDocumentId === null) {
    return (
      <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
        <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">{t("panelTitle")}</span>
          </div>
          <div
            className="list-wrap"
            style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
          >
            <span style={{ color: "var(--ink-3)" }}>{tCommon("selectDocument")}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{t("panelTitle")}</span>
          <span className="panel__rowcount">{rows.length}</span>
        </div>
        <div className="list-wrap">
          <GridList<FreightLineDetail>
            columns={columns}
            data={rows}
            rowKey={(row) => row.freightLineId}
            isLoading={isFetching}
            emptyMessage={t("noLines")}
          />
        </div>
      </div>
    </div>
  );
}
