"use client";

import { useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import type { Control } from "react-hook-form";
import { ComboBox } from "@/components/shared/inputs/combo-box";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { portUseCases } from "@/application/code/port/use-cases";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CreatePortRequestDto, UpdatePortRequestDto, PortType } from "@/domain/code/port";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface PortFormValues {
  portCode: string;
  portType: PortType;
  name: string;
  nameEn: string;
  countryCode: string;
  active: boolean;
}

const DEFAULT_FORM: PortFormValues = {
  portCode: "",
  portType: "SEA",
  name: "",
  nameEn: "",
  countryCode: "",
  active: true,
};

const PORT_TYPE_OPTIONS: { value: PortType; label: string }[] = [
  { value: "SEA", label: "SEA" },
  { value: "AIR", label: "AIR" },
];

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

interface FormFieldsProps {
  register: ReturnType<typeof useForm<PortFormValues>>["register"];
  control: Control<PortFormValues>;
  isEdit: boolean;
  isReadOnly: boolean;
}

function PortFormFields({ register, control, isEdit, isReadOnly }: FormFieldsProps) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <CodeBox
        kind="code-only"
        label="Port Code *"
        readOnly={isEdit || isReadOnly}
        onLookup={() => {}}
        codeProps={{
          placeholder: "Port Code",
          ...register("portCode"),
          ...(isEdit ? { style: { background: "var(--surface-2)", color: "var(--ink-3)" } } : {}),
        }}
      />
      <div className="lcn">
        <span className="lcn__label">Type *</span>
        <Controller
          name="portType"
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={PORT_TYPE_OPTIONS}
              value={field.value}
              onChange={field.onChange}
              disabled={isReadOnly}
            />
          )}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Port Name *</span>
        <input
          className="box-panel"
          placeholder="Port Name"
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
        <span className="lcn__label">Country Code</span>
        <input
          className="box-panel"
          placeholder="3-letter country code"
          maxLength={3}
          readOnly={isReadOnly}
          {...register("countryCode")}
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

function PortEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<PortFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, control, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-port", "detail", state?.id],
    queryFn: () => portUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        portCode: detail.portCode,
        portType: detail.portType,
        name: detail.name,
        nameEn: detail.nameEn ?? "",
        countryCode: detail.countryCode,
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
    mutationFn: (req: CreatePortRequestDto) => portUseCases.create(req),
    onSuccess: () => {
      toast.success("항구가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdatePortRequestDto }) =>
      portUseCases.update(id, req),
    onSuccess: () => {
      toast.success("항구가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => portUseCases.delete(id),
    onSuccess: () => {
      toast.success("항구가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: PortFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdatePortRequestDto = {
        portType: values.portType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        countryCode: values.countryCode.trim(),
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreatePortRequestDto = {
        portCode: values.portCode.trim(),
        portType: values.portType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        countryCode: values.countryCode.trim(),
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "항구 삭제",
      description: `${getValues("portCode")} / ${getValues("name")} 을 삭제하시겠습니까?`,
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
              Deleted port (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <PortFormFields register={register} control={control} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CODE_PORT_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CODE_PORT_UPDATE" : "BTN_ADMIN_CODE_PORT_CREATE"}
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

export function PortEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "항구 수정" : "항구 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <PortEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
