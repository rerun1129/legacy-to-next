import type { CSSProperties } from "react";

interface StyleOpts {
  required?: boolean;
  readOnly?: boolean;
  disabled?: boolean;
}

export function panelStyle(opts: StyleOpts = {}): CSSProperties {
  const base: CSSProperties = {
    height: 22,
    padding: "0 8px",
    fontSize: 10,
    border: "1px solid var(--border)",
    borderRadius: 4,
    background: "var(--surface-1)",
    color: "var(--ink)",
    outline: "none",
    boxSizing: "border-box",
    width: "100%",
  };

  if (opts.disabled) {
    return { ...base, opacity: 0.5, cursor: "not-allowed" };
  }

  if (opts.readOnly) {
    return { ...base, background: "var(--surface)", cursor: "default" };
  }

  if (opts.required) {
    return { ...base, boxShadow: "inset 3px 0 0 var(--required-bar)" };
  }

  return base;
}

// cell 모드: className 문자열 반환 (grid__cell-input 클래스 적용)
export function cellStyle(): string {
  return "grid__cell-input";
}

// cell 모드 required 표시용 inline style (className으로 처리 불가한 경우 병용)
export function cellRequiredStyle(opts: Pick<StyleOpts, "required">): CSSProperties {
  return opts.required ? { boxShadow: "inset 3px 0 0 var(--required-bar)" } : {};
}
