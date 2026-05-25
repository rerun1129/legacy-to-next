"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery } from "@tanstack/react-query";
import { RotateCcw, Search, Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { codeMasterUseCases } from "@/application/code-master/use-cases";
import { CodeMasterListFilter } from "./code-master-list-filter";
import { CodeMasterEntryModal } from "./code-master-entry-modal";
import type { CodeMasterEntryModalState } from "./code-master-entry-modal";
import type { CodeMasterRow, CodeMasterFilter } from "@/domain/code-master";

interface Props {
  selectedId: number | null;
  onSelect: (id: number) => void;
  onRowDoubleClick: (id: number) => void;
  selectedKeys: ReadonlySet<number>;
  onSelectionChange: (next: Set<number>) => void;
  onBulkDelete: () => void;
  isBulkDeletePending: boolean;
}

const DEFAULT_FILTER: CodeMasterFilter = {
  masterCode: "",
  masterName: "",
  active: "ALL",
};

const COLUMNS: GridColumn<CodeMasterRow>[] = [
  { key: "masterCode", label: "Master Code", minWidth: 140 },
  { key: "masterName", label: "Master Name", minWidth: 160 },
  { key: "description", label: "Description", minWidth: 160 },
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

export function CodeMasterListGrid({ selectedId, onSelect, onRowDoubleClick, selectedKeys, onSelectionChange, onBulkDelete, isBulkDeletePending }: Props) {
  const form = useForm<CodeMasterFilter>({ defaultValues: DEFAULT_FILTER });

  const [submittedFilter, setSubmittedFilter] = useState<CodeMasterFilter | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [entryModalState, setEntryModalState] = useState<CodeMasterEntryModalState | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-code-master", "list", submittedFilter, currentPage],
    queryFn: () => codeMasterUseCases.search(submittedFilter!, currentPage, 50),
    enabled: submittedFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  function handleSearch() {
    form.handleSubmit((values) => {
      setSubmittedFilter(values);
      setCurrentPage(1);
    })();
  }

  function handleReset() {
    form.reset(DEFAULT_FILTER);
    setSubmittedFilter(null);
    setCurrentPage(1);
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <Button size="sm" variant="normal" leftIcon={<RotateCcw size={12} />} onClick={handleReset}>
          Reset
        </Button>
        <Button size="sm" variant="search" leftIcon={<Search size={12} />} onClick={handleSearch}>
          Search
        </Button>
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
          onClick={() => setEntryModalState({ mode: "create" })}
        >
          <Plus size={12} style={{ marginRight: 4 }} />신규
        </ActionButton>
      </div>

      <CodeMasterListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        {submittedFilter === null ? (
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">Code Master</span>
            </div>
            <div
              className="list-wrap"
              style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
            >
              <span style={{ color: "var(--ink-3)" }}>Enter search criteria and click Search.</span>
            </div>
          </div>
        ) : error ? (
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">Code Master</span>
            </div>
            <div
              className="list-wrap"
              style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
            >
              <span className="text-error">Failed to load data.</span>
            </div>
          </div>
        ) : (
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
              <span className="panel__title">Code Master</span>
              <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
            </div>
            <div className="list-wrap">
              <GridList<CodeMasterRow>
                columns={COLUMNS}
                data={rows}
                onRowClick={(row) => onSelect(row.id)}
                rowKey={(row) => row.id}
                rowClassName={(row) =>
                  selectedId === row.id ? "is-selected" : undefined
                }
                isLoading={isFetching}
                emptyMessage="No results found."
                selectable
                selectedKeys={selectedKeys}
                onSelectionChange={(next) => onSelectionChange(new Set([...next].map(Number)))}
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

      <CodeMasterEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={(createdId) => {
          setEntryModalState(null);
          // 신규 생성 시 해당 master를 detail panel에 자동 선택
          if (createdId != null) {
            onSelect(createdId);
          }
        }}
      />
    </div>
  );
}
