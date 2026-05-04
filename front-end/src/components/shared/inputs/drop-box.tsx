"use client";

import { forwardRef } from "react";
import type { DropBoxProps } from "./_types";
import { panelStyle, cellStyle, cellRequiredStyle } from "./_styles";

export const DropBox = forwardRef<HTMLSelectElement, DropBoxProps>(
  function DropBox(
    {
      variant = "panel",
      required,
      readOnly,
      disabled,
      className,
      style,
      options,
      placeholder,
      ...rest
    },
    ref
  ) {
    // select는 readOnly 속성 미지원 → disabled로 대체하고 시각 스타일 적용
    const isDisabledByReadOnly = readOnly && !disabled;

    if (variant === "cell") {
      const baseClass = cellStyle();
      const combinedClass = className ? `${baseClass} ${className}` : baseClass;
      const inlineStyle: React.CSSProperties = {
        ...cellRequiredStyle({ required }),
        ...style,
      };
      return (
        <select
          ref={ref}
          className={combinedClass}
          style={inlineStyle}
          disabled={disabled || isDisabledByReadOnly}
          {...rest}
        >
          {placeholder !== undefined && <option value="">{placeholder}</option>}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      );
    }

    const inlineStyle: React.CSSProperties = {
      ...panelStyle({ required, readOnly, disabled }),
      ...style,
    };
    return (
      <select
        ref={ref}
        className={className}
        style={inlineStyle}
        disabled={disabled || isDisabledByReadOnly}
        {...rest}
      >
        {placeholder !== undefined && <option value="">{placeholder}</option>}
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    );
  }
);
