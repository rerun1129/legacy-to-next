"use client";

import { useState, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { noticeUseCases } from "@/application/notice/use-cases";
import type { NoticeRow, NoticeFilter } from "@/domain/notice";
import { DEFAULT_PAGE_SIZE } from "@/lib/grid-pagination";

interface Props {
  extraFilter: NoticeFilter | null;
  currentPage: number;
  onPageChange: (page: number) => void;
  onRowDoubleClick: (id: number) => void;
  selectedKeys: ReadonlySet<number>;
  onSelectionChange: (next: Set<number>) => void;
}

export function NoticeListGrid({ extraFilter, currentPage, onPageChange, onRowDoubleClick, selectedKeys, onSelectionChange }: Props) {
  const tCols = useTranslations("admin.notice.cols");
  const tMsg = useTranslations("admin.notice.msg");
  const tPanel = useTranslations("admin.notice.panel");

  const [selected, setSelected] = useState<number | null>(null);

  const columns = useMemo<GridColumn<NoticeRow>[]>(() => [
    { key: "title", label: tCols("title"), minWidth: 300 },
    {
      key: "pinned",
      label: tCols("pinned"),
      minWidth: 70,
      align: "center",
      render: (v) => (v ? "Y" : ""),
    },
    {
      key: "active",
      label: tCols("status"),
      minWidth: 90,
      align: "center",
      render: (_v, row) => {
        if (row.deletedAt) return tMsg("statusDeleted");
        return row.active ? tMsg("statusActive") : tMsg("statusInactive");
      },
    },
    { key: "publishedAt", label: tCols("publishedAt"), minWidth: 160 },
    { key: "expiresAt", label: tCols("expiresAt"), minWidth: 160 },
    { key: "updatedAt", label: tCols("updatedAt"), minWidth: 160 },
  ], [tCols, tMsg]);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-notice", "list", extraFilter, currentPage],
    queryFn: () => noticeUseCases.search(extraFilter!, currentPage, DEFAULT_PAGE_SIZE),
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
          <span className="panel__title">{tPanel("title")}</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span style={{ color: "var(--ink-3)" }}>{tMsg("enterCriteria")}</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tPanel("title")}</span>
        </div>
        <div
          className="list-wrap"
          style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
        >
          <span className="text-error">{tMsg("loadFailed")}</span>
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
        <span className="panel__title">{tPanel("title")}</span>
        <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
        <ColumnVisibilityMenu<NoticeRow> gridId="admin-notice" defaultColumns={columns} />
      </div>
      <div className="list-wrap">
        <GridList<NoticeRow>
          columns={columns}
          data={rows}
          onRowClick={(row) => setSelected(row.id)}
          rowKey={(row) => row.id}
          rowClassName={(row) => (selected === row.id ? "is-selected" : undefined)}
          isLoading={isFetching}
          emptyMessage={tMsg("noResults")}
          onClearRow={() => setSelected(null)}
          selectable
          selectedKeys={selectedKeys}
          onSelectionChange={(next) => onSelectionChange(new Set([...next].map(Number)))}
          gridId="admin-notice"
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
