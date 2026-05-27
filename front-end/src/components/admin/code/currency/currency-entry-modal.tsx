"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { currencyUseCases } from "@/application/code/currency/use-cases";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CreateCurrencyRequestDto, UpdateCurrencyRequestDto } from "@/domain/code/currency";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface CurrencyFormValues {
  currencyCode: string;
  name: string;
  nameEn: string;
  symbol: string;
  currencyUnit: number | null;
  active: boolean;
}

const DEFAULT_FORM: CurrencyFormValues = {
  currencyCode: "",
  name: "",
  nameEn: "",
  symbol: "",
  currencyUnit: null,
  active: true,
};

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function CurrencyFormFields({
  register,
  isEdit,
  isReadOnly,
}: {
  register: ReturnType<typeof useForm<CurrencyFormValues>>["register"];
  isEdit: boolean;
  isReadOnly: boolean;
}) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <CodeBox
        kind="code-only"
        label="Currency Code *"
        readOnly={isEdit || isReadOnly}
        onLookup={() => {}}
        codeProps={{
          placeholder: "e.g. USD",
          ...register("currencyCode"),
          ...(isEdit ? { style: { background: "var(--surface-2)", color: "var(--ink-3)" } } : {}),
        }}
      />
      <div className="lcn">
        <span className="lcn__label">Currency Name *</span>
        <input
          className="box-panel"
          placeholder="Currency Name"
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
        <span className="lcn__label">Symbol</span>
        <input
          className="box-panel"
          placeholder="Symbol (e.g. $)"
          readOnly={isReadOnly}
          {...register("symbol")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Currency Unit</span>
        <input
          className="box-panel"
          type="number"
          placeholder="Currency Unit (optional)"
          readOnly={isReadOnly}
          {...register("currencyUnit", { valueAsNumber: true })}
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

function CurrencyEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<CurrencyFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-currency", "detail", state?.id],
    queryFn: () => currencyUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        currencyCode: detail.currencyCode,
        name: detail.name ?? "",
        nameEn: detail.nameEn ?? "",
        symbol: detail.symbol ?? "",
        currencyUnit: detail.currencyUnit,
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
    mutationFn: (req: CreateCurrencyRequestDto) => currencyUseCases.create(req),
    onSuccess: () => {
      toast.success("통화가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateCurrencyRequestDto }) =>
      currencyUseCases.update(id, req),
    onSuccess: () => {
      toast.success("통화가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => currencyUseCases.delete(id),
    onSuccess: () => {
      toast.success("통화가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: CurrencyFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdateCurrencyRequestDto = {
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        symbol: parseNullable(values.symbol),
        currencyUnit: Number.isNaN(values.currencyUnit) ? null : values.currencyUnit,
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateCurrencyRequestDto = {
        currencyCode: values.currencyCode.trim().toUpperCase(),
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        symbol: parseNullable(values.symbol),
        currencyUnit: Number.isNaN(values.currencyUnit) ? null : values.currencyUnit,
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "통화 삭제",
      description: `${getValues("currencyCode")} / ${getValues("name")} 을 삭제하시겠습니까?`,
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
              Deleted currency (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <CurrencyFormFields register={register} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CODE_CURRENCY_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CODE_CURRENCY_UPDATE" : "BTN_ADMIN_CODE_CURRENCY_CREATE"}
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

export function CurrencyEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "통화 수정" : "통화 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <CurrencyEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
