"use client";

import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Save, X } from "lucide-react";
import { masterBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { ModalShell } from "@/components/shared/modal-shell";

interface MasterChangeBlNoModalProps {
  masterBlId: number;
  currentMblNo: string | null | undefined;
  currentMasterRefNo: string | null | undefined;
  blNoLabel: string;     // "MBL No" | "MAWB No" (modeLabels.blNo)
  isOpen: boolean;
  onClose: () => void;
  onChanged?: () => void;
}

interface FormValues {
  newMblNo: string;
  newMasterRefNo: string;
}

// ── Modal Inner (항상 mount 상태에서 실행 — outer가 isOpen 가드) ──────────
function MasterChangeBlNoModalInner({
  masterBlId,
  currentMblNo,
  currentMasterRefNo,
  blNoLabel,
  onClose,
  onChanged,
}: Omit<MasterChangeBlNoModalProps, "isOpen">) {
  const queryClient = useQueryClient();
  // BE SSOT — zodResolver/required/pattern 사용 금지
  const form = useForm<FormValues>({ defaultValues: { newMblNo: "", newMasterRefNo: "" } });

  const mutation = useMutation({
    mutationFn: (values: FormValues) =>
      masterBlPort.changeMblNo(masterBlId, values.newMblNo, values.newMasterRefNo),
    onSuccess: () => {
      toast.success("B/L No가 변경되었습니다.");
      // List 자동 invalidate 금지 — detail만 갱신
      queryClient.invalidateQueries({ queryKey: ["master-bl", "detail", masterBlId] });
      onChanged?.();
      onClose();
    },
  });

  function handleSubmit(values: FormValues) { mutation.mutate(values); }

  return (
    <>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="modal__body">
        <div className="field">
          <div className="field__label">현재 {blNoLabel}</div>
          <div className="field__input">
            <input value={currentMblNo ?? ""} readOnly />
          </div>
        </div>
        <div className="field">
          <div className="field__label">변경할 {blNoLabel}</div>
          <div className="field__input">
            <input {...form.register("newMblNo")} placeholder={`New ${blNoLabel}`} autoFocus />
          </div>
        </div>
        <div className="field">
          <div className="field__label">현재 Master Ref</div>
          <div className="field__input">
            <input value={currentMasterRefNo ?? ""} readOnly />
          </div>
        </div>
        <div className="field">
          <div className="field__label">변경할 Master Ref</div>
          <div className="field__input">
            <input {...form.register("newMasterRefNo")} placeholder="New Master Ref" />
          </div>
        </div>
      </form>
      <div className="modal__actions">
        <button
          type="button"
          className="btn btn--sm btn--primary"
          onClick={form.handleSubmit(handleSubmit)}
          disabled={mutation.isPending}
        >
          <Save size={12} />{mutation.isPending ? "Updating..." : "Update"}
        </button>
        <button type="button" className="btn btn--sm" onClick={onClose}>
          <X size={12} />Close
        </button>
      </div>
    </>
  );
}

// ── Modal 본체 (outer — isOpen 가드, mount 시 offset 0,0 reset 보장) ───────
export function MasterChangeBlNoModal({ isOpen, ...props }: MasterChangeBlNoModalProps) {
  return (
    <ModalShell isOpen={isOpen} title={`Change ${props.blNoLabel}`}>
      <MasterChangeBlNoModalInner {...props} />
    </ModalShell>
  );
}
