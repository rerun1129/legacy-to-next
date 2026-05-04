"use client";

import { forwardRef } from "react";
import type { InputHTMLAttributes } from "react";
import type { BoxBaseProps } from "./_types";
import { panelStyle, cellStyle, cellRequiredStyle } from "./_styles";

export type TextBoxProps = BoxBaseProps &
  Omit<InputHTMLAttributes<HTMLInputElement>, "required" | "readOnly" | "disabled">;

export const TextBox = forwardRef<HTMLInputElement, TextBoxProps>(
  function TextBox(
    { variant = "panel", required, readOnly, disabled, className, style, ...rest },
    ref
  ) {
    if (variant === "cell") {
      const baseClass = cellStyle();
      const combinedClass = className ? `${baseClass} ${className}` : baseClass;
      const inlineStyle = { ...cellRequiredStyle({ required }), ...style };
      return (
        <input
          ref={ref}
          className={combinedClass}
          style={inlineStyle}
          readOnly={readOnly}
          disabled={disabled}
          {...rest}
        />
      );
    }

    const inlineStyle = { ...panelStyle({ required, readOnly, disabled }), ...style };
    return (
      <input
        ref={ref}
        className={className}
        style={inlineStyle}
        readOnly={readOnly}
        disabled={disabled}
        {...rest}
      />
    );
  }
);
