"use client";

import type { BtnVariant } from "./_specimen-types";

const VARIANTS: BtnVariant[] = ["search", "transaction", "danger", "normal"];
const LABELS: Record<BtnVariant, string> = {
  search: "Search",
  transaction: "Transaction",
  danger: "Danger",
  normal: "Normal",
};

interface VariantToggleProps {
  value: BtnVariant;
  onChange: (v: BtnVariant) => void;
}

export function VariantToggle({ value, onChange }: VariantToggleProps) {
  return (
    <div className="variant-toggle">
      {VARIANTS.map((v) => (
        <button
          key={v}
          type="button"
          className={
            value === v
              ? `variant-toggle__btn is-active is-active--${v}`
              : "variant-toggle__btn"
          }
          onClick={() => onChange(v)}
        >
          {LABELS[v]}
        </button>
      ))}
    </div>
  );
}
