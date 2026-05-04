"use client";

import { forwardRef } from "react";
import { Search } from "lucide-react";
import type { CodeBoxProps } from "./_types";

const lookupBtnStyle: React.CSSProperties = {
  position: "absolute",
  right: 6,
  top: "50%",
  transform: "translateY(-50%)",
  background: "none",
  border: "none",
  cursor: "pointer",
  padding: 0,
  display: "flex",
  alignItems: "center",
  color: "var(--ink-muted)",
};

export const CodeBox = forwardRef<HTMLInputElement, CodeBoxProps>(
  function CodeBox(
    {
      kind = "lcn",
      label,
      required,
      readOnly,
      disabled,
      codeProps,
      nameProps,
      onLookup,
      mono = true,
      lookupAriaLabel,
    },
    ref
  ) {
    if (kind === "party-cn") {
      return (
        <div className="party-block">
          <div className="party-block__head">
            <span className={required ? "is-required" : undefined}>{label}</span>
            <div className="party-cn">
              <div className="party-cn__code" style={{ position: "relative" }}>
                <input
                  ref={ref}
                  className={mono ? "text-mono" : undefined}
                  readOnly={readOnly}
                  disabled={disabled}
                  {...codeProps}
                />
                {onLookup && (
                  <button
                    type="button"
                    onClick={onLookup}
                    style={lookupBtnStyle}
                    aria-label={lookupAriaLabel ?? "Lookup"}
                  >
                    <Search size={12} className="party-cn__icon" />
                  </button>
                )}
              </div>
              <input
                className="party-cn__name"
                readOnly={readOnly}
                disabled={disabled}
                {...nameProps}
              />
            </div>
          </div>
        </div>
      );
    }

    // kind="lcn" (default)
    const { className: codeCn, ...restCodeProps } = codeProps;
    const baseCodeCn = codeCn ?? (mono ? "text-mono" : undefined);
    const mergedCodeCn = [baseCodeCn, required ? "is-required" : undefined].filter(Boolean).join(" ") || undefined;

    return (
      <div className="lcn">
        {label && (
          <span className={`lcn__label${required ? " is-required" : ""}`}>
            {label}
          </span>
        )}
        <div className="lcn__code" style={{ position: "relative" }}>
          <input
            ref={ref}
            className={mergedCodeCn}
            readOnly={readOnly}
            disabled={disabled}
            {...restCodeProps}
          />
          {onLookup && (
            <button
              type="button"
              onClick={onLookup}
              style={lookupBtnStyle}
              aria-label={lookupAriaLabel ?? "Lookup"}
            >
              <Search size={12} />
            </button>
          )}
        </div>
        <input
          className="lcn__name"
          readOnly={readOnly}
          disabled={disabled}
          {...nameProps}
        />
      </div>
    );
  }
);
