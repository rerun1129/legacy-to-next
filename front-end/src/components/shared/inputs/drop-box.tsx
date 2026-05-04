"use client";

import { forwardRef } from "react";
import type { DropBoxProps } from "./_types";
import { panelClass, cellClass } from "./_styles";

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
    const isDisabledByReadOnly = readOnly && !disabled;

    if (variant === "cell") {
      const base = cellClass({ required });
      const combined = className ? `${base} ${className}` : base;
      return (
        <select
          ref={ref}
          className={combined}
          style={style}
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

    const base = panelClass({ required });
    const combined = className ? `${base} ${className}` : base;
    return (
      <select
        ref={ref}
        className={combined}
        style={style}
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
