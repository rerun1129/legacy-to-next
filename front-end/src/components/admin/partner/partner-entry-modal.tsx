"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useQuery, useMutation } from "@tanstack/react-query";
import { ModalShell } from "@/components/shared/modal-shell";
import { Button } from "@/components/shared/button";
import { confirm } from "@/components/confirm";
import { toast } from "@/lib/toast-store";
import { partnerUseCases } from "@/application/partner/use-cases";
import type { CreatePartnerRequestDto, UpdatePartnerRequestDto, PartnerType } from "@/domain/partner";

export interface EntryModalState {
  mode: "create" | "edit";
  id?: number;
}

interface Props {
  state: EntryModalState | null;
  onClose: () => void;
  onSaved: () => void;
}

interface PartnerFormValues {
  partnerCode: string;
  partnerType: PartnerType;
  name: string;
  nameEn: string;
  businessNo: string;
  representative: string;
  phone: string;
  email: string;
  address: string;
  memo: string;
  active: boolean;
}

const DEFAULT_FORM: PartnerFormValues = {
  partnerCode: "",
  partnerType: "FORWARDER",
  name: "",
  nameEn: "",
  businessNo: "",
  representative: "",
  phone: "",
  email: "",
  address: "",
  memo: "",
  active: true,
};

const PARTNER_TYPE_OPTIONS: { value: PartnerType; label: string }[] = [
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
  register: ReturnType<typeof useForm<PartnerFormValues>>["register"];
  isEdit: boolean;
  isReadOnly: boolean;
}

function PartnerFormFields({ register, isEdit, isReadOnly }: FormFieldsProps) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
      <div className="lcn">
        <span className="lcn__label">협력사 코드 *</span>
        {/* partnerCode는 edit 모드에서 UX readOnly 스타일. BE updatable=false가 변경 방지 SSOT */}
        <input
          className="text-box text-box--panel"
          placeholder="협력사 코드"
          readOnly={isEdit || isReadOnly}
          style={isEdit ? { background: "var(--surface-2)", color: "var(--ink-3)" } : undefined}
          {...register("partnerCode")}
        />
      </div>
      <div className="lcn">
        <span className="lcn__label">구분 *</span>
        <select className="text-box text-box--panel" disabled={isReadOnly} {...register("partnerType")}>
          {PARTNER_TYPE_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>
      <div className="lcn">
        <span className="lcn__label">협력사명 *</span>
        <input
          className="text-box text-box--panel"
          placeholder="협력사명"
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
function PartnerEntryModalInner({ state, onClose, onSaved }: Props) {
  const isEdit = state?.mode === "edit";

  const form = useForm<PartnerFormValues>({ defaultValues: DEFAULT_FORM });
  const { register, reset, getValues, formState: { isSubmitting } } = form;

  const { data: detail, isLoading: isDetailLoading } = useQuery({
    queryKey: ["admin-partner", "detail", state?.id],
    queryFn: () => partnerUseCases.getById(state!.id!),
    enabled: isEdit && state?.id != null,
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  useEffect(() => {
    if (detail) {
      reset({
        partnerCode: detail.partnerCode,
        partnerType: detail.partnerType,
        name: detail.name,
        nameEn: detail.nameEn ?? "",
        businessNo: detail.businessNo ?? "",
        representative: detail.representative ?? "",
        phone: detail.phone ?? "",
        email: detail.email ?? "",
        address: detail.address ?? "",
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
    mutationFn: (req: CreatePartnerRequestDto) => partnerUseCases.create(req),
    onSuccess: () => {
      toast.success("협력사가 등록되었습니다.");
      onSaved();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: UpdatePartnerRequestDto }) =>
      partnerUseCases.update(id, req),
    onSuccess: () => {
      toast.success("협력사가 수정되었습니다.");
      onSaved();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => partnerUseCases.delete(id),
    onSuccess: () => {
      toast.success("협력사가 삭제되었습니다.");
      onSaved();
    },
  });

  function handleSave(values: PartnerFormValues) {
    if (isEdit && state?.id != null) {
      const req: UpdatePartnerRequestDto = {
        partnerType: values.partnerType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        businessNo: parseNullable(values.businessNo),
        representative: parseNullable(values.representative),
        phone: parseNullable(values.phone),
        email: parseNullable(values.email),
        address: parseNullable(values.address),
        memo: parseNullable(values.memo),
        active: values.active,
      };
      updateMutation.mutate({ id: state.id, req });
    } else {
      const req: CreatePartnerRequestDto = {
        partnerCode: values.partnerCode.trim(),
        partnerType: values.partnerType,
        name: values.name.trim(),
        nameEn: parseNullable(values.nameEn),
        businessNo: parseNullable(values.businessNo),
        representative: parseNullable(values.representative),
        phone: parseNullable(values.phone),
        email: parseNullable(values.email),
        address: parseNullable(values.address),
        memo: parseNullable(values.memo),
        active: values.active,
      };
      createMutation.mutate(req);
    }
  }

  async function handleDelete() {
    if (!state?.id) return;
    const ok = await confirm({
      title: "협력사 삭제",
      description: `${getValues("partnerCode")} / ${getValues("name")} 을 삭제하시겠습니까?`,
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
              삭제된 협력사입니다 (삭제일시: {detail?.deletedAt ?? "—"}). 조회 전용입니다.
            </div>
          )}
          <PartnerFormFields register={register} isEdit={isEdit} isReadOnly={isReadOnly} />
        </form>
      )}
      <div className="modal__footer">
        {isEdit && (
          <Button
            variant="danger"
            size="sm"
            onClick={handleDelete}
            disabled={isBusy || isReadOnly}
          >
            삭제
          </Button>
        )}
        <Button
          variant="modal"
          size="sm"
          onClick={form.handleSubmit(handleSave)}
          disabled={isBusy || isReadOnly}
          loading={createMutation.isPending || updateMutation.isPending}
        >
          저장
        </Button>
        <Button size="sm" onClick={onClose} disabled={isBusy}>
          닫기
        </Button>
      </div>
    </>
  );
}

// ─── 외부 래퍼 (isOpen 가드 — false 시 unmount로 hook·캐시 초기화) ───────────
export function PartnerEntryModal({ state, onClose, onSaved }: Props) {
  const isOpen = state !== null;
  const title = state?.mode === "edit" ? "협력사 수정" : "협력사 등록";
  return (
    <ModalShell isOpen={isOpen} title={title} size="default">
      <PartnerEntryModalInner state={state} onClose={onClose} onSaved={onSaved} />
    </ModalShell>
  );
}
