import Link from "next/link";
import { FileText, Layers, Search, Printer, Receipt, FolderOpen } from "lucide-react";
import { shortcutData } from "@/adapter/out/mock/mock-data";

const icons = [FileText, Layers, Search, Printer, Receipt, FolderOpen];

export function Shortcuts() {
  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Quick Access
        </div>
      </div>
      <div className="shortcut-grid">
        {shortcutData.map((s, i) => {
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
