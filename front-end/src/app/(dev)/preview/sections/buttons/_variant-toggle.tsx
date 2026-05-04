"use client";

import type { BtnVariant } from "./_specimen-types";

const VARIANTS: BtnVariant[] = ["primary", "ghost", "danger"];
const LABELS: Record<BtnVariant, string> = {
  primary: "Primary",
  ghost: "Ghost",
  danger: "Danger",
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
