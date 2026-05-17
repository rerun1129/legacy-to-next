"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { termsUseCases } from "@/application/terms/use-cases";
import { TermsHistoryPanel } from "./terms-history-panel";
import { TermsFormFields, TERMS_DEFAULT_FORM } from "./terms-form-fields";
import type { TermsFormValues } from "./terms-form-fields";
import type { CreateTermsRequestDto, UpdateTermsRequestDto } from "@/domain/terms";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

// ISO 8601 → datetime-local 입력값("YYYY-MM-DDTHH:mm")으로 변환
function toLocalISOString(iso: string | null | undefined): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return "";
  const pad = (n: number) => String(n).padStart(2, "0");
  return (
    d.getFullYear() +
    "-" + pad(d.getMonth() + 1) +
    "-" + pad(d.getDate()) +
    "T" + pad(d.getHours()) +
    ":" + pad(d.getMinutes())
  );
}

// datetime-local 문자열 → ISO 8601
function toISOString(localStr: string): string {
  return new Date(localStr).toISOString();
}

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function TermsEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";
  const [showHistory, setShowHistory] = useState(false);

  const form = useForm<TermsFormValues>({ defaultValues: TERMS_DEFAULT_FORM });
  const { register, reset, getValues, watch, formState: { isSubmitting } } = form;

  const watchedType = watch("type");

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-terms", "detail", state?.id],
    queryFn: () => termsUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        type: detail.type,
        version: detail.version,
        effectiveAt: toLocalISOString(detail.effectiveAt),
        summary: detail.summary ?? "",
        content: detail.content,
      });
    }
  }, [detail, reset]);

  useEffect(() => {
    if (!isEdit) {
      reset(TERMS_DEFAULT_FORM);
      setShowHistory(false);
    }
  }, [isEdit, reset]);

  const createMutation = useMutation({
    mutationFn: (req: CreateTermsRequestDto) => termsUseCases.create(req),
    onSuccess: () => {
      toast.success("약관이 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateTermsRequestDto }) =>
      termsUseCases.update(id, req),
    onSuccess: () => {
      toast.success("약관이 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => termsUseCases.delete(id),
    onSuccess: () => {
      toast.success("약관이 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: TermsFormValues) {
    if (!values.effectiveAt) {
      toast.error("적용일시를 입력해주세요.");
      return;
    }
    if (isEdit && state?.id != null) {
      const req: UpdateTermsRequestDto = {
        content: values.content,
        summary: values.summary.trim() || null,
        effectiveAt: toISOString(values.effectiveAt),
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      if (values.version === "") {
        toast.error("버전을 입력해주세요.");
        return;
      }
      const req: CreateTermsRequestDto = {
        type: values.type,
        version: values.version as number,
        effectiveAt: toISOString(values.effectiveAt),
        content: values.content,
        summary: values.summary.trim() || null,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "약관 삭제",
      description: `버전 ${getValues("version")} 약관을 삭제하시겠습니까?`,
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
            <div style={{
              padding: "8px 12px",
              marginBottom: 12,
              background: "var(--surface-2, #fef2f2)",
              border: "1px solid var(--border, #fecaca)",
              borderRadius: 4,
              color: "var(--danger, #dc2626)",
              fontSize: 13,
            }}>
              삭제된 약관입니다 (삭제일시: {detail?.deletedAt ?? "—"}). 조회 전용입니다.
            </div>
          )}
          <TermsFormFields register={register} isReadOnly={isReadOnly} isEdit={isEdit} />
          {isEdit && (
            <div style={{ marginTop: 12 }}>
              <Button
                size="sm"
                variant="normal"
                type="button"
                onClick={() => setShowHistory((v) => !v)}
              >
                {showHistory ? "이력 숨기기" : "이 약관의 이력 보기"}
              </Button>
              {showHistory && <TermsHistoryPanel type={watchedType} />}
            </div>
          )}
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

// ─── 외부 래퍼 (isOpen 가드 — false 시 unmount로 hook·캐시 초기화) ───────────
export function TermsEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "약관·정책 수정" : "약관·정책 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="default">
      <TermsEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
