"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { codeDetailUseCases } from "@/application/code-detail/use-cases";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CreateCodeDetailRequestDto, UpdateCodeDetailRequestDto } from "@/domain/code-detail";

export interface CodeDetailEntryModalState {
  mode: "create" | "edit";
  masterId: number;
  id?: number;
}

interface Props {
  state: CodeDetailEntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface CodeDetailFormValues {
  codeValue: string;
  codeLabel: string;
  sortOrder: string;
  active: boolean;
  remark: string;
}

const DEFAULT_FORM: CodeDetailFormValues = {
  codeValue: "",
  codeLabel: "",
  sortOrder: "",
  active: true,
  remark: "",
};

function parseSortOrder(v: string): number | null {
  if (!v.trim()) return null;
  const n = Number(v);
  return isNaN(n) ? null : n;
}

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function CodeDetailEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";
  const qc = useQueryClient();

  const form = useForm<CodeDetailFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-detail", "detail", state?.id],
    queryFn: () => codeDetailUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        codeValue: detail.codeValue,
        codeLabel: detail.codeLabel,
        sortOrder: detail.sortOrder != null ? String(detail.sortOrder) : "",
        active: detail.active,
        remark: detail.remark ?? "",
      });
    }
  }, [detail, reset]);

  useEffect(() => {
    if (!isEdit) {
      reset(DEFAULT_FORM);
    }
  }, [isEdit, reset]);

  const createMutation = useMutation({
    mutationFn: (req: CreateCodeDetailRequestDto) => codeDetailUseCases.create(req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-detail"] });
      toast.success("코드 상세가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateCodeDetailRequestDto }) =>
      codeDetailUseCases.update(id, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-detail"] });
      toast.success("코드 상세가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => codeDetailUseCases.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-detail"] });
      toast.success("코드 상세가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: CodeDetailFormValues) {
    const sortOrder = parseSortOrder(values.sortOrder);
    const remark = parseNullable(values.remark);

    if (isEdit && state?.id != null) {
      const req: UpdateCodeDetailRequestDto = {
        codeLabel: values.codeLabel.trim(),
        sortOrder,
        active: values.active,
        remark,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateCodeDetailRequestDto = {
        masterId: state!.masterId,
        codeValue: values.codeValue.trim(),
        codeLabel: values.codeLabel.trim(),
        sortOrder,
        active: values.active,
        remark,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "코드 상세 삭제",
      description: `${getValues("codeValue")} / ${getValues("codeLabel")} 을 삭제하시겠습니까?`,
      variant: "destructive",
      confirmText: "삭제",
      cancelText: "취소",
    });
    if (!ok) return;
    deleteMutation.mutate(state.id);
  }

  const isBusy =
    isDetailLoading ||
    isSubmitting ||
    createMutation.isPending ||
    updateMutation.isPending ||
    deleteMutation.isPending;

  return (
    <>
      {isDetailLoading ? (
        <div className="modal__loading">Loading...</div>
      ) : (
        <form onSubmit={form.handleSubmit(handleSave)} className="modal__body">
          <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            <CodeBox
              kind="code-only"
              label="Code Value"
              readOnly={isEdit}
              onLookup={() => {}}
              codeProps={{
                placeholder: "Code Value",
                ...register("codeValue"),
                ...(isEdit ? { style: { background: "var(--surface-2)", color: "var(--ink-3)" } } : {}),
              }}
            />
            <div className="lcn">
              <span className="lcn__label">Code Label</span>
              <input
                className="box-panel"
                placeholder="Code Label"
                {...register("codeLabel")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Sort Order</span>
              <input
                type="number"
                className="box-panel"
                placeholder="1"
                {...register("sortOrder")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Active</span>
              <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <input type="checkbox" {...register("active")} />
                Active
              </label>
            </div>
            <div className="lcn" style={{ alignItems: "flex-start" }}>
              <span className="lcn__label" style={{ paddingTop: 4 }}>Remark</span>
              <textarea
                className="box-panel"
                rows={3}
                placeholder="Remark (optional)"
                style={{ resize: "vertical" }}
                {...register("remark")}
              />
            </div>
          </div>
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CODE_LIST_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CODE_LIST_UPDATE" : "BTN_ADMIN_CODE_LIST_CREATE"}
          className="btn btn--modal btn--sm"
          onClick={form.handleSubmit(handleSave)}
          disabled={isBusy}
        >
          저장
        </ActionButton>
        <Button size="sm" onClick={onClose} disabled={isBusy}>
          닫기
        </Button>
      </div>
    </>
  );
}

export function CodeDetailEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "코드 상세 수정" : "코드 상세 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <CodeDetailEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
