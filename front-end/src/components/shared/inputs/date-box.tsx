"use client";

import { forwardRef } from "react";
import type { DateBoxProps } from "./_types";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";

export const DateBox = forwardRef<HTMLInputElement, DateBoxProps>(
  function DateBox(
    { variant: _variant = "panel", required, readOnly, disabled, className, style, value, defaultValue, name, onChange, onBlur },
    ref
  ) {
    // variant="cell" 은 DateCell을 직접 사용. 본 컴포넌트는 panel 전용 wrapper.
    return (
      <PanelDateInput
        ref={ref}
        name={name}
        required={required}
        readOnly={readOnly}
        disabled={disabled}
        className={className}
        style={style}
        value={value}
        defaultValue={defaultValue}
        onChange={onChange}
        onBlur={onBlur}
      />
    );
  }
);
