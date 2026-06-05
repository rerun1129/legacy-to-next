import type { GridColumn } from "@/components/shared/grid-list";
import type { FinancialDocumentSearchRow } from "@/application/bms/financial-document/ports";
import { fmtDate, fmtNumber } from "@/lib/grid-formatters";

/**
 * 그룹화 모달 좌우 공용 컬럼. 마스터 그리드의 부분집합.
 */
export function buildGroupModalColumns(
  t: (key: string) => string,
): GridColumn<FinancialDocumentSearchRow>[] {
  return [
    { key: "documentNo",       label: t("documentNo"),   minWidth: 140 },
    { key: "groupFinancialNo", label: t("groupNo"),       minWidth: 120 },
    { key: "customerCode",     label: t("customerCode"),  minWidth: 80, align: "center" },
    { key: "customerName",     label: t("customerName"),  minWidth: 160 },
    {
      key: "documentDt",
      label: t("documentDt"),
      minWidth: 90,
      align: "center",
      render: (v) => fmtDate(v),
    },
    {
      key: "usdTotalAmount",
      label: t("usdAmount"),
      minWidth: 110,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
    {
      key: "localTotalAmount",
      label: t("localAmount"),
      minWidth: 110,
      align: "right",
      render: (v) => fmtNumber(v, 2),
    },
  ];
}
