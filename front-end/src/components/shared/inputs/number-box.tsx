"use client";

import { forwardRef, useEffect, useRef, useCallback } from "react";
import type { InputHTMLAttributes, FocusEvent, KeyboardEvent, ClipboardEvent, ChangeEvent } from "react";
import type { BoxBaseProps } from "./_types";
import { panelClass, cellClass } from "./_styles";

export type NumberBoxProps = BoxBaseProps &
  Omit<InputHTMLAttributes<HTMLInputElement>, "type" | "required" | "readOnly" | "disabled"> & {
    decimalPlaces?: number;
  };

export const NumberBox = forwardRef<HTMLInputElement, NumberBoxProps>(
  function NumberBox(
    { variant = "panel", required, readOnly, disabled, className, style, step, decimalPlaces, onBlur, onKeyDown, onPaste, onChange, ...rest },
    ref
  ) {
    // decimalPlaces가 있으면 자동 계산, 외부 step이 없으면 1(정수 전용) 폴백
    const resolvedStep = step ?? (decimalPlaces !== undefined ? Math.pow(10, -decimalPlaces) : 1);

    // 내부 ref와 외부 ref 합성 — useEffect에서 DOM 값에 접근하기 위해 필요
    const innerRef = useRef<HTMLInputElement>(null);
    const setRef = useCallback(
      (el: HTMLInputElement | null) => {
        (innerRef as React.MutableRefObject<HTMLInputElement | null>).current = el;
        if (typeof ref === "function") ref(el);
        else if (ref) (ref as React.MutableRefObject<HTMLInputElement | null>).current = el;
      },
      [ref]
    );

    // blur 복원을 위해 마지막으로 유효했던 DOM 값 추적
    const lastValidRef = useRef<string>("");

    // 마운트 시 RHF defaultValues가 비어 있으면 0.000 형태로 초기값 채움
    useEffect(() => {
      if (decimalPlaces !== undefined && innerRef.current && innerRef.current.value === "") {
        innerRef.current.value = (0).toFixed(decimalPlaces);
      }
    }, [decimalPlaces]);

    // 마운트 완료 후 초기 DOM 값을 lastValidRef에 반영
    useEffect(() => {
      if (innerRef.current) {
        lastValidRef.current = innerRef.current.value;
      }
    }, []);

    // +/- 항상 차단, decimalPlaces 없을 때 소수점 차단 — 브라우저 기본 number input이 허용하는 키를 제한
    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "+" || e.key === "-") e.preventDefault();
      if (decimalPlaces === undefined && e.key === ".") e.preventDefault();
      onKeyDown?.(e);
    };

    // 붙여넣기 시 부호(+/-)와, decimalPlaces 없을 때 소수점 포함 값 차단
    const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
      const pasted = e.clipboardData.getData("text");
      const isValid =
        decimalPlaces === undefined
          ? /^\d+$/.test(pasted)        // 정수만 (부호·소수점 없음)
          : /^\d*\.?\d*$/.test(pasted); // 부호 없는 소수 허용
      if (!isValid) e.preventDefault();
      onPaste?.(e);
    };

    // 유효한 keystroke마다 lastValidRef를 갱신하고 외부 onChange를 전달
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
      if (!e.target.validity.badInput) {
        const val = e.target.value;
        const isNumeric = decimalPlaces !== undefined
          ? /^\d*\.?\d*$/.test(val)
          : /^\d*$/.test(val);
        if (isNumeric || val === "") {
          lastValidRef.current = val;
        }
      }
      onChange?.(e);
    };

    // blur 시: decimalPlaces 있으면 toFixed 포맷, 없으면 선행 0 제거(정수 정규화)
    // DOM 값이 변경된 경우에만 onChange 재호출로 RHF string 재동기화
    const handleBlur = (e: FocusEvent<HTMLInputElement>) => {
      // badInput(문자열 등 파싱 불가 값)이면 마지막 유효 값으로 복원 후 조기 반환
      if (e.target.validity.badInput) {
        e.target.value = lastValidRef.current;
        onBlur?.(e);
        return;
      }

      // badInput=false이지만 숫자로 파싱 안 됨 (IME 한글 완성 입력 등)
      if (e.target.value !== "" && isNaN(Number(e.target.value))) {
        e.target.value = lastValidRef.current;
        onBlur?.(e);
        return;
      }

      let formatted: string;

      if (decimalPlaces !== undefined) {
        const parsed = parseFloat(e.target.value);
        formatted =
          isNaN(parsed) || e.target.value === ""
            ? (0).toFixed(decimalPlaces)
            : parsed.toFixed(decimalPlaces);
      } else {
        const parsed = parseInt(e.target.value, 10);
        formatted = isNaN(parsed) ? "" : String(parsed);
      }

      if (formatted !== "" && e.target.value !== formatted) {
        e.target.value = formatted;
        if (onChange) {
          onChange({ ...e, type: "change" } as unknown as ChangeEvent<HTMLInputElement>);
        }
      } else if (formatted !== "") {
        e.target.value = formatted;
      }

      onBlur?.(e);
    };

    if (variant === "cell") {
      const base = cellClass({ required });
      const combined = className ? `${base} ${className}` : base;
      return (
        <input
          ref={setRef}
          type="number"
          autoComplete="off"
          step={resolvedStep}
          className={combined}
          style={{ textAlign: "right", fontFamily: "var(--font-mono)", ...style }}
          readOnly={readOnly}
          disabled={disabled}
          onChange={handleChange}
          onBlur={handleBlur}
          onKeyDown={handleKeyDown}
          onPaste={handlePaste}
          {...rest}
        />
      );
    }

    const base = panelClass({ required });
    const combined = className ? `${base} ${className}` : base;
    return (
      <input
        ref={setRef}
        type="number"
        autoComplete="off"
        step={resolvedStep}
        className={combined}
        style={{ textAlign: "right", fontFamily: "var(--font-mono)", ...style }}
        readOnly={readOnly}
        disabled={disabled}
        onChange={handleChange}
        onBlur={handleBlur}
        onKeyDown={handleKeyDown}
        onPaste={handlePaste}
        {...rest}
      />
    );
  }
);
