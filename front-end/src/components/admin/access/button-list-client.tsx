"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { Save, Search, RotateCcw, ChevronsUpDown, ChevronsDownUp } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { accessButtonUseCases } from "@/application/access/button/use-cases";
import { accessAttributeValueUseCases } from "@/application/access/attribute-value/use-cases";
import { accessMenuPort } from "@/lib/ports";
import type { SaveButtonChangesRequest } from "@/domain/access/button";
import { ButtonTreeView, type ButtonTreeHandle } from "@/components/admin/access/button-tree-view";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { listFilterStore } from "@/lib/use-list-filter-store";
import {
  DEFAULT_BUTTON_FILTER,
  ButtonListFilter,
  type ButtonFilter,
} from "./button-list-filter";
import {
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toFormRow,
  type ButtonFormRow,
  type ButtonFormValues,
} from "./button-list-helpers";
import { useButtonFilter } from "./use-button-filter";

const BUTTON_LIST_SCOPE = "/admin/access/button/list";

// ─── 컴포넌트 ────────────────────────────────────────────────────────────────

export function AccessButtonListClient() {
  const tMsg = useTranslations("admin.button.msg");
  const tPanel = useTranslations("admin.button.panel");

  const qc = useQueryClient();

  const filterForm = useForm<ButtonFilter>({ defaultValues: DEFAULT_BUTTON_FILTER });

  // 필터 폼 입력값 복원/저장 — 메뉴 이동 후 복귀 시 유지
  useListFilterSync(filterForm, BUTTON_LIST_SCOPE);

  // activeFilter 영속: 복귀 시 store search 슬롯에서 복원
  const [activeFilter, setActiveFilter] = useState<ButtonFilter | null>(
    () =>
      (listFilterStore.getState().getSearch(BUTTON_LIST_SCOPE)?.extraFilter as ButtonFilter | undefined) ??
      null,
  );

  const { control, register, getValues, reset, formState: { isDirty } } =
    useForm<ButtonFormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  // expand/collapse 버튼용 ref
  const treeRef = useRef<ButtonTreeHandle>(null);

  // ─── 데이터 조회 ─────────────────────────────────────────────────────────

  const { data: buttonData, isFetching } = useQuery({
    queryKey: ["access-button", "list"],
    queryFn: () => accessButtonUseCases.search(1, 200),
    enabled: activeFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  // 메뉴 데이터: 트리 구조용, 항상 fetch
  const { data: menuData } = useQuery({
    queryKey: ["access-menu", "list"],
    queryFn: () => accessMenuPort.search(1, 200),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const { data: moduleData } = useQuery({
    queryKey: ["admin-access-attribute-value", "module"],
    queryFn: () => accessAttributeValueUseCases.listByKey("module"),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });
  const moduleOptions = useMemo(
    () => (moduleData ?? []).map((v) => ({ value: v.value, label: v.label ?? v.value })),
    [moduleData],
  );

  // ─── 클라이언트 필터링 ────────────────────────────────────────────────────

  const buttonContent = useMemo(() => buttonData?.content ?? [], [buttonData]);
  const menuContent = useMemo(() => menuData?.content ?? [], [menuData]);
  const { filteredButtons, filteredMenus } = useButtonFilter(
    buttonContent,
    menuContent,
    activeFilter,
  );

  const originalRows = useMemo<ButtonFormRow[]>(
    () => filteredButtons.map(toFormRow),
    [filteredButtons],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // reset이 바뀌면 안정성 문제가 없으므로 originalRows만 추적
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  // ─── entityId → fieldArray index 맵 ─────────────────────────────────────

  const indexByEntityId = useMemo(() => {
    const map = new Map<number, number>();
    (fields as unknown as ButtonFormRow[]).forEach((row, i) => {
      map.set(row.entityId, i);
    });
    return map;
  }, [fields]);

  // ─── 신규 버튼 추가 ──────────────────────────────────────────────────────

  const pendingFocusRef = useRef<number | null>(null);

  function handleAddButton(menuId: number) {
    const newEntityId = -Date.now();
    append({
      entityId: newEntityId,
      id: 0,
      buttonCode: "",
      menuId,
      label: "",
      actionType: "CUSTOM",
      apiMethod: null,
      apiPath: null,
      sortOrder: null,
      active: true,
    });
    pendingFocusRef.current = newEntityId;
  }

  // 신규 버튼 행 추가 후 buttonCode 셀 포커스
  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const rows = document.querySelectorAll<HTMLElement>(".tree-row.is-new input:not([type=hidden])");
      if (rows.length > 0) rows[rows.length - 1].focus();
    });
  });

  // ─── 신규 행 제거 ────────────────────────────────────────────────────────

  function handleRemoveNewRow(entityId: number) {
    const rows = getValues("rows");
    const idx = rows.findIndex((r) => r.entityId === entityId);
    if (idx >= 0) remove(idx);
  }

  // ─── Save ────────────────────────────────────────────────────────────────

  const saveChangesMutation = useMutation({
    mutationFn: (vars: SaveButtonChangesRequest) => accessButtonUseCases.saveChanges(vars),
    onSuccess: (result) => {
      toast.success(
        tMsg("saveSuccess", { created: result.createdCount, updated: result.updatedCount }),
      );
      qc.invalidateQueries({ queryKey: ["access-button", "list"] });
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
    // deleteIds는 payload에 포함하지 않음 (하드삭제 미도입)
    saveChangesMutation.mutate({ creates: changes.creates, updates: changes.updates });
  }, [getValues, originalRows, saveChangesMutation]);

  const hasRows = fields.length > 0;

  // ─── 렌더 ────────────────────────────────────────────────────────────────

  const liveRows = fields as unknown as ButtonFormRow[];

  return (
    <>
      {/* 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_BUTTON_RESET"
          className="btn btn--normal btn--sm"
          type="button"
          onClick={() => {
            filterForm.reset(DEFAULT_BUTTON_FILTER);
            setActiveFilter(null);
            reset({ rows: [] });
            listFilterStore.getState().clearFilter(BUTTON_LIST_SCOPE);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_BUTTON_SEARCH"
          className="btn btn--search btn--sm"
          type="button"
          onClick={() =>
            filterForm.handleSubmit((values) => {
              qc.invalidateQueries({ queryKey: ["access-button", "list"] });
              setActiveFilter(values);
              listFilterStore.getState().setSearch(BUTTON_LIST_SCOPE, { extraFilter: values });
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_BUTTON_SAVE"
          className="btn btn--transaction btn--sm"
          type="button"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={handleSave}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      {/* 검색 필터 */}
      <ButtonListFilter form={filterForm} moduleOptions={moduleOptions} />

      {/* 트리 패널 */}
      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", marginTop: 10 }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">{tPanel("title")}</span>
          <span className="panel__rowcount">{liveRows.length}</span>
          <div className="panel__actions">
            {/* 모두 펴기 / 모두 접기 */}
            <Button
              variant="normal"
              size="sm"
              iconOnly
              type="button"
              disabled={!hasRows}
              onClick={() => treeRef.current?.expandAll()}
              aria-label={tMsg("expandAll")}
            >
              <ChevronsUpDown size={12} />
            </Button>
            <Button
              variant="normal"
              size="sm"
              iconOnly
              type="button"
              disabled={!hasRows}
              onClick={() => treeRef.current?.collapseAll()}
              aria-label={tMsg("collapseAll")}
            >
              <ChevronsDownUp size={12} />
            </Button>
          </div>
        </div>

        <div className="list-wrap" style={{ overflowY: "auto" }}>
          {isFetching && liveRows.length === 0 ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>
              {tMsg("loading")}
            </div>
          ) : activeFilter === null ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>
              {tMsg("enterCriteria")}
            </div>
          ) : liveRows.length === 0 ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>
              {tMsg("noResults")}
            </div>
          ) : (
            <ButtonTreeView
              ref={treeRef}
              menus={filteredMenus}
              rows={liveRows}
              indexByEntityId={indexByEntityId}
              register={register}
              control={control}
              onAddButton={handleAddButton}
              onRemoveNewRow={handleRemoveNewRow}
            />
          )}
        </div>
      </div>
    </>
  );
}
