"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { noticeUseCases } from "@/application/notice/use-cases";
import type { CreateNoticeRequestDto, UpdateNoticeRequestDto } from "@/domain/notice";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface NoticeFormValues {
  title: string;
  content: string;
  pinned: boolean;
  active: boolean;
  publishedAt: string; // "YYYY-MM-DDTHH:mm" or ""
  expiresAt: string;   // "YYYY-MM-DDTHH:mm" or ""
}

const DEFAULT_FORM: NoticeFormValues = {
  title: "",
  content: "",
  pinned: false,
  active: true,
  publishedAt: "",
  expiresAt: "",
};

// ISO 8601 → datetime-local 입력값("YYYY-MM-DDTHH:mm")으로 변환
function toLocalISOString(iso: string | null | undefined): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return "";
  // 로컬 시간 기준 YYYY-MM-DDTHH:mm 형식
  const pad = (n: number) => String(n).padStart(2, "0");
  return (
    d.getFullYear() +
    "-" + pad(d.getMonth() + 1) +
    "-" + pad(d.getDate()) +
    "T" + pad(d.getHours()) +
    ":" + pad(d.getMinutes())
  );
}

// datetime-local 문자열 → ISO 8601 (null if empty)
function toISOOrNull(localStr: string): string | null {
  if (!localStr) return null;
  return new Date(localStr).toISOString();
}

// ─── 필드 영역 분리 (readOnly 상태 전파) ────────────────────────────────────
interface FormFieldsProps {
  register: ReturnType<typeof useForm<NoticeFormValues>>["register"];
  isReadOnly: boolean;
}

function NoticeFormFields({ register, isReadOnly }: FormFieldsProps) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div className="lcn">
        <span className="lcn__label">Title *</span>
        <input
          className="box-panel"
          placeholder="Notice title"
          readOnly={isReadOnly}
          {...register("title")}
        />
      </div>
      <div className="lcn" style={{ alignItems: "flex-start" }}>
        <span className="lcn__label" style={{ paddingTop: 4 }}>Content *</span>
        <textarea
          className="box-panel"
          rows={8}
          placeholder="Notice content"
          style={{ resize: "vertical", whiteSpace: "pre-wrap" }}
          readOnly={isReadOnly}
          {...register("content")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Published At</span>
        <input
          type="datetime-local"
          className="box-panel"
          readOnly={isReadOnly}
          disabled={isReadOnly}
          {...register("publishedAt")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Expires At</span>
        <input
          type="datetime-local"
          className="box-panel"
          readOnly={isReadOnly}
          disabled={isReadOnly}
          {...register("expiresAt")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Pinned</span>
        <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <input type="checkbox" disabled={isReadOnly} {...register("pinned")} />
          Pinned
        </label>
      </div>
      <div className="lcn">
        <span className="lcn__label">Active</span>
        <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <input type="checkbox" disabled={isReadOnly} {...register("active")} />
          Active
        </label>
      </div>
    </div>
  );
}

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function NoticeEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";
  const qc = useQueryClient();

  const form = useForm<NoticeFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-notice", "detail", state?.id],
    queryFn: () => noticeUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        title: detail.title,
        content: detail.content,
        pinned: detail.pinned,
        active: detail.active,
        publishedAt: toLocalISOString(detail.publishedAt),
        expiresAt: toLocalISOString(detail.expiresAt),
      });
    }
  }, [detail, reset]);

  useEffect(() => {
    if (!isEdit) {
      reset(DEFAULT_FORM);
    }
  }, [isEdit, reset]);

  const createMutation = useMutation({
    mutationFn: (req: CreateNoticeRequestDto) => noticeUseCases.create(req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-notice"] });
      toast.success("공지사항이 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateNoticeRequestDto }) =>
      noticeUseCases.update(id, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-notice"] });
      toast.success("공지사항이 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => noticeUseCases.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-notice"] });
      toast.success("공지사항이 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: NoticeFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdateNoticeRequestDto = {
        title: values.title.trim(),
        content: values.content,
        pinned: values.pinned,
        active: values.active,
        publishedAt: toISOOrNull(values.publishedAt),
        expiresAt: toISOOrNull(values.expiresAt),
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateNoticeRequestDto = {
        title: values.title.trim(),
        content: values.content,
        pinned: values.pinned,
        active: values.active,
        publishedAt: toISOOrNull(values.publishedAt),
        expiresAt: toISOOrNull(values.expiresAt),
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "공지사항 삭제",
      description: `"${getValues("title")}" 을(를) 삭제하시겠습니까?`,
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
              Deleted notice (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <NoticeFormFields register={register} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CMS_NOTICE_LIST_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CMS_NOTICE_LIST_UPDATE" : "BTN_ADMIN_CMS_NOTICE_LIST_CREATE"}
          className="btn btn--modal btn--sm"
          onClick={form.handleSubmit(handleSave)}
          disabled={isBusy || isReadOnly}
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

// ─── 외부 래퍼 (isOpen 가드 — false 시 unmount로 hook·캐시 초기화) ───────────
export function NoticeEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "공지사항 수정" : "공지사항 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <NoticeEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
