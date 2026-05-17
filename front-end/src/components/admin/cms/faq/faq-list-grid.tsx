"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { faqUseCases } from "@/application/faq/use-cases";
import type { FaqRow } from "@/domain/faq";

interface Props {
  categoryId: number | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  onRowDoubleClick: (id: number) => void;
}

const COLUMNS: GridColumn<FaqRow>[] = [
  { key: "question", label: "질문", minWidth: 400 },
  { key: "sortOrder", label: "정렬순서", minWidth: 80, align: "center" },
  {
    key: "active",
    label: "상태",
    minWidth: 80,
    align: "center",
    render: (_v, row) => (row.deletedAt ? "삭제됨" : row.active ? "활성" : "비활성"),
  },
  { key: "updatedAt", label: "수정일시", minWidth: 160 },
];

export function FaqListGrid({ categoryId, currentPage, onPageChange, onRowDoubleClick }: Props) {
  const [selected, setSelected] = useState<number | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-faq", "list", categoryId, currentPage],
    queryFn: () =>
      faqUseCases.search(
        { faqCategoryId: categoryId!, question: "", scope: "ALL" },
        currentPage,
        50,
      ),
    enabled: categoryId !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  if (categoryId === null) {
    return (
      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">FAQ 목록</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span style={{ color: "var(--ink-3)" }}>카테고리를 선택하세요.</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">FAQ 목록</span>
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
        <span className="panel__title">FAQ 목록</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
      </div>
      <div className="list-wrap">
        <GridList<FaqRow>
          columns={COLUMNS}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          isLoading={isFetching}
          emptyMessage="등록된 FAQ가 없습니다."
          onClearRow={() => setSelected(null)}
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
