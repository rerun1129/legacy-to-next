"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { faqUseCases } from "@/application/faq/use-cases";
import { faqCategoryUseCases } from "@/application/faq-category/use-cases";
import { FaqFormFields, FAQ_DEFAULT_FORM } from "./faq-form-fields";
import type { FaqFormValues } from "./faq-form-fields";
import type { CreateFaqRequestDto, UpdateFaqRequestDto } from "@/domain/faq";

export interface FaqEntryModalState {
  mode: "create" | "edit";
  id?: number;
  defaultCategoryId?: number;
}

interface Props {
  state: FaqEntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

function FaqEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";
  const queryClient = useQueryClient();

  const form = useForm<FaqFormValues>({ defaultValues: FAQ_DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  // 카테고리 목록 (selectbox용)
  const { data: categories = [] } = useQuery({
    queryKey: ["admin-faq-category", "list"],
    queryFn: () => faqCategoryUseCases.search(),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-faq", "detail", state?.id],
    queryFn: () => faqUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        faqCategoryId: detail.faqCategoryId,
        question: detail.question,
        answer: detail.answer,
        sortOrder: detail.sortOrder,
        active: detail.active,
      });
    }
  }, [detail, reset]);

  useEffect(() => {
    if (!isEdit) {
      reset({
        ...FAQ_DEFAULT_FORM,
        faqCategoryId: state?.defaultCategoryId ?? "",
      });
    }
  }, [isEdit, state?.defaultCategoryId, reset]);

  const createMutation = useMutation({
    mutationFn: (req: CreateFaqRequestDto) => faqUseCases.create(req),
    onSuccess: (_id, req) => {
      queryClient.invalidateQueries({
        queryKey: ["admin-faq", "list", req.faqCategoryId],
      });
      toast.success("FAQ가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateFaqRequestDto }) =>
      faqUseCases.update(id, req),
    onSuccess: (_data, { req }) => {
      queryClient.invalidateQueries({
        queryKey: ["admin-faq", "list", req.faqCategoryId],
      });
      queryClient.invalidateQueries({ queryKey: ["admin-faq", "detail", state?.id] });
      toast.success("FAQ가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => faqUseCases.delete(id),
    onSuccess: () => {
      // 카테고리 id를 모르므로 list 쿼리 전체 무효화
      queryClient.invalidateQueries({ queryKey: ["admin-faq", "list"] });
      toast.success("FAQ가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: FaqFormValues) {
    if (values.faqCategoryId === "") {
      toast.error("카테고리를 선택해주세요.");
      return;
    }
    const faqCategoryId = Number(values.faqCategoryId);
    const sortOrder = values.sortOrder === "" ? 0 : Number(values.sortOrder);
    if (isEdit && state?.id != null) {
      const req: UpdateFaqRequestDto = {
        faqCategoryId,
        question: values.question,
        answer: values.answer,
        sortOrder,
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateFaqRequestDto = {
        faqCategoryId,
        question: values.question,
        answer: values.answer,
        sortOrder,
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "FAQ 삭제",
      description: `"${getValues("question")}" FAQ를 삭제하시겠습니까?`,
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

  // 삭제된 카테고리 제외한 목록 (신규 등록 시 활성 카테고리만 제공)
  const activeCategories = isEdit ? categories : categories.filter((c) => !c.deletedAt);

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
              삭제된 FAQ입니다 (삭제일시: {detail?.deletedAt ?? "—"}). 조회 전용입니다.
            </div>
          )}
          <FaqFormFields
            register={register}
            isReadOnly={isReadOnly}
            categories={activeCategories}
          />
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

export function FaqEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "FAQ 수정" : "FAQ 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="lg">
      <FaqEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
