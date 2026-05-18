"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQueryClient } from "@tanstack/react-query";
import { RotateCcw, Search, Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { UserListFilter } from "./user-list-filter";
import { UserListGrid } from "./user-list-grid";
import { UserEntryModal } from "./user-entry-modal";
import type { EntryModalState } from "./user-entry-modal";
import type { UserFilter } from "@/domain/user";

const DEFAULT_VALUES: UserFilter = {
  username: "",
  scope: "ALL",
};

export function UserListClient() {
  const form = useForm<UserFilter>({ defaultValues: DEFAULT_VALUES });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<UserFilter | null>(null);
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
        <ActionButton
          buttonCode="BTN_ADMIN_USER_LIST_CREATE"
          className="btn btn--modal btn--sm"
          onClick={() => setEntryModalState({ mode: "create" })}
        >
          <Plus size={12} style={{ marginRight: 4 }} />신규
        </ActionButton>
      </div>

      <UserListFilter form={form} />

      <div style={{ flex: 1, overflow: "auto", marginTop: 10, display: "flex", flexDirection: "column" }}>
        <UserListGrid
          extraFilter={extraFilter}
          currentPage={currentPage}
          onPageChange={setCurrentPage}
          onRowDoubleClick={(id) => setEntryModalState({ mode: "edit", id })}
        />
      </div>

      <UserEntryModal
        state={entryModalState}
        onClose={() => setEntryModalState(null)}
        onSaved={() => {
          qc.invalidateQueries({ queryKey: ["admin-user", "list"] });
          setEntryModalState(null);
        }}
      />
    </>
  );
}
