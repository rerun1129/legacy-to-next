"use client";

import { forwardRef } from "react";
import type { TextareaHTMLAttributes } from "react";
import type { BoxBaseProps } from "./_types";
import { panelClass, cellClass } from "./_styles";
import { LineNumberTextarea } from "@/components/shared/line-number-textarea";

export type TextAreaProps = BoxBaseProps &
  Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, "required" | "readOnly" | "disabled"> & {
    lineNumbers?: boolean;
    rows?: number;
  };

export const TextArea = forwardRef<HTMLTextAreaElement, TextAreaProps>(
  function TextArea(
    { variant = "panel", required, readOnly, disabled, className, style, lineNumbers, rows, ...rest },
    ref
  ) {
    if (lineNumbers) {
      const { name, value, onChange, onBlur, placeholder, defaultValue } = rest;
      return (
        <LineNumberTextarea
          name={name}
          value={value as string | undefined}
          defaultValue={defaultValue as string | undefined}
          onChange={onChange}
          onBlur={onBlur}
          placeholder={placeholder}
          style={style}
        />
      );
    }

    if (variant === "cell") {
      const base = cellClass({ required });
      const combined = className ? `${base} ${className}` : base;
      return (
        <textarea
          ref={ref}
          className={combined}
          style={{ height: "100%", resize: "none", ...style }}
          readOnly={readOnly}
          disabled={disabled}
          rows={rows}
          {...rest}
        />
      );
    }

    const base = panelClass({ required });
    const combined = className ? `${base} ${className}` : base;
    return (
      <textarea
        ref={ref}
        className={combined}
        style={style}
        readOnly={readOnly}
        disabled={disabled}
        rows={rows}
        {...rest}
      />
    );
  }
);
