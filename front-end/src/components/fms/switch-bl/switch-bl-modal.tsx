"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Save, Trash2, X } from "lucide-react";
import { Button } from "@/components/shared/button";
import { switchBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { SwitchBlPartyPanel } from "./switch-bl-party-panel";

export interface SwitchBlFormValues {
  switchBlNo: string;
  shipperCode: string;
  shipperAddress: string;
  consigneeCode: string;
  consigneeAddress: string;
  notifyCode: string;
  notifyAddress: string;
  marks: string;
  natureQuantity: string;
}

interface SwitchBlModalProps {
  houseBlId: number;
  isOpen: boolean;
  onClose: () => void;
}

// ── Toolbar (Switch B/L No, House B/L No) ─────────────────────────────────
interface ToolbarProps {
  houseBlId: number;
  form: ReturnType<typeof useForm<SwitchBlFormValues>>;
}

function SwitchBlToolbar({ houseBlId, form }: ToolbarProps) {
  const { register } = form;
  return (
    <div className="toolbar">
      <div className="field">
        <div className="field__label">Switch B/L No</div>
        <div className="field__input">
          <input {...register("switchBlNo")} placeholder="Switch B/L No" />
        </div>
      </div>
      <div className="field">
        <div className="field__label">House B/L No</div>
        <div className="field__input">
          {/* read-only: houseBlId 표시 */}
          <input value={String(houseBlId)} readOnly />
        </div>
      </div>
    </div>
  );
}

// ── Marks & Nature 영역 ────────────────────────────────────────────────────
interface DescPanelProps {
  form: ReturnType<typeof useForm<SwitchBlFormValues>>;
}

function SwitchBlDescPanel({ form }: DescPanelProps) {
  const { register } = form;
  return (
    <div className="switch-bl-desc-panel">
      <div className="panel-section">
        <div className="panel-section__title">Marks and Numbers</div>
        <textarea
          {...register("marks")}
          className="panel-section__textarea"
          rows={8}
          placeholder="Marks and Numbers"
        />
      </div>
      <div className="panel-section">
        <div className="panel-section__title">Nature &amp; Quantity of Goods</div>
        <textarea
          {...register("natureQuantity")}
          className="panel-section__textarea"
          rows={8}
          placeholder="Nature & Quantity of Goods"
        />
      </div>
    </div>
  );
}

// ── Modal 본체 ─────────────────────────────────────────────────────────────
export function SwitchBlModal({ houseBlId, isOpen, onClose }: SwitchBlModalProps) {
  const form = useForm<SwitchBlFormValues>({
    defaultValues: {
      switchBlNo: "",
      shipperCode: "",
      shipperAddress: "",
      consigneeCode: "",
      consigneeAddress: "",
      notifyCode: "",
      notifyAddress: "",
      marks: "",
      natureQuantity: "",
    },
  });

  const { data: existing, isLoading } = useQuery({
    queryKey: ["switch-bl", "byHouseBl", houseBlId],
    queryFn: () => switchBlPort.getByHouseBlId(houseBlId),
    enabled: isOpen,
  });

  // 서버 데이터 로드 시 폼 reset
  useEffect(() => {
    if (existing) {
      form.reset({
        switchBlNo: existing.switchBlNo ?? "",
        shipperCode: existing.shipperCode ?? "",
        shipperAddress: existing.shipperAddress ?? "",
        consigneeCode: existing.consigneeCode ?? "",
        consigneeAddress: existing.consigneeAddress ?? "",
        notifyCode: existing.notifyCode ?? "",
        notifyAddress: existing.notifyAddress ?? "",
        marks: existing.description?.marks ?? "",
        natureQuantity: existing.description?.natureQuantity ?? "",
      });
    } else if (existing === null) {
      // 미존재 CREATE 모드: 폼 초기화
      form.reset();
    }
  }, [existing, form]);

  const isUpdateMode = Boolean(existing);

  const saveMutation = useMutation({
    mutationFn: (values: SwitchBlFormValues) => {
      const body = {
        houseBlId,
        switchBlNo: values.switchBlNo || undefined,
        shipperCode: values.shipperCode,
        shipperAddress: values.shipperAddress || undefined,
        consigneeCode: values.consigneeCode || undefined,
        consigneeAddress: values.consigneeAddress || undefined,
        notifyCode: values.notifyCode || undefined,
        notifyAddress: values.notifyAddress || undefined,
        description: (values.marks || values.natureQuantity)
          ? { marks: values.marks || undefined, natureQuantity: values.natureQuantity || undefined }
          : undefined,
      };
      return isUpdateMode
        ? switchBlPort.update(existing!.id, body)
        : switchBlPort.create(body);
    },
    onSuccess: () => {
      toast.success("Switch B/L saved.");
      onClose();
    },
    onError: (e: Error) => {
      toast.error(`Save failed: ${e.message}`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => switchBlPort.delete(existing!.id),
    onSuccess: () => {
      toast.success("Switch B/L deleted.");
      onClose();
    },
    onError: (e: Error) => {
      toast.error(`Delete failed: ${e.message}`);
    },
  });

  if (!isOpen) return null;

  function handleSubmit(values: SwitchBlFormValues) {
    saveMutation.mutate(values);
  }

  function handleDelete() {
    if (window.confirm("Switch B/L을 삭제하시겠습니까?")) {
      deleteMutation.mutate();
    }
  }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal modal--lg">
        {/* Header */}
        <div className="modal__header">
          <span className="modal__title">SEA Switch B/L Management</span>
          <div className="modal__header-actions">
            {isUpdateMode && (
              <Button
                variant="danger"
                size="sm"
                onClick={handleDelete}
                disabled={deleteMutation.isPending}
              >
                <Trash2 size={12} />Delete
              </Button>
            )}
            <button
              type="button"
              className="btn btn--sm btn--primary"
              onClick={form.handleSubmit(handleSubmit)}
              disabled={saveMutation.isPending}
            >
              <Save size={12} />{saveMutation.isPending ? "Saving..." : "Save"}
            </button>
            <button type="button" className="btn btn--sm" onClick={onClose}>
              <X size={12} />Close
            </button>
          </div>
        </div>

        {/* Body */}
        {isLoading ? (
          <div className="modal__loading">Loading...</div>
        ) : (
          <form onSubmit={form.handleSubmit(handleSubmit)} className="modal__body modal__body--2col">
            {/* 좌측: Toolbar + Party */}
            <div className="modal__col">
              <SwitchBlToolbar houseBlId={houseBlId} form={form} />
              <SwitchBlPartyPanel form={form} />
            </div>
            {/* 우측: Marks & Nature */}
            <div className="modal__col">
              <SwitchBlDescPanel form={form} />
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
