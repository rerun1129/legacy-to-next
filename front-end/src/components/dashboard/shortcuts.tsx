"use client";

import Link from "next/link";
import { useTranslations } from "next-intl";
import { FileText, Layers, Search, Printer, Receipt, FolderOpen } from "lucide-react";
import { SHORTCUT_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

const icons = [FileText, Layers, Search, Printer, Receipt, FolderOpen];

export function Shortcuts() {
  const t = useTranslations("fms.dashboard");

  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          {t("panels.quickAccess")}
        </div>
      </div>
      <div className="shortcut-grid">
        {SHORTCUT_DATA.map((s, i) => {
          const Icon = icons[i] ?? FileText;
          return (
            <Link key={s.label} href={s.href} className="shortcut">
              <Icon className="shortcut__icon" />
              <div className="shortcut__label">{s.label}</div>
              <div className="shortcut__hint">{s.hint}</div>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
