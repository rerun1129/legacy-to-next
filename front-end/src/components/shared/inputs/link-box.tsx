"use client";

import { forwardRef } from "react";
import { ExternalLink } from "lucide-react";
import type { LinkBoxProps } from "./_types";

const linkBtnStyle: React.CSSProperties = {
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

export const LinkBox = forwardRef<HTMLInputElement, LinkBoxProps>(
  function LinkBox(
    {
      label,
      required,
      readOnly,
      disabled,
      inputProps,
      onLink,
      linkAriaLabel = "Open link",
    },
    ref
  ) {
    const { className: inputCn, ...restInputProps } = inputProps ?? {};
    const mergedInputCn =
      [inputCn, required ? "is-required" : undefined].filter(Boolean).join(" ") || undefined;

    return (
      <div className="lnk">
        {label && (
          <span className={`lnk__label${required ? " is-required" : ""}`}>
            {label}
          </span>
        )}
        <div className="lnk__field">
          <input
            ref={ref}
            className={mergedInputCn}
            readOnly={readOnly}
            disabled={disabled}
            {...restInputProps}
          />
          {onLink && !readOnly && !disabled && (
            <button
              type="button"
              onClick={onLink}
              style={linkBtnStyle}
              aria-label={linkAriaLabel}
            >
              <ExternalLink size={12} />
            </button>
          )}
        </div>
      </div>
    );
  }
);
