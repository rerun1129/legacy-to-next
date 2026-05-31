"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { Plus, Minus, Save } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { DEFAULT_PAGE_SIZE } from "@/lib/grid-pagination";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { codeMasterUseCases } from "@/application/code-master/use-cases";
import type { CodeMasterFilter } from "@/domain/code-master";
import {
  buildCodeMasterColumns,
  getCodeMasterRowClassName,
  type CodeMasterFormRow,
  type CodeMasterFormValues,
} from "./code-master-grid-columns";
import {
  MASTER_PASTE_COLS,
  MASTER_ROW_IS_EQUAL,
  MASTER_TO_CREATE,
  MASTER_TO_UPDATE,
  toMasterFormRow,
} from "./code-master-list-helpers";

interface Props {
  submittedFilter: CodeMasterFilter | null;
  masterPage: number;
  onMasterPageChange: (p: number) => void;
  selectedMasterId: number | null;
  onSelectMaster: (id: number) => void;
}

export function CodeMasterListGrid({
  submittedFilter,
  masterPage,
  onMasterPageChange,
  selectedMasterId,
  onSelectMaster,
}: Props) {
  const qc = useQueryClient();

  const { control, register, getValues, setValue, reset, formState: { isDirty } } =
    useForm<CodeMasterFormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const { data, isFetching } = useQuery({
    queryKey: ["admin-code-master", "list", submittedFilter, masterPage],
    queryFn: () => codeMasterUseCases.search(submittedFilter!, masterPage, DEFAULT_PAGE_SIZE),
    enabled: submittedFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const originalRows = useMemo<CodeMasterFormRow[]>(
    () => (data?.content ?? []).map(toMasterFormRow),
    [data],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  // 클립보드 붙여넣기
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
      const startColIdx = MASTER_PASTE_COLS.indexOf(startColKey as typeof MASTER_PASTE_COLS[number]);
      if (startRowIdx === -1 || startColIdx === -1) return;

      e.preventDefault();
      const pastedRows = text.split(/\r?\n/).filter((l) => l.length > 0).map((l) => l.split("\t"));

      for (let ri = 0; ri < pastedRows.length; ri++) {
        const rowIdx = startRowIdx + ri;
        if (rowIdx >= rows.length) break;
        for (let ci = 0; ci < pastedRows[ri].length; ci++) {
          const colIdx = startColIdx + ci;
          if (colIdx >= MASTER_PASTE_COLS.length) break;
          const col = MASTER_PASTE_COLS[colIdx];
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
    append({ entityId: id, masterCode: "", masterName: "", description: "", sortOrder: null, active: true });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="masterCode"]`,
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

  const invalidateList = () =>
    qc.invalidateQueries({ queryKey: ["admin-code-master", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate: MASTER_TO_CREATE,
        toUpdate: MASTER_TO_UPDATE,
        isEqual: MASTER_ROW_IS_EQUAL,
      });
      return codeMasterUseCases.saveChanges({
        creates: changes.creates,
        updates: changes.updates,
        deleteIds: changes.deleteIds,
      });
    },
    onSuccess: (result) => {
      toast.success(
        `저장 완료 — 생성 ${result.createdCount}, 수정 ${result.updatedCount}, 삭제 ${result.deletedCount}`,
      );
      invalidateList();
    },
  });

  const columns = useMemo(
    () => buildCodeMasterColumns(register, control),
    [register, control],
  );

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      {/* 패널별 Save/Add/Remove 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_LIST_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={() => saveChangesMutation.mutate()}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
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

      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Common Code</span>
          <span className="panel__rowcount">{fields.length}</span>
        </div>
        <div className="list-wrap">
          <GridList<CodeMasterFormRow>
            columns={columns}
            data={fields as unknown as CodeMasterFormRow[]}
            rowKey={(row) => row.entityId}
            onRowClick={(row) => onSelectMaster(row.entityId)}
            rowClassName={(row) => getCodeMasterRowClassName(row, originalRows, selectedMasterId)}
            isLoading={isFetching}
            emptyMessage={
              submittedFilter === null
                ? "Enter search criteria and click Search."
                : "No results found."
            }
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
          />
        </div>
        <Pagination
          currentPage={masterPage}
          totalPages={data?.totalPages ?? 0}
          onPageChange={onMasterPageChange}
          disabled={isFetching}
        />
      </div>
    </div>
  );
}
