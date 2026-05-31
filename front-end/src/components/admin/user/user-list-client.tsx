"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useForm, useFieldArray } from "react-hook-form";
import { useQueryClient, useQuery, useMutation } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { RotateCcw, Search, Plus, Minus, Save } from "lucide-react";
import { listFilterStore, type SavedSearchState } from "@/lib/use-list-filter-store";
import { DEFAULT_PAGE_SIZE } from "@/lib/grid-pagination";
import { ActionButton } from "@/components/admin/access/action-button";
import { UserListFilter } from "./user-list-filter";
import { GridList } from "@/components/shared/grid-list";
import { Pagination } from "@/components/shared/pagination";
import { Button } from "@/components/shared/button";
import type { UserFilter } from "@/domain/user";
import { userUseCases } from "@/application/user/use-cases";
import { teamUseCases } from "@/application/team/use-cases";
import { accessAttributeValueUseCases } from "@/application/access/attribute-value/use-cases";
import { collectGridChanges } from "@/lib/collect-grid-changes";
import { toast } from "@/lib/toast-store";
import {
  buildUserColumns,
  getUserRowClassName,
  type UserFormRow,
  type FormValues,
} from "./user-grid-columns";
import {
  PASTE_COLS,
  ROW_IS_EQUAL,
  TO_CREATE,
  TO_UPDATE,
  toFormRow,
} from "./user-list-helpers";
import { UserPermissionPresetsSection } from "./user-permission-presets-section";


const DEFAULT_FILTER: UserFilter = {
  username: "",
  scope: "ALL",
};

const SCOPE = "/admin/user/list";

type UserSearchState = SavedSearchState & { extraFilter: UserFilter | null };

export function UserListClient() {
  const tMsg = useTranslations("admin.user.msg");
  const tPanel = useTranslations("admin.user.panel");
  const tCols = useTranslations("admin.user.cols");
  const tOptions = useTranslations("admin.user.options");

  const filterForm = useForm<UserFilter>({ defaultValues: DEFAULT_FILTER });
  const qc = useQueryClient();

  const [extraFilter, setExtraFilter] = useState<UserFilter | null>(() => {
    const s = listFilterStore.getState().getSearch(SCOPE) as UserSearchState | undefined;
    return s?.extraFilter ?? null;
  });
  const [currentPage, setCurrentPage] = useState(() => {
    const s = listFilterStore.getState().getSearch(SCOPE);
    return s?.currentPage ?? 1;
  });

  useEffect(() => {
    listFilterStore.getState().setSearch(SCOPE, { extraFilter, currentPage });
  }, [extraFilter, currentPage]);

  const methods = useForm<FormValues>({ defaultValues: { rows: [] } });
  const { control, register, getValues, setValue, reset, formState: { isDirty } } = methods;
  const { fields, append, remove } = useFieldArray({ control, name: "rows" });

  const { data, isFetching } = useQuery({
    queryKey: ["admin-user", "list", extraFilter, currentPage],
    queryFn: () => userUseCases.search(extraFilter!, currentPage, DEFAULT_PAGE_SIZE),
    enabled: extraFilter !== null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
    structuralSharing: false,
  });

  const { data: moduleValues = [] } = useQuery({
    queryKey: ["admin-access-attribute-value", "module"],
    queryFn: () => accessAttributeValueUseCases.listByKey("module"),
    staleTime: Infinity,
  });

  const { data: teamsRaw } = useQuery({
    queryKey: ["admin-team", "list-all"],
    queryFn: () => teamUseCases.listAll(),
    staleTime: Infinity,
  });
  // ?? [] 인라인 시 매 렌더 새 참조 → useMemo 안정화
  const teams = useMemo(() => teamsRaw ?? [], [teamsRaw]);

  const moduleValueOptions = useMemo(
    () => moduleValues.map((v) => ({ value: v.value, label: v.label ?? v.value })),
    [moduleValues],
  );

  const originalRows = useMemo<UserFormRow[]>(
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
  const [presetTargetUserId, setPresetTargetUserId] = useState<number | null>(null);

  const handleUsernameDoubleClick = useCallback((entityId: number) => {
    // 신규 행(entityId < 0)은 아직 저장되지 않았으므로 preset 노출 대상 제외
    if (entityId < 0) return;
    // 더블클릭은 열기 전용 — 닫기 트리거는 Reset/Search/Save 버튼
    setPresetTargetUserId(entityId);
  }, []);

  const pendingFocusRef = useRef<number | null>(null);

  function handleAdd() {
    const id = -Date.now();
    append({
      entityId: id,
      username: "",
      email: "",
      password: "",
      role: "USER",
      modules: "",
      active: true,
      teamId: null,
      _originalAttributes: {},
    });
    pendingFocusRef.current = id;
  }

  useEffect(() => {
    if (pendingFocusRef.current === null) return;
    const key = pendingFocusRef.current;
    pendingFocusRef.current = null;
    requestAnimationFrame(() => {
      const td = document.querySelector(
        `td[data-row-key="${key}"][data-col-key="username"]`,
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

  const invalidateList = () => qc.invalidateQueries({ queryKey: ["admin-user", "list"] });

  const saveChangesMutation = useMutation({
    mutationFn: () => {
      const liveRows = getValues("rows");
      const changes = collectGridChanges(originalRows, liveRows, {
        rowKey: (r) => r.entityId,
        toCreate: TO_CREATE,
        toUpdate: TO_UPDATE,
        isEqual: ROW_IS_EQUAL,
      });
      return userUseCases.saveChanges({
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
      setPresetTargetUserId(null);
      invalidateList();
    },
  });

  const columns = useMemo(
    () => buildUserColumns(register, control, moduleValueOptions, tCols, tOptions, handleUsernameDoubleClick, teams),
    [register, control, moduleValueOptions, tCols, tOptions, handleUsernameDoubleClick, teams],
  );

  const totalPages = data?.totalPages ?? 0;

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_USER_LIST_RESET"
          className="btn btn--normal btn--sm"
          onClick={() => {
            filterForm.reset(DEFAULT_FILTER);
            invalidateList();
            setExtraFilter(null);
            setCurrentPage(1);
            setPresetTargetUserId(null);
          }}
          icon={<RotateCcw size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_USER_LIST_SEARCH"
          className="btn btn--search btn--sm"
          onClick={() =>
            filterForm.handleSubmit((values) => {
              invalidateList();
              setExtraFilter(values);
              setCurrentPage(1);
              setPresetTargetUserId(null);
            })()
          }
          icon={<Search size={12} style={{ marginRight: 4 }} />}
        />
        <ActionButton
          buttonCode="BTN_ADMIN_USER_LIST_SAVE"
          className="btn btn--transaction btn--sm"
          disabled={!isDirty || saveChangesMutation.isPending}
          onClick={() => saveChangesMutation.mutate()}
          icon={<Save size={12} style={{ marginRight: 4 }} />}
        />
      </div>

      <UserListFilter form={filterForm} />

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
          <GridList<UserFormRow>
            columns={columns}
            data={fields as unknown as UserFormRow[]}
            rowKey={(row) => row.entityId}
            rowClassName={(row) => getUserRowClassName(row, originalRows)}
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

      {presetTargetUserId !== null && (() => {
        const userRow = data?.content.find((r) => r.id === presetTargetUserId);
        return <UserPermissionPresetsSection key={presetTargetUserId} userId={presetTargetUserId} username={userRow?.username} />;
      })()}
    </>
  );
}
