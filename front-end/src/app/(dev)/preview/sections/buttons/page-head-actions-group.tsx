"use client";

import { useState } from "react";
import { ConfirmModal } from "@/components/shared/confirm-modal";
import { Button } from "@/components/shared/button";
import { VariantToggle } from "./_variant-toggle";
import { ACTION_SPECIMENS } from "./_actions-data";
import type { ToggleableVariant, ActionSpecimen } from "./_specimen-types";
import { sectionStyle, labelStyle } from "./_shared";

interface ActionCardProps {
  specimen: ActionSpecimen;
  onShowModal: (message: string) => void;
}

function ActionCard({ specimen, onShowModal }: ActionCardProps) {
  const [variant, setVariant] = useState<ToggleableVariant>(specimen.defaultVariant);
  const Icon = specimen.icon;

  return (
    <div className="specimen-card">
      <div style={labelStyle}>{specimen.label}</div>
      <VariantToggle value={variant} onChange={setVariant} />
      <Button variant={variant} leftIcon={<Icon size={12} />} onClick={() => onShowModal(specimen.confirmMessage)}>
        {specimen.label}
      </Button>
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
