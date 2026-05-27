"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { RotateCcw, Search, Plus } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { CountryListFilter } from "./country-list-filter";
import { CountryListGrid } from "./country-list-grid";
import { CountryEntryModal } from "./country-entry-modal";
import type { EntryModalState } from "./country-entry-modal";
import type { CountryFilter } from "@/domain/code/country";
import { countryUseCases } from "@/application/code/country/use-cases";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";

const DEFAULT_VALUES: CountryFilter = {
  countryCode: "",
  name: "",
  scope: "ALL",
};

const SCOPE = "/admin/code/country/list";

type CountrySearchState = SavedSearchState & { extraFilter: CountryFilter | null };

export function CountryListClient() {
  const form = useForm<CountryFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<CountryFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as CountrySearchState | undefined;
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
    mutationFn: (ids: number[]) => countryUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-country", "list"] });
      setSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_COUNTRY_RESET"
          className="btn btn--normal btn--sm"
          onClick={() => {
            form.reset(DEFAULT_VALUES);
            setExtraFilter(null);
            setCurrentPage(1);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_COUNTRY_SEARCH"
          className="btn btn--search btn--sm"
          onClick={() =>
            form.handleSubmit((values) => {
              setExtraFilter(values);
              setCurrentPage(1);
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_COUNTRY_DELETE"
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
          icon={null}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_COUNTRY_CREATE"
          className="btn btn--modal btn--sm"
          onClick={() => setEntryModalState({ mode: "create" })}
          icon={<Plus size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      <CountryListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <CountryListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
          selectedKeys={selectedKeys}
          onSelectionChange={setSelectedKeys}
        />
      </div>

      <CountryEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => {
          qc.invalidateQueries({ queryKey: ["admin-code-country", "list"] });
          setEntryModalState(null);
        }}
      />
    </>
  );
}
