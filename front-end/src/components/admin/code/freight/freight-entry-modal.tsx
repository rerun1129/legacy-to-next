"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { freightUseCases } from "@/application/code/freight/use-cases";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CreateFreightRequestDto, UpdateFreightRequestDto } from "@/domain/code/freight";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface FreightFormValues {
  freightCode: string;
  name: string;
  nameEn: string;
  description: string;
  freightUnit: string;
  freightGroup: string;
  active: boolean;
}

const DEFAULT_FORM: FreightFormValues = {
  freightCode: "",
  name: "",
  nameEn: "",
  description: "",
  freightUnit: "",
  freightGroup: "",
  active: true,
};

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function FreightFormFields({
  register,
  isEdit,
  isReadOnly,
}: {
  register: ReturnType<typeof useForm<FreightFormValues>>["register"];
  isEdit: boolean;
  isReadOnly: boolean;
}) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <CodeBox
        kind="code-only"
        label="Freight Code *"
        readOnly={isEdit || isReadOnly}
        onLookup={() => {}}
        codeProps={{
          placeholder: "Freight Code",
          ...register("freightCode"),
          ...(isEdit ? { style: { background: "var(--surface-2)", color: "var(--ink-3)" } } : {}),
        }}
      />
      <div className="lcn">
        <span className="lcn__label">Freight Name *</span>
        <input
          className="box-panel"
          placeholder="Freight Name"
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
      <div className="lcn" style={{ alignItems: "flex-start" }}>
        <span className="lcn__label" style={{ paddingTop: 4 }}>Description</span>
        <textarea
          className="box-panel"
          rows={3}
          placeholder="Description (optional)"
          style={{ resize: "vertical" }}
          readOnly={isReadOnly}
          {...register("description")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Freight Unit</span>
        <input
          className="box-panel"
          placeholder="Freight Unit (max 10)"
          maxLength={10}
          readOnly={isReadOnly}
          {...register("freightUnit")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Freight Group</span>
        <input
          className="box-panel"
          placeholder="Freight Group (max 50)"
          maxLength={50}
          readOnly={isReadOnly}
          {...register("freightGroup")}
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

function FreightEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<FreightFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-freight", "detail", state?.id],
    queryFn: () => freightUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        freightCode: detail.freightCode,
        name: detail.name,
        nameEn: detail.nameEn ?? "",
        description: detail.description ?? "",
        freightUnit: detail.freightUnit ?? "",
        freightGroup: detail.freightGroup ?? "",
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
    mutationFn: (req: CreateFreightRequestDto) => freightUseCases.create(req),
    onSuccess: () => {
      toast.success("운임이 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateFreightRequestDto }) =>
      freightUseCases.update(id, req),
    onSuccess: () => {
      toast.success("운임이 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => freightUseCases.delete(id),
    onSuccess: () => {
      toast.success("운임이 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: FreightFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdateFreightRequestDto = {
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        description: parseNullable(values.description),
        freightUnit: parseNullable(values.freightUnit),
        freightGroup: parseNullable(values.freightGroup),
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateFreightRequestDto = {
        freightCode: values.freightCode.trim(),
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        description: parseNullable(values.description),
        freightUnit: parseNullable(values.freightUnit),
        freightGroup: parseNullable(values.freightGroup),
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "운임 삭제",
      description: `${getValues("freightCode")} / ${getValues("name")} 을 삭제하시겠습니까?`,
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
              Deleted freight (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <FreightFormFields register={register} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CODE_FREIGHT_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CODE_FREIGHT_UPDATE" : "BTN_ADMIN_CODE_FREIGHT_CREATE"}
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

export function FreightEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "운임 수정" : "운임 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <FreightEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
