"use client";

import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Save, X } from "lucide-react";
import { useTranslations } from "next-intl";
import { nonBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { ModalShell } from "@/components/shared/modal-shell";

interface ChangeBlNoModalProps {
  houseBlId: number;
  currentHblNo: string | null | undefined;
  isOpen: boolean;
  onClose: () => void;
  onChanged?: () => void;
}

interface FormValues { newHblNo: string; }

// ── Modal Inner (항상 mount 상태에서 실행 — outer가 isOpen 가드) ──────────
function ChangeBlNoModalInner({
  houseBlId,
  currentHblNo,
  onClose,
  onChanged,
}: Omit<ChangeBlNoModalProps, "isOpen">) {
  // Rules of Hooks: ALL hooks unconditionally before any early-return
  const tm = useTranslations("fms.nonBl.entry.modal.changeBlNo");

  const queryClient = useQueryClient();
  // BE SSOT — zodResolver/required/pattern 사용 금지
  const form = useForm<FormValues>({ defaultValues: { newHblNo: "" } });

  const mutation = useMutation({
    mutationFn: (values: FormValues) => nonBlPort.changeHblNo(houseBlId, values.newHblNo),
    onSuccess: () => {
      toast.success(tm("blNoChanged"));
      queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", houseBlId] });
      // list 자동 invalidate 금지 — Entry mutation 후 사용자가 Search로 직접 재조회
      onChanged?.();
      onClose();
    },
  });

  function handleSubmit(values: FormValues) { mutation.mutate(values); }

  return (
    <>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="modal__body">
        <div className="field">
          <div className="field__label">{tm("currentBlNo")}</div>
          <div className="field__input">
            <input value={currentHblNo ?? ""} readOnly />
          </div>
        </div>
        <div className="field">
          <div className="field__label">{tm("newBlNo")}</div>
          <div className="field__input">
            <input {...form.register("newHblNo")} placeholder="New B/L No" autoFocus />
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
          <Save size={12} />{mutation.isPending ? tm("updating") : tm("update")}
        </button>
        <button type="button" className="btn btn--sm" onClick={onClose}>
          <X size={12} />{tm("close")}
        </button>
      </div>
    </>
  );
}

// ── Modal 본체 (outer — isOpen 가드, mount 시 offset 0,0 reset 보장) ───────
export function ChangeBlNoModal({ isOpen, ...props }: ChangeBlNoModalProps) {
  const tm = useTranslations("fms.nonBl.entry.modal.changeBlNo");
  return (
    <ModalShell isOpen={isOpen} title={tm("title")}>
      <ChangeBlNoModalInner {...props} />
    </ModalShell>
  );
}
