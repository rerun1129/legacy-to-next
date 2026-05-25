"use client";

import { useState, useMemo, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2, Pencil } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessModulePort } from "@/lib/ports";
import { accessModuleUseCases } from "@/application/access/module/use-cases";
import { ActionButton } from "@/components/admin/access/action-button";
import { ModuleUpdateModal } from "@/components/admin/access/module-update-modal";
import { GridList } from "@/components/shared/grid-list";
import type { GridColumn } from "@/components/shared/grid-list";
import { ColumnVisibilityMenu } from "@/components/shared/column-visibility-menu";
import type { CreateModuleDto, UpdateModuleDto, ModuleRow } from "@/domain/access/module";

const DEFAULT_CREATE: CreateModuleDto = {
  moduleCode: "",
  name: "",
  description: null,
  sortOrder: null,
  active: true,
};

const MODULE_COLUMNS: GridColumn<ModuleRow>[] = [
  { key: "moduleCode", label: "moduleCode", minWidth: 140 },
  { key: "name", label: "name", minWidth: 120 },
  { key: "description", label: "description", minWidth: 160, render: (v) => (v as string | null) ?? "-" },
  { key: "sortOrder", label: "sortOrder", minWidth: 80, align: "right", render: (v) => (v as number | null) ?? "-" },
  { key: "active", label: "active", minWidth: 70, align: "center", render: (v) => (v ? "활성" : "비활성") },
];

export function AccessModuleListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<ModuleRow | null>(null);
  const [selectedKeys, setSelectedKeys] = useState<Set<string>>(new Set());
  const createForm = useForm<CreateModuleDto>({ defaultValues: DEFAULT_CREATE });

  const { data, isFetching } = useQuery({
    queryKey: ["access-module", "list"],
    queryFn: () => accessModulePort.search(1, 100),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMutation = useMutation({
    mutationFn: (req: CreateModuleDto) => accessModulePort.create(req),
    onSuccess: () => {
      toast.success("모듈이 등록되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-module", "list"] });
      setCreateOpen(false);
      createForm.reset(DEFAULT_CREATE);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ moduleCode, req }: { moduleCode: string; req: UpdateModuleDto }) =>
      accessModulePort.update(moduleCode, req),
    onSuccess: () => {
      toast.success("모듈이 수정되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-module", "list"] });
      setEditTarget(null);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (moduleCode: string) => accessModulePort.delete(moduleCode),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-module", "list"] });
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: (codes: string[]) => accessModuleUseCases.deleteMany(codes),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["access-module", "list"] });
      setSelectedKeys(new Set());
      toast.success("선택한 항목이 삭제되었습니다.");
    },
  });

  const handleDelete = useCallback(async (moduleCode: string) => {
    const ok = await confirm({
      title: "모듈 삭제",
      description: `"${moduleCode}" 모듈을 삭제하시겠습니까?`,
      variant: "destructive",
      confirmText: "삭제",
      cancelText: "취소",
    });
    if (!ok) return;
    deleteMutation.mutate(moduleCode);
  }, [deleteMutation]);

  async function handleBulkDelete() {
    const ok = await confirm({ title: "선택 삭제", description: `선택한 ${selectedKeys.size}개 항목을 삭제하시겠습니까?`, variant: "destructive" });
    if (ok) bulkDeleteMutation.mutate([...selectedKeys]);
  }

  const columns = useMemo<GridColumn<ModuleRow>[]>(() => [
    ...MODULE_COLUMNS,
    {
      key: "_actions",
      label: "",
      minWidth: 70,
      render: (_v, row) => (
        <div style={{ display: "flex", gap: 4 }} onClick={(e) => e.stopPropagation()}>
          <ActionButton buttonCode="BTN_ADMIN_ACCESS_MODULE_UPDATE" className="btn btn--sm" onClick={() => setEditTarget(row)}>
            <Pencil size={12} />
          </ActionButton>
          <ActionButton buttonCode="BTN_ADMIN_ACCESS_MODULE_DELETE" className="btn btn--danger btn--sm" onClick={() => handleDelete(row.moduleCode)} disabled={deleteMutation.isPending}>
            <Trash2 size={12} />
          </ActionButton>
        </div>
      ),
    },
  ], [deleteMutation.isPending, handleDelete]);

  const rows = data?.content ?? [];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginBottom: 8 }}>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_MODULE_DELETE"
          className="btn btn--modal btn--sm"
          disabled={selectedKeys.size === 0 || bulkDeleteMutation.isPending}
          onClick={handleBulkDelete}
        >
          선택 삭제
        </ActionButton>
        <ActionButton
          buttonCode="BTN_ADMIN_ACCESS_MODULE_CREATE"
          className="btn btn--modal btn--sm"
          onClick={() => setCreateOpen(true)}
        >
          <Plus size={12} style={{ marginRight: 4 }} />신규
        </ActionButton>
      </div>

      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Modules</span>
          <span className="panel__rowcount">{rows.length}</span>
          <ColumnVisibilityMenu<ModuleRow> gridId="access-module" defaultColumns={MODULE_COLUMNS} />
        </div>
        <div className="list-wrap">
          <GridList<ModuleRow>
            columns={columns}
            data={rows}
            gridId="access-module"
            rowKey={(r) => r.moduleCode}
            selectable
            selectedKeys={selectedKeys}
            onSelectionChange={(next) => setSelectedKeys(new Set([...next].map(String)))}
            isLoading={isFetching}
            emptyMessage="데이터가 없습니다."
          />
        </div>
      </div>

      {/* 신규 등록 모달 */}
      <ModalShell isOpen={createOpen} title="모듈 등록" size="md">
        <form
          onSubmit={createForm.handleSubmit((v) => createMutation.mutate(v))}
          className="modal__body"
        >
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn">
              <span className="lcn__label">moduleCode</span>
              <input className="text-box text-box--panel" {...createForm.register("moduleCode")} />
            </div>
            <div className="lcn">
              <span className="lcn__label">name</span>
              <input className="text-box text-box--panel" {...createForm.register("name")} />
            </div>
            <div className="lcn">
              <span className="lcn__label">description</span>
              <input
                className="text-box text-box--panel"
                placeholder="선택"
                {...createForm.register("description")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">sortOrder</span>
              <input
                type="number"
                className="text-box text-box--panel"
                placeholder="선택"
                {...createForm.register("sortOrder", {
                  setValueAs: (v) => (v === "" ? null : Number(v)),
                })}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">활성</span>
              <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <input type="checkbox" {...createForm.register("active")} />활성
              </label>
            </div>
          </div>
        </form>
        <div className="modal__footer">
          <Button
            variant="modal"
            size="sm"
            onClick={createForm.handleSubmit((v) => createMutation.mutate(v))}
            loading={createMutation.isPending}
          >
            저장
          </Button>
          <Button
            size="sm"
            onClick={() => { setCreateOpen(false); createForm.reset(DEFAULT_CREATE); }}
          >
            닫기
          </Button>
        </div>
      </ModalShell>

      {/* 수정 모달 (분리된 컴포넌트) */}
      <ModuleUpdateModal
        target={editTarget}
        isPending={updateMutation.isPending}
        onSave={(moduleCode, req) => updateMutation.mutate({ moduleCode, req })}
        onClose={() => setEditTarget(null)}
      />
    </>
  );
}
