"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { Plus, Minus, Save, Search, RotateCcw } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { GridList } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { accessAttributeUseCases } from "@/application/access/attribute/use-cases";
import type { SaveAttributeDefinitionChangesRequest } from "@/domain/access/attribute";
import { AttributeValueSection } from "@/components/admin/access/attribute-value-section";
import {
  buildAttributeColumns,
  getAttributeRowClassName,
  type AttributeFormRow,
  type AttributeFormValues,
} from "./attribute-grid-columns";
import {
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toFormRow,
} from "./attribute-list-helpers";
import { useAttributePasteHandler } from "./use-attribute-paste-handler";
import {
  DEFAULT_ATTRIBUTE_FILTER,
  AttributeListFilter,
  type AttributeFilter,
} from "./attribute-list-filter";

// ─── 컴포넌트 ────────────────────────────────────────────────────────────────

export function AccessAttributeListClient() {
  const qc = useQueryClient();

  const filterForm = useForm<AttributeFilter>({
    defaultValues: DEFAULT_ATTRIBUTE_FILTER,
  });
  const [activeFilter, setActiveFilter] = useState<AttributeFilter | null>(null);

  const { control, register, getValues, setValue, reset, formState: { isDirty } } =
    useForm<AttributeFormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const [drillTargetKey, setDrillTargetKey] = useState<string | null>(null);

  // ─── 데이터 조회 ─────────────────────────────────────────────────────────

  const { data, isFetching } = useQuery({
    queryKey: ["access-attribute", "list"],
    queryFn: () => accessAttributeUseCases.search(1, 100),
    enabled: activeFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  // ─── 클라이언트 필터링 (attributeKey prefix-match + Status) ───────────

  const filteredData = useMemo(() => {
    if (!data || !activeFilter) return [];
    const keyQ = activeFilter.attributeKey.trim().toLowerCase();
    return data.content.filter((r) => {
      if (keyQ && !r.attributeKey.toLowerCase().includes(keyQ)) return false;
      if (activeFilter.status === "ACTIVE" && !r.active) return false;
      if (activeFilter.status === "INACTIVE" && r.active) return false;
      return true;
    });
  }, [data, activeFilter]);

  const originalRows = useMemo<AttributeFormRow[]>(
    () => filteredData.map(toFormRow),
    [filteredData],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  // ─── 클립보드 붙여넣기 ──────────────────────────────────────────────────

  useAttributePasteHandler(getValues, setValue);

  // ─── 행 추가/삭제 ────────────────────────────────────────────────────────

  const pendingFocusRef = useRef<number | null>(null);

  function handleAdd() {
    const id = -Date.now();
    append({ entityId: id, attributeKey: "", name: "", valueType: "STRING", allowMulti: false, active: true });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="attributeKey"]`,
      ) as HTMLElement | null;
      const input = td?.querySelector("input:not([type=hidden])") as HTMLInputElement | null;
      input?.focus();
    });
  });

  function handleRemove() {
    if (selectedKeys.size === 0) return;
    const rows = getValues("rows");
    const removedIds = new Set<number>();
    const indices = rows
      .map((r, i) => {
        if (selectedKeys.has(r.entityId)) {
          removedIds.add(r.entityId);
          return i;
        }
        return -1;
      })
      .filter((i) => i !== -1)
      .sort((a, b) => b - a);
    for (const idx of indices) remove(idx);
    setSelectedKeys(new Set());
    if (drillTargetKey !== null) {
      // 삭제된 행의 attributeKey 가 drill target이면 닫기
      const removed = originalRows.filter((r) => removedIds.has(r.entityId));
      if (removed.some((r) => r.attributeKey === drillTargetKey)) {
        setDrillTargetKey(null);
      }
    }
  }

  // ─── 더블클릭 → 하단 값 섹션 토글 ──────────────────────────────────────

  const handleKeyDoubleClick = useCallback((_entityId: number, allowMulti: boolean) => {
    if (!allowMulti) return;
    const rows = getValues("rows");
    const row = rows.find((r) => r.entityId === _entityId);
    if (!row || row.entityId < 0) return;
    setDrillTargetKey(row.attributeKey);
  }, [getValues]);

  // ─── saveChanges ─────────────────────────────────────────────────────────

  const invalidateList = () =>
    qc.invalidateQueries({ queryKey: ["access-attribute", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: (vars: SaveAttributeDefinitionChangesRequest) =>
      accessAttributeUseCases.saveChanges(vars),
    onSuccess: (result) => {
      toast.success(
        `저장 완료 — 생성 ${result.createdCount}, 수정 ${result.updatedCount}, 삭제 ${result.deletedCount}`,
      );
      setDrillTargetKey(null);
      invalidateList();
    },
  });

  const handleSave = useCallback(() => {
    const liveRows = getValues("rows");
    const changes = collectGridChanges(originalRows, liveRows, {
      rowKey: (r) => r.entityId,
      toCreate: TO_CREATE,
      toUpdate: TO_UPDATE,
      isEqual: ROW_IS_EQUAL,
    });
    saveChangesMutation.mutate({
      creates: changes.creates,
      updates: changes.updates,
      deleteKeys: changes.deleteIds
        .map((id) => originalRows.find((r) => r.entityId === id)?.attributeKey)
        .filter((k): k is string => typeof k === "string" && k.length > 0),
    });
  }, [getValues, originalRows, saveChangesMutation]);

  // ─── 컬럼 ────────────────────────────────────────────────────────────────

  const columns = useMemo(
    () => buildAttributeColumns(register, control, handleKeyDoubleClick),
    [register, control, handleKeyDoubleClick],
  );

  // ─── 렌더 ────────────────────────────────────────────────────────────────

  return (
    <>
      {/* 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_ATTRIBUTE_RESET"
          className="btn btn--normal btn--sm"
          onClick={() => {
            filterForm.reset(DEFAULT_ATTRIBUTE_FILTER);
            setActiveFilter(null);
            reset({ rows: [] });
            setSelectedKeys(new Set());
            setDrillTargetKey(null);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_ATTRIBUTE_SEARCH"
          className="btn btn--search btn--sm"
          onClick={() =>
            filterForm.handleSubmit((values) => {
              invalidateList();
              setActiveFilter(values);
              setDrillTargetKey(null);
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_ATTRIBUTE_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={handleSave}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      {/* 검색 필터 */}
      <AttributeListFilter form={filterForm} />

      {/* 그리드 패널 */}
      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", marginTop: 10 }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Attributes</span>
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
          <GridList<AttributeFormRow>
            columns={columns}
            data={fields as unknown as AttributeFormRow[]}
            rowKey={(row) => row.entityId}
            rowClassName={(row) => getAttributeRowClassName(row, originalRows)}
            isLoading={isFetching}
            emptyMessage={
              activeFilter === null
                ? "Enter search criteria and click Search."
                : "No results found."
            }
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
          />
        </div>
      </div>

      {/* allowMulti 행 더블클릭 시 attribute-value 인라인 그리드 노출 */}
      {drillTargetKey !== null && (
        <AttributeValueSection
          key={drillTargetKey}
          attributeKey={drillTargetKey}
        />
      )}
    </>
  );
}
