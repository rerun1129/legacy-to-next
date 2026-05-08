"use client";

import { forwardRef } from "react";
import type { CSSProperties } from "react";
import type { DateBoxProps } from "./_types";
import { DateInputBase, PanelDateInput } from "@/components/shared/grid-cell-inputs";

const errorBg = "rgba(220,38,38,0.13)";

export const DateBox = forwardRef<HTMLInputElement, DateBoxProps>(
  function DateBox(
    { variant, required, readOnly, disabled, value, defaultValue, name, onChange, onBlur },
    ref
  ) {
    if (variant === "cell") {
      return (
        <DateInputBase
          ref={ref}
          name={name}
          required={required}
          readOnly={readOnly}
          disabled={disabled}
          value={value}
          defaultValue={defaultValue}
          onChange={onChange}
          onBlur={onBlur}
          inputClassName={`grid__cell-input${required ? " is-required" : ""}`}
          getInputStyle={({ error }: { focused: boolean; error: boolean }): CSSProperties => ({
            paddingRight: 18,
            backgroundColor: error ? errorBg : undefined,
          })}
        />
      );
    }
    return (
      <PanelDateInput
        ref={ref}
        name={name}
        required={required}
        readOnly={readOnly}
        disabled={disabled}
        value={value}
        defaultValue={defaultValue}
        onChange={onChange}
        onBlur={onBlur}
      />
    );
  }
);
