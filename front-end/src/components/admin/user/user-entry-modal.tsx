"use client";

import { useEffect, useState } from "react";
import { useForm, Controller } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { userUseCases } from "@/application/user/use-cases";
import { accessAttributeUseCases } from "@/application/access/attribute/use-cases";
import type { CreateUserRequestDto, UpdateUserRequestDto } from "@/domain/user";
import type { ModuleAttributeDto } from "@/domain/access/attribute";

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
  modules: string[];
}

const DEFAULT_FORM: UserFormValues = {
  username: "",
  email: "",
  password: "",
  role: "USER",
  active: true,
  modules: [],
};

const ROLE_OPTIONS: Array<{ value: "ADMIN" | "USER"; label: string }> = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "USER", label: "USER" },
];

const MODULE_OPTIONS = [
  { value: "ADMIN", label: "ADMIN" },
  { value: "FMS", label: "FMS" },
] as const;

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function UserEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<UserFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, control, watch, formState: { isSubmitting } } = form;

  // 동적 속성 값 (role·module 외 나머지 ABAC 속성)
  const [dynamicAttrs, setDynamicAttrs] = useState<Record<string, string[]>>({});

  const selectedModules = watch("modules");

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
        modules: detail.attributes?.module ?? [],
      });
      // role·module 제외한 나머지 속성을 동적 상태로 복원
      // eslint 오류 방지를 위해 구조분해 후 rest 사용 (role, module은 고정 필드)
      const { role: _r, module: _m, ...rest } = detail.attributes ?? {};
      setDynamicAttrs(rest as Record<string, string[]>);
    }
  }, [detail, reset]);

  // 신규 모드: 폼 초기화
  useEffect(() => {
    if (!isEdit) {
      reset(DEFAULT_FORM);
      setDynamicAttrs({});
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

  // 선택된 모듈에 속한 동적 속성을 BE에서 조회
  const { data: moduleAttributes = [] } = useQuery<ModuleAttributeDto[]>({
    queryKey: ["module-attributes", selectedModules],
    queryFn: async () => {
      if (selectedModules.length === 0) return [];
      const results = await Promise.all(
        selectedModules.map((mod) => accessAttributeUseCases.getByModule(mod))
      );
      // 모듈별 결과를 합치고 attributeKey 기준 중복 제거 (첫 번째 등장 우선)
      const merged = new Map<string, ModuleAttributeDto>();
      results.flat().forEach((attr) => {
        if (!merged.has(attr.attributeKey)) merged.set(attr.attributeKey, attr);
      });
      return Array.from(merged.values());
    },
    enabled: selectedModules.length > 0,
  });

  function handleSave(values: UserFormValues) {
    // role·module은 ABAC attributes로 전송
    const attributes: Record<string, string[]> = { role: [values.role] };
    if (values.modules.length > 0) {
      attributes.module = values.modules;
    }
    // 동적 속성 추가
    for (const [key, vals] of Object.entries(dynamicAttrs)) {
      if (vals.length > 0) {
        attributes[key] = vals;
      }
    }

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
                Deleted user (deleted at: {detail?.deletedAt ?? "—"}). Read only.
              </div>
            )}
            <div className="lcn">
              <span className="lcn__label">Username</span>
              <input
                className="box-panel"
                placeholder="Username"
                readOnly={isEdit || isReadOnly}
                style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
                {...register("username")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Email</span>
              <input
                className="box-panel"
                placeholder="Email (optional)"
                readOnly={isReadOnly}
                {...register("email")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Password</span>
              <input
                type="password"
                className="box-panel"
                placeholder={isEdit ? "Enter only to change" : "Min 8 characters"}
                readOnly={isReadOnly}
                {...register("password")}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Role</span>
              <Controller
                name="role"
                control={control}
                render={({ field }) => (
                  <ComboBox
                    variant="panel"
                    options={ROLE_OPTIONS}
                    value={field.value}
                    onChange={field.onChange}
                    disabled={isReadOnly}
                  />
                )}
              />
            </div>
            <div className="lcn">
              <span className="lcn__label">Module</span>
              <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                {MODULE_OPTIONS.map((opt) => (
                  <Controller
                    key={opt.value}
                    name="modules"
                    control={control}
                    render={({ field }) => (
                      <label style={{ display: "flex", alignItems: "center", gap: 4 }}>
                        <input
                          type="checkbox"
                          checked={field.value.includes(opt.value)}
                          disabled={isReadOnly}
                          onChange={(e) => {
                            const next = e.target.checked
                              ? [...field.value, opt.value]
                              : field.value.filter((v: string) => v !== opt.value);
                            field.onChange(next);
                          }}
                        />
                        {opt.label}
                      </label>
                    )}
                  />
                ))}
              </div>
            </div>
            {moduleAttributes.map((attr) => (
              <div className="lcn" key={attr.attributeKey}>
                <span className="lcn__label">{attr.name}</span>
                <div style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
                  {attr.valueType === "ENUM" && attr.allowMulti && attr.values.map((opt) => (
                    <label key={opt.value} style={{ display: "flex", alignItems: "center", gap: 4 }}>
                      <input
                        type="checkbox"
                        checked={(dynamicAttrs[attr.attributeKey] ?? []).includes(opt.value)}
                        disabled={isReadOnly}
                        onChange={(e) => {
                          const prev = dynamicAttrs[attr.attributeKey] ?? [];
                          const next = e.target.checked
                            ? [...prev, opt.value]
                            : prev.filter((v) => v !== opt.value);
                          setDynamicAttrs((p) => ({ ...p, [attr.attributeKey]: next }));
                        }}
                      />
                      {opt.label}
                    </label>
                  ))}
                  {attr.valueType === "ENUM" && !attr.allowMulti && (
                    <ComboBox
                      variant="panel"
                      options={attr.values.map((v) => ({ value: v.value, label: v.label }))}
                      value={(dynamicAttrs[attr.attributeKey] ?? [])[0] ?? ""}
                      onChange={(e) =>
                        setDynamicAttrs((p) => ({
                          ...p,
                          [attr.attributeKey]: e.target.value ? [e.target.value] : [],
                        }))
                      }
                      disabled={isReadOnly}
                    />
                  )}
                  {attr.valueType === "STRING" && (
                    <input
                      className="box-panel"
                      value={(dynamicAttrs[attr.attributeKey] ?? [])[0] ?? ""}
                      disabled={isReadOnly}
                      onChange={(e) =>
                        setDynamicAttrs((p) => ({
                          ...p,
                          [attr.attributeKey]: e.target.value ? [e.target.value] : [],
                        }))
                      }
                    />
                  )}
                  {attr.valueType === "NUMBER" && (
                    <input
                      type="number"
                      className="box-panel"
                      value={(dynamicAttrs[attr.attributeKey] ?? [])[0] ?? ""}
                      disabled={isReadOnly}
                      onChange={(e) =>
                        setDynamicAttrs((p) => ({
                          ...p,
                          [attr.attributeKey]: e.target.value ? [e.target.value] : [],
                        }))
                      }
                    />
                  )}
                </div>
              </div>
            ))}
            <div className="lcn">
              <span className="lcn__label">Active</span>
              <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <input type="checkbox" disabled={isReadOnly} {...register("active")} />
                Active
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
    <ModalShell isOpen={isOpen} title={title} size="md">
      <UserEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
