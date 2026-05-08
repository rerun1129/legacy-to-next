"use client";

import { forwardRef, useEffect, useRef, useCallback, useState } from "react";
import { useController } from "react-hook-form";
import type { InputHTMLAttributes, KeyboardEvent, ClipboardEvent, ChangeEvent, FocusEvent } from "react";
import type { BoxBaseProps } from "./_types";
import { panelClass, cellClass } from "./_styles";

export type NumberBoxProps = BoxBaseProps &
  Omit<InputHTMLAttributes<HTMLInputElement>, "type" | "required" | "readOnly" | "disabled"> & {
    decimalPlaces?: number;
    valueAsNumber?: boolean;
  };

function toDisplayString(value: unknown, decimalPlaces?: number): string {
  if (value == null || value === "") {
    return decimalPlaces !== undefined ? (0).toFixed(decimalPlaces) : "";
  }
  const num = typeof value === "number" ? value : parseFloat(String(value));
  if (isNaN(num)) return decimalPlaces !== undefined ? (0).toFixed(decimalPlaces) : "";
  return decimalPlaces !== undefined ? num.toFixed(decimalPlaces) : String(num);
}

// FormProvider 내에서 name prop과 함께 사용하는 controlled 버전
const NumberBoxControlled = forwardRef<HTMLInputElement, NumberBoxProps & { name: string }>(
  function NumberBoxControlled(
    {
      name, variant = "panel", required, readOnly, disabled, className, style, step, decimalPlaces,
      valueAsNumber = true, onBlur, onFocus, onKeyDown, onPaste, onChange, ...rest
    },
    ref
  ) {
    const { field } = useController({ name });
    const resolvedStep = step ?? (decimalPlaces !== undefined ? Math.pow(10, -decimalPlaces) : 1);

    const innerRef = useRef<HTMLInputElement>(null);
    const setRef = useCallback(
      (el: HTMLInputElement | null) => {
        (innerRef as React.MutableRefObject<HTMLInputElement | null>).current = el;
        if (typeof ref === "function") ref(el);
        else if (ref) (ref as React.MutableRefObject<HTMLInputElement | null>).current = el;
      },
      [ref]
    );

    const lastValidRef = useRef<string>("");
    const [focused, setFocused] = useState(false);

    // field.value 변화(reset / setValue 등) 시 DOM 동기화 — 편집 중에는 실행 안 함
    useEffect(() => {
      if (focused || !innerRef.current) return;
      const display = toDisplayString(field.value, decimalPlaces);
      innerRef.current.value = display;
      lastValidRef.current = display;
    }, [field.value, decimalPlaces, focused]);

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "+" || e.key === "-") e.preventDefault();
      if (decimalPlaces === undefined && e.key === ".") e.preventDefault();
      onKeyDown?.(e);
    };

    const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
      const pasted = e.clipboardData.getData("text");
      const isValid = decimalPlaces === undefined
        ? /^\d+$/.test(pasted)
        : /^\d*\.?\d*$/.test(pasted);
      if (!isValid) e.preventDefault();
      onPaste?.(e);
    };

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
      const val = e.target.value;
      const isNumeric = decimalPlaces !== undefined ? /^\d*\.?\d*$/.test(val) : /^\d*$/.test(val);
      if (e.target.validity.badInput || (!isNumeric && val !== "")) {
        e.target.value = lastValidRef.current;
        return;
      }
      lastValidRef.current = val;
      field.onChange(valueAsNumber ? (val === "" ? undefined : parseFloat(val)) : val);
      onChange?.(e);
    };

    const handleFocus = (e: FocusEvent<HTMLInputElement>) => {
      setFocused(true);
      onFocus?.(e);
    };

    const handleBlur = (e: FocusEvent<HTMLInputElement>) => {
      setFocused(false);
      field.onBlur();
      onBlur?.(e);
    };

    const sharedProps = {
      ref: setRef,
      type: "number" as const,
      autoComplete: "off" as const,
      step: resolvedStep,
      readOnly,
      disabled,
      onChange: handleChange,
      onBlur: handleBlur,
      onFocus: handleFocus,
      onKeyDown: handleKeyDown,
      onPaste: handlePaste,
      name: field.name,
      ...rest,
    };

    if (variant === "cell") {
      const base = cellClass({ required });
      return (
        <input
          {...sharedProps}
          className={className ? `${base} ${className}` : base}
          style={{ textAlign: "right", fontFamily: "var(--font-mono)", ...style }}
        />
      );
    }
    const base = panelClass({ required });
    return (
      <input
        {...sharedProps}
        className={className ? `${base} ${className}` : base}
        style={{ textAlign: "right", fontFamily: "var(--font-mono)", ...style }}
      />
    );
  }
);

// Uncontrolled 모드 — name 없이 사용하거나 FormProvider 없는 환경(테스트용)
const NumberBoxUncontrolled = forwardRef<HTMLInputElement, NumberBoxProps>(
  function NumberBoxUncontrolled(
    { variant = "panel", required, readOnly, disabled, className, style, step, decimalPlaces, onBlur, onKeyDown, onPaste, onChange, ...rest },
    ref
  ) {
    const resolvedStep = step ?? (decimalPlaces !== undefined ? Math.pow(10, -decimalPlaces) : 1);

    const innerRef = useRef<HTMLInputElement>(null);
    const setRef = useCallback(
      (el: HTMLInputElement | null) => {
        (innerRef as React.MutableRefObject<HTMLInputElement | null>).current = el;
        if (typeof ref === "function") ref(el);
        else if (ref) (ref as React.MutableRefObject<HTMLInputElement | null>).current = el;
      },
      [ref]
    );
    const lastValidRef = useRef<string>("");

    useEffect(() => {
      if (decimalPlaces !== undefined && innerRef.current && innerRef.current.value === "") {
        innerRef.current.value = (0).toFixed(decimalPlaces);
      }
    }, [decimalPlaces]);

    useEffect(() => {
      if (innerRef.current) lastValidRef.current = innerRef.current.value;
    }, []);

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "+" || e.key === "-") e.preventDefault();
      if (decimalPlaces === undefined && e.key === ".") e.preventDefault();
      onKeyDown?.(e);
    };

    const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
      const pasted = e.clipboardData.getData("text");
      const isValid = decimalPlaces === undefined ? /^\d+$/.test(pasted) : /^\d*\.?\d*$/.test(pasted);
      if (!isValid) e.preventDefault();
      onPaste?.(e);
    };

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
      const val = e.target.value;
      const isNumeric = decimalPlaces !== undefined ? /^\d*\.?\d*$/.test(val) : /^\d*$/.test(val);
      if (e.target.validity.badInput || (!isNumeric && val !== "")) {
        e.target.value = lastValidRef.current;
        return;
      }
      lastValidRef.current = val;
      onChange?.(e);
    };

    const sharedProps = {
      ref: setRef, type: "number" as const, autoComplete: "off" as const, step: resolvedStep,
      readOnly, disabled, onChange: handleChange, onBlur, onKeyDown: handleKeyDown, onPaste: handlePaste,
      ...rest,
    };

    if (variant === "cell") {
      const base = cellClass({ required });
      return (
        <input
          {...sharedProps}
          className={className ? `${base} ${className}` : base}
          style={{ textAlign: "right", fontFamily: "var(--font-mono)", ...style }}
        />
      );
    }
    const base = panelClass({ required });
    return (
      <input
        {...sharedProps}
        className={className ? `${base} ${className}` : base}
        style={{ textAlign: "right", fontFamily: "var(--font-mono)", ...style }}
      />
    );
  }
);

export const NumberBox = forwardRef<HTMLInputElement, NumberBoxProps>(
  function NumberBox({ name, ...props }, ref) {
    if (name) return <NumberBoxControlled name={name} {...props} ref={ref} />;
    return <NumberBoxUncontrolled {...props} ref={ref} />;
  }
);
