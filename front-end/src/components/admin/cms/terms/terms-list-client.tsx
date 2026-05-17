"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { RotateCcw, Search, Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { TermsListFilter } from "./terms-list-filter";
import { TermsListGrid } from "./terms-list-grid";
import { TermsEntryModal } from "./terms-entry-modal";
import type { EntryModalState } from "./terms-entry-modal";
import type { TermsFilter } from "@/domain/terms";

const DEFAULT_VALUES: TermsFilter = {
  type: "ALL",
  scope: "ALL",
  version: "",
  summary: "",
};

export function TermsListClient() {
  const form = useForm<TermsFilter>({ defaultValues: DEFAULT_VALUES });

  const [extraFilter, setExtraFilter] = useState<TermsFilter | null>(null);
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

      <TermsListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <TermsListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
        />
      </div>

      <TermsEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => setEntryModalState(null)}
      />
    </>
  );
}
