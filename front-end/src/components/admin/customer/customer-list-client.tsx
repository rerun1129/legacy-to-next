"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { RotateCcw, Search, Plus, Minus, Save } from "lucide-react";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { ActionButton } from "@/components/admin/access/action-button";
import { CustomerListFilter } from "./customer-list-filter";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { Button } from "@/components/shared/button";
import type { CustomerFilter } from "@/domain/customer";
import { customerUseCases } from "@/application/customer/use-cases";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { toast } from "@/lib/toast-store";
import {
  buildCustomerColumns,
  getCustomerRowClassName,
  type CustomerFormRow,
  type FormValues,
} from "./customer-grid-columns";
import {
  VALID_CUSTOMER_TYPES,
  PASTE_COLS,
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toFormRow,
} from "./customer-list-helpers";

const DEFAULT_FILTER: CustomerFilter = {
  customerCode: "",
  name: "",
  customerType: "ALL",
  scope: "ALL",
};

const SCOPE = "/admin/customer/list";

type CustomerSearchState = SavedSearchState & { extraFilter: CustomerFilter | null };

export function CustomerListClient() {
  const filterForm = useForm<CustomerFilter>({ defaultValues: DEFAULT_FILTER });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<CustomerFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as CustomerSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.currentPage ?? 1;
  });

  useEffect(() => {
    listFilterStore.getState().setSearch(SCOPE, { extraFilter, currentPage });
  }, [extraFilter, currentPage]);

  const { control, register, getValues, setValue, reset, formState: { isDirty } } = useForm<FormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const { data, isFetching } = useQuery({
    queryKey: ["admin-customer", "list", extraFilter, currentPage],
    queryFn: () => customerUseCases.search(extraFilter!, currentPage, 50),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const originalRows = useMemo<CustomerFormRow[]>(
    () => (data?.content ?? []).map(toFormRow),
    [data],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  useEffect(() => {
    function handlePaste(e: ClipboardEvent) {
      const active = document.activeElement as HTMLElement | null;
      const td = active?.closest("td[data-row-key][data-col-key]") as HTMLElement | null;
      if (!td) return;
      const text = e.clipboardData?.getData("text/plain");
      if (!text) return;

      const rows = getValues("rows");
      const startRowKey = td.dataset.rowKey!;
      const startColKey = td.dataset.colKey!;
      const startRowIdx = rows.findIndex((r) => String(r.entityId) === startRowKey);
      const startColIdx = PASTE_COLS.indexOf(startColKey as typeof PASTE_COLS[number]);
      if (startRowIdx === -1 || startColIdx === -1) return;

      e.preventDefault();
      const pastedRows = text.split(/\r?\n/).filter((l) => l.length > 0).map((l) => l.split("\t"));

      for (let ri = 0; ri < pastedRows.length; ri++) {
        const rowIdx = startRowIdx + ri;
        if (rowIdx >= rows.length) break;
        for (let ci = 0; ci < pastedRows[ri].length; ci++) {
          const colIdx = startColIdx + ci;
          if (colIdx >= PASTE_COLS.length) break;
          const col = PASTE_COLS[colIdx];
          const val = pastedRows[ri][ci];
          if (col === "active") {
            setValue(`rows.${rowIdx}.active`, val === "true" || val === "Active", { shouldDirty: true });
          } else if (col === "customerType") {
            const type = VALID_CUSTOMER_TYPES.find((t) => t === val) ?? "OTHER";
            setValue(`rows.${rowIdx}.customerType`, type, { shouldDirty: true });
          } else {
            setValue(`rows.${rowIdx}.${col}`, val, { shouldDirty: true });
          }
        }
      }
    }

    document.addEventListener("paste", handlePaste);
    return () => document.removeEventListener("paste", handlePaste);
  }, [getValues, setValue]);

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const pendingFocusRef = useRef<number | null>(null);

  function handleAdd() {
    const id = -Date.now();
    append({
      entityId: id,
      customerCode: "",
      customerType: "CUSTOMER",
      name: "",
      nameEn: "",
      businessNo: "",
      representative: "",
      phone: "",
      email: "",
      customerLocalAddress: "",
      customerEnglishAddress: "",
      memo: "",
      countryCode: "",
      active: true,
    });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="customerCode"]`,
      ) as HTMLElement | null;
      const input = td?.querySelector("input:not([type=hidden])") as HTMLInputElement | null;
      input?.focus();
    });
  });

  function handleRemove() {
    if (selectedKeys.size === 0) return;
    const rows = getValues("rows");
    const indices = rows
      .map((r, i) => (selectedKeys.has(r.entityId) ? i : -1))
      .filter((i) => i !== -1)
      .sort((a, b) => b - a);
    for (const idx of indices) remove(idx);
    setSelectedKeys(new Set());
  }

  const invalidateList = () => qc.invalidateQueries({ queryKey: ["admin-customer", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate: TO_CREATE,
        toUpdate: TO_UPDATE,
        isEqual: ROW_IS_EQUAL,
      });
      return customerUseCases.saveChanges({
        creates: changes.creates,
        updates: changes.updates,
        deleteIds: changes.deleteIds,
      });
    },
    onSuccess: (result) => {
      toast.success(
        `저장 완료 — 생성 ${result.createdCount}, 수정 ${result.updatedCount}, 삭제 ${result.deletedCount}`
      );
      invalidateList();
    },
  });

  const columns = useMemo(
    () => buildCustomerColumns(register, control),
    [register, control]
  );

  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CUSTOMER_RESET"
          className="btn btn--normal btn--sm"
          onClick={() => {
            filterForm.reset(DEFAULT_FILTER);
            invalidateList();
            setExtraFilter(null);
            setCurrentPage(1);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_CUSTOMER_SEARCH"
          className="btn btn--search btn--sm"
          onClick={() =>
            filterForm.handleSubmit((values) => {
              invalidateList();
              setExtraFilter(values);
              setCurrentPage(1);
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <Button
          variant="transaction"
          size="sm"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={() => saveChangesMutation.mutate()}
        >
          <Save size={12} style={{ marginRight: 4 }} />
          Save
        </Button>
      </div>

      <CustomerListFilter form={filterForm} />

      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", marginTop: 10 }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">고객 관리</span>
          <span className="panel__rowcount">{fields.length}</span>
          <div className="panel__actions">
            <Button variant="success" size="sm" iconOnly onClick={handleAdd}>
              <Plus size={12} />
            </Button>
            <Button
              variant="danger"
              size="sm"
              iconOnly
              onClick={handleRemove}
              disabled={selectedKeys.size === 0}
            >
              <Minus size={12} />
            </Button>
          </div>
        </div>

        <div className="list-wrap">
          <GridList<CustomerFormRow>
            columns={columns}
            data={fields as unknown as CustomerFormRow[]}
            rowKey={(row) => row.entityId}
            rowClassName={(row) => getCustomerRowClassName(row, originalRows)}
            isLoading={isFetching}
            emptyMessage={
              extraFilter === null
                ? "Enter search criteria and click Search."
                : "No results found."
            }
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
          />
        </div>

        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
          disabled={isFetching}
        />
      </div>
    </>
  );
}
