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
import { carrierUseCases } from "@/application/code/carrier/use-cases";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CreateCarrierRequestDto, UpdateCarrierRequestDto, CarrierType } from "@/domain/code/carrier";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface CarrierFormValues {
  carrierCode: string;
  carrierType: CarrierType;
  name: string;
  nameEn: string;
  carrierAddress: string;
  ediCode: string;
  active: boolean;
}

const DEFAULT_FORM: CarrierFormValues = {
  carrierCode: "",
  carrierType: "SEA",
  name: "",
  nameEn: "",
  carrierAddress: "",
  ediCode: "",
  active: true,
};

const CARRIER_TYPE_OPTIONS: { value: CarrierType; label: string }[] = [
  { value: "SEA", label: "SEA" },
  { value: "AIR", label: "AIR" },
];

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

interface FormFieldsProps {
  register: ReturnType<typeof useForm<CarrierFormValues>>["register"];
  control: Control<CarrierFormValues>;
  isEdit: boolean;
  isReadOnly: boolean;
}

function CarrierFormFields({ register, control, isEdit, isReadOnly }: FormFieldsProps) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <CodeBox
        kind="code-only"
        label="Carrier Code *"
        readOnly={isEdit || isReadOnly}
        onLookup={() => {}}
        codeProps={{
          placeholder: "Carrier Code",
          ...register("carrierCode"),
          ...(isEdit ? { style: { background: "var(--surface-2)", color: "var(--ink-3)" } } : {}),
        }}
      />
      <div className="lcn">
        <span className="lcn__label">Type *</span>
        <Controller
          name="carrierType"
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={CARRIER_TYPE_OPTIONS}
              value={field.value}
              onChange={field.onChange}
              disabled={isReadOnly}
            />
          )}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Carrier Name *</span>
        <input
          className="box-panel"
          placeholder="Carrier Name"
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
        <span className="lcn__label" style={{ paddingTop: 4 }}>Address</span>
        <textarea
          className="box-panel"
          rows={3}
          placeholder="Carrier Address (optional)"
          style={{ resize: "vertical" }}
          readOnly={isReadOnly}
          {...register("carrierAddress")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">EDI Code</span>
        <input
          className="box-panel"
          placeholder="EDI Code (max 2)"
          maxLength={2}
          readOnly={isReadOnly}
          {...register("ediCode")}
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

function CarrierEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<CarrierFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, control, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-carrier", "detail", state?.id],
    queryFn: () => carrierUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        carrierCode: detail.carrierCode,
        carrierType: detail.carrierType,
        name: detail.name,
        nameEn: detail.nameEn ?? "",
        carrierAddress: detail.carrierAddress ?? "",
        ediCode: detail.ediCode ?? "",
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
    mutationFn: (req: CreateCarrierRequestDto) => carrierUseCases.create(req),
    onSuccess: () => {
      toast.success("선사가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateCarrierRequestDto }) =>
      carrierUseCases.update(id, req),
    onSuccess: () => {
      toast.success("선사가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => carrierUseCases.delete(id),
    onSuccess: () => {
      toast.success("선사가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: CarrierFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdateCarrierRequestDto = {
        carrierType: values.carrierType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        carrierAddress: parseNullable(values.carrierAddress),
        ediCode: parseNullable(values.ediCode),
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateCarrierRequestDto = {
        carrierCode: values.carrierCode.trim(),
        carrierType: values.carrierType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        carrierAddress: parseNullable(values.carrierAddress),
        ediCode: parseNullable(values.ediCode),
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "선사 삭제",
      description: `${getValues("carrierCode")} / ${getValues("name")} 을 삭제하시겠습니까?`,
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
              Deleted carrier (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <CarrierFormFields register={register} control={control} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CODE_CARRIER_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CODE_CARRIER_UPDATE" : "BTN_ADMIN_CODE_CARRIER_CREATE"}
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

export function CarrierEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "선사 수정" : "선사 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <CarrierEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
