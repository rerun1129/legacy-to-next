"use client";

import { useEffect } from "react";
import { useForm, FormProvider, Controller, type UseFormReturn } from "react-hook-form";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Save, Search, Trash2, X } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ComboBox, TextBox } from "@/components/shared/inputs";
import { useModalDrag } from "@/components/shared/use-modal-drag";
import { useEnumOptions } from "@/application/enums/use-enum";
import { switchBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { SwitchBlPartyPanel } from "./switch-bl-party-panel";
import { SwitchBlMarksPanel } from "./switch-bl-marks-panel";
import { SwitchBlDescPanel } from "./switch-bl-desc-panel";

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
  incoterms: string;
  blType: string;
}

interface InitialFromHouseBl {
  shipperCode: string;
  shipperAddress: string;
  consigneeCode: string;
  consigneeAddress: string;
  notifyCode: string;
  notifyAddress: string;
  marks: string;
  natureQuantity: string;
  incoterms: string;
  blType: string;
}

interface SwitchBlModalProps {
  houseBlId: number;
  houseBlNo: string;
  isExp: boolean;
  isOpen: boolean;
  onClose: () => void;
  initialFromHouseBl: InitialFromHouseBl;
}

// ── Toolbar (Switch B/L No | House B/L No | Incoterms | B/L Type) ─────────
interface ToolbarProps {
  houseBlNo: string;
  form: UseFormReturn<SwitchBlFormValues>;
}

function SwitchBlToolbar({ houseBlNo, form }: ToolbarProps) {
  const { control } = form;
  const { options: incotermsOptions, placeholder: incotermsPh } = useEnumOptions("Incoterms");
  const { options: blTypeOptions,    placeholder: blTypePh }    = useEnumOptions("BlType");

  return (
    <div className="toolbar" style={{ gridTemplateColumns: "repeat(4, 1fr)" }}>
      <div className="field is-required">
        <div className="field__label is-required">Switch B/L No</div>
        <div className="field__input">
          <Controller
            name="switchBlNo"
            control={control}
            render={({ field }) => (
              <TextBox
                placeholder="Switch B/L No"
                value={field.value ?? ""}
                onChange={field.onChange}
                onBlur={field.onBlur}
              />
            )}
          />
        </div>
      </div>
      <div className="field">
        <div className="field__label">House B/L No</div>
        <div className="field__input">
          <input value={houseBlNo} readOnly />
        </div>
      </div>
      <div className="field">
        <div className="field__label">Incoterms</div>
        <div className="field__input">
          <Controller
            name="incoterms"
            control={control}
            render={({ field }) => (
              <ComboBox
                options={incotermsOptions}
                placeholder={incotermsPh}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        </div>
      </div>
      <div className="field is-required">
        <div className="field__label is-required">B/L Type</div>
        <div className="field__input">
          <Controller
            name="blType"
            control={control}
            render={({ field }) => (
              <ComboBox
                options={blTypeOptions}
                placeholder={blTypePh}
                value={field.value ?? ""}
                onChange={field.onChange}
              />
            )}
          />
        </div>
      </div>
    </div>
  );
}

// ── Modal Inner (항상 mount 상태에서 실행 — outer가 isOpen 가드) ──────────
function SwitchBlModalInner({ houseBlId, houseBlNo, isExp, onClose, initialFromHouseBl }: Omit<SwitchBlModalProps, "isOpen">) {
  const { offset, onHeaderMouseDown } = useModalDrag();

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
      incoterms: "",
      blType: "",
    },
  });

  const { data: existing, isLoading, refetch } = useQuery({
    queryKey: ["switch-bl", "byHouseBl", houseBlId],
    queryFn: () => switchBlPort.getByHouseBlId(houseBlId),
  });

  // 서버 데이터 로드 시 폼 reset
  // initialFromHouseBl은 모달 mount 시점 한 번만 바인딩되므로 deps 제외
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
        incoterms: "",
        blType: "",
      });
    } else if (existing === null) {
      // 미존재 CREATE 모드: Switch B/L No만 비우고 나머지는 House B/L 폼 값으로 초기 바인딩
      form.reset({ switchBlNo: "", ...initialFromHouseBl });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
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
      <div
        className="modal modal--lg"
        style={{ transform: `translate(${offset.x}px, ${offset.y}px)` }}
      >
        {/* Header (title only, drag handle) */}
        <div
          className="modal__header"
          style={{ cursor: "move", userSelect: "none" }}
          onMouseDown={onHeaderMouseDown}
        >
          <span className="modal__title">SEA Switch B/L Management</span>
        </div>

        {/* Body */}
        {isLoading ? (
          <div className="modal__loading">Loading...</div>
        ) : (
          <FormProvider {...form}>
            <SwitchBlToolbar houseBlNo={houseBlNo} form={form} />
            <form
              onSubmit={form.handleSubmit(handleSubmit)}
              className="modal__body modal__body--2col"
            >
              {/* 좌측 (3): Party */}
              <div className="modal__col">
                <SwitchBlPartyPanel isExp={isExp} />
              </div>
              {/* 우측 (2): Marks + Description */}
              <div className="modal__col">
                <SwitchBlMarksPanel />
                <SwitchBlDescPanel />
              </div>
            </form>
          </FormProvider>
        )}

        {/* Footer (Delete / Save / Close) */}
        <div className="modal__footer">
          {isUpdateMode && (
            <Button
              variant="danger"
              size="sm"
              leftIcon={<Trash2 size={12} />}
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
            >
              Delete
            </Button>
          )}
          <Button
            type="button"
            variant="transaction"
            size="sm"
            leftIcon={<Save size={12} />}
            onClick={form.handleSubmit(handleSubmit)}
            disabled={saveMutation.isPending || isLoading}
          >
            {saveMutation.isPending ? "Saving..." : "Save"}
          </Button>
          <Button
            type="button"
            size="sm"
            leftIcon={<Search size={12} />}
            onClick={() => refetch()}
            disabled={isLoading || saveMutation.isPending}
          >
            Search
          </Button>
          <Button
            type="button"
            size="sm"
            leftIcon={<X size={12} />}
            onClick={onClose}
          >
            Close
          </Button>
        </div>
      </div>
    </div>
  );
}

// ── Modal 본체 (outer — isOpen 가드, mount 시 offset 0,0 reset 보장) ───────
// isOpen=false 시 unmount로 useQuery 캐시가 초기화되어 재진입 시 재조회됨
export function SwitchBlModal({ isOpen, ...props }: SwitchBlModalProps) {
  if (!isOpen) return null;
  return <SwitchBlModalInner {...props} />;
}
