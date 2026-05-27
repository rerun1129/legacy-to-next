"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { packageUnitUseCases } from "@/application/code/package-unit/use-cases";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CreatePackageUnitRequestDto, UpdatePackageUnitRequestDto } from "@/domain/code/package-unit";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface PackageUnitFormValues {
  packageCode: string;
  name: string;
  nameEn: string;
  active: boolean;
}

const DEFAULT_FORM: PackageUnitFormValues = {
  packageCode: "",
  name: "",
  nameEn: "",
  active: true,
};

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function PackageUnitFormFields({
  register,
  isEdit,
  isReadOnly,
}: {
  register: ReturnType<typeof useForm<PackageUnitFormValues>>["register"];
  isEdit: boolean;
  isReadOnly: boolean;
}) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <CodeBox
        kind="code-only"
        label="Package Code *"
        readOnly={isEdit || isReadOnly}
        onLookup={() => {}}
        codeProps={{
          placeholder: "Package Code",
          ...register("packageCode"),
          ...(isEdit ? { style: { background: "var(--surface-2)", color: "var(--ink-3)" } } : {}),
        }}
      />
      <div className="lcn">
        <span className="lcn__label">Package Name *</span>
        <input
          className="box-panel"
          placeholder="Package Name"
          readOnly={isReadOnly}
          {...register("name")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">English Name</span>
        <input
          className="box-panel"
          placeholder="English Name (optional)"
          readOnly={isReadOnly}
          {...register("nameEn")}
        />
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

function PackageUnitEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<PackageUnitFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-package", "detail", state?.id],
    queryFn: () => packageUnitUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        packageCode: detail.packageCode,
        name: detail.name ?? "",
        nameEn: detail.nameEn ?? "",
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
    mutationFn: (req: CreatePackageUnitRequestDto) => packageUnitUseCases.create(req),
    onSuccess: () => {
      toast.success("포장단위가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdatePackageUnitRequestDto }) =>
      packageUnitUseCases.update(id, req),
    onSuccess: () => {
      toast.success("포장단위가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => packageUnitUseCases.delete(id),
    onSuccess: () => {
      toast.success("포장단위가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: PackageUnitFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdatePackageUnitRequestDto = {
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreatePackageUnitRequestDto = {
        packageCode: values.packageCode.trim(),
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "포장단위 삭제",
      description: `${getValues("packageCode")} / ${getValues("name")} 을 삭제하시겠습니까?`,
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
              Deleted package unit (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <PackageUnitFormFields register={register} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CODE_PACKAGE_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CODE_PACKAGE_UPDATE" : "BTN_ADMIN_CODE_PACKAGE_CREATE"}
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

export function PackageUnitEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "포장단위 수정" : "포장단위 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <PackageUnitEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
