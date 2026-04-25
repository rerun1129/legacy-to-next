"use client";

import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import {
  LayoutDashboard, FileText, Layers, Truck, Package,
  ChevronRight, List, FilePlus,
} from "lucide-react";
import { useTabs } from "@/lib/use-tabs";

// ─── Types ──────────────────────────────────────────────────
interface NavLeaf {
  label: string;
  href: string;
  icon: React.ComponentType<{ size?: number }>;
  exact?: boolean;
}

interface NavSection {
  group: string;
  icon: React.ComponentType<{ size?: number }>;
  href?: string;
  children?: NavLeaf[];
  defaultOpen?: boolean;
}

// ─── Nav data ───────────────────────────────────────────────
const NAV: NavSection[] = [
  { group: "Dashboard", icon: LayoutDashboard, href: "/dashboard" },
  {
    group: "House B/L", icon: FileText, defaultOpen: true,
    children: [
      { label: "Sea Export — List",  href: "/fms/house-bl/sea-exp",     icon: List,     exact: true },
      { label: "Sea Export — Entry", href: "/fms/house-bl/sea-exp/new", icon: FilePlus },
      { label: "Sea Import — List",  href: "/fms/house-bl/sea-imp",     icon: List,     exact: true },
      { label: "Sea Import — Entry", href: "/fms/house-bl/sea-imp/new", icon: FilePlus },
      { label: "Air Export — List",  href: "/fms/house-bl/air-exp",     icon: List,     exact: true },
      { label: "Air Export — Entry", href: "/fms/house-bl/air-exp/new", icon: FilePlus },
      { label: "Air Import — List",  href: "/fms/house-bl/air-imp",     icon: List,     exact: true },
      { label: "Air Import — Entry", href: "/fms/house-bl/air-imp/new", icon: FilePlus },
    ],
  },
  {
    group: "Master B/L", icon: Layers, defaultOpen: false,
    children: [
      { label: "Sea Export — List",  href: "/fms/master-bl/sea-exp",     icon: List,     exact: true },
      { label: "Sea Export — Entry", href: "/fms/master-bl/sea-exp/new", icon: FilePlus },
      { label: "Sea Import — List",  href: "/fms/master-bl/sea-imp",     icon: List,     exact: true },
      { label: "Sea Import — Entry", href: "/fms/master-bl/sea-imp/new", icon: FilePlus },
      { label: "Air Export — List",  href: "/fms/master-bl/air-exp",     icon: List,     exact: true },
      { label: "Air Export — Entry", href: "/fms/master-bl/air-exp/new", icon: FilePlus },
      { label: "Air Import — List",  href: "/fms/master-bl/air-imp",     icon: List,     exact: true },
      { label: "Air Import — Entry", href: "/fms/master-bl/air-imp/new", icon: FilePlus },
    ],
  },
  {
    group: "Truck B/L", icon: Truck, defaultOpen: false,
    children: [
      { label: "List",  href: "/fms/truck-bl",     icon: List,     exact: true },
      { label: "Entry", href: "/fms/truck-bl/new", icon: FilePlus },
    ],
  },
  {
    group: "Non B/L", icon: Package, defaultOpen: false,
    children: [
      { label: "List",  href: "/fms/non-bl",     icon: List,     exact: true },
      { label: "Entry", href: "/fms/non-bl/new", icon: FilePlus },
    ],
  },
];

// ─── Helpers ────────────────────────────────────────────────
function leafActive(pathname: string, leaf: NavLeaf) {
  if (leaf.exact) return pathname === leaf.href;
  return pathname === leaf.href || pathname.startsWith(leaf.href.replace(/\/new$/, "/"));
}

function sectionActive(pathname: string, s: NavSection) {
  if (s.href) return pathname === s.href || pathname.startsWith(s.href + "/");
  return s.children?.some((c) => leafActive(pathname, c)) ?? false;
}

// ─── Component ──────────────────────────────────────────────
export function Sidebar() {
  const pathname = usePathname();
  const router   = useRouter();
  const addTab   = useTabs((s) => s.addTab);

  const [open, setOpen] = useState<Record<string, boolean>>(() => {
    const init: Record<string, boolean> = {};
    NAV.forEach((s) => {
      if (s.children) init[s.group] = sectionActive(pathname, s) || (s.defaultOpen ?? false);
    });
    return init;
  });

  const toggle = (group: string) => setOpen((p) => ({ ...p, [group]: !p[group] }));

  function navigate(label: string, href: string) {
    addTab(label, href);
    router.push(href);
  }

  return (
    <nav className="app__side">
      <div className="side-group">
        <div className="side-group__label">FMS</div>

        {NAV.map((section) => {
          const secActive = sectionActive(pathname, section);

          /* Direct link */
          if (section.href) {
            return (
              <button
                key={section.group}
                className={`side-item${secActive ? " is-active" : ""}`}
                style={{ width: "100%" }}
                onClick={() => navigate(section.group, section.href!)}
              >
                <span className="side-item__icon"><section.icon size={14} /></span>
                {section.group}
              </button>
            );
          }

          const isOpen = open[section.group];

          return (
            <div key={section.group}>
              <button
                className={`side-item${secActive ? " is-active" : ""}`}
                style={{ width: "100%" }}
                onClick={() => toggle(section.group)}
              >
                <span className="side-item__icon"><section.icon size={14} /></span>
                <span style={{ flex: 1, textAlign: "left" }}>{section.group}</span>
                <ChevronRight
                  size={12}
                  style={{
                    flexShrink: 0,
                    color: "var(--ink-4)",
                    transform: isOpen ? "rotate(90deg)" : undefined,
                    transition: "transform 160ms ease",
                  }}
                />
              </button>

              {isOpen && section.children?.map((leaf) => {
                const active = leafActive(pathname, leaf);
                return (
                  <button
                    key={leaf.href}
                    className={`side-item${active ? " is-active" : ""}`}
                    style={{ paddingLeft: 24, fontSize: "var(--fs-xs)", width: "100%" }}
                    onClick={() => navigate(leaf.label, leaf.href)}
                  >
                    <span className="side-item__icon"><leaf.icon size={11} /></span>
                    {leaf.label}
                  </button>
                );
              })}
            </div>
          );
        })}
      </div>
    </nav>
  );
}
