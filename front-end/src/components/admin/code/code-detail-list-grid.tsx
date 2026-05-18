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
import { codeDetailUseCases } from "@/application/code-detail/use-cases";
import { CodeDetailListFilter } from "./code-detail-list-filter";
import { CodeDetailEntryModal } from "./code-detail-entry-modal";
import type { CodeDetailEntryModalState } from "./code-detail-entry-modal";
import type { CodeDetailRow, CodeDetailFilter } from "@/domain/code-detail";

interface Props {
  masterId: number | null;
}

const DEFAULT_FILTER: CodeDetailFilter = {
  codeValue: "",
  codeLabel: "",
  active: "ALL",
};

const COLUMNS: GridColumn<CodeDetailRow>[] = [
  { key: "codeValue", label: "코드 값", minWidth: 120 },
  { key: "codeLabel", label: "코드 라벨", minWidth: 160 },
  { key: "sortOrder", label: "정렬순서", minWidth: 80, align: "right" },
  {
    key: "active",
    label: "활성",
    minWidth: 70,
    align: "center",
    render: (v) => (v ? "활성" : "비활성"),
  },
  { key: "updatedAt", label: "수정일시", minWidth: 150 },
];

export function CodeDetailListGrid({ masterId }: Props) {
  const form = useForm<CodeDetailFilter>({ defaultValues: DEFAULT_FILTER });

  const [submittedFilter, setSubmittedFilter] = useState<CodeDetailFilter | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [entryModalState, setEntryModalState] = useState<CodeDetailEntryModalState | null>(null);

  const { data, isFetching, error } = useQuery({
    queryKey: ["admin-code-detail", "list", masterId, submittedFilter, currentPage],
    queryFn: () => codeDetailUseCases.search(masterId!, submittedFilter!, currentPage, 50),
    enabled: masterId !== null && submittedFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const rows = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;

  function handleSearch() {
    if (masterId === null) return;
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

  if (masterId === null) {
    return (
      <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
        <div
          className="panel"
          style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}
        >
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">코드 상세</span>
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
        <Button size="sm" variant="normal" leftIcon={<RotateCcw size={12} />} onClick={handleReset}>
          Reset
        </Button>
        <Button size="sm" variant="search" leftIcon={<Search size={12} />} onClick={handleSearch}>
          Search
        </Button>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_LIST_CREATE"
          className="btn btn--modal btn--sm"
          onClick={() => setEntryModalState({ mode: "create", masterId })}
        >
          <Plus size={12} style={{ marginRight: 4 }} />신규
        </ActionButton>
      </div>

      <CodeDetailListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        {submittedFilter === null ? (
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">코드 상세</span>
            </div>
            <div
              className="list-wrap"
              style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
            >
              <span style={{ color: "var(--ink-3)" }}>검색 조건을 입력 후 Search를 클릭하세요.</span>
            </div>
          </div>
        ) : error ? (
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">코드 상세</span>
            </div>
            <div
              className="list-wrap"
              style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
            >
              <span className="text-error">데이터를 불러오지 못했습니다.</span>
            </div>
          </div>
        ) : (
          <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
            <div className="panel__head">
              <div className="panel__title-accent" />
              <span className="panel__title">코드 상세</span>
              <span className="panel__rowcount">{data?.totalElements ?? 0}</span>
            </div>
            <div className="list-wrap">
              <GridList<CodeDetailRow>
                columns={COLUMNS}
                data={rows}
                rowKey={(row) => row.id}
                onRowClick={(row) => setEntryModalState({ mode: "edit", masterId, id: row.id })}
                isLoading={isFetching}
                emptyMessage="검색 결과가 없습니다."
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
