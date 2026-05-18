"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessAttributePort } from "@/lib/ports";
import type { CreateAttributeDefinitionDto, AttributeValueType } from "@/domain/access/attribute";

const VALUE_TYPES: AttributeValueType[] = ["STRING", "NUMBER", "BOOLEAN", "ENUM"];

const DEFAULT_FORM: CreateAttributeDefinitionDto = {
  attributeKey: "",
  name: "",
  valueType: "STRING",
  allowMulti: false,
  active: true,
};

export function AccessAttributeListClient() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const form = useForm<CreateAttributeDefinitionDto>({ defaultValues: DEFAULT_FORM });

  const { data, isFetching } = useQuery({
    queryKey: ["access-attribute", "list"],
    queryFn: () => accessAttributePort.search(1, 100),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMutation = useMutation({
    mutationFn: (req: CreateAttributeDefinitionDto) => accessAttributePort.create(req),
    onSuccess: () => {
      toast.success("속성이 등록되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-attribute", "list"] });
      setCreateOpen(false);
      form.reset(DEFAULT_FORM);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (attributeKey: string) => accessAttributePort.delete(attributeKey),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-attribute", "list"] });
    },
  });

  async function handleDelete(attributeKey: string) {
    const ok = await confirm({ title: "속성 삭제", description: `"${attributeKey}" 속성을 삭제하시겠습니까?`, variant: "destructive", confirmText: "삭제", cancelText: "취소" });
    if (!ok) return;
    deleteMutation.mutate(attributeKey);
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
          <span className="panel__title">Attribute 목록</span>
          <span className="panel__rowcount">{rows.length}</span>
        </div>
        <div className="list-wrap">
          {isFetching ? <div style={{ padding: 16, color: "var(--ink-3)" }}>로딩 중...</div> : (
            <table className="grid" style={{ width: "100%" }}>
              <thead><tr><th>attributeKey</th><th>name</th><th>valueType</th><th>allowMulti</th><th>active</th><th></th></tr></thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.attributeKey}>
                    <td>{r.attributeKey}</td>
                    <td>{r.name}</td>
                    <td>{r.valueType}</td>
                    <td style={{ textAlign: "center" }}>{r.allowMulti ? "Y" : "N"}</td>
                    <td style={{ textAlign: "center" }}>{r.active ? "활성" : "비활성"}</td>
                    <td>
                      <button className="btn btn--danger btn--sm" onClick={() => handleDelete(r.attributeKey)} disabled={deleteMutation.isPending}>
                        <Trash2 size={12} />
                      </button>
                    </td>
                  </tr>
                ))}
                {rows.length === 0 && <tr><td colSpan={6} style={{ textAlign: "center", color: "var(--ink-3)" }}>데이터가 없습니다.</td></tr>}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <ModalShell isOpen={createOpen} title="속성 등록">
        <form onSubmit={form.handleSubmit((v) => createMutation.mutate(v))} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn"><span className="lcn__label">attributeKey</span><input className="text-box text-box--panel" {...form.register("attributeKey")} /></div>
            <div className="lcn"><span className="lcn__label">name</span><input className="text-box text-box--panel" {...form.register("name")} /></div>
            <div className="lcn"><span className="lcn__label">valueType</span>
              <select className="text-box text-box--panel" {...form.register("valueType")}>
                {VALUE_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="lcn"><span className="lcn__label">allowMulti</span><label style={{ display: "flex", alignItems: "center", gap: 6 }}><input type="checkbox" {...form.register("allowMulti")} />허용</label></div>
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
