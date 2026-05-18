"use client";

import { useState } from "react";
import { getSession, hasButtonAccess } from "@/lib/admin-session";

interface ActionButtonProps {
  buttonCode: string;
  onClick?: React.MouseEventHandler<HTMLButtonElement>;
  children: React.ReactNode;
  className?: string;
  disabled?: boolean;
  title?: string;
}

export function ActionButton({ buttonCode, children, ...rest }: ActionButtonProps) {
  const [session] = useState(() => getSession());
  const allowed = hasButtonAccess(session, buttonCode);
  if (!allowed) return null;
  return <button {...rest}>{children}</button>;
}
