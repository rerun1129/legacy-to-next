"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { codeMasterUseCases } from "@/application/code-master/use-cases";
import type { CreateCodeMasterRequestDto, UpdateCodeMasterRequestDto } from "@/domain/code-master";

export interface CodeMasterEntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: CodeMasterEntryModalState | null;
  onClose: () => void;
  onSaved: (createdId?: number) => void;
}

interface CodeMasterFormValues {
  masterCode: string;
  masterName: string;
  description: string;
  sortOrder: string;
  active: boolean;
}

const DEFAULT_FORM: CodeMasterFormValues = {
  masterCode: "",
  masterName: "",
  description: "",
  sortOrder: "",
  active: true,
};

function parseSortOrder(v: string): number | null {
  if (!v.trim()) return null;
  const n = Number(v);
  return isNaN(n) ? null : n;
}

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function CodeMasterEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";
  const qc = useQueryClient();

  const form = useForm<CodeMasterFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-master", "detail", state?.id],
    queryFn: () => codeMasterUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        masterCode: detail.masterCode,
        masterName: detail.masterName,
        description: detail.description ?? "",
        sortOrder: detail.sortOrder != null ? String(detail.sortOrder) : "",
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
    mutationFn: (req: CreateCodeMasterRequestDto) => codeMasterUseCases.create(req),
    onSuccess: (createdId) => {
      qc.invalidateQueries({ queryKey: ["admin-code-master"] });
      toast.success("마스터 코드가 등록되었습니다.");
      onSaved(createdId);
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateCodeMasterRequestDto }) =>
      codeMasterUseCases.update(id, req),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-master"] });
      toast.success("마스터 코드가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => codeMasterUseCases.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["admin-code-master"] });
      toast.success("마스터 코드가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: CodeMasterFormValues) {
    const sortOrder = parseSortOrder(values.sortOrder);
    const description = parseNullable(values.description);

    if (isEdit && state?.id != null) {
      const req: UpdateCodeMasterRequestDto = {
        masterName: values.masterName.trim(),
        description,
        sortOrder,
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateCodeMasterRequestDto = {
        masterCode: values.masterCode.trim(),
        masterName: values.masterName.trim(),
        description,
        sortOrder,
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "마스터 코드 삭제",
      description: `${getValues("masterCode")} / ${getValues("masterName")} 을 삭제하시겠습니까?`,
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
              <span className="lcn__label">마스터 코드</span>
              {isEdit ? (
                <span
                  className="text-box text-box--panel"
                  style={{ background: "var(--surface-2)", color: "var(--ink-3)", display: "inline-flex", alignItems: "center" }}
                >
                  {detail?.masterCode ?? getValues("masterCode")}
                </span>
              ) : (
                <input
                  className="text-box text-box--panel"
                  placeholder="마스터 코드 (예: SHIP_TYPE)"
                  {...register("masterCode")}
                />
              )}
            </div>
            <div className="lcn">
              <span className="lcn__label">마스터 명</span>
              <input
                className="text-box text-box--panel"
                placeholder="마스터 명"
                {...register("masterName")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">설명</span>
              <input
                className="text-box text-box--panel"
                placeholder="설명 (선택)"
                {...register("description")}
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

export function CodeMasterEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "마스터 코드 수정" : "마스터 코드 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="default">
      <CodeMasterEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
