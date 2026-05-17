"use client";

import { useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { userUseCases } from "@/application/user/use-cases";
import type { CreateUserRequestDto, UpdateUserRequestDto, UserRole } from "@/domain/user";
import type { Permission } from "@/domain/permission";
import { ALL_PERMISSIONS, PERMISSION_LABEL } from "@/domain/permission";

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
  role: UserRole;
  active: boolean;
  permissions: Permission[];
}

const DEFAULT_FORM: UserFormValues = {
  username: "",
  email: "",
  password: "",
  role: "ADMIN",
  active: true,
  permissions: [...ALL_PERMISSIONS],
};

const ROLE_OPTIONS: { value: UserRole; label: string }[] = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "USER", label: "USER" },
];

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function UserEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<UserFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, watch, formState: { isSubmitting } } = form;

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
      reset({
        username: detail.username,
        email: detail.email ?? "",
        password: "", // 응답에 password 없음 — 변경 시에만 입력
        role: detail.role,
        active: detail.active,
        permissions: detail.permissions,
      });
    }
  }, [detail, reset]);

  // 신규 모드: 폼 초기화
  useEffect(() => {
    if (!isEdit) {
      reset(DEFAULT_FORM);
    }
  }, [isEdit, reset]);

  // ADMIN role 선택 시 전체 권한 자동 부여
  const role = watch("role");
  useEffect(() => {
    if (role === "ADMIN") {
      form.setValue("permissions", [...ALL_PERMISSIONS]);
    }
  }, [role, form]);

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
    if (isEdit && state?.id != null) {
      const req: UpdateUserRequestDto = {
        email: values.email.trim() || null,
        // 빈 값이면 null — 어댑터에서도 재정규화하지만 여기서도 명시적으로 처리
        password: values.password.trim() || null,
        role: values.role,
        active: values.active,
        permissions: values.permissions,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateUserRequestDto = {
        username: values.username.trim(),
        email: values.email.trim() || null,
        password: values.password,
        role: values.role,
        active: values.active,
        permissions: values.permissions,
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
              <span className="lcn__label">사용자명</span>
              <input
                className="text-box text-box--panel"
                placeholder="사용자명"
                readOnly={isEdit}
                style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
                {...register("username")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">이메일</span>
              <input
                className="text-box text-box--panel"
                placeholder="이메일 (선택)"
                {...register("email")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">비밀번호</span>
              <input
                type="password"
                className="text-box text-box--panel"
                placeholder={isEdit ? "변경 시에만 입력" : "8자 이상 필수"}
                {...register("password")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">역할</span>
              <select className="text-box text-box--panel" {...register("role")}>
                {ROLE_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </div>
            <Controller
              name="permissions"
              control={form.control}
              render={({ field }) => {
                const isAdmin = watch("role") === "ADMIN";
                return (
                  <div className="lcn" style={{ alignItems: "flex-start" }}>
                    <span className="lcn__label" style={{ paddingTop: 4 }}>권한</span>
                    <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
                      {ALL_PERMISSIONS.map((p) => {
                        const checked = isAdmin || (field.value ?? []).includes(p);
                        return (
                          <label key={p} style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 13 }}>
                            <input
                              type="checkbox"
                              checked={checked}
                              disabled={isAdmin}
                              onChange={(e) => {
                                const current = field.value ?? [];
                                field.onChange(
                                  e.target.checked
                                    ? [...current, p]
                                    : current.filter((v) => v !== p)
                                );
                              }}
                            />
                            {PERMISSION_LABEL[p]}
                          </label>
                        );
                      })}
                    </div>
                  </div>
                );
              }}
            />
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
export function UserEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "사용자 수정" : "사용자 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="default">
      <UserEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
