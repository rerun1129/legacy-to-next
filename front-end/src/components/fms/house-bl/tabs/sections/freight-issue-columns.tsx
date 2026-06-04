import { useTranslations } from "next-intl";
import { type GridColumn } from "@/components/shared/grid-list";
import { resolvePerLabel } from "@/components/fms/house-bl/freight-per";
import { type SelectedFreightLine } from "./freight-issue-types";

// 외부 Freight 탭(Selling/Buying) 그리드와 동일한 컬럼 구성·너비로 표시
export function buildFreightLineColumns(
  tf: ReturnType<typeof useTranslations>,
  taxTypeLabelMap: Map<string, string>,
): GridColumn<SelectedFreightLine>[] {
  return [
    {
      key: "freightCode",
      label: tf("cols.freightCode"),
      width: 80,
      align: "center",
    },
    {
      key: "freightName",
      label: tf("cols.freightName"),
      width: 260,
    },
    {
      key: "currency",
      label: tf("cols.currency"),
      width: 60,
      align: "center",
    },
    {
      key: "exchangeRate",
      label: tf("cols.exchangeRate"),
      className: "is-num",
      width: 90,
      render: (_, row) => row.exchangeRate != null ? row.exchangeRate.toFixed(2) : "",
    },
    {
      key: "per",
      label: tf("cols.per"),
      width: 80,
      align: "center",
      render: (_, row) => row.per ? resolvePerLabel(row.per) : "",
    },
    {
      key: "qty",
      label: tf("cols.qty"),
      className: "is-num",
      width: 80,
      render: (_, row) => row.qty != null ? String(row.qty) : "",
    },
    {
      key: "price",
      label: tf("cols.price"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.price != null ? row.price.toFixed(2) : "",
    },
    {
      key: "settleAmount",
      label: tf("cols.settleAmount"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.settleAmount != null ? row.settleAmount.toFixed(2) : "",
    },
    {
      key: "localAmount",
      label: tf("cols.localAmount"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.localAmount != null ? row.localAmount.toFixed(2) : "",
    },
    {
      key: "usdAmount",
      label: tf("cols.usdAmount"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.usdAmount != null ? row.usdAmount.toFixed(2) : "",
    },
    {
      key: "taxType",
      label: tf("cols.taxType"),
      width: 100,
      align: "center",
      render: (_, row) => row.taxType ? (taxTypeLabelMap.get(row.taxType) ?? row.taxType) : "",
    },
    {
      key: "vat",
      label: tf("cols.vat"),
      className: "is-num",
      width: 100,
      render: (_, row) => row.vat != null ? row.vat.toFixed(2) : "",
    },
    {
      key: "_total",
      label: tf("cols.total"),
      className: "is-num",
      width: 100,
      render: (_, row) => {
        if (row.localAmount == null && row.vat == null) return "";
        return ((row.localAmount ?? 0) + (row.vat ?? 0)).toFixed(2);
      },
    },
  ];
}
