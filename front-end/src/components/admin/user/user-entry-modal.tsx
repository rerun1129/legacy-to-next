"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { userUseCases } from "@/application/user/use-cases";
import type { CreateUserRequestDto, UpdateUserRequestDto } from "@/domain/user";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface UserFormValues {
  username: string;
  email: string;
  password: string; // create 필수, edit 빈 값이면 미갱신
  role: "ADMIN" | "USER";
  active: boolean;
}

const DEFAULT_FORM: UserFormValues = {
  username: "",
  email: "",
  password: "",
  role: "USER",
  active: true,
};

const ROLE_OPTIONS: Array<{ value: "ADMIN" | "USER"; label: string }> = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "USER", label: "USER" },
];

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function UserEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<UserFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  // 수정 모드: 상세 조회 후 form.reset
  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-user", "detail", state?.id],
    queryFn: () => userUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      const roleAttr = detail.attributes?.role?.[0] as "ADMIN" | "USER" | undefined;
      reset({
        username: detail.username,
        email: detail.email ?? "",
        password: "",
        role: roleAttr ?? "USER",
        active: detail.active,
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
    mutationFn: (req: CreateUserRequestDto) => userUseCases.create(req),
    onSuccess: () => {
      toast.success("사용자가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateUserRequestDto }) =>
      userUseCases.update(id, req),
    onSuccess: () => {
      toast.success("수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => userUseCases.delete(id),
    onSuccess: () => {
      toast.success("삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: UserFormValues) {
    // role은 ABAC attributes로 전송
    const attributes: Record<string, string[]> = { role: [values.role] };

    if (isEdit && state?.id != null) {
      const req: UpdateUserRequestDto = {
        email: values.email.trim() || null,
        password: values.password.trim() || null,
        active: values.active,
        attributes,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateUserRequestDto = {
        username: values.username.trim(),
        email: values.email.trim() || null,
        password: values.password,
        active: values.active,
        attributes,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "사용자 삭제",
      description: `${getValues("username")} 사용자를 삭제하시겠습니까?`,
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
          <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
            {isReadOnly && (
              <div style={{
                padding: "8px 12px",
                background: "var(--surface-2, #fef2f2)",
                border: "1px solid var(--border, #fecaca)",
                borderRadius: 4,
                color: "var(--danger, #dc2626)",
                fontSize: 13,
              }}>
                삭제된 사용자입니다 (삭제일시: {detail?.deletedAt ?? "—"}). 조회 전용입니다.
              </div>
            )}
            <div className="lcn">
              <span className="lcn__label">사용자명</span>
              <input
                className="text-box text-box--panel"
                placeholder="사용자명"
                readOnly={isEdit || isReadOnly}
                style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
                {...register("username")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">이메일</span>
              <input
                className="text-box text-box--panel"
                placeholder="이메일 (선택)"
                readOnly={isReadOnly}
                {...register("email")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">비밀번호</span>
              <input
                type="password"
                className="text-box text-box--panel"
                placeholder={isEdit ? "변경 시에만 입력" : "8자 이상 필수"}
                readOnly={isReadOnly}
                {...register("password")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">역할</span>
              <select className="text-box text-box--panel" disabled={isReadOnly} {...register("role")}>
                {ROLE_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="lcn">
              <span className="lcn__label">활성</span>
              <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <input type="checkbox" disabled={isReadOnly} {...register("active")} />
                활성
              </label>
            </div>
          </div>
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_USER_LIST_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_USER_LIST_UPDATE" : "BTN_ADMIN_USER_LIST_CREATE"}
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
export function UserEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "사용자 수정" : "사용자 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="default">
      <UserEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
