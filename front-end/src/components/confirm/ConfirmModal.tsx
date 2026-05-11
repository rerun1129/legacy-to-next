"use client";

import type { RefObject } from "react";
import * as Dialog from "@radix-ui/react-dialog";
import { Trash2, AlertTriangle } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/shared/button";
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
}: Omit<ConfirmModalProps, "open" | "variant">) {
  return (
    <>
      <Dialog.Title className="confirm-title">{title}</Dialog.Title>
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
}: Omit<ConfirmModalProps, "open" | "variant">) {
  return (
    <>
      <div className="confirm-icon confirm-icon--danger" aria-hidden>
        <Trash2 size={20} aria-hidden />
      </div>
      <Dialog.Title className="confirm-title">{title}</Dialog.Title>
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
}: Omit<ConfirmModalProps, "open" | "variant">) {
  return (
    <>
      <div className="confirm-row">
        <div className="confirm-icon confirm-icon--warn" aria-hidden>
          <AlertTriangle size={20} aria-hidden />
        </div>
        <div>
          <Dialog.Title className="confirm-title">{title}</Dialog.Title>
          {description && (
            <Dialog.Description className="confirm-description">
              {description}
            </Dialog.Description>
          )}
          {details && <ConfirmDetails details={details} />}
        </div>
      </div>
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
  const contentProps = {
    title,
    description,
    details,
    confirmText,
    cancelText,
    loading,
    onConfirm,
    onCancel,
    cancelRef,
  };

  return (
    <Dialog.Portal>
      <Dialog.Overlay className="confirm-overlay" />
      <Dialog.Content
        className={cn("confirm-panel", `confirm-panel--${variant}`)}
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
