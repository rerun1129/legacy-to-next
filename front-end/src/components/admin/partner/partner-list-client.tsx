"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { RotateCcw, Search, Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { PartnerListFilter } from "./partner-list-filter";
import { PartnerListGrid } from "./partner-list-grid";
import { PartnerEntryModal } from "./partner-entry-modal";
import type { EntryModalState } from "./partner-entry-modal";
import type { PartnerFilter } from "@/domain/partner";

const DEFAULT_VALUES: PartnerFilter = {
  partnerCode: "",
  name: "",
  partnerType: "ALL",
  scope: "ALL",
};

export function PartnerListClient() {
  const form = useForm<PartnerFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<PartnerFilter | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [entryModalState, setEntryModalState] = useState<EntryModalState | null>(null);

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <Button
          size="sm"
          variant="normal"
          leftIcon={<RotateCcw size={12} />}
          onClick={() => {
            form.reset(DEFAULT_VALUES);
            setExtraFilter(null);
            setCurrentPage(1);
          }}
        >
          Reset
        </Button>
        <Button
          size="sm"
          variant="search"
          leftIcon={<Search size={12} />}
          onClick={() =>
            form.handleSubmit((values) => {
              setExtraFilter(values);
              setCurrentPage(1);
            })()
          }
        >
          Search
        </Button>
        <Button
          size="sm"
          variant="modal"
          leftIcon={<Plus size={12} />}
          onClick={() => setEntryModalState({ mode: "create" })}
        >
          신규
        </Button>
      </div>

      <PartnerListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <PartnerListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
        />
      </div>

      <PartnerEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => {
          qc.invalidateQueries({ queryKey: ["admin-partner", "list"] });
          setEntryModalState(null);
        }}
      />
    </>
  );
}
