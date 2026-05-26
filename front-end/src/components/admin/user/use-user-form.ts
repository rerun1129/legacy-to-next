"use client";

import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { userUseCases } from "@/application/user/use-cases";
import { accessAttributeUseCases } from "@/application/access/attribute/use-cases";
import type { CreateUserRequestDto, UpdateUserRequestDto } from "@/domain/user";
import type { ModuleAttributeDto } from "@/domain/access/attribute";
import type { EntryModalState } from "./user-entry-modal";

export interface UserFormValues {
  username: string;
  email: string;
  password: string; // create 필수, edit 빈 값이면 미갱신
  role: "ADMIN" | "USER";
  active: boolean;
  modules: string[];
}

export const DEFAULT_FORM: UserFormValues = {
  username: "",
  email: "",
  password: "",
  role: "USER",
  active: true,
  modules: [],
};

export function useUserForm(state: EntryModalState | null, onSaved: () => void) {
  const isEdit = state?.mode === "edit";

  const form = useForm<UserFormValues>({ defaultValues: DEFAULT_FORM });
  const { reset, getValues, watch, formState: { isSubmitting } } = form;

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

  return {
    form,
    dynamicAttrs,
    setDynamicAttrs,
    moduleAttributes,
    handleSave,
    handleDelete,
    isReadOnly,
    isBusy,
    isDetailLoading,
    detail,
  };
}
