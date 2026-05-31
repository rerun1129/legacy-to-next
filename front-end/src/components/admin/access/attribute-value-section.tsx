"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Plus, Minus, Save } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { Button } from "@/components/shared/button";
import { GridList } from "@/components/shared/grid-list";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { accessAttributeValueUseCases } from "@/application/access/attribute-value/use-cases";
import type { SaveAttributeValueChangesRequest } from "@/domain/access/attribute-value";
import {
  buildAttributeValueColumns,
  getAttributeValueRowClassName,
  type AttributeValueFormRow,
  type AttributeValueFormValues,
} from "./attribute-value-grid-columns";
import {
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toValueFormRow,
} from "./attribute-value-list-helpers";

interface Props {
  attributeKey: string;
}

export function AttributeValueSection({ attributeKey }: Props) {
  // useTranslations MUST be called unconditionally before any early return
  const tSection = useTranslations("admin.attribute.section");
  const tValueCols = useTranslations("admin.attribute.valueCols");
  const tOptions = useTranslations("admin.attribute.options");
  const tMsg = useTranslations("admin.attribute.msg");

  const qc = useQueryClient();

  const { control, register, getValues, reset, formState: { isDirty } } =
    useForm<AttributeValueFormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());

  // ─── 데이터 조회 ─────────────────────────────────────────────────────────

  const { data, isFetching } = useQuery({
    queryKey: ["access-attribute-value", attributeKey],
    queryFn: () => accessAttributeValueUseCases.listByKey(attributeKey),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const originalRows = useMemo<AttributeValueFormRow[]>(
    () => (data ?? []).map(toValueFormRow),
    [data],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  // ─── 행 추가/삭제 ────────────────────────────────────────────────────────

  const pendingFocusRef = useRef<number | null>(null);

  function handleAdd() {
    const id = -Date.now();
    append({ entityId: id, value: "", label: "", sortOrder: null, active: true });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="value"]`,
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

  // ─── saveChanges ─────────────────────────────────────────────────────────

  const invalidateList = useCallback(
    () => qc.invalidateQueries({ queryKey: ["access-attribute-value", attributeKey] }),
    [qc, attributeKey],
  );

  const saveChangesMutation = useMutation({
    mutationFn: (vars: SaveAttributeValueChangesRequest) =>
      accessAttributeValueUseCases.saveChanges(vars),
    onSuccess: (result) => {
      toast.success(
        tMsg("saveSuccess", { created: result.createdCount, updated: result.updatedCount, deleted: result.deletedCount }),
      );
      invalidateList();
    },
  });

  const handleSave = useCallback(() => {
    const liveRows = getValues("rows");
    const changes = collectGridChanges(originalRows, liveRows, {
      rowKey: (r) => r.entityId,
      toCreate: (r) => TO_CREATE(r, attributeKey),
      toUpdate: TO_UPDATE,
      isEqual: ROW_IS_EQUAL,
    });
    saveChangesMutation.mutate({
      attributeKey,
      creates: changes.creates,
      updates: changes.updates,
      deleteIds: changes.deleteIds,
    });
  }, [attributeKey, getValues, originalRows, saveChangesMutation]);

  // ─── 컬럼 ────────────────────────────────────────────────────────────────

  const columns = useMemo(
    () => buildAttributeValueColumns(register, control, tValueCols, tOptions),
    [register, control, tValueCols, tOptions],
  );

  // ─── 렌더 ────────────────────────────────────────────────────────────────

  return (
    <div
      className="panel"
      style={{ marginTop: 16, display: "flex", flexDirection: "column" }}
    >
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">{tSection("title", { attributeKey })}</span>
        <span className="panel__rowcount">{fields.length}</span>
        <div className="panel__actions">
          <Button variant="success" size="sm" iconOnly type="button" onClick={handleAdd}>
            <Plus size={12} />
          </Button>
          <Button
            variant="danger"
            size="sm"
            iconOnly
            type="button"
            onClick={handleRemove}
            disabled={selectedKeys.size === 0}
          >
            <Minus size={12} />
          </Button>
          <ActionButton
            buttonCode="BTN_ADMIN_ACCESS_ATTRIBUTE_VALUE_SAVE"
            className="btn btn--transaction btn--sm"
            type="button"
            disabled={!isDirty || saveChangesMutation.isPending}
            onClick={handleSave}
            icon={<Save size={12} style={{ marginRight: 4 }} />}
          />
        </div>
      </div>

      <div className="list-wrap">
        <GridList<AttributeValueFormRow>
          columns={columns}
          data={fields as unknown as AttributeValueFormRow[]}
          rowKey={(row) => row.entityId}
          rowClassName={(row) => getAttributeValueRowClassName(row, originalRows)}
          isLoading={isFetching}
          emptyMessage={tMsg("noValues")}
          selectable
          selectedKeys={selectedKeys}
          onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
        />
      </div>
    </div>
  );
}
