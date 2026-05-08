"use client";

import type { Dispatch, SetStateAction } from "react";
import type { BoxVariant } from "@/components/shared/inputs";
import type { FormValues } from "./_shared";
import { toggleStyle } from "./_shared";

type ToolbarProps = {
  variant: BoxVariant;
  setVariant: Dispatch<SetStateAction<BoxVariant>>;
  required: boolean;
  setRequired: Dispatch<SetStateAction<boolean>>;
  readOnly: boolean;
  setReadOnly: Dispatch<SetStateAction<boolean>>;
  disabled: boolean;
  setDisabled: Dispatch<SetStateAction<boolean>>;
  getValues: import("react-hook-form").UseFormReturn<FormValues>["getValues"];
};

export function InputsToolbar({
  variant,
  setVariant,
  required,
  setRequired,
  readOnly,
  setReadOnly,
  disabled,
  setDisabled,
  getValues,
}: ToolbarProps) {
  return (
    <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
      <button style={toggleStyle(variant === "panel")} onClick={() => setVariant("panel")}>
        panel
      </button>
      <button style={toggleStyle(variant === "cell")} onClick={() => setVariant("cell")}>
        cell
      </button>
      <button style={toggleStyle(required)} onClick={() => setRequired((v) => !v)}>
        required
      </button>
      <button style={toggleStyle(readOnly)} onClick={() => setReadOnly((v) => !v)}>
        readOnly
      </button>
      <button style={toggleStyle(disabled)} onClick={() => setDisabled((v) => !v)}>
        disabled
      </button>
      <button
        style={{ ...toggleStyle(false), marginLeft: 16 }}
        onClick={() => alert(JSON.stringify(getValues(), null, 2))}
      >
        getValues
      </button>
    </div>
  );
}
