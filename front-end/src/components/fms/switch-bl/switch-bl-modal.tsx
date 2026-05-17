"use client";

import { FormProvider } from "react-hook-form";
import { Save, Search, Trash2, X } from "lucide-react";
import { Button } from "@/components/shared/button";
import { ModalShell } from "@/components/shared/modal-shell";
import { SwitchBlPartyPanel } from "./switch-bl-party-panel";
import { SwitchBlMarksPanel } from "./switch-bl-marks-panel";
import { SwitchBlDescPanel } from "./switch-bl-desc-panel";
import { SwitchBlToolbar } from "./switch-bl-toolbar";
import { useSwitchBlForm } from "./use-switch-bl-form";

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

export interface InitialFromHouseBl {
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

// ── Modal Inner (항상 mount 상태에서 실행 — outer가 isOpen 가드) ──────────
function SwitchBlModalInner({ houseBlId, houseBlNo, isExp, onClose, initialFromHouseBl }: Omit<SwitchBlModalProps, "isOpen">) {
  const {
    form,
    isLoading,
    isUpdateMode,
    saveMutation,
    deleteMutation,
    refetch,
    handleSubmit,
    handleDelete,
  } = useSwitchBlForm({ houseBlId, initialFromHouseBl, onClose });

  return (
    <>
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

      {/* Footer (Delete / Save / Search / Close) */}
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
    </>
  );
}

// ── Modal 본체 (outer — isOpen 가드, mount 시 offset 0,0 reset 보장) ───────
// isOpen=false 시 unmount로 useQuery 캐시가 초기화되어 재진입 시 재조회됨
export function SwitchBlModal({ isOpen, ...props }: SwitchBlModalProps) {
  return (
    <ModalShell isOpen={isOpen} title="SEA Switch B/L Management" size="lg">
      <SwitchBlModalInner {...props} />
    </ModalShell>
  );
}
