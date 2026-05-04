"use client";

import { useState } from "react";
import { ConfirmModal } from "@/components/shared/confirm-modal";
import { Button } from "@/components/shared/button";
import { VariantToggle } from "./_variant-toggle";
import { PAGE_BUNDLES } from "./_bundles-data";
import type { ToggleableVariant, ButtonInBundle, PageBundle } from "./_specimen-types";
import { sectionStyle, labelStyle } from "./_shared";

interface BundleBtnProps {
  btn: ButtonInBundle;
  onShowModal: (message: string) => void;
}

function BundleBtn({ btn, onShowModal }: BundleBtnProps) {
  const [variant, setVariant] = useState<ToggleableVariant | "default">(
    btn.initialVariant
  );
  const Icon = btn.icon;

  // "default" variant는 VariantToggle에서 ToggleableVariant로만 변경 가능하므로
  // toggle 표시 시 "default"면 normal을 초기값으로 사용
  const toggleValue: ToggleableVariant =
    variant === "default" ? "normal" : variant;

  function handleVariantChange(v: ToggleableVariant) {
    setVariant(v);
  }

  return (
    <div className="specimen-bundle__btn-wrap">
      <VariantToggle value={toggleValue} onChange={handleVariantChange} />
      <Button type={btn.type} variant={variant} leftIcon={Icon ? <Icon size={12} /> : undefined} onClick={() => onShowModal(btn.confirmMessage)}>
        {btn.label}
      </Button>
    </div>
  );
}

interface BundleSectionProps {
  bundle: PageBundle;
  onShowModal: (message: string) => void;
}

function BundleSection({ bundle, onShowModal }: BundleSectionProps) {
  return (
    <div className="specimen-bundle">
      <div className="specimen-bundle__header">{bundle.pageLabel}</div>
      <div className="specimen-bundle__source">{bundle.sourceFile}</div>
      <div className="specimen-bundle__btn-row">
        {bundle.buttons.map((btn) => (
          <BundleBtn key={btn.id} btn={btn} onShowModal={onShowModal} />
        ))}
      </div>
    </div>
  );
}

export function PageBundleGroup() {
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
      <div style={labelStyle}>B. Page Head Actions — 페이지별 묶음 견본</div>
      {PAGE_BUNDLES.map((bundle) => (
        <BundleSection
          key={bundle.pageId}
          bundle={bundle}
          onShowModal={handleShowModal}
        />
      ))}

      <ConfirmModal
        isOpen={modal.open}
        message={modal.message}
        onConfirm={handleCloseModal}
        onClose={handleCloseModal}
      />
    </section>
  );
}
