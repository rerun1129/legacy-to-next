"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessMenuPort } from "@/lib/ports";
import type { CreateMenuDto } from "@/domain/access/menu";

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

export function AccessMenuListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const form = useForm<CreateMenuDto>({ defaultValues: DEFAULT_FORM });

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
      setCreateOpen(false);
      form.reset(DEFAULT_FORM);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => accessMenuPort.delete(id),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-menu", "list"] });
    },
  });

  async function handleDelete(id: number, label: string) {
    const ok = await confirm({ title: "메뉴 삭제", description: `"${label}" 메뉴를 삭제하시겠습니까?`, variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteMutation.mutate(id);
  }

  const rows = data?.content ?? [];

  return (
    <>
      <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 8 }}>
        <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setCreateOpen(true)}>신규</Button>
      </div>
      <div className="panel" style={{ flex: 1, minHeight: 0, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Menu 목록</span>
          <span className="panel__rowcount">{rows.length}</span>
        </div>
        <div className="list-wrap">
          {isFetching ? <div style={{ padding: 16, color: "var(--ink-3)" }}>로딩 중...</div> : (
            <table className="grid" style={{ width: "100%" }}>
              <thead><tr><th>ID</th><th>menuCode</th><th>label</th><th>moduleCode</th><th>path</th><th>active</th><th></th></tr></thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.menuCode}</td>
                    <td>{r.label}</td>
                    <td>{r.moduleCode}</td>
                    <td>{r.path ?? "-"}</td>
                    <td style={{ textAlign: "center" }}>{r.active ? "활성" : "비활성"}</td>
                    <td>
                      <button className="btn btn--danger btn--sm" onClick={() => handleDelete(r.id, r.label)} disabled={deleteMutation.isPending}>
                        <Trash2 size={12} />
                      </button>
                    </td>
                  </tr>
                ))}
                {rows.length === 0 && <tr><td colSpan={7} style={{ textAlign: "center", color: "var(--ink-3)" }}>데이터가 없습니다.</td></tr>}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <ModalShell isOpen={createOpen} title="메뉴 등록">
        <form onSubmit={form.handleSubmit((v) => createMutation.mutate(v))} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn"><span className="lcn__label">menuCode</span><input className="text-box text-box--panel" {...form.register("menuCode")} /></div>
            <div className="lcn"><span className="lcn__label">label</span><input className="text-box text-box--panel" {...form.register("label")} /></div>
            <div className="lcn"><span className="lcn__label">moduleCode</span><input className="text-box text-box--panel" {...form.register("moduleCode")} /></div>
            <div className="lcn"><span className="lcn__label">path</span><input className="text-box text-box--panel" placeholder="선택" {...form.register("path")} /></div>
            <div className="lcn"><span className="lcn__label">활성</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...form.register("active")} />활성</label></div>
          </div>
        </form>
        <div className="modal__footer">
          <Button variant="modal" size="sm" onClick={form.handleSubmit((v) => createMutation.mutate(v))} loading={createMutation.isPending}>저장</Button>
          <Button size="sm" onClick={() => { setCreateOpen(false); form.reset(DEFAULT_FORM); }}>닫기</Button>
        </div>
      </ModalShell>
    </>
  );
}
