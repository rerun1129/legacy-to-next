"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { faqCategoryUseCases } from "@/application/faq-category/use-cases";
import type { CreateFaqCategoryRequestDto, UpdateFaqCategoryRequestDto } from "@/domain/faq-category";

export interface FaqCategoryEntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: FaqCategoryEntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface FormValues {
  name: string;
  sortOrder: number | "";
  active: boolean;
}

const DEFAULT_FORM: FormValues = {
  name: "",
  sortOrder: 0,
  active: true,
};

function FaqCategoryEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";
  const queryClient = useQueryClient();

  const form = useForm<FormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-faq-category", "detail", state?.id],
    queryFn: () => faqCategoryUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        name: detail.name,
        sortOrder: detail.sortOrder,
        active: detail.active,
      });
    }
  }, [detail, reset]);

  useEffect(() => {
    if (!isEdit) {
      reset(DEFAULT_FORM);
    }
  }, [isEdit, reset]);

  const createMutation = useMutation({
    mutationFn: (req: CreateFaqCategoryRequestDto) => faqCategoryUseCases.create(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-faq-category", "list"] });
      toast.success("FAQ 카테고리가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateFaqCategoryRequestDto }) =>
      faqCategoryUseCases.update(id, req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-faq-category", "list"] });
      queryClient.invalidateQueries({ queryKey: ["admin-faq-category", "detail", state?.id] });
      toast.success("FAQ 카테고리가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => faqCategoryUseCases.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-faq-category", "list"] });
      toast.success("FAQ 카테고리가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: FormValues) {
    const req = {
      name: values.name,
      sortOrder: values.sortOrder === "" ? 0 : Number(values.sortOrder),
      active: values.active,
    };
    if (isEdit && state?.id != null) {
      updateMutation.mutate({ id: state.id, req });
    } else {
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "카테고리 삭제",
      description: `"${getValues("name")}" 카테고리를 삭제하시겠습니까?`,
      variant: "destructive",
      confirmText: "삭제",
      cancelText: "취소",
    });
    if (!ok) return;
    deleteMutation.mutate(state.id);
  }

  const isReadOnly = isEdit && detail?.deletedAt != null;

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
          {isReadOnly && (
            <div
              style={{
                padding: "8px 12px",
                marginBottom: 12,
                background: "var(--surface-2, #fef2f2)",
                border: "1px solid var(--border, #fecaca)",
                borderRadius: 4,
                color: "var(--danger, #dc2626)",
                fontSize: 13,
              }}
            >
              삭제된 카테고리입니다 (삭제일시: {detail?.deletedAt ?? "—"}). 조회 전용입니다.
            </div>
          )}
          <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            <div className="lcn">
              <span className="lcn__label">카테고리명 *</span>
              <input
                className="text-box text-box--panel"
                placeholder="카테고리명"
                readOnly={isReadOnly}
                {...register("name")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">정렬순서</span>
              <input
                type="number"
                className="text-box text-box--panel"
                placeholder="0"
                readOnly={isReadOnly}
                disabled={isReadOnly}
                {...register("sortOrder", { setValueAs: (v) => (v === "" ? "" : Number(v)) })}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">활성</span>
              <input
                type="checkbox"
                disabled={isReadOnly}
                {...register("active")}
              />
            </div>
          </div>
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <Button
            variant="danger"
            size="sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </Button>
        )}
        <Button
          variant="modal"
          size="sm"
          onClick={form.handleSubmit(handleSave)}
          disabled={isBusy || isReadOnly}
          loading={createMutation.isPending || updateMutation.isPending}
        >
          저장
        </Button>
        <Button size="sm" onClick={onClose} disabled={isBusy}>
          닫기
        </Button>
      </div>
    </>
  );
}

export function FaqCategoryEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "FAQ 카테고리 수정" : "FAQ 카테고리 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="default">
      <FaqCategoryEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
