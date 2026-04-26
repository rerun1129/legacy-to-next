"use client";
import { useEffect, useRef, useState } from "react";
import { Calendar } from "lucide-react";

function toDateMask(raw: string) {
  const d = raw.replace(/\D/g, "").slice(0, 8);
  return d.length === 8 ? `${d.slice(0, 4)}-${d.slice(4, 6)}-${d.slice(6, 8)}` : d;
}
function toTimeMask(raw: string) {
  const t = raw.replace(/\D/g, "").slice(0, 4);
  return t.length === 4 ? `${t.slice(0, 2)}:${t.slice(2, 4)}` : t;
}

function isValidDate(raw: string) {
  if (raw.length !== 8) return false;
  const y = parseInt(raw.slice(0, 4), 10);
  const m = parseInt(raw.slice(4, 6), 10);
  const d = parseInt(raw.slice(6, 8), 10);
  if (m < 1 || m > 12) return false;
  return d >= 1 && d <= new Date(y, m, 0).getDate();
}
function isValidTime(raw: string) {
  if (raw.length !== 4) return false;
  const h = parseInt(raw.slice(0, 2), 10);
  const m = parseInt(raw.slice(2, 4), 10);
  return h >= 0 && h <= 23 && m >= 0 && m <= 59;
}

const TOOLTIP_MS = 5000;
const errorBg = "rgba(220,38,38,0.13)";
const tooltipStyle: React.CSSProperties = {
  position: "absolute",
  bottom: "calc(100% + 2px)",
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

function DateInputBase({ defaultValue = "", inputClassName, getInputStyle }: {
  defaultValue?: string;
  inputClassName?: string;
  getInputStyle?: (s: StyleState) => React.CSSProperties;
}) {
  const [focused, setFocused]        = useState(false);
  const [raw, setRaw]                = useState(defaultValue.replace(/-/g, ""));
  const [error, setError]            = useState(false);
  const [tooltipVisible, setTooltip] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const dateRef  = useRef<HTMLInputElement>(null);

  useEffect(() => () => { if (timerRef.current) clearTimeout(timerRef.current); }, []);

  const triggerTooltip = () => {
    setTooltip(true);
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setTooltip(false), TOOLTIP_MS);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setRaw(e.target.value.replace(/\D/g, "").slice(0, 8));
    setError(false);
    setTooltip(false);
    if (timerRef.current) clearTimeout(timerRef.current);
  };

  const handleBlur = () => {
    setFocused(false);
    if (raw.length === 0) return;
    if (!isValidDate(raw)) { setRaw(""); setError(true); triggerTooltip(); }
    else                   { setError(false); setTooltip(false); }
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
        className={inputClassName}
        style={computedStyle}
        value={focused ? raw : toDateMask(raw)}
        onChange={handleChange}
        onFocus={() => setFocused(true)}
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
        onChange={e => { setRaw(e.target.value.replace(/-/g, "")); setError(false); setTooltip(false); }}
        tabIndex={-1}
        style={{ position: "absolute", opacity: 0, pointerEvents: "none", width: 1, height: 1, top: 0, left: 0 }}
      />
      {tooltipVisible && <div style={tooltipStyle}>유효하지 않은 날짜</div>}
    </div>
  );
}

export function NumericCell({ defaultValue }: { defaultValue?: string | number }) {
  const v = typeof defaultValue === "string" ? defaultValue.replace(/,/g, "") : defaultValue;
  return <input type="number" step="any" className="grid__cell-input" defaultValue={v} />;
}

export function DateCell({ defaultValue }: { defaultValue?: string }) {
  return (
    <DateInputBase
      defaultValue={defaultValue}
      inputClassName="grid__cell-input"
      getInputStyle={({ error }) => ({ paddingRight: 18, backgroundColor: error ? errorBg : undefined })}
    />
  );
}

export function PanelDateInput({ defaultValue, required }: { defaultValue?: string; required?: boolean }) {
  return (
    <DateInputBase
      defaultValue={defaultValue}
      getInputStyle={({ focused, error }) => {
        const base: React.CSSProperties = {
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
    />
  );
}

export function TimeCell({ defaultValue = "" }: { defaultValue?: string }) {
  const [focused, setFocused]        = useState(false);
  const [raw, setRaw]                = useState(defaultValue.replace(":", ""));
  const [error, setError]            = useState(false);
  const [tooltipVisible, setTooltip] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => () => { if (timerRef.current) clearTimeout(timerRef.current); }, []);

  const triggerTooltip = () => {
    setTooltip(true);
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => setTooltip(false), TOOLTIP_MS);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setRaw(e.target.value.replace(/\D/g, "").slice(0, 4));
    setError(false);
    setTooltip(false);
    if (timerRef.current) clearTimeout(timerRef.current);
  };

  const handleBlur = () => {
    setFocused(false);
    if (raw.length === 0) return;
    if (!isValidTime(raw)) { setRaw(""); setError(true); triggerTooltip(); }
    else                   { setError(false); setTooltip(false); }
  };

  return (
    <div style={{ position: "relative", width: "100%" }}>
      <input
        className="grid__cell-input"
        style={{ backgroundColor: error ? errorBg : undefined }}
        value={focused ? raw : toTimeMask(raw)}
        onChange={handleChange}
        onFocus={() => setFocused(true)}
        onBlur={handleBlur}
        placeholder="HHmm"
        maxLength={focused ? 4 : 5}
      />
      {tooltipVisible && <div style={tooltipStyle}>유효하지 않은 시간</div>}
    </div>
  );
}
