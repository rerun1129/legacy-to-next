"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { GridList, GridColumn } from "@/components/shared/grid-list";

const ROWS = [
  { nbl: "NBL-2026-04-001", div: "Sea",       status: "처리중", cust: "한진무역(주)", sh: "한진무역(주)", cn: "SHANGHAI CO.",    etd: "04/24", eta: "05/08", pol: "KRBSAN", pod: "CNSHA", ref: "HBLKR24041956",  op: "KYS" },
  { nbl: "NBL-2026-04-002", div: "Air",       status: "완료",   cust: "LG전자(주)",  sh: "LG전자(주)",  cn: "LG JAPAN K.K.", etd: "04/22", eta: "04/23", pol: "ICN",    pod: "NRT",   ref: "HAWBKR24041002", op: "PKH" },
  { nbl: "NBL-2026-04-003", div: "Warehouse", status: "접수",   cust: "삼성전자",    sh: "-",          cn: "-",             etd: "04/20", eta: "04/20", pol: "KRICN",  pod: "KRICN", ref: "",               op: "LJY" },
  { nbl: "NBL-2026-04-004", div: "Trucking",  status: "접수",   cust: "포스코",      sh: "포스코",      cn: "평택항",         etd: "04/25", eta: "04/25", pol: "KRSEL",  pod: "KRPTK", ref: "",               op: "KYS" },
];

type NonBlRow = typeof ROWS[number];

const statusPill: Record<string, string> = {
  "완료":   "pill--ok",
  "처리중": "pill--sent",
  "접수":   "pill--draft",
};

export function NonBlGrid() {
  const router = useRouter();
  const [selected, setSelected] = useState<number | null>(null);

  const columns: GridColumn<NonBlRow>[] = [
    {
      key: "nbl",
      label: "Non B/L No.",
      minWidth: 150,
      render: (value) => (
        <span
          className="cell-hbl"
          onDoubleClick={() => router.push("/fms/non-bl/new")}
          style={{ cursor: "pointer" }}
          title="더블클릭하여 Entry 열기"
        >
          {String(value ?? "")}
        </span>
      ),
    },
    {
      key: "div",
      label: "Work Div.",
      minWidth: 90,
      render: (v) => <span className="chip chip--accent">{String(v ?? "")}</span>,
    },
    {
      key: "status",
      label: "Status",
      minWidth: 80,
      render: (v) => {
        const s = String(v ?? "");
        return <span className={`pill ${statusPill[s] ?? ""}`}>{s}</span>;
      },
    },
    { key: "cust", label: "Actual Customer", minWidth: 130 },
    { key: "sh",   label: "Shipper",         minWidth: 130 },
    { key: "cn",   label: "Consignee",       minWidth: 130 },
    { key: "etd",  label: "ETD", minWidth: 60, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "eta",  label: "ETA", minWidth: 60, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "pol",  label: "POL", minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "pod",  label: "POD", minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
    { key: "ref",  label: "원본 B/L Ref", minWidth: 140, render: (v) => <span className="cell-mono cell-dim">{String(v ?? "")}</span> },
    { key: "op",   label: "Operator",     minWidth: 70, render: (v) => <span className="cell-mono">{String(v ?? "")}</span> },
  ];

  return (
    <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Non B/L</span>
        <span className="panel__rowcount">{ROWS.length}</span>
      </div>
      <div className="list-wrap">
        <GridList<NonBlRow>
          columns={columns}
          data={ROWS}
          onRowClick={(_, i) => setSelected(i)}
          rowKey={(_, i) => i}
          rowClassName={(_, i) => (selected === i ? "is-selected" : undefined)}
        />
      </div>
    </div>
  );
}
