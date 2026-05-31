"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Plus, Minus, Save, Search, RotateCcw } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { GridList } from "@/components/shared/grid-list";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { permissionPresetUseCases } from "@/application/access/permission-preset/use-cases";
import type { SavePermissionPresetChangesRequest } from "@/domain/access/permission-preset";
import { PermissionPresetAttributeValuesSection } from "@/components/admin/access/permission-preset-attribute-values-section";
import type { CodeBoxSuggestion } from "@/components/shared/inputs/_types";
import {
  buildPresetColumns,
  getPresetRowClassName,
  type PresetFormRow,
  type PresetFormValues,
} from "./permission-preset-grid-columns";
import {
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toFormRow,
} from "./permission-preset-list-helpers";
import { usePresetPasteHandler } from "./use-preset-paste-handler";
import {
  DEFAULT_PERMISSION_PRESET_FILTER,
  PermissionPresetListFilter,
  type PermissionPresetFilter,
} from "./permission-preset-list-filter";

// ─── 컴포넌트 ────────────────────────────────────────────────────────────────

export function PermissionPresetListClient() {
  const tCols = useTranslations("admin.permissionPreset.cols");
  const tOptions = useTranslations("admin.permissionPreset.options");
  const tPanel = useTranslations("admin.permissionPreset.panel");
  const tMsg = useTranslations("admin.permissionPreset.msg");

  const qc = useQueryClient();

  const filterForm = useForm<PermissionPresetFilter>({
    defaultValues: DEFAULT_PERMISSION_PRESET_FILTER,
  });
  const [activeFilter, setActiveFilter] = useState<PermissionPresetFilter | null>(null);
  const [acQuery, setAcQuery] = useState("");

  const { control, register, getValues, setValue, reset, formState: { isDirty } } =
    useForm<PresetFormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const [presetTargetId, setPresetTargetId] = useState<number | null>(null);

  // ─── 데이터 조회 ─────────────────────────────────────────────────────────

  const { data, isFetching } = useQuery({
    queryKey: ["permission-preset", "list"],
    queryFn: () => permissionPresetUseCases.search({}),
    enabled: activeFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  // ─── Code 자동완성 (BE endpoint) ────────────────────────────────────────

  const { data: acItems = [] } = useQuery({
    queryKey: ["permission-preset", "autocomplete", acQuery],
    queryFn: () => permissionPresetUseCases.autocomplete(acQuery),
    enabled: acQuery.length >= 1,
    staleTime: 30_000,
  });

  const codeSuggestions: CodeBoxSuggestion[] = acItems.map((item) => ({
    code: item.code,
    name: item.name,
  }));

  const handleCodeSelect = useCallback(
    (item: CodeBoxSuggestion) => {
      filterForm.setValue("code", item.code);
    },
    [filterForm],
  );

  // ─── 클라이언트 필터링 (Code prefix-match + Status) ────────────────────

  const filteredData = useMemo(() => {
    if (!data || !activeFilter) return [];
    const codeQ = activeFilter.code.trim().toUpperCase();
    return data.filter((r) => {
      if (codeQ && !r.code.toUpperCase().includes(codeQ)) return false;
      if (activeFilter.status === "ACTIVE" && !r.active) return false;
      if (activeFilter.status === "INACTIVE" && r.active) return false;
      return true;
    });
  }, [data, activeFilter]);

  const originalRows = useMemo<PresetFormRow[]>(
    () => filteredData.map(toFormRow),
    [filteredData],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  // ─── 클립보드 붙여넣기 ──────────────────────────────────────────────────

  usePresetPasteHandler(getValues, setValue);

  // ─── 행 추가/삭제 ────────────────────────────────────────────────────────

  const pendingFocusRef = useRef<number | null>(null);

  function handleAdd() {
    const id = -Date.now();
    append({ entityId: id, code: "", name: "", description: "", active: true });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="code"]`,
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
    if (presetTargetId !== null && removedIds.has(presetTargetId)) {
      setPresetTargetId(null);
    }
  }

  // ─── 더블클릭 → 하단 섹션 토글 ─────────────────────────────────────────

  const handleCodeDoubleClick = useCallback((entityId: number) => {
    // 신규 행(entityId < 0)은 아직 저장되지 않았으므로 attribute section 제외
    if (entityId < 0) return;
    // 더블클릭은 열기 전용 — 닫기 트리거는 Reset/Search/Save 버튼
    setPresetTargetId(entityId);
  }, []);

  // ─── saveChanges ─────────────────────────────────────────────────────────

  const invalidateList = () =>
    qc.invalidateQueries({ queryKey: ["permission-preset", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: (vars: SavePermissionPresetChangesRequest) =>
      permissionPresetUseCases.saveChanges(vars),
    onSuccess: (result) => {
      toast.success(
        tMsg("saveSuccess", { created: result.createdCount, updated: result.updatedCount, deleted: result.deletedCount }),
      );
      setPresetTargetId(null);
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
      deleteIds: changes.deleteIds,
    });
  }, [getValues, originalRows, saveChangesMutation]);

  // ─── 컬럼 ────────────────────────────────────────────────────────────────

  const columns = useMemo(
    () => buildPresetColumns(register, control, tCols, tOptions, handleCodeDoubleClick),
    [register, control, tCols, tOptions, handleCodeDoubleClick],
  );

  // ─── 렌더 ────────────────────────────────────────────────────────────────

  return (
    <>
      {/* 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_PERMISSION_PRESET_RESET"
          className="btn btn--normal btn--sm"
          type="button"
          onClick={() => {
            setActiveFilter(null);
            filterForm.reset(DEFAULT_PERMISSION_PRESET_FILTER);
            reset({ rows: [] });
            setSelectedKeys(new Set());
            setPresetTargetId(null);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_PERMISSION_PRESET_SEARCH"
          className="btn btn--search btn--sm"
          type="button"
          onClick={() =>
            filterForm.handleSubmit((values) => {
              invalidateList();
              setActiveFilter(values);
              setPresetTargetId(null);
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_PERMISSION_PRESET_SAVE"
          className="btn btn--transaction btn--sm"
          type="button"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={handleSave}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      {/* 검색 필터 */}
      <PermissionPresetListFilter
        form={filterForm}
        suggestions={codeSuggestions}
        onCodeSearch={setAcQuery}
        onCodeSelect={handleCodeSelect}
      />

      {/* 그리드 패널 */}
      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", marginTop: 10 }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tPanel("title")}</span>
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
          </div>
        </div>

        <div className="list-wrap">
          <GridList<PresetFormRow>
            columns={columns}
            data={fields as unknown as PresetFormRow[]}
            rowKey={(row) => row.entityId}
            rowClassName={(row) => getPresetRowClassName(row, originalRows)}
            isLoading={isFetching}
            emptyMessage={
              activeFilter === null
                ? tMsg("enterCriteria")
                : tMsg("noResults")
            }
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
          />
        </div>
      </div>

      {/* 더블클릭 시 attribute values 섹션 */}
      {presetTargetId !== null && presetTargetId > 0 && (() => {
        const row = data?.find((r) => r.id === presetTargetId);
        return (
          <PermissionPresetAttributeValuesSection
            key={presetTargetId}
            presetId={presetTargetId}
            presetCode={row?.code ?? String(presetTargetId)}
          />
        );
      })()}
    </>
  );
}
