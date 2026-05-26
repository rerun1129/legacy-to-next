"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { RotateCcw, Search, Plus } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { NoticeListFilter } from "./notice-list-filter";
import { NoticeListGrid } from "./notice-list-grid";
import { NoticeEntryModal } from "./notice-entry-modal";
import type { EntryModalState } from "./notice-entry-modal";
import type { NoticeFilter } from "@/domain/notice";
import { noticeUseCases } from "@/application/notice/use-cases";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";

const DEFAULT_VALUES: NoticeFilter = {
  title: "",
  pinned: "ALL",
  scope: "ALL",
  publishedOnly: false,
};

const SCOPE = "/admin/notice/list";

type NoticeSearchState = SavedSearchState & { extraFilter: NoticeFilter | null };

export function NoticeListClient() {
  const form = useForm<NoticeFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<NoticeFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as NoticeSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.currentPage ?? 1;
  });
  const [entryModalState, setEntryModalState] = useState<EntryModalState | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());

  useEffect(() => {
    listFilterStore.getState().setSearch(SCOPE, { extraFilter, currentPage });
  }, [extraFilter, currentPage]);

  const bulkDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => noticeUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-notice"] });
      setSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CMS_NOTICE_LIST_RESET"
          className="btn btn--normal btn--sm"
          onClick={() => {
            form.reset(DEFAULT_VALUES);
            setExtraFilter(null);
            setCurrentPage(1);
          }}
        >
          <RotateCcw size={12} style={{ marginRight: 4 }} />Reset
        </ActionButton>
        <ActionButton
          buttonCode="BTN_ADMIN_CMS_NOTICE_LIST_SEARCH"
          className="btn btn--search btn--sm"
          onClick={() =>
            form.handleSubmit((values) => {
              setExtraFilter(values);
              setCurrentPage(1);
            })()
          }
        >
          <Search size={12} style={{ marginRight: 4 }} />Search
        </ActionButton>
        <ActionButton
          buttonCode="BTN_ADMIN_CMS_NOTICE_LIST_DELETE"
          className="btn btn--modal btn--sm"
          disabled={selectedKeys.size === 0 || bulkDeleteMutation.isPending}
          onClick={async () => {
            const ok = await confirm({
              title: "선택 삭제",
              description: `선택한 ${selectedKeys.size}개 항목을 삭제하시겠습니까?`,
              variant: "destructive",
            });
            if (ok) bulkDeleteMutation.mutate([...selectedKeys]);
          }}
        >
          선택 삭제
        </ActionButton>
        <ActionButton
          buttonCode="BTN_ADMIN_CMS_NOTICE_LIST_CREATE"
          className="btn btn--modal btn--sm"
          onClick={() => setEntryModalState({ mode: "create" })}
        >
          <Plus size={12} style={{ marginRight: 4 }} />신규
        </ActionButton>
      </div>

      <NoticeListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <NoticeListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
          selectedKeys={selectedKeys}
          onSelectionChange={setSelectedKeys}
        />
      </div>

      <NoticeEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => setEntryModalState(null)}
      />
    </>
  );
}
