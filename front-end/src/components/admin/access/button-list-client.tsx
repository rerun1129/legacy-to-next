"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessButtonPort } from "@/lib/ports";
import type { CreateButtonDto, ButtonActionType } from "@/domain/access/button";

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

export function AccessButtonListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const form = useForm<CreateButtonDto>({ defaultValues: DEFAULT_FORM });

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
      form.reset(DEFAULT_FORM);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => accessButtonPort.delete(id),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-button", "list"] });
    },
  });

  async function handleDelete(id: number, label: string) {
    const ok = await confirm({ title: "버튼 삭제", description: `"${label}" 버튼을 삭제하시겠습니까?`, variant: "destructive", confirmText: "삭제", cancelText: "취소" });
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
          <span className="panel__title">Button 목록</span>
          <span className="panel__rowcount">{rows.length}</span>
        </div>
        <div className="list-wrap">
          {isFetching ? <div style={{ padding: 16, color: "var(--ink-3)" }}>로딩 중...</div> : (
            <table className="grid" style={{ width: "100%" }}>
              <thead><tr><th>ID</th><th>buttonCode</th><th>label</th><th>menuId</th><th>actionType</th><th>active</th><th></th></tr></thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.id}>
                    <td>{r.id}</td>
                    <td>{r.buttonCode}</td>
                    <td>{r.label}</td>
                    <td>{r.menuId}</td>
                    <td>{r.actionType}</td>
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

      <ModalShell isOpen={createOpen} title="버튼 등록">
        <form onSubmit={form.handleSubmit((v) => createMutation.mutate(v))} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn"><span className="lcn__label">menuId</span><input type="number" className="text-box text-box--panel" {...form.register("menuId", { valueAsNumber: true })} /></div>
            <div className="lcn"><span className="lcn__label">buttonCode</span><input className="text-box text-box--panel" {...form.register("buttonCode")} /></div>
            <div className="lcn"><span className="lcn__label">label</span><input className="text-box text-box--panel" {...form.register("label")} /></div>
            <div className="lcn"><span className="lcn__label">actionType</span>
              <select className="text-box text-box--panel" {...form.register("actionType")}>
                {ACTION_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
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
