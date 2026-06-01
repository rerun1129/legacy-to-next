"use client";

import { useState, type ReactNode } from "react";
import { useLocale, useTranslations } from "next-intl";
import { getSession, hasButtonAccess, getButtonLabel } from "@/lib/admin-session";
import { resolveButtonLabelKey } from "@/lib/button-label-i18n";

interface ActionButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  buttonCode: string;
  icon?: ReactNode;
}

export function ActionButton({ buttonCode, icon, children, type = "button", ...rest }: ActionButtonProps) {
  // Rules of Hooks: 모든 hook은 early-return 이전에 무조건 호출
  const locale = useLocale();
  const t = useTranslations();            // root translator → dotted key + t.has 사용
  const [session] = useState(() => getSession());

  const allowed = hasButtonAccess(session, buttonCode);
  if (!allowed) return null;

  // children이 있으면 명시적 override (동적 Save의 saving 상태 보존)
  if (children !== undefined) {
    return <button type={type} {...rest}>{children}</button>;
  }

  // i18n 우선 → DB 권한 라벨 fallback
  const dbLabel = getButtonLabel(session, buttonCode, locale);
  const i18nKey = resolveButtonLabelKey(buttonCode);
  const label = i18nKey && t.has(i18nKey) ? t(i18nKey) : dbLabel;

  if (label !== null) {
    return <button type={type} {...rest}>{icon}{label}</button>;
  }
  return <button type={type} {...rest}>{icon}</button>;
}
