"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { RotateCcw, Search, Plus, Minus, Save } from "lucide-react";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { DEFAULT_PAGE_SIZE } from "@/lib/grid-pagination";
import { ActionButton } from "@/components/admin/access/action-button";
import { PackageUnitListFilter } from "./package-unit-list-filter";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { Button } from "@/components/shared/button";
import type { PackageUnitFilter } from "@/domain/code/package-unit";
import { packageUnitUseCases } from "@/application/code/package-unit/use-cases";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { toast } from "@/lib/toast-store";
import {
  buildPackageUnitColumns,
  getPackageUnitRowClassName,
  type PackageUnitFormRow,
  type FormValues,
} from "./package-unit-grid-columns";

const DEFAULT_FILTER: PackageUnitFilter = {
  packageCode: "",
  name: "",
  scope: "ALL",
};

const SCOPE = "/admin/code/package/list";
const PASTE_COLS = ["packageCode", "name", "nameEn", "active"] as const;

type PackageUnitSearchState = SavedSearchState & { extraFilter: PackageUnitFilter | null };

const ROW_IS_EQUAL = (a: PackageUnitFormRow, b: PackageUnitFormRow) =>
  a.name === b.name && a.nameEn === b.nameEn && a.active === b.active;

const TO_CREATE = (row: PackageUnitFormRow) => ({
  packageCode: row.packageCode,
  name: row.name,
  nameEn: row.nameEn || null,
  active: row.active,
});

const TO_UPDATE = (row: PackageUnitFormRow) => ({
  id: row.entityId,
  name: row.name,
  nameEn: row.nameEn || null,
  active: row.active,
});

export function PackageUnitListClient() {
  const tMsg = useTranslations("admin.packageUnit.msg");
  const tPanel = useTranslations("admin.packageUnit.panel");
  const tCols = useTranslations("admin.packageUnit.cols");
  const tOptions = useTranslations("admin.packageUnit.options");

  const filterForm = useForm<PackageUnitFilter>({ defaultValues: DEFAULT_FILTER });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<PackageUnitFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as PackageUnitSearchState | undefined;
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
    queryKey: ["admin-code-package-unit", "list", extraFilter, currentPage],
    queryFn: () => packageUnitUseCases.search(extraFilter!, currentPage, DEFAULT_PAGE_SIZE),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const originalRows = useMemo<PackageUnitFormRow[]>(
    () => (data?.content ?? []).map((row) => ({
      entityId: row.id,
      packageCode: row.packageCode,
      name: row.name ?? "",
      nameEn: row.nameEn ?? "",
      active: row.active,
    })),
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
    append({ entityId: id, packageCode: "", name: "", nameEn: "", active: true });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="packageCode"]`,
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

  const invalidateList = () => qc.invalidateQueries({ queryKey: ["admin-code-package-unit", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate: TO_CREATE,
        toUpdate: TO_UPDATE,
        isEqual: ROW_IS_EQUAL,
      });
      return packageUnitUseCases.saveChanges({
        creates: changes.creates,
        updates: changes.updates,
        deleteIds: changes.deleteIds,
      });
    },
    onSuccess: (result) => {
      toast.success(
        tMsg("saveSuccess", {
          created: result.createdCount,
          updated: result.updatedCount,
          deleted: result.deletedCount,
        }),
      );
      invalidateList();
    },
  });

  const columns = useMemo(
    () => buildPackageUnitColumns(register, control, tCols, tOptions),
    [register, control, tCols, tOptions],
  );

  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_PACKAGE_RESET"
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
          buttonCode="BTN_ADMIN_CODE_PACKAGE_SEARCH"
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
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_PACKAGE_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={() => saveChangesMutation.mutate()}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      <PackageUnitListFilter form={filterForm} />

      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", marginTop: 10 }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tPanel("title")}</span>
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
          <GridList<PackageUnitFormRow>
            columns={columns}
            data={fields as unknown as PackageUnitFormRow[]}
            rowKey={(row) => row.entityId}
            rowClassName={(row) => getPackageUnitRowClassName(row, originalRows)}
            isLoading={isFetching}
            emptyMessage={
              extraFilter === null
                ? tMsg("enterCriteria")
                : tMsg("noResults")
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
