"use client";

import { Button } from "@/components/shared/button";
import { useModalDrag } from "@/components/shared/use-modal-drag";

interface ConfirmModalProps {
  isOpen: boolean;
  title?: string;
  message: string;
  confirmLabel?: string;
  onConfirm: () => void;
  onClose: () => void;
}

// ── Modal Inner (항상 mount 상태에서 실행 — outer가 isOpen 가드) ──────────
function ConfirmModalInner({
  title = "확인",
  message,
  confirmLabel = "OK",
  onConfirm,
  onClose,
}: Omit<ConfirmModalProps, "isOpen">) {
  void onClose;
  const { offset, onHeaderMouseDown } = useModalDrag();

  return (
    <div className="modal-backdrop">
      <div className="modal" style={{ transform: `translate(${offset.x}px, ${offset.y}px)` }}>
        <div
          className="modal__header"
          style={{ cursor: "move", userSelect: "none" }}
          onMouseDown={onHeaderMouseDown}
        >
          <span className="modal__title">{title}</span>
        </div>
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

// ── Modal 본체 (outer — isOpen 가드, mount 시 offset 0,0 reset 보장) ───────
export function ConfirmModal({ isOpen, ...props }: ConfirmModalProps) {
  if (!isOpen) return null;
  return <ConfirmModalInner {...props} />;
}
