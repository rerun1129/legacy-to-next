"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { RotateCcw, Search, Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { CodeListFilter } from "./code-list-filter";
import { CodeListGrid } from "./code-list-grid";
import { CodeEntryModal } from "./code-entry-modal";
import type { EntryModalState } from "./code-entry-modal";
import type { CodeFilter } from "@/domain/code";

const DEFAULT_VALUES: CodeFilter = {
  codeGroup: "",
  codeValue: "",
  codeLabel: "",
  active: "ALL",
};

export function CodeListClient() {
  const form = useForm<CodeFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<CodeFilter | null>(null);
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

      <CodeListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <CodeListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
        />
      </div>

      <CodeEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => {
          qc.invalidateQueries({ queryKey: ["admin-code", "list"] });
          setEntryModalState(null);
        }}
      />
    </>
  );
}
