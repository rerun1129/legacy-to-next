"use client";

import { forwardRef } from "react";
import type { DateRangeBoxProps } from "./_types";
import { PanelDateInput } from "@/components/shared/grid-cell-inputs";
import { LcnLabel } from "./lcn-label";

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
      labelOptions,
      labelValue,
      onLabelChange,
    },
    ref
  ) {
    const wrapperCn = ["lcn", className].filter(Boolean).join(" ");

    return (
      <div ref={ref} className={wrapperCn} style={style}>
        {(label || labelOptions) && (
          <LcnLabel
            options={labelOptions}
            value={labelValue}
            onChange={onLabelChange}
            required={required}
          >
            {label}
          </LcnLabel>
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
