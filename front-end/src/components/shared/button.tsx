"use client";

import { forwardRef } from "react";
import type { ButtonHTMLAttributes, ReactNode } from "react";
import { clsx } from "clsx";

export type ButtonVariant =
  | "default"
  | "primary"
  | "ghost"
  | "danger"
  | "success"
  | "search"
  | "transaction"
  | "normal";

export type ButtonSize = "md" | "sm";

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  iconOnly?: boolean;
  loading?: boolean;
  leftIcon?: ReactNode;
  kbd?: string;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  function Button(
    {
      variant = "default",
      size = "md",
      iconOnly,
      loading,
      leftIcon,
      kbd,
      type,
      className,
      disabled,
      children,
      ...rest
    },
    ref
  ) {
    return (
      <button
        ref={ref}
        type={type ?? "button"}
        className={clsx(
          "btn",
          variant !== "default" && `btn--${variant}`,
          size === "sm" && "btn--sm",
          iconOnly && "btn--icon",
          loading && "is-busy",
          className
        )}
        disabled={disabled || loading}
        aria-busy={loading || undefined}
        {...rest}
      >
        {leftIcon}
        {children}
        {kbd ? <span className="btn__kbd">{kbd}</span> : null}
      </button>
    );
  }
);
