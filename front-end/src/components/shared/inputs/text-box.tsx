"use client";

import { forwardRef } from "react";
import type { InputHTMLAttributes } from "react";
import type { BoxBaseProps } from "./_types";
import { panelClass, cellClass } from "./_styles";

export type TextBoxProps = BoxBaseProps &
  Omit<InputHTMLAttributes<HTMLInputElement>, "required" | "readOnly" | "disabled">;

export const TextBox = forwardRef<HTMLInputElement, TextBoxProps>(
  function TextBox(
    { variant = "panel", required, readOnly, disabled, className, style, ...rest },
    ref
  ) {
    if (variant === "cell") {
      const base = cellClass({ required });
      const combined = className ? `${base} ${className}` : base;
      return (
        <input
          ref={ref}
          autoComplete="off"
          className={combined}
          style={style}
          readOnly={readOnly}
          disabled={disabled}
          {...rest}
        />
      );
    }

    const base = panelClass({ required });
    const combined = className ? `${base} ${className}` : base;
    return (
      <input
        ref={ref}
        autoComplete="off"
        className={combined}
        style={style}
        readOnly={readOnly}
        disabled={disabled}
        {...rest}
      />
    );
  }
);
