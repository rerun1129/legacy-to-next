"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { RotateCcw, Search, Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { CustomerListFilter } from "./customer-list-filter";
import { CustomerListGrid } from "./customer-list-grid";
import { CustomerEntryModal } from "./customer-entry-modal";
import type { EntryModalState } from "./customer-entry-modal";
import type { CustomerFilter } from "@/domain/customer";

const DEFAULT_VALUES: CustomerFilter = {
  customerCode: "",
  name: "",
  customerType: "ALL",
  scope: "ALL",
};

export function CustomerListClient() {
  const form = useForm<CustomerFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<CustomerFilter | null>(null);
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

      <CustomerListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <CustomerListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
        />
      </div>

      <CustomerEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => {
          qc.invalidateQueries({ queryKey: ["admin-customer", "list"] });
          setEntryModalState(null);
        }}
      />
    </>
  );
}
