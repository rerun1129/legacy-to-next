"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { currencyUseCases } from "@/application/code/currency/use-cases";
import type { CurrencyRow, CurrencyFilter } from "@/domain/code/currency";

interface Props {
  extraFilter: CurrencyFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  onRowDoubleClick: (id: number) => void;
  selectedKeys: ReadonlySet<number>;
  onSelectionChange: (next: Set<number>) => void;
}

const COLUMNS: GridColumn<CurrencyRow>[] = [
  { key: "currencyCode", label: "Currency Code", minWidth: 130 },
  { key: "name", label: "Name", minWidth: 160 },
  { key: "nameEn", label: "English Name", minWidth: 160 },
  { key: "symbol", label: "Symbol", minWidth: 80 },
  { key: "currencyUnit", label: "Unit", minWidth: 100, align: "right" },
  {
    key: "active",
    label: "Status",
    minWidth: 90,
    align: "center",
    render: (_v, row) => {
      if (row.deletedAt) return "Deleted";
      return row.active ? "Active" : "Inactive";
    },
  },
];

export function CurrencyListGrid({ extraFilter, currentPage, onPageChange, onRowDoubleClick, selectedKeys, onSelectionChange }: Props) {
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-code-currency", "list", extraFilter, currentPage],
    queryFn: () => currencyUseCases.search(extraFilter!, currentPage, 50),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  if (extraFilter === null) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">통화 관리</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span style={{ color: "var(--ink-3)" }}>Enter search criteria and click Search.</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">통화 관리</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span className="text-error">Failed to load data.</span>
        </div>
      </div>
    );
  }

  return (
    <div
      className="panel"
      style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}
      onDoubleClick={(e) => {
        const td = (e.target as HTMLElement).closest("td[data-row-key]");
        if (!td) return;
        const key = Number((td as HTMLElement).getAttribute("data-row-key"));
        if (!isNaN(key)) onRowDoubleClick(key);
      }}
    >
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">통화 관리</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        <ColumnVisibilityMenu<CurrencyRow> gridId="admin-code-currency" defaultColumns={COLUMNS} />
      </div>
      <div className="list-wrap">
        <GridList<CurrencyRow>
          columns={COLUMNS}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          isLoading={isFetching}
          emptyMessage="No results found."
          onClearRow={() => setSelected(null)}
          selectable
          selectedKeys={selectedKeys}
          onSelectionChange={(next) => onSelectionChange(new Set([...next].map(Number)))}
          gridId="admin-code-currency"
        />
      </div>
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={onPageChange}
        disabled={isFetching}
      />
    </div>
  );
}
