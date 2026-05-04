"use client";

import { forwardRef, useEffect, useRef, useState } from "react";
import type { ChangeEvent, CSSProperties, FocusEvent } from "react";
import type { TimeBoxProps } from "./_types";
import { cellClass } from "./_styles";

export function toTimeMask(raw: string) {
  const t = raw.replace(/\D/g, "").slice(0, 4);
  return t.length === 4 ? `${t.slice(0, 2)}:${t.slice(2, 4)}` : t;
}

export function isValidTime(raw: string) {
  if (raw.length !== 4) return false;
  const h = parseInt(raw.slice(0, 2), 10);
  const m = parseInt(raw.slice(2, 4), 10);
  return h >= 0 && h <= 23 && m >= 0 && m <= 59;
}

const TOOLTIP_MS = 5000;
const errorBg = "rgba(220,38,38,0.13)";

const tooltipStyle: CSSProperties = {
  position: "absolute",
  top: "calc(100% + 2px)",
  left: 0,
  backgroundColor: "#dc2626",
  color: "#fff",
  fontSize: 10,
  padding: "2px 6px",
  borderRadius: 3,
  whiteSpace: "nowrap",
  zIndex: 200,
  pointerEvents: "none",
};

function buildPanelStyle(opts: {
  focused: boolean;
  error: boolean;
  required?: boolean;
}): CSSProperties {
  const { focused, error, required } = opts;
  const base: CSSProperties = {
    width: "100%",
    height: 22,
    fontSize: 10,
    border: "1px solid var(--border)",
    borderRadius: 4,
    boxSizing: "border-box",
    outline: "none",
    background: error ? errorBg : "var(--surface-1)",
    color: "var(--ink)",
    borderStyle: "solid",
    borderWidth: 1,
    borderColor: "var(--border)",
  };
  if (required && focused) return {
    ...base,
    paddingLeft: 11,
    background: error ? errorBg : "var(--required-soft)",
    boxShadow: "inset 4px 0 0 var(--required-bar), 0 0 0 2px color-mix(in srgb, var(--required-bar) 15%, transparent)",
    borderColor: "color-mix(in srgb, var(--required-bar) 55%, var(--border))",
  };
  if (required) return {
    ...base,
    paddingLeft: 11,
    boxShadow: "inset 3px 0 0 var(--required-bar)",
    borderColor: "color-mix(in srgb, var(--required-bar) 30%, var(--border))",
  };
  if (focused) return {
    ...base,
    paddingLeft: 8,
    borderColor: "var(--accent)",
    boxShadow: "0 0 0 2px var(--accent-soft)",
  };
  return { ...base, paddingLeft: 8 };
}

export const TimeBox = forwardRef<HTMLInputElement, TimeBoxProps>(
  function TimeBox(
    { variant = "panel", required, readOnly, disabled, className, style, value, defaultValue, onChange, onBlur, name, ...rest },
    ref
  ) {
    const controlled = value !== undefined;
    const [focused, setFocused]           = useState(false);
    const [internalRaw, setInternalRaw]   = useState((defaultValue ?? "").replace(":", ""));
    const [error, setError]               = useState(false);
    const [tooltipVisible, setTooltip]    = useState(false);
    const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const inputRef = useRef<HTMLInputElement | null>(null);

    const raw = controlled ? String(value).replace(":", "") : internalRaw;

    useEffect(() => () => { if (timerRef.current) clearTimeout(timerRef.current); }, []);

    function bindRef(node: HTMLInputElement | null) {
      inputRef.current = node;
      if (typeof ref === "function") ref(node);
      else if (ref) ref.current = node;
    }

    function setRaw(nextRaw: string) {
      if (!controlled) setInternalRaw(nextRaw);
    }

    function notifyChange(nextRaw: string, e: ChangeEvent<HTMLInputElement> | FocusEvent<HTMLInputElement>) {
      if (!inputRef.current) return;
      inputRef.current.value = nextRaw;
      onChange?.({
        ...e,
        target: inputRef.current,
        currentTarget: inputRef.current,
      } as ChangeEvent<HTMLInputElement>);
    }

    function triggerTooltip() {
      setTooltip(true);
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => setTooltip(false), TOOLTIP_MS);
    }

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
      const nextRaw = e.target.value.replace(/\D/g, "").slice(0, 4);
      e.target.value = nextRaw;
      setRaw(nextRaw);
      setError(false);
      setTooltip(false);
      if (timerRef.current) clearTimeout(timerRef.current);
      onChange?.(e);
    };

    const handleBlur = (e: FocusEvent<HTMLInputElement>) => {
      setFocused(false);
      if (raw.length === 0) {
        onBlur?.(e);
        return;
      }
      if (!isValidTime(raw)) {
        setRaw("");
        notifyChange("", e);
        setError(true);
        triggerTooltip();
      } else {
        setError(false);
        setTooltip(false);
      }
      onBlur?.(e);
    };

    const displayValue = focused ? raw : toTimeMask(raw);

    if (variant === "cell") {
      const base = cellClass({ required });
      const combined = className ? `${base} ${className}` : base;
      return (
        <div style={{ position: "relative", width: "100%" }}>
          <input
            ref={bindRef}
            name={name}
            className={combined}
            style={{ backgroundColor: error ? errorBg : undefined, ...style }}
            value={displayValue}
            onChange={handleChange}
            onFocus={() => setFocused(true)}
            onBlur={handleBlur}
            readOnly={readOnly}
            disabled={disabled}
            placeholder="HHmm"
            maxLength={focused ? 4 : 5}
            {...rest}
          />
          {tooltipVisible && <div style={tooltipStyle}>유효하지 않은 시간</div>}
        </div>
      );
    }

    // panel variant
    const panelInlineStyle = buildPanelStyle({ focused, error, required });
    return (
      <div style={{ position: "relative", width: "100%" }}>
        <input
          ref={bindRef}
          name={name}
          style={{ ...panelInlineStyle, ...style }}
          value={displayValue}
          onChange={handleChange}
          onFocus={() => setFocused(true)}
          onBlur={handleBlur}
          readOnly={readOnly}
          disabled={disabled}
          placeholder="HHmm"
          maxLength={focused ? 4 : 5}
          {...rest}
        />
        {tooltipVisible && <div style={tooltipStyle}>유효하지 않은 시간</div>}
      </div>
    );
  }
);
