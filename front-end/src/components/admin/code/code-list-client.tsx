"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { RotateCcw, Search } from "lucide-react";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { ActionButton } from "@/components/admin/access/action-button";
import { CodeMasterListFilter } from "./code-master-list-filter";
import { CodeMasterListGrid } from "./code-master-list-grid";
import { CodeDetailListGrid } from "./code-detail-list-grid";
import { confirm } from "@/components/confirm";
import type { CodeMasterFilter } from "@/domain/code-master";

const SCOPE = "/admin/code/list";

const DEFAULT_FILTER: CodeMasterFilter = {
  masterCode: "",
  masterName: "",
  active: "ALL",
};

type CodeSearchState = SavedSearchState & {
  submittedFilter: CodeMasterFilter | null;
  masterPage: number;
  selectedMasterId: number | null;
};

export function CodeListClient() {
  const qc = useQueryClient();

  const filterForm = useForm<CodeMasterFilter>({ defaultValues: DEFAULT_FILTER });
  // 필터 폼 값 영속/복원
  useListFilterSync(filterForm, SCOPE);

  const [submittedFilter, setSubmittedFilter] = useState<CodeMasterFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as CodeSearchState | undefined;
    return s?.submittedFilter ?? null;
  });
  const [masterPage, setMasterPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.currentPage ?? 1;
  });
  const [selectedMasterId, setSelectedMasterId] = useState<number | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as CodeSearchState | undefined;
    return s?.selectedMasterId ?? null;
  });
  const [detailDirty, setDetailDirty] = useState(false);

  // 검색 상태 영속화
  useEffect(() => {
    listFilterStore.getState().setSearch(SCOPE, {
      submittedFilter,
      currentPage: masterPage,
      selectedMasterId,
    });
  }, [submittedFilter, masterPage, selectedMasterId]);

  async function handleSelectMaster(id: number) {
    if (id === selectedMasterId) return;
    if (detailDirty) {
      const ok = await confirm({
        title: "미저장 변경",
        description: "저장하지 않은 Detail 변경이 있습니다. 버리고 이동할까요?",
        variant: "destructive",
      });
      if (!ok) return;
    }
    setSelectedMasterId(id);
  }

  function handleReset() {
    filterForm.reset(DEFAULT_FILTER);
    setSubmittedFilter(null);
    setSelectedMasterId(null);
    setMasterPage(1);
    qc.invalidateQueries({ queryKey: ["admin-code-master", "list"] });
    qc.invalidateQueries({ queryKey: ["admin-code-detail", "list"] });
  }

  function handleSearch() {
    filterForm.handleSubmit((values) => {
      qc.invalidateQueries({ queryKey: ["admin-code-master", "list"] });
      qc.invalidateQueries({ queryKey: ["admin-code-detail", "list"] });
      setSubmittedFilter(values);
      setMasterPage(1);
      setSelectedMasterId(null);
    })();
  }

  return (
    <>
      {/* 공용 상단 툴바: Reset/Search 만 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_LIST_RESET"
          className="btn btn--normal btn--sm"
          onClick={handleReset}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_LIST_SEARCH"
          className="btn btn--search btn--sm"
          onClick={handleSearch}
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      {/* 필터 카드 */}
      <CodeMasterListFilter form={filterForm} />

      {/* 2분할 컨테이너 */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "400px 1fr",
          gap: 12,
          flex: 1,
          minHeight: 0,
          overflow: "hidden",
          marginTop: 10,
        }}
      >
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <CodeMasterListGrid
            submittedFilter={submittedFilter}
            masterPage={masterPage}
            onMasterPageChange={setMasterPage}
            selectedMasterId={selectedMasterId}
            onSelectMaster={handleSelectMaster}
          />
        </div>
        <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
          <CodeDetailListGrid
            key={selectedMasterId ?? "none"}
            masterId={selectedMasterId}
            onDirtyChange={setDetailDirty}
          />
        </div>
      </div>
    </>
  );
}
