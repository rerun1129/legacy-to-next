"use client";

import { forwardRef } from "react";
import { Search } from "lucide-react";
import type { CodeBoxProps } from "./_types";
import { panelStyle } from "./_styles";

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
    const codeInputStyle: React.CSSProperties = {
      ...panelStyle({ required, readOnly, disabled }),
      fontFamily: mono ? "var(--font-mono)" : undefined,
      paddingRight: onLookup ? "24px" : undefined,
    };

    const nameInputStyle = panelStyle({ readOnly, disabled });

    if (kind === "party-cn") {
      return (
        <>
          <div className="party-cn__code" style={{ position: "relative" }}>
            <input
              ref={ref}
              className={mono ? "text-mono" : undefined}
              style={codeInputStyle}
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
            style={nameInputStyle}
            readOnly={readOnly}
            disabled={disabled}
            {...nameProps}
          />
        </>
      );
    }

    // kind="lcn" (default)
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
            style={codeInputStyle}
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
              <Search size={12} />
            </button>
          )}
        </div>
        <input
          className="lcn__name"
          style={nameInputStyle}
          readOnly={readOnly}
          disabled={disabled}
          {...nameProps}
        />
      </div>
    );
  }
);
