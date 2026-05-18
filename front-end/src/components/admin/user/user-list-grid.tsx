"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { userUseCases } from "@/application/user/use-cases";
import type { UserRow, UserFilter } from "@/domain/user";

interface Props {
  extraFilter: UserFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  onRowDoubleClick: (id: number) => void;
  selectedKeys: ReadonlySet<number>;
  onSelectionChange: (next: Set<number>) => void;
}

const COLUMNS: GridColumn<UserRow>[] = [
  { key: "username", label: "사용자명", minWidth: 140 },
  { key: "email", label: "이메일", minWidth: 200 },
  {
    key: "active",
    label: "상태",
    minWidth: 90,
    align: "center",
    render: (v, row) => {
      if (row.deletedAt) return "삭제됨";
      return row.active ? "활성" : "비활성";
    },
  },
  {
    key: "deletedAt",
    label: "삭제일시",
    minWidth: 160,
    render: (v) => (v ? String(v) : ""),
  },
  { key: "updatedAt", label: "수정일시", minWidth: 160 },
];

export function UserListGrid({ extraFilter, currentPage, onPageChange, onRowDoubleClick, selectedKeys, onSelectionChange }: Props) {
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-user", "list", extraFilter, currentPage],
    queryFn: () => userUseCases.search(extraFilter!, currentPage, 50),
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
          <span className="panel__title">사용자 관리</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span style={{ color: "var(--ink-3)" }}>검색 조건을 입력 후 Search를 클릭하세요.</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">사용자 관리</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span className="text-error">데이터를 불러오지 못했습니다.</span>
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
        <span className="panel__title">사용자 관리</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
      </div>
      <div className="list-wrap">
        <GridList<UserRow>
          columns={COLUMNS}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          isLoading={isFetching}
          emptyMessage="검색 결과가 없습니다."
          onClearRow={() => setSelected(null)}
          selectable
          selectedKeys={selectedKeys}
          onSelectionChange={(next) => onSelectionChange(new Set([...next].map(Number)))}
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
