"use client";

import { useTranslations } from "next-intl";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

// ── Account Documents Panel ────────────────────────────────────

interface AccountRow {
  id: number;
  docType: string;
  docNo: string;
  issueDate: string;
  amount: string;
  currency: string;
  status: string;
}

const ACCOUNT_ROWS: AccountRow[] = [];

export function FreightAccountPanel() {
  const tf = useTranslations("fms.houseBl.entry.freight");

  const accountCols: GridColumn<AccountRow>[] = [
    { key: "docType",   label: tf("cols.docType")  },
    { key: "docNo",     label: tf("cols.docNo")    },
    { key: "issueDate", label: tf("cols.issueDate") },
    { key: "amount",    label: tf("cols.amount"),  className: "is-num" },
    { key: "currency",  label: tf("cols.currency") },
    { key: "status",    label: tf("cols.status")   },
  ];

  return (
    <div
      className="panel"
      style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}
    >
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tf("panels.accountDocuments")}</span>
        <span className="panel__rowcount">{ACCOUNT_ROWS.length}</span>
      </div>
      <div className="panel__body--flush">
        <GridList columns={accountCols} data={ACCOUNT_ROWS} rowKey={(row) => row.id} />
      </div>
    </div>
  );
}
