"use client";

import { useState } from "react";
import { ConfirmModal } from "@/components/shared/confirm-modal";
import { VariantToggle } from "./_variant-toggle";
import { ACTION_SPECIMENS } from "./_actions-data";
import type { BtnVariant, ActionSpecimen } from "./_specimen-types";
import { sectionStyle, labelStyle } from "./_shared";

const VARIANT_CLASS: Record<BtnVariant, string> = {
  search: "btn btn--search",
  transaction: "btn btn--transaction",
  danger: "btn btn--danger",
  normal: "btn btn--normal",
};

interface ActionCardProps {
  specimen: ActionSpecimen;
  onShowModal: (message: string) => void;
}

function ActionCard({ specimen, onShowModal }: ActionCardProps) {
  const [variant, setVariant] = useState<BtnVariant>(specimen.defaultVariant);
  const Icon = specimen.icon;

  return (
    <div className="specimen-card">
      <div style={labelStyle}>{specimen.label}</div>
      <VariantToggle value={variant} onChange={setVariant} />
      <button
        type="button"
        className={VARIANT_CLASS[variant]}
        onClick={() => onShowModal(specimen.confirmMessage)}
      >
        <Icon size={12} />
        {specimen.label}
      </button>
    </div>
  );
}

export function PageHeadActionsGroup() {
  const [modal, setModal] = useState<{ open: boolean; message: string }>({
    open: false,
    message: "",
  });

  function handleShowModal(message: string) {
    setModal({ open: true, message });
  }

  function handleCloseModal() {
    setModal({ open: false, message: "" });
  }

  return (
    <section style={sectionStyle}>
      <div style={labelStyle}>A. Page Head Actions — 유니크 액션 견본</div>
      <div className="specimen-cards">
        {ACTION_SPECIMENS.map((specimen) => (
          <ActionCard
            key={specimen.id}
            specimen={specimen}
            onShowModal={handleShowModal}
          />
        ))}
      </div>

      <ConfirmModal
        isOpen={modal.open}
        message={modal.message}
        onConfirm={handleCloseModal}
        onClose={handleCloseModal}
      />
    </section>
  );
}
