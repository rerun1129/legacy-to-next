"use client";

import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { accessAttributeValuePort } from "@/lib/ports";
import { useState } from "react";
import type { CreateAttributeValueDto } from "@/domain/access/attribute-value";

interface Props {
  attributeKey: string;
}

interface CreateFormValues {
  value: string;
  label: string;
  sortOrder: string;
  active: boolean;
}

const DEFAULT_FORM: CreateFormValues = {
  value: "",
  label: "",
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

export function AttributeValueSection({ attributeKey }: Props) {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const form = useForm<CreateFormValues>({ defaultValues: DEFAULT_FORM });

  const { data, isFetching } = useQuery({
    queryKey: ["access-attribute-value", attributeKey],
    queryFn: () => accessAttributeValuePort.listByKey(attributeKey),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const createMutation = useMutation({
    mutationFn: (req: CreateAttributeValueDto) => accessAttributeValuePort.create(req),
    onSuccess: () => {
      toast.success("값이 등록되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-attribute-value", attributeKey] });
      setCreateOpen(false);
      form.reset(DEFAULT_FORM);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: ({ value }: { value: string }) =>
      accessAttributeValuePort.delete(attributeKey, value),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      qc.invalidateQueries({ queryKey: ["access-attribute-value", attributeKey] });
    },
  });

  async function handleDelete(value: string) {
    const ok = await confirm({
      title: "값 삭제",
      description: `"${value}" 값을 삭제하시겠습니까?`,
      variant: "destructive",
      confirmText: "삭제",
      cancelText: "취소",
    });
    if (!ok) return;
    deleteMutation.mutate({ value });
  }

  function handleCreate(values: CreateFormValues) {
    const req: CreateAttributeValueDto = {
      attributeKey,
      value: values.value.trim(),
      label: parseNullableStr(values.label),
      sortOrder: parseNullableNum(values.sortOrder),
      active: values.active,
    };
    createMutation.mutate(req);
  }

  const rows = data ?? [];

  return (
    <>
      <div className="panel" style={{ marginTop: 16, display: "flex", flexDirection: "column" }}>
        <div className="panel__head">
          <div className="panel__title-accent" />
          <span className="panel__title">Attribute Value — {attributeKey}</span>
          <span className="panel__rowcount">{rows.length}</span>
          <div style={{ marginLeft: "auto" }}>
            <Button size="sm" variant="modal" leftIcon={<Plus size={12} />} onClick={() => setCreateOpen(true)}>
              값 추가
            </Button>
          </div>
        </div>
        <div className="list-wrap">
          {isFetching ? (
            <div style={{ padding: 16, color: "var(--ink-3)" }}>로딩 중...</div>
          ) : (
            <table className="grid" style={{ width: "100%" }}>
              <thead>
                <tr>
                  <th>value</th>
                  <th>label</th>
                  <th>sortOrder</th>
                  <th>active</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {rows.map((r) => (
                  <tr key={r.value}>
                    <td>{r.value}</td>
                    <td>{r.label ?? "-"}</td>
                    <td style={{ textAlign: "right" }}>{r.sortOrder ?? "-"}</td>
                    <td style={{ textAlign: "center" }}>{r.active ? "활성" : "비활성"}</td>
                    <td>
                      <button
                        className="btn btn--danger btn--sm"
                        onClick={() => handleDelete(r.value)}
                        disabled={deleteMutation.isPending}
                      >
                        <Trash2 size={12} />
                      </button>
                    </td>
                  </tr>
                ))}
                {rows.length === 0 && (
                  <tr>
                    <td colSpan={5} style={{ textAlign: "center", color: "var(--ink-3)" }}>
                      값이 없습니다.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <ModalShell isOpen={createOpen} title="Attribute Value 추가">
        <form onSubmit={form.handleSubmit(handleCreate)} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            <div className="lcn">
              <span className="lcn__label">attributeKey</span>
              <span className="text-box text-box--panel" style={{ background: "var(--surface-2)", color: "var(--ink-3)", display: "inline-flex", alignItems: "center" }}>
                {attributeKey}
              </span>
            </div>
            <div className="lcn">
              <span className="lcn__label">value</span>
              <input className="text-box text-box--panel" {...form.register("value")} />
            </div>
            <div className="lcn">
              <span className="lcn__label">label</span>
              <input className="text-box text-box--panel" placeholder="선택" {...form.register("label")} />
            </div>
            <div className="lcn">
              <span className="lcn__label">sortOrder</span>
              <input type="number" className="text-box text-box--panel" placeholder="선택" {...form.register("sortOrder")} />
            </div>
            <div className="lcn">
              <span className="lcn__label">활성</span>
              <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <input type="checkbox" {...form.register("active")} />활성
              </label>
            </div>
          </div>
        </form>
        <div className="modal__footer">
          <Button
            variant="modal"
            size="sm"
            onClick={form.handleSubmit(handleCreate)}
            loading={createMutation.isPending}
          >
            저장
          </Button>
          <Button size="sm" onClick={() => { setCreateOpen(false); form.reset(DEFAULT_FORM); }}>
            닫기
          </Button>
        </div>
      </ModalShell>
    </>
  );
}
