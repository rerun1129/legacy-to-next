"use client";

import { forwardRef } from "react";
import type { RadioBoxProps } from "./_types";

export const RadioBox = forwardRef<HTMLInputElement, RadioBoxProps>(
  function RadioBox(
    {
      label,
      name,
      options,
      required,
      readOnly,
      disabled,
      onChange,
      value,
      defaultValue,
      // BoxBaseProps fields not passed to input
      variant: _variant,
      className: _className,
      style: _style,
      ...rest
    },
    ref
  ) {
    const isDisabledByReadOnly = readOnly && !disabled;

    return (
      <div
        className="rdo__field"
        data-readonly={readOnly && !disabled ? true : undefined}
        data-disabled={disabled ? true : undefined}
      >
        {label && (
          <span className={"rdo__label" + (required ? " is-required" : "")}>
            {label}
          </span>
        )}
        <div className="rdo__options">
          {options.map((opt) => (
            <label key={opt.value} className="rdo__option">
              <input
                type="radio"
                name={name}
                value={opt.value}
                disabled={disabled || isDisabledByReadOnly}
                required={required}
                onChange={onChange}
                ref={ref}
                {...rest}
              />
              {opt.label}
            </label>
          ))}
        </div>
      </div>
    );
  }
);
