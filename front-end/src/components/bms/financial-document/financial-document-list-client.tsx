"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { RotateCcw, Search } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { DEFAULT_PAGE_SIZE, cyclePageSize } from "@/lib/grid-pagination";
import { FinancialDocumentListFilter } from "./financial-document-list-filter";
import { FinancialDocumentMasterGrid } from "./financial-document-master-grid";
import { FinancialDocumentDetailGrid } from "./financial-document-detail-grid";
import type { FinancialDocumentListConfig } from "./financial-document-list-config";
import type { FinancialDocumentFilter } from "./use-financial-document-list-filter-model";
import { DEFAULT_FILTER } from "./use-financial-document-list-filter-model";
import type { SearchFinancialDocumentInput, FinancialDocumentSearchRow } from "@/application/bms/financial-document/ports";

interface SearchState extends SavedSearchState {
  submittedFilter: SearchFinancialDocumentInput | null;
  selectedId: number | null;
}

interface Props {
  config: FinancialDocumentListConfig;
}

/**
 * BS-01 Invoice / BS-02 Payment / BS-03 D/C Note 공용 Shell.
 * config 주입으로 3화면 공유, 검색은 Search 명시 트리거만(자동조회 없음).
 */
export function FinancialDocumentListClient({ config }: Props) {
  const scope = config.routeKey;

  const form = useForm<FinancialDocumentFilter>({ defaultValues: DEFAULT_FILTER });

  // 영속화된 검색 상태 복원 (listFilterStore)
  const [submittedFilter, setSubmittedFilter] = useState<SearchFinancialDocumentInput | null>(() => {
    const s = listFilterStore.getState().getSearch(scope) as SearchState | undefined;
    return s?.submittedFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(scope);
    return s?.currentPage ?? 1;
  });
  const [pageSize, setPageSize] = useState(() => {
    const s = listFilterStore.getState().getSearch(scope);
    return s?.pageSize ?? DEFAULT_PAGE_SIZE;
  });
  const [selectedRow, setSelectedRow] = useState<FinancialDocumentSearchRow | null>(() => {
    // 선택 행은 세션 내 영속하지 않음(디테일은 서류 선택 시에만 의미 있음)
    return null;
  });

  // 영속화 저장
  useEffect(() => {
    listFilterStore.getState().setSearch(scope, {
      submittedFilter,
      currentPage,
      pageSize,
      selectedId: selectedRow?.financialDocumentId ?? null,
    });
  }, [submittedFilter, currentPage, pageSize, selectedRow, scope]);

  const handleCyclePageSize = () => {
    setPageSize(cyclePageSize(pageSize));
    setCurrentPage(1);
  };

  function handleReset() {
    form.reset(DEFAULT_FILTER);
    setSubmittedFilter(null);
    setSelectedRow(null);
    setCurrentPage(1);
  }

  function handleSearch() {
    form.handleSubmit((values) => {
      // dateKind → 대응 날짜 범위 필드에 매핑
      const dateFrom = values.dateFrom || null;
      const dateTo   = values.dateTo || null;

      const filter: SearchFinancialDocumentInput = {
        documentTypes: config.documentTypes,
        documentStatus: values.documentStatus || null,
        customerCode: values.customerCode || null,
        documentNoLike: values.documentNoLike || null,
        teamCode: values.teamCode || null,
        operator: values.operator || null,
        documentDtFrom:     values.dateKind === "DOCUMENT_DT"   ? dateFrom : null,
        documentDtTo:       values.dateKind === "DOCUMENT_DT"   ? dateTo   : null,
        performanceDtFrom:  values.dateKind === "PERFORMANCE_DT" ? dateFrom : null,
        performanceDtTo:    values.dateKind === "PERFORMANCE_DT" ? dateTo   : null,
        etdFrom:            values.dateKind === "ETD"            ? dateFrom : null,
        etdTo:              values.dateKind === "ETD"            ? dateTo   : null,
        etaFrom:            values.dateKind === "ETA"            ? dateFrom : null,
        etaTo:              values.dateKind === "ETA"            ? dateTo   : null,
        jobDiv: values.jobDiv || null,
        bound:  values.bound  || null,
      };

      setSubmittedFilter(filter);
      setSelectedRow(null);
      setCurrentPage(1);
    })();
  }

  return (
    <>
      {/* 상단 툴바: Reset / Search */}
      <div className="page-head__actions">
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
          <ActionButton
            buttonCode={config.resetButtonCode}
            className="btn btn--normal btn--sm"
            onClick={handleReset}
            icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
          />
          <ActionButton
            buttonCode={config.searchButtonCode}
            className="btn btn--search btn--sm"
            onClick={handleSearch}
            icon={<Search size={12} style={{ marginRight: 4 }} />}
          />
        </div>
      </div>

      {/* 필터 카드 */}
      <FinancialDocumentListFilter form={form} scope={scope} />

      {/* 마스터-디테일 2그리드 */}
      <div
        style={{
          display: "grid",
          gridTemplateRows: "1fr 1fr",
          gap: 8,
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
          margin: "10px 14px 0",
        }}
      >
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <FinancialDocumentMasterGrid
            searchFilter={submittedFilter}
            currentPage={currentPage}
            onPageChange={setCurrentPage}
            pageSize={pageSize}
            onCyclePageSize={handleCyclePageSize}
            selectedId={selectedRow?.financialDocumentId ?? null}
            onSelectRow={setSelectedRow}
          />
        </div>
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <FinancialDocumentDetailGrid
            selectedDocumentId={selectedRow?.financialDocumentId ?? null}
          />
        </div>
      </div>
    </>
  );
}
