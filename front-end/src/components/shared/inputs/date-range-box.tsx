"use client";

import { forwardRef } from "react";
import type { DateRangeBoxProps } from "./_types";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";

export const DateRangeBox = forwardRef<HTMLDivElement, DateRangeBoxProps>(
  function DateRangeBox(
    {
      label,
      fromProps,
      toProps,
      tildeText = "~",
      required,
      readOnly,
      disabled,
      className,
      style,
    },
    ref
  ) {
    const wrapperCn = ["lcn", className].filter(Boolean).join(" ");

    return (
      <div ref={ref} className={wrapperCn} style={style}>
        {label && (
          <span className={`lcn__label${required ? " is-required" : ""}`}>
            {label}
          </span>
        )}
        <div className="lcn__daterange" style={{ gridColumn: "2 / span 2" }}>
          <PanelDateInput
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            {...fromProps}
          />
          <span className="lcn__tilde">{tildeText}</span>
          <PanelDateInput
            required={required}
            readOnly={readOnly}
            disabled={disabled}
            {...toProps}
          />
        </div>
      </div>
    );
  }
);
