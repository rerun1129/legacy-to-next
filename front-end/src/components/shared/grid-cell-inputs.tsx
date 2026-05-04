"use client";
import { forwardRef, useEffect, useRef, useState } from "react";
import type React from "react";
import type { ChangeEvent, CSSProperties, FocusEvent, InputHTMLAttributes } from "react";
import { Calendar } from "lucide-react";

function toDateMask(raw: string) {
  const d = raw.replace(/\D/g, "").slice(0, 8);
  return d.length === 8 ? `${d.slice(0, 4)}-${d.slice(4, 6)}-${d.slice(6, 8)}` : d;
}
function isValidDate(raw: string) {
  if (raw.length !== 8) return false;
  const y = parseInt(raw.slice(0, 4), 10);
  const m = parseInt(raw.slice(4, 6), 10);
  const d = parseInt(raw.slice(6, 8), 10);
  if (m < 1 || m > 12) return false;
  return d >= 1 && d <= new Date(y, m, 0).getDate();
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

type StyleState = { focused: boolean; error: boolean };

function cellInputClass(className?: string) {
  return className ? `grid__cell-input ${className}` : "grid__cell-input";
}

export const TextCell = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function TextCell({ className, ...props }, ref) { return <input ref={ref} className={cellInputClass(className)} {...props} />; }
);

type DateInputBaseProps = Omit<
  InputHTMLAttributes<HTMLInputElement>,
  "className" | "style" | "type" | "value" | "defaultValue" | "maxLength"
> & {
  defaultValue?: string;
  value?: string;
  inputClassName?: string;
  getInputStyle?: (s: StyleState) => CSSProperties;
};

const DateInputBase = forwardRef<HTMLInputElement, DateInputBaseProps>(function DateInputBase({
  defaultValue = "",
  value,
  inputClassName,
  getInputStyle,
  onChange,
  onBlur,
  onFocus,
  ...inputProps
}, ref) {
  const [focused, setFocused]        = useState(false);
  const [internalRaw, setInternalRaw] = useState(defaultValue.replace(/-/g, ""));
  const [error, setError]            = useState(false);
  const [tooltipVisible, setTooltip] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const dateRef  = useRef<HTMLInputElement>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);
  const controlled = value !== undefined;
  const raw = controlled ? String(value).replace(/-/g, "") : internalRaw;

  useEffect(() => () => { if (timerRef.current) clearTimeout(timerRef.current); }, []);

  function bindInput(node: HTMLInputElement | null) {
    inputRef.current = node;
    if (typeof ref === "function") ref(node); else if (ref) ref.current = node;
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

  const triggerTooltip = () => {
    setTooltip(true);
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setTooltip(false), TOOLTIP_MS);
  };

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const nextRaw = e.target.value.replace(/\D/g, "").slice(0, 8);
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
    if (!isValidDate(raw)) { setRaw(""); notifyChange("", e); setError(true); triggerTooltip(); }
    else                   { setError(false); setTooltip(false); }
    onBlur?.(e);
  };

  const handlePickerChange = (e: ChangeEvent<HTMLInputElement>) => {
    const nextRaw = e.target.value.replace(/-/g, "");
    setRaw(nextRaw);
    setError(false);
    setTooltip(false);
    notifyChange(nextRaw, e);
  };

  const calendarValue = raw.length === 8
    ? `${raw.slice(0, 4)}-${raw.slice(4, 6)}-${raw.slice(6, 8)}`
    : "";

  const computedStyle = getInputStyle
    ? getInputStyle({ focused, error })
    : { backgroundColor: error ? errorBg : undefined };

  return (
    <div style={{ position: "relative", width: "100%" }}>
      <input
        {...inputProps}
        ref={bindInput}
        className={inputClassName}
        style={computedStyle}
        value={focused ? raw : toDateMask(raw)}
        onChange={handleChange}
        onFocus={(e) => {
          setFocused(true);
          requestAnimationFrame(() => {
            inputRef.current?.select();
          });
          onFocus?.(e);
        }}
        onBlur={handleBlur}
        placeholder="yyyyMMdd"
        maxLength={focused ? 8 : 10}
      />
      <button
        type="button"
        tabIndex={-1}
        onMouseDown={e => e.preventDefault()}
        onClick={() => dateRef.current?.showPicker?.()}
        style={{ position: "absolute", right: 3, top: "50%", transform: "translateY(-50%)", display: "flex", background: "none", border: "none", padding: 0, cursor: "pointer", color: "var(--ink-4)" }}
      >
        <Calendar size={10} />
      </button>
      <input
        ref={dateRef}
        type="date"
        value={calendarValue}
        onChange={handlePickerChange}
        tabIndex={-1}
        style={{ position: "absolute", opacity: 0, pointerEvents: "none", width: 1, height: 1, top: 0, left: 0 }}
      />
      {tooltipVisible && <div style={tooltipStyle}>유효하지 않은 날짜</div>}
    </div>
  );
});

type NumericCellProps = Omit<InputHTMLAttributes<HTMLInputElement>, "type" | "defaultValue"> & {
  defaultValue?: string | number;
};

export const NumericCell = forwardRef<HTMLInputElement, NumericCellProps>(
  function NumericCell({ defaultValue, className, step = "any", ...props }, ref) {
    const v = typeof defaultValue === "string" ? defaultValue.replace(/,/g, "") : defaultValue;
    return (
      <input ref={ref} type="number" step={step} className={cellInputClass(className)} defaultValue={v} {...props} />
    );
  }
);

type DateCellProps = {
  defaultValue?: string;
  value?: string;
  onChange?: React.ChangeEventHandler<HTMLInputElement>;
  onBlur?: React.FocusEventHandler<HTMLInputElement>;
  required?: boolean;
  readOnly?: boolean;
  name?: string;
};

export const DateCell = forwardRef<HTMLInputElement, DateCellProps>(
  function DateCell({ defaultValue, value, onChange, onBlur, required, readOnly, name }, ref) {
    return (
      <DateInputBase
        ref={ref}
        name={name}
        defaultValue={defaultValue}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        readOnly={readOnly}
        inputClassName={`grid__cell-input${required ? " is-required" : ""}`}
        getInputStyle={({ error }) => ({ paddingRight: 18, backgroundColor: error ? errorBg : undefined })}
      />
    );
  }
);

type PanelDateInputProps = DateInputBaseProps & { required?: boolean };

export const PanelDateInput = forwardRef<HTMLInputElement, PanelDateInputProps>(
  function PanelDateInput({ defaultValue, required, ...props }, ref) {
  return (
    <DateInputBase
      ref={ref}
      defaultValue={defaultValue}
      getInputStyle={({ focused, error }) => {
        const base: CSSProperties = {
          width: "100%", height: 22, fontSize: 10,
          background: error ? errorBg : "var(--surface-1)",
          borderWidth: 1,
          borderStyle: "solid",
          borderColor: "var(--border)",
          borderRadius: 4,
          color: "var(--ink)",
          boxSizing: "border-box",
          outline: "none",
        };
        if (required && focused) return {
          ...base,
          padding: "0 20px 0 11px",
          background: error ? errorBg : "var(--required-soft)",
          boxShadow: "inset 4px 0 0 var(--required-bar), 0 0 0 2px color-mix(in srgb, var(--required-bar) 15%, transparent)",
          borderColor: "color-mix(in srgb, var(--required-bar) 55%, var(--border))",
        };
        if (required) return {
          ...base,
          padding: "0 20px 0 11px",
          boxShadow: "inset 3px 0 0 var(--required-bar)",
          borderColor: "color-mix(in srgb, var(--required-bar) 30%, var(--border))",
        };
        if (focused) return {
          ...base,
          padding: "0 20px 0 8px",
          borderColor: "var(--accent)",
          boxShadow: "0 0 0 2px var(--accent-soft)",
        };
        return { ...base, padding: "0 20px 0 8px" };
      }}
      {...props}
    />
  );
});

