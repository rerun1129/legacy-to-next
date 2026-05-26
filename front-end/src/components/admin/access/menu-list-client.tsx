"use client";

import { useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessMenuPort } from "@/lib/ports";
import { accessMenuUseCases } from "@/application/access/menu/use-cases";
import { ActionButton } from "@/components/admin/access/action-button";
import { MenuTreeView } from "@/components/admin/access/menu-tree-view";
import type { CreateMenuDto, UpdateMenuDto, MenuRow } from "@/domain/access/menu";

const DEFAULT_FORM: CreateMenuDto = {
  menuCode: "",
  parentId: null,
  path: null,
  label: "",
  labelEn: null,
  icon: null,
  sortOrder: null,
  active: true,
  moduleCode: "",
};

interface UpdateFormValues {
  parentId: string;
  path: string;
  label: string;
  labelEn: string;
  icon: string;
  sortOrder: string;
  active: boolean;
  moduleCode: string;
}

const DEFAULT_UPDATE: UpdateFormValues = {
  parentId: "",
  path: "",
  label: "",
  labelEn: "",
  icon: "",
  sortOrder: "",
  active: true,
  moduleCode: "",
};

function parseNullableStr(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function parseNullableNum(v: string): number | null {
  if (!v.trim()) return null;
  const n = Number(v);
  return isNaN(n) ? null : n;
}

export function AccessMenuListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<MenuRow | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());
  const createForm = useForm<CreateMenuDto>({ defaultValues: DEFAULT_FORM });
  const editForm = useForm<UpdateFormValues>({ defaultValues: DEFAULT_UPDATE });

  const { data, isFetching } = useQuery({
    queryKey: ["access-menu", "list"],
    queryFn: () => accessMenuPort.search(1, 100),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMutation = useMutation({
    mutationFn: (req: CreateMenuDto) => accessMenuPort.create(req),
    onSuccess: () => {
      toast.success("메뉴가 등록되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-menu", "list"] });
      qc.invalidateQueries({ queryKey: ["sidebar-menu", "accessible"] });
      setCreateOpen(false);
      createForm.reset(DEFAULT_FORM);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateMenuDto }) =>
      accessMenuPort.update(id, req),
    onSuccess: () => {
      toast.success("메뉴가 수정되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-menu", "list"] });
      qc.invalidateQueries({ queryKey: ["sidebar-menu", "accessible"] });
      setEditTarget(null);
      editForm.reset(DEFAULT_UPDATE);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => accessMenuPort.delete(id),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-menu", "list"] });
      qc.invalidateQueries({ queryKey: ["sidebar-menu", "accessible"] });
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => accessMenuUseCases.deleteMany(ids),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["access-menu", "list"] });
      qc.invalidateQueries({ queryKey: ["sidebar-menu", "accessible"] });
      setSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  const openEdit = useCallback((row: MenuRow) => {
    setEditTarget(row);
    editForm.reset({
      parentId: row.parentId != null ? String(row.parentId) : "",
      path: row.path ?? "",
      label: row.label,
      labelEn: row.labelEn ?? "",
      icon: row.icon ?? "",
      sortOrder: row.sortOrder != null ? String(row.sortOrder) : "",
      active: row.active,
      moduleCode: row.moduleCode,
    });
  }, [editForm]);

  function handleEditSave(values: UpdateFormValues) {
    if (!editTarget) return;
    const req: UpdateMenuDto = {
      parentId: parseNullableNum(values.parentId),
      path: parseNullableStr(values.path),
      label: values.label.trim(),
      labelEn: parseNullableStr(values.labelEn),
      icon: parseNullableStr(values.icon),
      sortOrder: parseNullableNum(values.sortOrder),
      active: values.active,
      moduleCode: values.moduleCode.trim(),
    };
    updateMutation.mutate({ id: editTarget.id, req });
  }

  const handleDelete = useCallback(async (id: number, label: string) => {
    const ok = await confirm({ title: "메뉴 삭제", description: `"${label}" 메뉴를 삭제하시겠습니까?`, variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteMutation.mutate(id);
  }, [deleteMutation]);

  async function handleBulkDelete() {
    const ok = await confirm({ title: "선택 삭제", description: `선택한 ${selectedKeys.size}개 항목을 삭제하시겠습니까?`, variant: "destructive" });
    if (ok) bulkDeleteMutation.mutate([...selectedKeys]);
  }

  const rows = data?.content ?? [];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_MENU_DELETE"
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
          <span className="panel__title">Menus</span>
          <span className="panel__rowcount">{rows.length}</span>
        </div>
        <div className="list-wrap" style={{ overflowY: "auto" }}>
          {isFetching && rows.length === 0 ? (
            <div style={{ padding: "24px", textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>로딩 중...</div>
          ) : rows.length === 0 ? (
            <div style={{ padding: "24px", textAlign: "center", color: "var(--ink-4)", fontSize: 13 }}>데이터가 없습니다.</div>
          ) : (
            <MenuTreeView
              rows={rows}
              selectedKeys={selectedKeys}
              deleteIsPending={deleteMutation.isPending}
              onSelectionChange={setSelectedKeys}
              onDoubleClick={openEdit}
              onEdit={openEdit}
              onDelete={handleDelete}
            />
          )}
        </div>
      </div>

      {/* 신규 등록 모달 */}
      <ModalShell isOpen={createOpen} title="메뉴 등록" size="md">
        <form onSubmit={createForm.handleSubmit((v) => createMutation.mutate(v))} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn"><span className="lcn__label">menuCode</span><input className="text-box text-box--panel" {...createForm.register("menuCode")} /></div>
            <div className="lcn"><span className="lcn__label">label</span><input className="text-box text-box--panel" {...createForm.register("label")} /></div>
            <div className="lcn"><span className="lcn__label">moduleCode</span><input className="text-box text-box--panel" {...createForm.register("moduleCode")} /></div>
            <div className="lcn"><span className="lcn__label">path</span><input className="text-box text-box--panel" placeholder="선택" {...createForm.register("path")} /></div>
            <div className="lcn"><span className="lcn__label">활성</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...createForm.register("active")} />활성</label></div>
          </div>
        </form>
        <div className="modal__footer">
          <Button variant="modal" size="sm" onClick={createForm.handleSubmit((v) => createMutation.mutate(v))} loading={createMutation.isPending}>저장</Button>
          <Button size="sm" onClick={() => { setCreateOpen(false); createForm.reset(DEFAULT_FORM); }}>닫기</Button>
        </div>
      </ModalShell>

      {/* 수정 모달 */}
      <ModalShell isOpen={editTarget !== null} title="메뉴 수정" size="md">
        <form onSubmit={editForm.handleSubmit(handleEditSave)} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn">
              <span className="lcn__label">menuCode</span>
              <span className="text-box text-box--panel" style={{ background: "var(--surface-2)", color: "var(--ink-3)", display: "inline-flex", alignItems: "center" }}>
                {editTarget?.menuCode}
              </span>
            </div>
            <div className="lcn"><span className="lcn__label">label</span><input className="text-box text-box--panel" {...editForm.register("label")} /></div>
            <div className="lcn"><span className="lcn__label">moduleCode</span><input className="text-box text-box--panel" {...editForm.register("moduleCode")} /></div>
            <div className="lcn"><span className="lcn__label">path</span><input className="text-box text-box--panel" placeholder="선택" {...editForm.register("path")} /></div>
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
