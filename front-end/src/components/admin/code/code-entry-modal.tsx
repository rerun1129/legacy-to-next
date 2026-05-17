"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { codeUseCases } from "@/application/code/use-cases";
import type { CreateCodeRequestDto, UpdateCodeRequestDto } from "@/domain/code";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface CodeFormValues {
  codeGroup: string;
  codeValue: string;
  codeLabel: string;
  sortOrder: string;
  active: boolean;
  remark: string;
}

const DEFAULT_FORM: CodeFormValues = {
  codeGroup: "",
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

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function CodeEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<CodeFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  // 수정 모드: 상세 조회 후 form.reset
  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code", "detail", state?.id],
    queryFn: () => codeUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        codeGroup: detail.codeGroup,
        codeValue: detail.codeValue,
        codeLabel: detail.codeLabel,
        sortOrder: detail.sortOrder != null ? String(detail.sortOrder) : "",
        active: detail.active,
        remark: detail.remark ?? "",
      });
    }
  }, [detail, reset]);

  // 신규 모드: 폼 초기화
  useEffect(() => {
    if (!isEdit) {
      reset(DEFAULT_FORM);
    }
  }, [isEdit, reset]);

  const createMutation = useMutation({
    mutationFn: (req: CreateCodeRequestDto) => codeUseCases.create(req),
    onSuccess: () => {
      toast.success("코드가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateCodeRequestDto }) =>
      codeUseCases.update(id, req),
    onSuccess: () => {
      toast.success("코드가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => codeUseCases.delete(id),
    onSuccess: () => {
      toast.success("코드가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: CodeFormValues) {
    const sortOrder = parseSortOrder(values.sortOrder);
    const remark = parseNullable(values.remark);

    if (isEdit && state?.id != null) {
      const req: UpdateCodeRequestDto = {
        codeLabel: values.codeLabel.trim(),
        sortOrder,
        active: values.active,
        remark,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateCodeRequestDto = {
        codeGroup: values.codeGroup.trim(),
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
      title: "코드 삭제",
      description: `${getValues("codeGroup")} / ${getValues("codeValue")} 을 삭제하시겠습니까?`,
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
            <div className="lcn">
              <span className="lcn__label">코드 그룹</span>
              <input
                className="text-box text-box--panel"
                placeholder="코드 그룹"
                readOnly={isEdit}
                style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
                {...register("codeGroup")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">코드 값</span>
              <input
                className="text-box text-box--panel"
                placeholder="코드 값"
                readOnly={isEdit}
                style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
                {...register("codeValue")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">코드 라벨</span>
              <input
                className="text-box text-box--panel"
                placeholder="코드 라벨"
                {...register("codeLabel")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">정렬순서</span>
              <input
                type="number"
                className="text-box text-box--panel"
                placeholder="1"
                {...register("sortOrder")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">활성</span>
              <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <input type="checkbox" {...register("active")} />
                활성
              </label>
            </div>
            <div className="lcn" style={{ alignItems: "flex-start" }}>
              <span className="lcn__label" style={{ paddingTop: 4 }}>비고</span>
              <textarea
                className="text-box text-box--panel"
                rows={3}
                placeholder="비고"
                style={{ resize: "vertical" }}
                {...register("remark")}
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
            disabled={isBusy}
          >
            삭제
          </Button>
        )}
        <Button
          variant="modal"
          size="sm"
          onClick={form.handleSubmit(handleSave)}
          disabled={isBusy}
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
export function CodeEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "코드 수정" : "코드 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="default">
      <CodeEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
