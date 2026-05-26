"use client";

import { useState, type ReactNode } from "react";
import { getSession, hasButtonAccess, getButtonLabel } from "@/lib/admin-session";

interface ActionButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  buttonCode: string;
  icon?: ReactNode;
}

export function ActionButton({ buttonCode, icon, children, ...rest }: ActionButtonProps) {
  const [session] = useState(() => getSession());
  const allowed = hasButtonAccess(session, buttonCode);
  if (!allowed) return null;
  const dbLabel = getButtonLabel(session, buttonCode);
  if (dbLabel !== null && icon !== undefined) {
    return <button {...rest}>{icon}{dbLabel}</button>;
  }
  return <button {...rest}>{children}</button>;
}
