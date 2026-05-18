"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { ActionButton } from "@/components/admin/access/action-button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { customerUseCases } from "@/application/customer/use-cases";
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
  address: string;
  addressEn: string;
  memo: string;
  active: boolean;
}

const DEFAULT_FORM: CustomerFormValues = {
  customerCode: "",
  customerType: "FORWARDER",
  name: "",
  nameEn: "",
  businessNo: "",
  representative: "",
  phone: "",
  email: "",
  address: "",
  addressEn: "",
  memo: "",
  active: true,
};

const CUSTOMER_TYPE_OPTIONS: { value: CustomerType; label: string }[] = [
  { value: "FORWARDER", label: "FORWARDER" },
  { value: "SHIPPER", label: "SHIPPER" },
  { value: "CONSIGNEE", label: "CONSIGNEE" },
  { value: "CARRIER", label: "CARRIER" },
  { value: "AGENT", label: "AGENT" },
  { value: "CUSTOMS_BROKER", label: "CUSTOMS_BROKER" },
];

function parseNullable(v: string): string | null {
  return v.trim() === "" ? null : v.trim();
}

// ─── 필드 영역 분리 (11 필드 수용, readOnly 상태 전파) ───────────────────────
interface FormFieldsProps {
  register: ReturnType<typeof useForm<CustomerFormValues>>["register"];
  isEdit: boolean;
  isReadOnly: boolean;
}

function CustomerFormFields({ register, isEdit, isReadOnly }: FormFieldsProps) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div className="lcn">
        <span className="lcn__label">고객 코드 *</span>
        {/* customerCode는 edit 모드에서 UX readOnly 스타일. BE updatable=false가 변경 방지 SSOT */}
        <input
          className="text-box text-box--panel"
          placeholder="고객 코드"
          readOnly={isEdit || isReadOnly}
          style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
          {...register("customerCode")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">구분 *</span>
        <select className="text-box text-box--panel" disabled={isReadOnly} {...register("customerType")}>
          {CUSTOMER_TYPE_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>
      <div className="lcn">
        <span className="lcn__label">고객명 *</span>
        <input
          className="text-box text-box--panel"
          placeholder="고객명"
          readOnly={isReadOnly}
          {...register("name")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">영문명</span>
        <input
          className="text-box text-box--panel"
          placeholder="영문명 (선택)"
          readOnly={isReadOnly}
          {...register("nameEn")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">사업자번호</span>
        <input
          className="text-box text-box--panel"
          placeholder="사업자번호 (선택)"
          readOnly={isReadOnly}
          {...register("businessNo")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">대표자</span>
        <input
          className="text-box text-box--panel"
          placeholder="대표자 (선택)"
          readOnly={isReadOnly}
          {...register("representative")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">전화번호</span>
        <input
          className="text-box text-box--panel"
          placeholder="전화번호 (선택)"
          readOnly={isReadOnly}
          {...register("phone")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">이메일</span>
        <input
          className="text-box text-box--panel"
          placeholder="이메일 (선택)"
          readOnly={isReadOnly}
          {...register("email")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">주소</span>
        <input
          className="text-box text-box--panel"
          placeholder="주소 (선택)"
          readOnly={isReadOnly}
          {...register("address")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">영문 주소</span>
        <input
          className="text-box text-box--panel"
          placeholder="영문 주소 (선택)"
          readOnly={isReadOnly}
          {...register("addressEn")}
        />
      </div>
      <div className="lcn" style={{ alignItems: "flex-start" }}>
        <span className="lcn__label" style={{ paddingTop: 4 }}>메모</span>
        <textarea
          className="text-box text-box--panel"
          rows={3}
          placeholder="메모 (선택)"
          style={{ resize: "vertical" }}
          readOnly={isReadOnly}
          {...register("memo")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">활성</span>
        <label style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <input type="checkbox" disabled={isReadOnly} {...register("active")} />
          활성
        </label>
      </div>
    </div>
  );
}

// ─── 모달 내부 (isOpen=true일 때만 mount) ───────────────────────────────────
function CustomerEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<CustomerFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

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
        name: detail.name,
        nameEn: detail.nameEn ?? "",
        businessNo: detail.businessNo ?? "",
        representative: detail.representative ?? "",
        phone: detail.phone ?? "",
        email: detail.email ?? "",
        address: detail.address ?? "",
        addressEn: detail.addressEn ?? "",
        memo: detail.memo ?? "",
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
        address: parseNullable(values.address),
        addressEn: parseNullable(values.addressEn),
        memo: parseNullable(values.memo),
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
        address: parseNullable(values.address),
        addressEn: parseNullable(values.addressEn),
        memo: parseNullable(values.memo),
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
              삭제된 고객입니다 (삭제일시: {detail?.deletedAt ?? "—"}). 조회 전용입니다.
            </div>
          )}
          <CustomerFormFields register={register} isEdit={isEdit} isReadOnly={isReadOnly} />
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
    <ModalShell isOpen={isOpen} title={title} size="default">
      <CustomerEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
