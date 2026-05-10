"use client";

import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Save, X } from "lucide-react";
import { nonBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";

interface ChangeBlNoModalProps {
  houseBlId: number;
  currentHblNo: string | null | undefined;
  isOpen: boolean;
  onClose: () => void;
  onChanged?: () => void;
}

interface FormValues { newHblNo: string; }

export function ChangeBlNoModal({ houseBlId, currentHblNo, isOpen, onClose, onChanged }: ChangeBlNoModalProps) {
  const queryClient = useQueryClient();
  // BE SSOT — zodResolver/required/pattern 사용 금지
  const form = useForm<FormValues>({ defaultValues: { newHblNo: "" } });

  useEffect(() => {
    if (isOpen) form.reset({ newHblNo: "" });
  }, [isOpen, form]);

  const mutation = useMutation({
    mutationFn: (values: FormValues) => nonBlPort.changeHblNo(houseBlId, values.newHblNo),
    onSuccess: () => {
      toast.success("B/L No가 변경되었습니다.");
      queryClient.invalidateQueries({ queryKey: ["non-bl", "detail", houseBlId] });
      // list 자동 invalidate 금지 — Entry mutation 후 사용자가 Search로 직접 재조회
      onChanged?.();
      onClose();
    },
    onError: (e: Error) => toast.error(`변경 실패: ${e.message}`),
  });

  if (!isOpen) return null;

  function handleSubmit(values: FormValues) { mutation.mutate(values); }

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal">
        <div className="modal__title">Change B/L No</div>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="modal__body">
          <div className="field">
            <div className="field__label">현재 B/L No</div>
            <div className="field__input">
              <input value={currentHblNo ?? ""} readOnly />
            </div>
          </div>
          <div className="field">
            <div className="field__label">변경할 B/L No</div>
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
            <Save size={12} />{mutation.isPending ? "Updating..." : "Update"}
          </button>
          <button type="button" className="btn btn--sm" onClick={onClose}>
            <X size={12} />Close
          </button>
        </div>
      </div>
    </div>
  );
}
