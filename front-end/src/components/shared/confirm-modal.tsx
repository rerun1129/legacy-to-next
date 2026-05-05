"use client";

import { Button } from "@/components/shared/button";

interface ConfirmModalProps {
  isOpen: boolean;
  title?: string;
  message: string;
  confirmLabel?: string;
  onConfirm: () => void;
  onClose: () => void;
}

export function ConfirmModal({
  isOpen,
  title = "확인",
  message,
  confirmLabel = "OK",
  onConfirm,
  onClose,
}: ConfirmModalProps) {
  if (!isOpen) return null;

  return (
    <div className="modal-backdrop">
      <div className="modal">
        <div className="modal__title">{title}</div>
        <div className="modal__body">{message}</div>
        <div className="modal__actions">
          <Button variant="transaction" onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}
