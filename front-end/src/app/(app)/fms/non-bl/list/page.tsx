"use client";

import { useForm } from "react-hook-form";
import { Package, RotateCcw, Search } from "lucide-react";
import { NonBlGrid } from "@/components/fms/non-bl/non-bl-grid";
import { NonBlListFilter } from "@/components/fms/non-bl/non-bl-list-filter";
import type { NonBlListFilterValues } from "@/components/fms/non-bl/non-bl-list-filter";

function getDefaultMonthRange() {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();
  const pad = (n: number) => String(n).padStart(2, "0");
  const lastDate = new Date(y, m + 1, 0).getDate();
  return {
    from: `${y}${pad(m + 1)}01`,
    to: `${y}${pad(m + 1)}${pad(lastDate)}`,
  };
}

// 모듈 로드 시 1회 계산 — client component이므로 hydration mismatch 없음
const { from, to } = getDefaultMonthRange();

const DEFAULT_VALUES: NonBlListFilterValues = {
  bound: "ALL",
  dateFrom: from,
  dateTo: to,
  linerCode: "", linerName: "",
  nonBlNo: "",
  partyCode: "", partyName: "",
  portCode: "", portName: "",
  vessel: "", voyage: "",
  operatorCode: "", operatorName: "",
  teamCode: "", teamName: "",
};

export default function NonBLListPage() {
  const form = useForm<NonBlListFilterValues>({ defaultValues: DEFAULT_VALUES });

  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L List
        </div>
        <div className="page-head__actions">
          <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
            <button className="btn btn--sm btn--ghost" onClick={() => form.reset(DEFAULT_VALUES)}>
              <RotateCcw size={12} />
              Reset
            </button>
            <button className="btn btn--sm btn--primary">
              <Search size={12} />
              Search
            </button>
          </div>
        </div>
      </div>

      <NonBlListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", margin: "10px 14px 0", display: "flex", flexDirection: "column" }}>
        <NonBlGrid />
      </div>
    </div>
  );
}
