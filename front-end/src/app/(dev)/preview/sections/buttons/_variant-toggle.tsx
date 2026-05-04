"use client";

import type { ToggleableVariant } from "./_specimen-types";

const VARIANTS: ToggleableVariant[] = ["search", "transaction", "danger", "normal"];
const LABELS: Record<ToggleableVariant, string> = {
  search: "Search",
  transaction: "Transaction",
  danger: "Danger",
  normal: "Normal",
};

interface VariantToggleProps {
  value: ToggleableVariant;
  onChange: (v: ToggleableVariant) => void;
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
