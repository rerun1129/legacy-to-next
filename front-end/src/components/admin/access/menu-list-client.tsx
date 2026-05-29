"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Minus, Save, Search, RotateCcw, ChevronsUpDown, ChevronsDownUp } from "lucide-react";
import { ActionButton } from "@/components/admin/access/action-button";
import { Button } from "@/components/shared/button";
import { toast } from "@/lib/toast-store";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { accessMenuUseCases } from "@/application/access/menu/use-cases";
import { accessAttributeValueUseCases } from "@/application/access/attribute-value/use-cases";
import type { SaveMenuChangesRequest } from "@/domain/access/menu";
import { MenuTreeView, type MenuTreeHandle } from "@/components/admin/access/menu-tree-view";
import { useListFilterSync } from "@/lib/use-list-filter-sync";
import { listFilterStore } from "@/lib/use-list-filter-store";
import {
  DEFAULT_MENU_FILTER,
  MenuListFilter,
  type MenuFilter,
} from "./menu-list-filter";
import {
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toFormRow,
  type MenuFormRow,
  type MenuFormValues,
} from "./menu-list-helpers";

const MENU_LIST_SCOPE = "/admin/access/menu/list";

// ─── 컴포넌트 ────────────────────────────────────────────────────────────────

export function AccessMenuListClient() {
  const qc = useQueryClient();

  const filterForm = useForm<MenuFilter>({ defaultValues: DEFAULT_MENU_FILTER });

  // 필터 폼 입력값 복원/저장 — 메뉴 이동 후 복귀 시 유지
  useListFilterSync(filterForm, MENU_LIST_SCOPE);

  // activeFilter 영속: 복귀 시 store search 슬롯에서 복원
  const [activeFilter, setActiveFilter] = useState<MenuFilter | null>(
    () =>
      (listFilterStore.getState().getSearch(MENU_LIST_SCOPE)?.extraFilter as MenuFilter | undefined) ??
      null,
  );

  const { control, register, getValues, reset, formState: { isDirty } } =
    useForm<MenuFormValues>({ defaultValues: { rows: [] } });
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());

  // expand/collapse 버튼용 ref
  const treeRef = useRef<MenuTreeHandle>(null);

  // ─── 데이터 조회 ─────────────────────────────────────────────────────────

  const { data, isFetching } = useQuery({
    queryKey: ["access-menu", "list"],
    queryFn: () => accessMenuUseCases.search(1, 100),
    enabled: activeFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
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

  const filteredData = useMemo(() => {
    if (!data || !activeFilter) return [];
    const codeQ = activeFilter.menuCode.trim().toLowerCase();
    const modQ = activeFilter.moduleCode;
    return data.content.filter((r) => {
      if (modQ && r.moduleCode !== modQ) return false;
      if (codeQ && !r.menuCode.toLowerCase().includes(codeQ)) return false;
      if (activeFilter.status === "ACTIVE" && !r.active) return false;
      if (activeFilter.status === "INACTIVE" && r.active) return false;
      return true;
    });
  }, [data, activeFilter]);

  const originalRows = useMemo<MenuFormRow[]>(
    () => filteredData.map(toFormRow),
    [filteredData],
  );

  useEffect(() => {
    reset({ rows: originalRows });
  // reset이 바뀌면 안정성 문제가 없으므로 originalRows만 추적
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [originalRows]);

  // ─── entityId → fieldArray index 맵 ─────────────────────────────────────

  const indexByEntityId = useMemo(() => {
    const map = new Map<number, number>();
    (fields as unknown as MenuFormRow[]).forEach((row, i) => {
      map.set(row.entityId, i);
    });
    return map;
  }, [fields]);

  // ─── 신규 행 추가 ────────────────────────────────────────────────────────

  const pendingFocusRef = useRef<number | null>(null);

  function handleAdd() {
    // 선택된 노드 중 기존(양수 entityId) 하나를 부모로 사용
    const selectedArr = [...selectedKeys];
    const parentEntityId = selectedArr.find((k) => k > 0) ?? null;
    if (parentEntityId === null) return;

    const currentRows = getValues("rows");
    const parentRow = currentRows.find((r) => r.entityId === parentEntityId);
    const newEntityId = -Date.now();

    append({
      entityId: newEntityId,
      id: 0,
      menuCode: "",
      parentId: parentEntityId,
      path: null,
      label: "",
      labelEn: null,
      icon: null,
      sortOrder: null,
      active: true,
      moduleCode: parentRow?.moduleCode ?? (moduleOptions[0]?.value ?? ""),
    });
    pendingFocusRef.current = newEntityId;
  }

  // 신규 행 추가 후 menuCode 셀 포커스
  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      // tree-row 내 첫 번째 input(menuCode TextBox)으로 포커스
      const rows = document.querySelectorAll<HTMLElement>(".tree-row.is-new input:not([type=hidden])");
      if (rows.length > 0) rows[rows.length - 1].focus();
    });
  });

  // ─── 미저장 신규 행 제거 ─────────────────────────────────────────────────

  function handleRemove() {
    if (selectedKeys.size === 0) return;
    const rows = getValues("rows");
    const indices = rows
      .map((r, i) => (selectedKeys.has(r.entityId) && r.entityId < 0 ? i : -1))
      .filter((i) => i !== -1)
      .sort((a, b) => b - a);
    for (const idx of indices) remove(idx);
    setSelectedKeys((prev) => {
      const next = new Set(prev);
      rows.forEach((r) => { if (r.entityId < 0) next.delete(r.entityId); });
      return next;
    });
  }

  function handleRemoveNewRow(entityId: number) {
    const rows = getValues("rows");
    const idx = rows.findIndex((r) => r.entityId === entityId);
    if (idx >= 0) remove(idx);
    setSelectedKeys((prev) => { const next = new Set(prev); next.delete(entityId); return next; });
  }

  // ─── Save ────────────────────────────────────────────────────────────────

  const invalidateList = useCallback(() => {
    qc.invalidateQueries({ queryKey: ["access-menu", "list"] });
    qc.invalidateQueries({ queryKey: ["sidebar-menu", "accessible"] });
  }, [qc]);

  const saveChangesMutation = useMutation({
    mutationFn: (vars: SaveMenuChangesRequest) => accessMenuUseCases.saveChanges(vars),
    onSuccess: (result) => {
      toast.success(
        `저장 완료 — 생성 ${result.createdCount}, 수정 ${result.updatedCount}`,
      );
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
    // deleteIds는 batch payload에 포함하지 않음(하드 삭제 미도입)
    saveChangesMutation.mutate({ creates: changes.creates, updates: changes.updates });
  }, [getValues, originalRows, saveChangesMutation]);

  // ─── 선택 노드 중 양수(기존) entityId가 있을 때 Add 활성 ─────────────────

  const canAdd = useMemo(
    () => [...selectedKeys].some((k) => k > 0),
    [selectedKeys],
  );

  const hasRows = fields.length > 0;

  // ─── 렌더 ────────────────────────────────────────────────────────────────

  const liveRows = fields as unknown as MenuFormRow[];

  return (
    <>
      {/* 툴바 */}
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_MENU_RESET"
          className="btn btn--normal btn--sm"
          type="button"
          onClick={() => {
            filterForm.reset(DEFAULT_MENU_FILTER);
            setActiveFilter(null);
            reset({ rows: [] });
            setSelectedKeys(new Set());
            listFilterStore.getState().clearFilter(MENU_LIST_SCOPE);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_MENU_SEARCH"
          className="btn btn--search btn--sm"
          type="button"
          onClick={() =>
            filterForm.handleSubmit((values) => {
              qc.invalidateQueries({ queryKey: ["access-menu", "list"] });
              setActiveFilter(values);
              listFilterStore.getState().setSearch(MENU_LIST_SCOPE, { extraFilter: values });
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_MENU_SAVE"
          className="btn btn--transaction btn--sm"
          type="button"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={handleSave}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      {/* 검색 필터 */}
      <MenuListFilter form={filterForm} moduleOptions={moduleOptions} />

      {/* 트리 패널 */}
      <div
        className="panel"
        style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column", marginTop: 10 }}
      >
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Menus</span>
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
              aria-label="모두 펴기"
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
              aria-label="모두 접기"
            >
              <ChevronsDownUp size={12} />
            </Button>
            <Button
              variant="success"
              size="sm"
              iconOnly
              type="button"
              disabled={!canAdd}
              onClick={handleAdd}
            >
              <Plus size={12} />
            </Button>
            <Button
              variant="danger"
              size="sm"
              iconOnly
              type="button"
              disabled={[...selectedKeys].every((k) => k > 0)}
              onClick={handleRemove}
            >
              <Minus size={12} />
            </Button>
          </div>
        </div>

        <div className="list-wrap" style={{ overflowY: "auto" }}>
          {isFetching && liveRows.length === 0 ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>
              로딩 중...
            </div>
          ) : activeFilter === null ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>
              Enter search criteria and click Search.
            </div>
          ) : liveRows.length === 0 ? (
            <div style={{ padding: 24, textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>
              No results found.
            </div>
          ) : (
            <MenuTreeView
              ref={treeRef}
              rows={liveRows}
              indexByEntityId={indexByEntityId}
              register={register}
              control={control}
              moduleOptions={moduleOptions}
              selectedKeys={selectedKeys}
              onSelectionChange={setSelectedKeys}
              onRemoveNewRow={handleRemoveNewRow}
            />
          )}
        </div>
      </div>
    </>
  );
}
