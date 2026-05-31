"use client";

import { useState, type ReactNode } from "react";
import { useLocale } from "next-intl";
import { getSession, hasButtonAccess, getButtonLabel } from "@/lib/admin-session";

interface ActionButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  buttonCode: string;
  icon?: ReactNode;
}

export function ActionButton({ buttonCode, icon, children, type = "button", ...rest }: ActionButtonProps) {
  const locale = useLocale();
  const [session] = useState(() => getSession());
  const allowed = hasButtonAccess(session, buttonCode);
  if (!allowed) return null;
  const dbLabel = getButtonLabel(session, buttonCode, locale);
  if (dbLabel !== null && icon !== undefined) {
    return <button type={type} {...rest}>{icon}{dbLabel}</button>;
  }
  return <button type={type} {...rest}>{children}</button>;
}
