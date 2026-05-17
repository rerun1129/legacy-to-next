"use client";

import type { RefObject } from "react";
import * as Dialog from "@radix-ui/react-dialog";
import { Trash2, AlertTriangle } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/shared/button";
import { useModalDrag } from "@/components/shared/use-modal-drag";
import type { ConfirmOptions, ConfirmVariant } from "./confirmStore";

export interface ConfirmModalProps {
  open: boolean;
  variant: ConfirmVariant;
  title: string;
  description?: string;
  details?: ConfirmOptions["details"];
  confirmText: string;
  cancelText: string;
  loading: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  cancelRef: RefObject<HTMLButtonElement | null>;
}

function ConfirmDetails({ details }: { details: NonNullable<ConfirmOptions["details"]> }) {
  return (
    <dl className="confirm-details">
      {details.map(([k, v]) => (
        <div key={k} className="confirm-details__row">
          <dt>{k}</dt>
          <dd>{v}</dd>
        </div>
      ))}
    </dl>
  );
}

interface ContentProps extends Omit<ConfirmModalProps, "open" | "variant"> {
  onHeaderMouseDown: (e: React.MouseEvent<HTMLDivElement>) => void;
}

function DefaultContent({
  title,
  description,
  details,
  confirmText,
  cancelText,
  loading,
  onConfirm,
  onCancel,
  cancelRef,
  onHeaderMouseDown,
}: ContentProps) {
  return (
    <>
      <div
        className="confirm-header"
        style={{ cursor: "move", userSelect: "none" }}
        onMouseDown={onHeaderMouseDown}
      >
        <Dialog.Title className="confirm-title">{title}</Dialog.Title>
      </div>
      {description && (
        <Dialog.Description className="confirm-description">
          {description}
        </Dialog.Description>
      )}
      {details && <ConfirmDetails details={details} />}
      <div className="confirm-actions">
        <button
          ref={cancelRef}
          className="btn"
          disabled={loading}
          onClick={onCancel}
        >
          {cancelText}
        </button>
        <Button loading={loading} variant="transaction" onClick={onConfirm}>
          {confirmText}
        </Button>
      </div>
    </>
  );
}

function DestructiveContent({
  title,
  description,
  details,
  confirmText,
  cancelText,
  loading,
  onConfirm,
  onCancel,
  cancelRef,
  onHeaderMouseDown,
}: ContentProps) {
  return (
    <>
      <div
        className="confirm-header"
        style={{ cursor: "move", userSelect: "none" }}
        onMouseDown={onHeaderMouseDown}
      >
        <div className="confirm-icon confirm-icon--danger" aria-hidden>
          <Trash2 size={20} aria-hidden />
        </div>
        <Dialog.Title className="confirm-title">{title}</Dialog.Title>
      </div>
      {description && (
        <Dialog.Description className="confirm-description">
          {description}
        </Dialog.Description>
      )}
      {details && <ConfirmDetails details={details} />}
      <div className="confirm-actions">
        <button
          ref={cancelRef}
          className="btn"
          disabled={loading}
          onClick={onCancel}
        >
          {cancelText}
        </button>
        <Button loading={loading} variant="danger" onClick={onConfirm}>
          {confirmText}
        </Button>
      </div>
    </>
  );
}

function WarningContent({
  title,
  description,
  details,
  confirmText,
  cancelText,
  loading,
  onConfirm,
  onCancel,
  cancelRef,
  onHeaderMouseDown,
}: ContentProps) {
  return (
    <>
      <div
        className="confirm-header"
        style={{ cursor: "move", userSelect: "none" }}
        onMouseDown={onHeaderMouseDown}
      >
        <div className="confirm-icon confirm-icon--warn" aria-hidden>
          <AlertTriangle size={20} aria-hidden />
        </div>
        <Dialog.Title className="confirm-title">{title}</Dialog.Title>
      </div>
      {description && (
        <Dialog.Description className="confirm-description">
          {description}
        </Dialog.Description>
      )}
      {details && <ConfirmDetails details={details} />}
      <div className="confirm-actions">
        <button
          ref={cancelRef}
          className="btn"
          disabled={loading}
          onClick={onCancel}
        >
          {cancelText}
        </button>
        <Button loading={loading} variant="transaction" onClick={onConfirm}>
          {confirmText}
        </Button>
      </div>
    </>
  );
}

export function ConfirmModal({
  open,
  variant,
  title,
  description,
  details,
  confirmText,
  cancelText,
  loading,
  onConfirm,
  onCancel,
  cancelRef,
}: ConfirmModalProps) {
  const { offset, onHeaderMouseDown } = useModalDrag();

  const contentProps: ContentProps = {
    title,
    description,
    details,
    confirmText,
    cancelText,
    loading,
    onConfirm,
    onCancel,
    cancelRef,
    onHeaderMouseDown,
  };

  // 기존 confirm.css: transform: translate(-50%, -50%) 에 드래그 offset 합성
  const transform = `translate(calc(-50% + ${offset.x}px), calc(-50% + ${offset.y}px))`;

  return (
    <Dialog.Portal>
      <Dialog.Overlay className="confirm-overlay" />
      <Dialog.Content
        className={cn("confirm-panel", `confirm-panel--${variant}`)}
        style={{ transform }}
        onOpenAutoFocus={(e) => {
          e.preventDefault();
          cancelRef.current?.focus();
        }}
      >
        {variant === "destructive" && <DestructiveContent {...contentProps} />}
        {variant === "warning" && <WarningContent {...contentProps} />}
        {variant === "default" && <DefaultContent {...contentProps} />}
      </Dialog.Content>
    </Dialog.Portal>
  );
}
