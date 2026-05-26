"use client";

import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { RotateCcw, Search, Plus } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { CarrierListFilter } from "./carrier-list-filter";
import { CarrierListGrid } from "./carrier-list-grid";
import { CarrierEntryModal } from "./carrier-entry-modal";
import type { EntryModalState } from "./carrier-entry-modal";
import type { CarrierFilter } from "@/domain/code/carrier";
import { carrierUseCases } from "@/application/code/carrier/use-cases";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";

const DEFAULT_VALUES: CarrierFilter = {
  carrierCode: "",
  name: "",
  carrierType: "ALL",
  scope: "ALL",
};

const SCOPE = "/admin/code/carrier/list";

type CarrierSearchState = SavedSearchState & { extraFilter: CarrierFilter | null };

export function CarrierListClient() {
  const form = useForm<CarrierFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<CarrierFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as CarrierSearchState | undefined;
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
    mutationFn: (ids: number[]) => carrierUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-carrier", "list"] });
      setSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_CARRIER_RESET"
          className="btn btn--normal btn--sm"
          onClick={() => {
            form.reset(DEFAULT_VALUES);
            setExtraFilter(null);
            setCurrentPage(1);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_CARRIER_SEARCH"
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
          buttonCode="BTN_ADMIN_CODE_CARRIER_DELETE"
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
          buttonCode="BTN_ADMIN_CODE_CARRIER_CREATE"
          className="btn btn--modal btn--sm"
          onClick={() => setEntryModalState({ mode: "create" })}
          icon={<Plus size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      <CarrierListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <CarrierListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
          selectedKeys={selectedKeys}
          onSelectionChange={setSelectedKeys}
        />
      </div>

      <CarrierEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => {
          qc.invalidateQueries({ queryKey: ["admin-code-carrier", "list"] });
          setEntryModalState(null);
        }}
      />
    </>
  );
}
