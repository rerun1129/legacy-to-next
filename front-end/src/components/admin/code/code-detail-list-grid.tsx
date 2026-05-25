"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { Pagination } from "@/components/shared/pagination";
import { codeDetailUseCases } from "@/application/code-detail/use-cases";
import { CodeDetailEntryModal } from "./code-detail-entry-modal";
import type { CodeDetailEntryModalState } from "./code-detail-entry-modal";
import type { CodeDetailRow } from "@/domain/code-detail";

interface Props {
  masterId: number | null;
  selectedKeys: ReadonlySet<number>;
  onSelectionChange: (next: Set<number>) => void;
  onBulkDelete: () => void;
  isBulkDeletePending: boolean;
}

const COLUMNS: GridColumn<CodeDetailRow>[] = [
  { key: "codeValue", label: "Code Value", minWidth: 120 },
  { key: "codeLabel", label: "Code Label", minWidth: 160 },
  { key: "sortOrder", label: "Sort Order", minWidth: 80, align: "right" },
  {
    key: "active",
    label: "Active",
    minWidth: 70,
    align: "center",
    render: (v) => (v ? "Active" : "Inactive"),
  },
  { key: "updatedAt", label: "Updated At", minWidth: 150 },
];

export function CodeDetailListGrid({ masterId, selectedKeys, onSelectionChange, onBulkDelete, isBulkDeletePending }: Props) {
  const [currentPage, setCurrentPage] = useState(1);
  const [entryModalState, setEntryModalState] = useState<CodeDetailEntryModalState | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-code-detail", "list", masterId, currentPage],
    queryFn: () => codeDetailUseCases.search(masterId!, currentPage, 50),
    enabled: masterId !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  if (masterId === null) {
    return (
      <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
        <div
          className="panel"
          style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}
        >
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">Code Detail</span>
          </div>
          <div
            className="list-wrap"
            style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
          >
            <span style={{ color: "var(--ink-3)" }}>좌측에서 마스터 코드를 선택하세요.</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_LIST_DELETE"
          className="btn btn--modal btn--sm"
          disabled={selectedKeys.size === 0 || isBulkDeletePending}
          onClick={onBulkDelete}
        >
          선택 삭제
        </ActionButton>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_LIST_CREATE"
          className="btn btn--modal btn--sm"
          onClick={() => setEntryModalState({ mode: "create", masterId })}
        >
          <Plus size={12} style={{ marginRight: 4 }} />신규
        </ActionButton>
      </div>

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        {error ? (
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">Code Detail</span>
            </div>
            <div
              className="list-wrap"
              style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
            >
              <span className="text-error">Failed to load data.</span>
            </div>
          </div>
        ) : (
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">Code Detail</span>
              <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
              <ColumnVisibilityMenu<CodeDetailRow> gridId="admin-code-detail" defaultColumns={COLUMNS} />
            </div>
            <div className="list-wrap">
              <GridList<CodeDetailRow>
                columns={COLUMNS}
                data={rows}
                rowKey={(row) => row.id}
                onRowClick={(row) => setEntryModalState({ mode: "edit", masterId, id: row.id })}
                isLoading={isFetching}
                emptyMessage="No results found."
                selectable
                selectedKeys={selectedKeys}
                onSelectionChange={(next) => onSelectionChange(new Set([...next].map(Number)))}
                gridId="admin-code-detail"
              />
            </div>
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
              disabled={isFetching}
            />
          </div>
        )}
      </div>

      <CodeDetailEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => {
          setEntryModalState(null);
        }}
      />
    </div>
  );
}
