"use client";

import { forwardRef } from "react";
import type { DateBoxProps } from "./_types";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";

export const DateBox = forwardRef<HTMLInputElement, DateBoxProps>(
  function DateBox(
    // variant/className/style은 DateBox 공통 prop이지만 PanelDateInput은 이를 받지 않으므로 제외
    { required, readOnly, disabled, value, defaultValue, name, onChange, onBlur },
    ref
  ) {
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
