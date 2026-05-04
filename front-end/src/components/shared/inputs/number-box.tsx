"use client";

import { forwardRef } from "react";
import type { InputHTMLAttributes } from "react";
import type { BoxBaseProps } from "./_types";
import { panelStyle, cellStyle, cellRequiredStyle } from "./_styles";

export type NumberBoxProps = BoxBaseProps &
  Omit<InputHTMLAttributes<HTMLInputElement>, "type" | "required" | "readOnly" | "disabled">;

export const NumberBox = forwardRef<HTMLInputElement, NumberBoxProps>(
  function NumberBox(
    { variant = "panel", required, readOnly, disabled, className, style, step = "any", ...rest },
    ref
  ) {
    if (variant === "cell") {
      const baseClass = cellStyle();
      const combinedClass = className ? `${baseClass} ${className}` : baseClass;
      const inlineStyle: React.CSSProperties = {
        textAlign: "right",
        ...cellRequiredStyle({ required }),
        ...style,
      };
      return (
        <input
          ref={ref}
          type="number"
          step={step}
          className={combinedClass}
          style={inlineStyle}
          readOnly={readOnly}
          disabled={disabled}
          {...rest}
        />
      );
    }

    const inlineStyle: React.CSSProperties = {
      ...panelStyle({ required, readOnly, disabled }),
      textAlign: "right",
      ...style,
    };
    return (
      <input
        ref={ref}
        type="number"
        step={step}
        className={className}
        style={inlineStyle}
        readOnly={readOnly}
        disabled={disabled}
        {...rest}
      />
    );
  }
);
