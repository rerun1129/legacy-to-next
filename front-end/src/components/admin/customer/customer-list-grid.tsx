"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { customerUseCases } from "@/application/customer/use-cases";
import type { CustomerRow, CustomerFilter } from "@/domain/customer";

interface Props {
  extraFilter: CustomerFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  onRowDoubleClick: (id: number) => void;
  selectedKeys: ReadonlySet<number>;
  onSelectionChange: (next: Set<number>) => void;
}

const COLUMNS: GridColumn<CustomerRow>[] = [
  { key: "customerCode", label: "Customer Code", minWidth: 140 },
  { key: "customerType", label: "Type", minWidth: 120 },
  { key: "name", label: "Name", minWidth: 180 },
  { key: "nameEn", label: "English Name", minWidth: 180 },
  { key: "businessNo", label: "Business No.", minWidth: 140 },
  { key: "representative", label: "Representative", minWidth: 160 },
  { key: "phone", label: "Phone", minWidth: 140 },
  { key: "email", label: "Email", minWidth: 200 },
  { key: "customerLocalAddress", label: "Local Address", minWidth: 260 },
  { key: "customerEnglishAddress", label: "English Address", minWidth: 260 },
  { key: "memo", label: "Memo", minWidth: 200 },
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

export function CustomerListGrid({ extraFilter, currentPage, onPageChange, onRowDoubleClick, selectedKeys, onSelectionChange }: Props) {
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-customer", "list", extraFilter, currentPage],
    queryFn: () => customerUseCases.search(extraFilter!, currentPage, 50),
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
          <span className="panel__title">Customer Management</span>
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
          <span className="panel__title">Customer Management</span>
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
    // 이벤트 위임: td[data-row-key]에서 더블클릭을 캡처하여 행 id를 전달
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
        <span className="panel__title">고객 관리</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        <ColumnVisibilityMenu<CustomerRow> gridId="admin-customer" defaultColumns={COLUMNS} />
      </div>
      <div className="list-wrap">
        <GridList<CustomerRow>
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
          gridId="admin-customer"
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
