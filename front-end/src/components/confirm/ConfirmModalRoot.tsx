"use client";

import { useRef } from "react";
import * as Dialog from "@radix-ui/react-dialog";
import { useConfirmStore } from "./confirmStore";
import { ConfirmModal } from "./ConfirmModal";

export function ConfirmModalRoot() {
  const cancelRef = useRef<HTMLButtonElement>(null);

  const open = useConfirmStore((s) => s.open);
  const options = useConfirmStore((s) => s.options);
  const loading = useConfirmStore((s) => s.loading);
  const handleConfirm = useConfirmStore((s) => s.handleConfirm);
  const handleCancel = useConfirmStore((s) => s.handleCancel);

  return (
    <Dialog.Root
      open={open}
      onOpenChange={(o) => {
        // ESC / 백드롭 클릭 → handleCancel (loading 중에는 store에서 무시)
        if (!o) handleCancel();
      }}
    >
      {options && (
        <ConfirmModal
          open={open}
          variant={options.variant ?? "default"}
          title={options.title}
          description={options.description}
          details={options.details}
          confirmText={options.confirmText ?? "확인"}
          cancelText={options.cancelText ?? "취소"}
          loading={loading}
          onConfirm={handleConfirm}
          onCancel={handleCancel}
          cancelRef={cancelRef}
        />
      )}
    </Dialog.Root>
  );
}
