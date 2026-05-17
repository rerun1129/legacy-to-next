"use client";

import { useQuery } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { faqCategoryUseCases } from "@/application/faq-category/use-cases";
import type { FaqCategoryRow } from "@/domain/faq-category";

interface Props {
  selectedId: number | null;
  onSelect: (id: number) => void;
  onEdit: (id: number) => void;
  onCreate: () => void;
}

const COLUMNS: GridColumn<FaqCategoryRow>[] = [
  { key: "name", label: "카테고리명", minWidth: 160 },
  { key: "sortOrder", label: "정렬순서", minWidth: 80, align: "center" },
  {
    key: "active",
    label: "상태",
    minWidth: 70,
    align: "center",
    render: (_v, row) => (row.deletedAt ? "삭제됨" : row.active ? "활성" : "비활성"),
  },
];

export function FaqCategoryPanel({ selectedId, onSelect, onEdit, onCreate }: Props) {
  const { data: rows = [], isFetching, error } = useQuery({
    queryKey: ["admin-faq-category", "list"],
    queryFn: () => faqCategoryUseCases.search(),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  function handleDoubleClick(e: React.MouseEvent<HTMLDivElement>) {
    const td = (e.target as HTMLElement).closest("td[data-row-key]");
    if (!td) return;
    const key = Number((td as HTMLElement).getAttribute("data-row-key"));
    if (!isNaN(key)) onEdit(key);
  }

  return (
    <div
      className="panel"
      style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}
    >
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">FAQ 카테고리</span>
        <span className="panel__rowcount">{rows.length}</span>
        <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={onCreate}>
          신규
        </Button>
      </div>

      {error ? (
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span className="text-error">카테고리를 불러오지 못했습니다.</span>
        </div>
      ) : (
        <div className="list-wrap" onDoubleClick={handleDoubleClick}>
          <GridList<FaqCategoryRow>
            columns={COLUMNS}
            data={rows}
            onRowClick={(row) => onSelect(row.id)}
            rowKey={(row) => row.id}
            rowClassName={(row) => (selectedId === row.id ? "is-selected" : undefined)}
            isLoading={isFetching}
            emptyMessage="등록된 카테고리가 없습니다."
          />
        </div>
      )}
    </div>
  );
}
