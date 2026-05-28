"use client";

import { useState, useMemo, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2, Pencil, Search } from "lucide-react";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { permissionPresetPort } from "@/lib/ports";
import { ActionButton } from "@/components/admin/access/action-button";
import { PermissionPresetAttributeValuesSection } from "@/components/admin/access/permission-preset-attribute-values-section";
import {
  PermissionPresetCreateModal,
  PermissionPresetUpdateModal,
} from "@/components/admin/access/permission-preset-modals";
import type { CreateFormValues, UpdateFormValues } from "@/components/admin/access/permission-preset-modals";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import { ApiError } from "@/adapter/out/api/errors";
import type {
  PermissionPresetSummary,
} from "@/domain/access/permission-preset";

// ─── 검색 폼 ─────────────────────────────────────────────────────────────────

interface SearchFormValues {
  code: string;
  name: string;
  activeOnly: "" | "true" | "false";
}

const DEFAULT_SEARCH: SearchFormValues = { code: "", name: "", activeOnly: "" };
const DEFAULT_CREATE: CreateFormValues = { code: "", name: "", description: "", active: true };
const DEFAULT_UPDATE: UpdateFormValues = { name: "", description: "", active: true };

// ─── 그리드 컬럼 ─────────────────────────────────────────────────────────────

const BASE_COLUMNS: GridColumn<PermissionPresetSummary>[] = [
  { key: "code", label: "code", minWidth: 160 },
  { key: "name", label: "name", minWidth: 160 },
  { key: "description", label: "description", minWidth: 200, render: (v) => (v as string | null) ?? "-" },
  { key: "active", label: "active", minWidth: 70, align: "center", render: (v) => (v ? "활성" : "비활성") },
  { key: "attributeValueIds", label: "value count", minWidth: 90, align: "right", render: (v) => (v as number[]).length },
];

// ─── 컴포넌트 ─────────────────────────────────────────────────────────────────

export function PermissionPresetListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<PermissionPresetSummary | null>(null);
  const [selectedPreset, setSelectedPreset] = useState<PermissionPresetSummary | null>(null);
  const [searchCriteria, setSearchCriteria] = useState<SearchFormValues>(DEFAULT_SEARCH);

  const searchForm = useForm<SearchFormValues>({ defaultValues: DEFAULT_SEARCH });
  const createForm = useForm<CreateFormValues>({ defaultValues: DEFAULT_CREATE });
  const editForm = useForm<UpdateFormValues>({ defaultValues: DEFAULT_UPDATE });

  const { data, isFetching } = useQuery({
    queryKey: ["permission-preset", "list", searchCriteria],
    queryFn: () =>
      permissionPresetPort.search({
        code: searchCriteria.code.trim() || undefined,
        name: searchCriteria.name.trim() || undefined,
        activeOnly:
          searchCriteria.activeOnly === "true"
            ? true
            : searchCriteria.activeOnly === "false"
              ? false
              : undefined,
      }),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMutation = useMutation({
    mutationFn: (cmd: CreateFormValues) =>
      permissionPresetPort.create({
        code: cmd.code.trim(),
        name: cmd.name.trim(),
        description: cmd.description.trim() || undefined,
        active: cmd.active,
      }),
    onSuccess: () => {
      toast.success("Permission Preset이 등록되었습니다.");
      qc.invalidateQueries({ queryKey: ["permission-preset", "list"] });
      setCreateOpen(false);
      createForm.reset(DEFAULT_CREATE);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, cmd }: { id: number; cmd: UpdateFormValues }) =>
      permissionPresetPort.update(id, {
        name: cmd.name.trim(),
        description: cmd.description.trim() || undefined,
        active: cmd.active,
      }),
    onSuccess: () => {
      toast.success("Permission Preset이 수정되었습니다.");
      qc.invalidateQueries({ queryKey: ["permission-preset", "list"] });
      setEditTarget(null);
      editForm.reset(DEFAULT_UPDATE);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => permissionPresetPort.delete(id),
    onSuccess: (_data, id) => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["permission-preset", "list"] });
      if (selectedPreset?.id === id) setSelectedPreset(null);
    },
    onError: (e) => {
      if (e instanceof ApiError && e.statusCode === 409) {
        toast.error("이 프리셋은 사용 중인 user가 있어 삭제할 수 없습니다. User 화면에서 먼저 해제하세요.");
      } else {
        toast.error("삭제 중 오류가 발생했습니다.");
      }
    },
  });

  const openEdit = useCallback(
    (row: PermissionPresetSummary) => {
      setEditTarget(row);
      editForm.reset({ name: row.name, description: row.description ?? "", active: row.active });
    },
    [editForm],
  );

  const handleDelete = useCallback(
    async (row: PermissionPresetSummary) => {
      const ok = await confirm({
        title: "Preset 삭제",
        description: `"${row.code}" 프리셋을 삭제하시겠습니까?`,
        variant: "destructive",
        confirmText: "삭제",
        cancelText: "취소",
      });
      if (!ok) return;
      deleteMutation.mutate(row.id);
    },
    [deleteMutation],
  );

  const handleRowInteract = useCallback((row: PermissionPresetSummary) => {
    setSelectedPreset((prev) => (prev?.id === row.id ? null : row));
  }, []);

  const columns = useMemo<GridColumn<PermissionPresetSummary>[]>(
    () => [
      ...BASE_COLUMNS,
      {
        key: "_actions",
        label: "",
        minWidth: 70,
        render: (_v, row) => (
          <div style={{ display: "flex", gap: 4 }} onClick={(e) => e.stopPropagation()}>
            <ActionButton
              buttonCode="BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE"
              className="btn btn--sm"
              onClick={(e) => { e.stopPropagation(); openEdit(row); }}
            >
              <Pencil size={12} />
            </ActionButton>
            <ActionButton
              buttonCode="BTN_ADMIN_ACCESS_PERMISSION_PRESET_DELETE"
              className="btn btn--danger btn--sm"
              onClick={(e) => { e.stopPropagation(); handleDelete(row); }}
              disabled={deleteMutation.isPending}
            >
              <Trash2 size={12} />
            </ActionButton>
          </div>
        ),
      },
    ],
    [openEdit, handleDelete, deleteMutation.isPending],
  );

  const rows = data ?? [];

  return (
    <>
      {/* 검색 필터 */}
      <form onSubmit={searchForm.handleSubmit(setSearchCriteria)}>
        <div style={{ display: "flex", gap: 8, marginBottom: 8, flexWrap: "wrap", alignItems: "center" }}>
          <input
            className="text-box text-box--panel"
            placeholder="code prefix"
            style={{ width: 160 }}
            {...searchForm.register("code")}
          />
          <input
            className="text-box text-box--panel"
            placeholder="name keyword"
            style={{ width: 200 }}
            {...searchForm.register("name")}
          />
          <select className="text-box text-box--panel" style={{ width: 120 }} {...searchForm.register("activeOnly")}>
            <option value="">전체</option>
            <option value="true">활성만</option>
            <option value="false">비활성만</option>
          </select>
          <Button type="submit" size="sm" leftIcon={<Search size={12} />}>검색</Button>
          <div style={{ marginLeft: "auto" }}>
            <ActionButton
              buttonCode="BTN_ADMIN_ACCESS_PERMISSION_PRESET_CREATE"
              className="btn btn--modal btn--sm"
              onClick={() => setCreateOpen(true)}
            >
              <Plus size={12} /> 신규
            </ActionButton>
          </div>
        </div>
      </form>

      {/* 그리드 */}
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Permission Presets</span>
          <span className="panel__rowcount">{rows.length}</span>
          <ColumnVisibilityMenu<PermissionPresetSummary>
            gridId="access-permission-preset"
            defaultColumns={BASE_COLUMNS}
          />
        </div>
        <div className="list-wrap">
          <GridList<PermissionPresetSummary>
            columns={columns}
            data={rows}
            gridId="access-permission-preset"
            rowKey={(row) => String(row.id)}
            rowClassName={(row) => (selectedPreset?.id === row.id ? "is-selected" : undefined)}
            isLoading={isFetching}
            emptyMessage="데이터가 없습니다."
            onRowClick={handleRowInteract}
          />
        </div>
      </div>

      {/* 행 선택/더블클릭 시 attribute_value 섹션 */}
      {selectedPreset !== null && (
        <PermissionPresetAttributeValuesSection
          presetId={selectedPreset.id}
          presetCode={selectedPreset.code}
        />
      )}

      {/* 신규 / 수정 모달 */}
      <PermissionPresetCreateModal
        isOpen={createOpen}
        form={createForm}
        onSave={(v) => createMutation.mutate(v)}
        onClose={() => { setCreateOpen(false); createForm.reset(DEFAULT_CREATE); }}
        isPending={createMutation.isPending}
      />
      <PermissionPresetUpdateModal
        editTarget={editTarget}
        form={editForm}
        onSave={(v) => {
          if (!editTarget) return;
          updateMutation.mutate({ id: editTarget.id, cmd: v });
        }}
        onClose={() => { setEditTarget(null); editForm.reset(DEFAULT_UPDATE); }}
        isPending={updateMutation.isPending}
      />
    </>
  );
}
