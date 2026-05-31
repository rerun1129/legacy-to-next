"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Plus, Minus, Save } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { DEFAULT_PAGE_SIZE } from "@/lib/grid-pagination";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { codeDetailUseCases } from "@/application/code-detail/use-cases";
import {
  buildCodeDetailColumns,
  getCodeDetailRowClassName,
  type CodeDetailFormRow,
  type CodeDetailFormValues,
} from "./code-detail-grid-columns";
import {
  DETAIL_PASTE_COLS,
  DETAIL_ROW_IS_EQUAL,
  DETAIL_TO_UPDATE,
  toDetailFormRow,
} from "./code-detail-list-helpers";

interface Props {
  masterId: number | null;
  onDirtyChange: (dirty: boolean) => void;
}

export function CodeDetailListGrid({ masterId, onDirtyChange }: Props) {
  const tMsg = useTranslations("admin.code.detail.msg");
  const tPanel = useTranslations("admin.code.detail.panel");
  const tCols = useTranslations("admin.code.detail.cols");
  const tOptions = useTranslations("admin.code.detail.options");
  const qc = useQueryClient();
  const [detailPage, setDetailPage] = useState(1);

  const { control, register, getValues, setValue, reset, formState: { isDirty } } =
    useForm<CodeDetailFormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  // isDirty 상향 통지
  useEffect(() => {
    onDirtyChange(isDirty);
  }, [isDirty, onDirtyChange]);

  const enabled = masterId !== null && masterId > 0;

  const { data, isFetching } = useQuery({
    queryKey: ["admin-code-detail", "list", masterId, detailPage],
    queryFn: () => codeDetailUseCases.search(masterId!, detailPage, DEFAULT_PAGE_SIZE),
    enabled,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const originalRows = useMemo<CodeDetailFormRow[]>(
    () => (data?.content ?? []).map(toDetailFormRow),
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
      const startColIdx = DETAIL_PASTE_COLS.indexOf(startColKey as typeof DETAIL_PASTE_COLS[number]);
      if (startRowIdx === -1 || startColIdx === -1) return;

      e.preventDefault();
      const pastedRows = text.split(/\r?\n/).filter((l) => l.length > 0).map((l) => l.split("\t"));

      for (let ri = 0; ri < pastedRows.length; ri++) {
        const rowIdx = startRowIdx + ri;
        if (rowIdx >= rows.length) break;
        for (let ci = 0; ci < pastedRows[ri].length; ci++) {
          const colIdx = startColIdx + ci;
          if (colIdx >= DETAIL_PASTE_COLS.length) break;
          const col = DETAIL_PASTE_COLS[colIdx];
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
    append({ entityId: id, codeValue: "", codeLabel: "", sortOrder: null, active: true, remark: "" });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="codeValue"]`,
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
    qc.invalidateQueries({ queryKey: ["admin-code-detail", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      // toCreate는 masterId 클로저 필요하므로 내부 인라인 정의
      const toCreate = (row: CodeDetailFormRow) => ({
        masterId: masterId!,
        codeValue: row.codeValue,
        codeLabel: row.codeLabel,
        sortOrder: row.sortOrder,
        active: row.active,
        remark: row.remark || null,
      });
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate,
        toUpdate: DETAIL_TO_UPDATE,
        isEqual: DETAIL_ROW_IS_EQUAL,
      });
      return codeDetailUseCases.saveChanges({
        masterId: masterId!,
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
    () => buildCodeDetailColumns(register, control, tCols, tOptions),
    [register, control, tCols, tOptions],
  );

  const isSaveDisabled = !isDirty || saveChangesMutation.isPending || !enabled;
  const isAddDisabled = masterId === null || masterId <= 0;

  // masterId === null: 마스터 미선택
  if (masterId === null) {
    return (
      <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
        <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
          <div className="panel__head">
            <div className="panel__title-accent" />
            <span className="panel__title">{tPanel("title")}</span>
          </div>
          <div
            className="list-wrap"
            style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1 }}
          >
            <span style={{ color: "var(--ink-3)" }}>{tMsg("selectMaster")}</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0 }}>
      {/* 패널별 Save/Add/Remove 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_CODE_LIST_DETAIL_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={isSaveDisabled}
          onClick={() => saveChangesMutation.mutate()}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
        <Button variant="success" size="sm" iconOnly onClick={handleAdd} disabled={isAddDisabled}>
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
          <span className="panel__title">{tPanel("title")}</span>
          <span className="panel__rowcount">{fields.length}</span>
        </div>
        <div className="list-wrap">
          {/* masterId < 0: 마스터 미저장 — 빈 그리드 + 안내 */}
          {masterId < 0 ? (
            <div
              style={{ display: "flex", alignItems: "center", justifyContent: "center", flex: 1, padding: 24 }}
            >
              <span style={{ color: "var(--ink-3)" }}>{tMsg("saveMasterFirst")}</span>
            </div>
          ) : (
            <GridList<CodeDetailFormRow>
              columns={columns}
              data={fields as unknown as CodeDetailFormRow[]}
              rowKey={(row) => row.entityId}
              rowClassName={(row) => getCodeDetailRowClassName(row, originalRows)}
              isLoading={isFetching}
              emptyMessage={tMsg("noResults")}
              selectable
              selectedKeys={selectedKeys}
              onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
            />
          )}
        </div>
        <Pagination
          currentPage={detailPage}
          totalPages={data?.totalPages ?? 0}
          onPageChange={setDetailPage}
          disabled={isFetching}
        />
      </div>
    </div>
  );
}
