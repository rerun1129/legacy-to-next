"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { exchangeRateUseCases } from "@/application/code/exchange-rate/use-cases";
import type { CreateExchangeRateRequestDto, UpdateExchangeRateRequestDto } from "@/domain/code/exchange-rate";
import { ExchangeRateFormFields } from "./exchange-rate-form-fields";
import { type ExchangeRateFormValues, DEFAULT_EXCHANGE_RATE_FORM } from "./exchange-rate-form-types";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

function parseNullableNumber(v: number | null): number | null {
  return v === null || Number.isNaN(v) ? null : v;
}

function ExchangeRateEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<ExchangeRateFormValues>({ defaultValues: DEFAULT_EXCHANGE_RATE_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-code-exchange-rate", "detail", state?.id],
    queryFn: () => exchangeRateUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        fromCurrencyCode: detail.fromCurrencyCode,
        toCurrencyCode: detail.toCurrencyCode,
        exchangeDate: detail.exchangeDate ?? "",
        cashSellExchangeRate: detail.cashSellExchangeRate,
        cashBuyExchangeRate: detail.cashBuyExchangeRate,
        wireSendExchangeRate: detail.wireSendExchangeRate,
        wireReceiveExchangeRate: detail.wireReceiveExchangeRate,
        standardExchangeRate: detail.standardExchangeRate,
        name: detail.name,
        nameEn: detail.nameEn ?? "",
        active: detail.active,
      });
    }
  }, [detail, reset]);

  useEffect(() => {
    if (!isEdit) {
      reset(DEFAULT_EXCHANGE_RATE_FORM);
    }
  }, [isEdit, reset]);

  const createMutation = useMutation({
    mutationFn: (req: CreateExchangeRateRequestDto) => exchangeRateUseCases.create(req),
    onSuccess: () => {
      toast.success("환율이 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateExchangeRateRequestDto }) =>
      exchangeRateUseCases.update(id, req),
    onSuccess: () => {
      toast.success("환율이 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => exchangeRateUseCases.delete(id),
    onSuccess: () => {
      toast.success("환율이 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: ExchangeRateFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdateExchangeRateRequestDto = {
        exchangeDate: parseNullable(values.exchangeDate),
        cashSellExchangeRate: parseNullableNumber(values.cashSellExchangeRate),
        cashBuyExchangeRate: parseNullableNumber(values.cashBuyExchangeRate),
        wireSendExchangeRate: parseNullableNumber(values.wireSendExchangeRate),
        wireReceiveExchangeRate: parseNullableNumber(values.wireReceiveExchangeRate),
        standardExchangeRate: parseNullableNumber(values.standardExchangeRate),
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateExchangeRateRequestDto = {
        fromCurrencyCode: values.fromCurrencyCode.trim().toUpperCase(),
        toCurrencyCode: values.toCurrencyCode.trim().toUpperCase(),
        exchangeDate: parseNullable(values.exchangeDate),
        cashSellExchangeRate: parseNullableNumber(values.cashSellExchangeRate),
        cashBuyExchangeRate: parseNullableNumber(values.cashBuyExchangeRate),
        wireSendExchangeRate: parseNullableNumber(values.wireSendExchangeRate),
        wireReceiveExchangeRate: parseNullableNumber(values.wireReceiveExchangeRate),
        standardExchangeRate: parseNullableNumber(values.standardExchangeRate),
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
      title: "환율 삭제",
      description: `${getValues("fromCurrencyCode")}/${getValues("toCurrencyCode")} 환율을 삭제하시겠습니까?`,
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
              Deleted exchange rate (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <ExchangeRateFormFields register={register} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CODE_EXCHANGE_RATE_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CODE_EXCHANGE_RATE_UPDATE" : "BTN_ADMIN_CODE_EXCHANGE_RATE_CREATE"}
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

export function ExchangeRateEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "환율 수정" : "환율 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <ExchangeRateEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
