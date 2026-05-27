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
import { customerUseCases } from "@/application/customer/use-cases";
import { CodeBox } from "@/components/shared/inputs/code-box";
import type { CreateCustomerRequestDto, UpdateCustomerRequestDto, CustomerType } from "@/domain/customer";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface CustomerFormValues {
  customerCode: string;
  customerType: CustomerType;
  name: string;
  nameEn: string;
  businessNo: string;
  representative: string;
  phone: string;
  email: string;
  customerLocalAddress: string;
  customerEnglishAddress: string;
  memo: string;
  countryCode: string;
  active: boolean;
}

const DEFAULT_FORM: CustomerFormValues = {
  customerCode: "",
  customerType: "CUSTOMER",
  name: "",
  nameEn: "",
  businessNo: "",
  representative: "",
  phone: "",
  email: "",
  customerLocalAddress: "",
  customerEnglishAddress: "",
  memo: "",
  countryCode: "",
  active: true,
};

const CUSTOMER_TYPE_OPTIONS: { value: CustomerType; label: string }[] = [
  { value: "CUSTOMER", label: "CUSTOMER" },
  { value: "PARTNER", label: "PARTNER" },
  { value: "AIRCARRIER", label: "AIRCARRIER" },
  { value: "LINER", label: "LINER" },
  { value: "TRUCKER", label: "TRUCKER" },
  { value: "WAREHOUSE", label: "WAREHOUSE" },
  { value: "OTHER", label: "OTHER" },
];

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

// ─── 필드 영역 분리 (11 필드 수용, readOnly 상태 전파) ───────────────────────
interface FormFieldsProps {
  register: ReturnType<typeof useForm<CustomerFormValues>>["register"];
  control: Control<CustomerFormValues>;
  isEdit: boolean;
  isReadOnly: boolean;
}

function CustomerFormFields({ register, control, isEdit, isReadOnly }: FormFieldsProps) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <CodeBox
        kind="code-only"
        label="Customer Code *"
        readOnly={isEdit || isReadOnly}
        onLookup={() => {}}
        codeProps={{
          placeholder: "Customer Code",
          ...register("customerCode"),
          ...(isEdit ? { style: { background: "var(--surface-2)", color: "var(--ink-3)" } } : {}),
        }}
      />
      <div className="lcn">
        <span className="lcn__label">Type *</span>
        <Controller
          name="customerType"
          control={control}
          render={({ field }) => (
            <ComboBox
              variant="panel"
              options={CUSTOMER_TYPE_OPTIONS}
              value={field.value}
              onChange={field.onChange}
              disabled={isReadOnly}
            />
          )}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Customer Name *</span>
        <input
          className="box-panel"
          placeholder="Customer Name"
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
        <span className="lcn__label">Business No.</span>
        <input
          className="box-panel"
          placeholder="Business No. (optional)"
          readOnly={isReadOnly}
          {...register("businessNo")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Representative</span>
        <input
          className="box-panel"
          placeholder="Representative (optional)"
          readOnly={isReadOnly}
          {...register("representative")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Phone</span>
        <input
          className="box-panel"
          placeholder="Phone (optional)"
          readOnly={isReadOnly}
          {...register("phone")}
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
        <span className="lcn__label">Address</span>
        <input
          className="box-panel"
          placeholder="Address (optional)"
          readOnly={isReadOnly}
          {...register("customerLocalAddress")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">English Address</span>
        <input
          className="box-panel"
          placeholder="English Address (optional)"
          readOnly={isReadOnly}
          {...register("customerEnglishAddress")}
        />
      </div>
      <div className="lcn" style={{ alignItems: "flex-start" }}>
        <span className="lcn__label" style={{ paddingTop: 4 }}>Memo</span>
        <textarea
          className="box-panel"
          rows={3}
          placeholder="Memo (optional)"
          style={{ resize: "vertical" }}
          readOnly={isReadOnly}
          {...register("memo")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">Country Code</span>
        <input
          className="box-panel"
          placeholder="Country Code (optional)"
          maxLength={5}
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

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function CustomerEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<CustomerFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, control, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-customer", "detail", state?.id],
    queryFn: () => customerUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        customerCode: detail.customerCode,
        customerType: detail.customerType,
        name: detail.name ?? "",
        nameEn: detail.nameEn ?? "",
        businessNo: detail.businessNo ?? "",
        representative: detail.representative ?? "",
        phone: detail.phone ?? "",
        email: detail.email ?? "",
        customerLocalAddress: detail.customerLocalAddress ?? "",
        customerEnglishAddress: detail.customerEnglishAddress ?? "",
        memo: detail.memo ?? "",
        countryCode: detail.countryCode ?? "",
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
    mutationFn: (req: CreateCustomerRequestDto) => customerUseCases.create(req),
    onSuccess: () => {
      toast.success("고객이 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdateCustomerRequestDto }) =>
      customerUseCases.update(id, req),
    onSuccess: () => {
      toast.success("고객이 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => customerUseCases.delete(id),
    onSuccess: () => {
      toast.success("고객이 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: CustomerFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdateCustomerRequestDto = {
        customerType: values.customerType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        businessNo: parseNullable(values.businessNo),
        representative: parseNullable(values.representative),
        phone: parseNullable(values.phone),
        email: parseNullable(values.email),
        customerLocalAddress: parseNullable(values.customerLocalAddress),
        customerEnglishAddress: parseNullable(values.customerEnglishAddress),
        memo: parseNullable(values.memo),
        countryCode: parseNullable(values.countryCode),
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreateCustomerRequestDto = {
        customerCode: values.customerCode.trim(),
        customerType: values.customerType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        businessNo: parseNullable(values.businessNo),
        representative: parseNullable(values.representative),
        phone: parseNullable(values.phone),
        email: parseNullable(values.email),
        customerLocalAddress: parseNullable(values.customerLocalAddress),
        customerEnglishAddress: parseNullable(values.customerEnglishAddress),
        memo: parseNullable(values.memo),
        countryCode: parseNullable(values.countryCode),
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "고객 삭제",
      description: `${getValues("customerCode")} / ${getValues("name")} 을 삭제하시겠습니까?`,
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
              Deleted customer (deleted at: {detail?.deletedAt ?? "—"}). Read only.
            </div>
          )}
          <CustomerFormFields register={register} control={control} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <ActionButton
            buttonCode="BTN_ADMIN_CUSTOMER_LIST_DELETE"
            className="btn btn--danger btn--sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </ActionButton>
        )}
        <ActionButton
          buttonCode={isEdit ? "BTN_ADMIN_CUSTOMER_LIST_UPDATE" : "BTN_ADMIN_CUSTOMER_LIST_CREATE"}
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
export function CustomerEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "고객 수정" : "고객 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="md">
      <CustomerEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
