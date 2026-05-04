"use client";

import { forwardRef } from "react";
import type { TextareaHTMLAttributes } from "react";
import type { BoxBaseProps } from "./_types";
import { panelStyle, cellStyle, cellRequiredStyle } from "./_styles";
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
    // lineNumbers=true 시 LineNumberTextarea에 위임
    // LineNumberTextarea는 name/value/onChange/onBlur/placeholder/style만 수신
    // readOnly/required/disabled는 LineNumberTextarea가 미지원 (향후 확장 대상)
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
      const baseClass = cellStyle();
      const combinedClass = className ? `${baseClass} ${className}` : baseClass;
      // cell textarea: height auto, 100% fill
      const inlineStyle: React.CSSProperties = {
        height: "100%",
        resize: "none",
        ...cellRequiredStyle({ required }),
        ...style,
      };
      return (
        <textarea
          ref={ref}
          className={combinedClass}
          style={inlineStyle}
          readOnly={readOnly}
          disabled={disabled}
          rows={rows}
          {...rest}
        />
      );
    }

    const base = panelStyle({ required, readOnly, disabled });
    const inlineStyle: React.CSSProperties = {
      ...base,
      height: rows ? undefined : 72,
      resize: "vertical",
      ...style,
    };
    return (
      <textarea
        ref={ref}
        className={className}
        style={inlineStyle}
        readOnly={readOnly}
        disabled={disabled}
        rows={rows}
        {...rest}
      />
    );
  }
);
