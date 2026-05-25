"use client";

import { useState, useMemo, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2, Pencil } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessButtonPort } from "@/lib/ports";
import { accessButtonUseCases } from "@/application/access/button/use-cases";
import { ActionButton } from "@/components/admin/access/action-button";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import type { CreateButtonDto, UpdateButtonDto, ButtonActionType, ButtonRow } from "@/domain/access/button";

const ACTION_TYPES: ButtonActionType[] = ["CREATE", "UPDATE", "DELETE", "EXPORT", "CUSTOM"];

const DEFAULT_FORM: CreateButtonDto = {
  menuId: 0,
  buttonCode: "",
  label: "",
  actionType: "CREATE",
  apiMethod: null,
  apiPath: null,
  sortOrder: null,
  active: true,
};

interface UpdateFormValues {
  menuId: string;
  label: string;
  actionType: ButtonActionType;
  apiMethod: string;
  apiPath: string;
  sortOrder: string;
  active: boolean;
}

const DEFAULT_UPDATE: UpdateFormValues = {
  menuId: "",
  label: "",
  actionType: "CREATE",
  apiMethod: "",
  apiPath: "",
  sortOrder: "",
  active: true,
};

function parseNullableStr(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function parseNullableNum(v: string): number | null {
  if (!v.trim()) return null;
  const n = Number(v);
  return isNaN(n) ? null : n;
}

const BUTTON_COLUMNS: GridColumn<ButtonRow>[] = [
  { key: "id", label: "ID", minWidth: 60 },
  { key: "buttonCode", label: "buttonCode", minWidth: 140 },
  { key: "label", label: "label", minWidth: 120 },
  { key: "menuId", label: "menuId", minWidth: 80 },
  { key: "actionType", label: "actionType", minWidth: 100 },
  { key: "active", label: "active", minWidth: 70, align: "center", render: (v) => (v ? "활성" : "비활성") },
];

export function AccessButtonListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<ButtonRow | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const createForm = useForm<CreateButtonDto>({ defaultValues: DEFAULT_FORM });
  const editForm = useForm<UpdateFormValues>({ defaultValues: DEFAULT_UPDATE });

  const { data, isFetching } = useQuery({
    queryKey: ["access-button", "list"],
    queryFn: () => accessButtonPort.search(1, 100),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMutation = useMutation({
    mutationFn: (req: CreateButtonDto) => accessButtonPort.create(req),
    onSuccess: () => {
      toast.success("버튼이 등록되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-button", "list"] });
      setCreateOpen(false);
      createForm.reset(DEFAULT_FORM);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateButtonDto }) =>
      accessButtonPort.update(id, req),
    onSuccess: () => {
      toast.success("버튼이 수정되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-button", "list"] });
      setEditTarget(null);
      editForm.reset(DEFAULT_UPDATE);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => accessButtonPort.delete(id),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-button", "list"] });
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => accessButtonUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["access-button", "list"] });
      setSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  const openEdit = useCallback((row: ButtonRow) => {
    setEditTarget(row);
    editForm.reset({
      menuId: String(row.menuId),
      label: row.label,
      actionType: row.actionType,
      apiMethod: row.apiMethod ?? "",
      apiPath: row.apiPath ?? "",
      sortOrder: row.sortOrder != null ? String(row.sortOrder) : "",
      active: row.active,
    });
  }, [editForm]);

  function handleEditSave(values: UpdateFormValues) {
    if (!editTarget) return;
    const req: UpdateButtonDto = {
      menuId: Number(values.menuId),
      label: values.label.trim(),
      actionType: values.actionType,
      apiMethod: parseNullableStr(values.apiMethod),
      apiPath: parseNullableStr(values.apiPath),
      sortOrder: parseNullableNum(values.sortOrder),
      active: values.active,
    };
    updateMutation.mutate({ id: editTarget.id, req });
  }

  const handleDelete = useCallback(async (id: number, label: string) => {
    const ok = await confirm({ title: "버튼 삭제", description: `"${label}" 버튼을 삭제하시겠습니까?`, variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteMutation.mutate(id);
  }, [deleteMutation]);

  async function handleBulkDelete() {
    const ok = await confirm({ title: "선택 삭제", description: `선택한 ${selectedKeys.size}개 항목을 삭제하시겠습니까?`, variant: "destructive" });
    if (ok) bulkDeleteMutation.mutate([...selectedKeys]);
  }

  const columns = useMemo<GridColumn<ButtonRow>[]>(() => [
    ...BUTTON_COLUMNS,
    {
      key: "_actions",
      label: "",
      minWidth: 70,
      render: (_v, row) => (
        <div style={{ display: "flex", gap: 4 }} onClick={(e) => e.stopPropagation()}>
          <ActionButton buttonCode="BTN_ADMIN_ACCESS_BUTTON_UPDATE" className="btn btn--sm" onClick={() => openEdit(row)}>
            <Pencil size={12} />
          </ActionButton>
          <ActionButton buttonCode="BTN_ADMIN_ACCESS_BUTTON_DELETE" className="btn btn--danger btn--sm" onClick={() => handleDelete(row.id, row.label)} disabled={deleteMutation.isPending}>
            <Trash2 size={12} />
          </ActionButton>
        </div>
      ),
    },
  ], [deleteMutation.isPending, openEdit, handleDelete]);

  const rows = data?.content ?? [];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_BUTTON_DELETE"
          className="btn btn--modal btn--sm"
          disabled={selectedKeys.size === 0 || bulkDeleteMutation.isPending}
          onClick={handleBulkDelete}
        >
          선택 삭제
        </ActionButton>
        <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setCreateOpen(true)}>신규</Button>
      </div>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Buttons</span>
          <span className="panel__rowcount">{rows.length}</span>
          <ColumnVisibilityMenu<ButtonRow> gridId="access-button" defaultColumns={BUTTON_COLUMNS} />
        </div>
        <div className="list-wrap">
          <GridList<ButtonRow>
            columns={columns}
            data={rows}
            gridId="access-button"
            rowKey={(r) => r.id}
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(Number)))}
            isLoading={isFetching}
            emptyMessage="데이터가 없습니다."
          />
        </div>
      </div>

      {/* 신규 등록 모달 */}
      <ModalShell isOpen={createOpen} title="버튼 등록" size="md">
        <form onSubmit={createForm.handleSubmit((v) => createMutation.mutate(v))} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn"><span className="lcn__label">menuId</span><input type="number" className="text-box text-box--panel" {...createForm.register("menuId", { valueAsNumber: true })} /></div>
            <div className="lcn"><span className="lcn__label">buttonCode</span><input className="text-box text-box--panel" {...createForm.register("buttonCode")} /></div>
            <div className="lcn"><span className="lcn__label">label</span><input className="text-box text-box--panel" {...createForm.register("label")} /></div>
            <div className="lcn"><span className="lcn__label">actionType</span>
              <select className="text-box text-box--panel" {...createForm.register("actionType")}>
                {ACTION_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="lcn"><span className="lcn__label">활성</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...createForm.register("active")} />활성</label></div>
          </div>
        </form>
        <div className="modal__footer">
          <Button variant="modal" size="sm" onClick={createForm.handleSubmit((v) => createMutation.mutate(v))} loading={createMutation.isPending}>저장</Button>
          <Button size="sm" onClick={() => { setCreateOpen(false); createForm.reset(DEFAULT_FORM); }}>닫기</Button>
        </div>
      </ModalShell>

      {/* 수정 모달 */}
      <ModalShell isOpen={editTarget !== null} title="버튼 수정" size="md">
        <form onSubmit={editForm.handleSubmit(handleEditSave)} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn">
              <span className="lcn__label">buttonCode</span>
              <span className="text-box text-box--panel" style={{ background: "var(--surface-2)", color: "var(--ink-3)", display: "inline-flex", alignItems: "center" }}>
                {editTarget?.buttonCode}
              </span>
            </div>
            <div className="lcn"><span className="lcn__label">menuId</span><input type="number" className="text-box text-box--panel" {...editForm.register("menuId")} /></div>
            <div className="lcn"><span className="lcn__label">label</span><input className="text-box text-box--panel" {...editForm.register("label")} /></div>
            <div className="lcn"><span className="lcn__label">actionType</span>
              <select className="text-box text-box--panel" {...editForm.register("actionType")}>
                {ACTION_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="lcn"><span className="lcn__label">apiMethod</span><input className="text-box text-box--panel" placeholder="선택" {...editForm.register("apiMethod")} /></div>
            <div className="lcn"><span className="lcn__label">apiPath</span><input className="text-box text-box--panel" placeholder="선택" {...editForm.register("apiPath")} /></div>
            <div className="lcn"><span className="lcn__label">활성</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...editForm.register("active")} />활성</label></div>
          </div>
        </form>
        <div className="modal__footer">
          <Button variant="modal" size="sm" onClick={editForm.handleSubmit(handleEditSave)} loading={updateMutation.isPending}>저장</Button>
          <Button size="sm" onClick={() => { setEditTarget(null); editForm.reset(DEFAULT_UPDATE); }}>닫기</Button>
        </div>
      </ModalShell>
    </>
  );
}
