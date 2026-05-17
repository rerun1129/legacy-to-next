"use client";

import { usePathname, useRouter } from "next/navigation";
import { useState, useEffect } from "react";
import {
  LayoutDashboard, FileText, Layers, Truck, Package,
  ChevronRight, List, FilePlus, LayoutGrid,
  KeyRound, UserCog, Building2, Megaphone, ScrollText,
} from "lucide-react";
import { useTabs } from "@/lib/use-tabs";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { getSession, hasPermission } from "@/lib/admin-session";
import type { Permission } from "@/domain/permission";


// ─── Types ──────────────────────────────────────────────────
interface NavLeaf {
  label: string;
  href: string;
  icon: React.ComponentType<{ size?: number }>;
  requiredPermission?: Permission;
}

interface NavSection {
  group: string;
  icon: React.ComponentType<{ size?: number }>;
  children: NavLeaf[];
  defaultOpen?: boolean;
}

interface NavModule {
  module: string;
  defaultOpen?: boolean;
  sections: NavSection[];
}

// ─── Nav data ───────────────────────────────────────────────
const DASHBOARD_HREF = "/dashboard";

const NAV_MODULES: NavModule[] = [
  {
    module: "FMS", defaultOpen: true,
    sections: [
      {
        group: "House B/L", icon: FileText, defaultOpen: true,
        children: [
          { label: "Sea Export List",  href: "/fms/house-bl/sea-exp/list",  icon: List },
          { label: "Sea Export Entry", href: "/fms/house-bl/sea-exp/entry", icon: FilePlus },
          { label: "Sea Import List",  href: "/fms/house-bl/sea-imp/list",  icon: List },
          { label: "Sea Import Entry", href: "/fms/house-bl/sea-imp/entry", icon: FilePlus },
          { label: "Air Export List",  href: "/fms/house-bl/air-exp/list",  icon: List },
          { label: "Air Export Entry", href: "/fms/house-bl/air-exp/entry", icon: FilePlus },
          { label: "Air Import List",  href: "/fms/house-bl/air-imp/list",  icon: List },
          { label: "Air Import Entry", href: "/fms/house-bl/air-imp/entry", icon: FilePlus },
        ],
      },
      {
        group: "Master B/L", icon: Layers, defaultOpen: false,
        children: [
          { label: "Sea Export List",  href: "/fms/master-bl/sea-exp/list",  icon: List },
          { label: "Sea Export Entry", href: "/fms/master-bl/sea-exp/entry", icon: FilePlus },
          { label: "Sea Import List",  href: "/fms/master-bl/sea-imp/list",  icon: List },
          { label: "Sea Import Entry", href: "/fms/master-bl/sea-imp/entry", icon: FilePlus },
          { label: "Air Export List",  href: "/fms/master-bl/air-exp/list",  icon: List },
          { label: "Air Export Entry", href: "/fms/master-bl/air-exp/entry", icon: FilePlus },
          { label: "Air Import List",  href: "/fms/master-bl/air-imp/list",  icon: List },
          { label: "Air Import Entry", href: "/fms/master-bl/air-imp/entry", icon: FilePlus },
        ],
      },
      {
        group: "Truck B/L", icon: Truck, defaultOpen: false,
        children: [
          { label: "List",  href: "/fms/truck-bl/list",  icon: List },
          { label: "Entry", href: "/fms/truck-bl/entry", icon: FilePlus },
        ],
      },
      {
        group: "Non B/L", icon: Package, defaultOpen: false,
        children: [
          { label: "List",  href: "/fms/non-bl/list",  icon: List },
          { label: "Entry", href: "/fms/non-bl/entry", icon: FilePlus },
        ],
      },
    ],
  },
  {
    module: "Admin", defaultOpen: false,
    sections: [
      {
        group: "Code Master", icon: KeyRound, defaultOpen: false,
        children: [
          { label: "List", href: "/admin/code/list", icon: List, requiredPermission: "CODE_MANAGE" },
        ],
      },
      {
        group: "사용자 관리", icon: UserCog, defaultOpen: false,
        children: [
          { label: "List", href: "/admin/user/list", icon: List, requiredPermission: "USER_MANAGE" },
        ],
      },
      {
        group: "Partner", icon: Building2, defaultOpen: false,
        children: [
          { label: "List", href: "/admin/partner/list", icon: List, requiredPermission: "PARTNER_MANAGE" },
        ],
      },
      {
        group: "공지사항", icon: Megaphone, defaultOpen: false,
        children: [
          { label: "List", href: "/admin/cms/notice/list", icon: List, requiredPermission: "CMS_MANAGE" },
        ],
      },
      {
        group: "약관·정책", icon: ScrollText, defaultOpen: false,
        children: [
          { label: "List", href: "/admin/cms/terms/list", icon: List, requiredPermission: "CMS_MANAGE" },
        ],
      },
    ],
  },
];

// ─── Helpers ────────────────────────────────────────────────
function leafActive(pathname: string, leaf: NavLeaf) {
  return pathname === leaf.href || pathname.startsWith(leaf.href + "/");
}

function sectionActive(pathname: string, s: NavSection) {
  return s.children.some((c) => leafActive(pathname, c));
}

function moduleActive(pathname: string, m: NavModule) {
  return m.sections.some((s) => sectionActive(pathname, s));
}

// ─── Component ──────────────────────────────────────────────
export function Sidebar() {
  const pathname = usePathname();
  const router   = useRouter();
  const addTab   = useTabs((s) => s.addTab);
  // SSR 시 session=null이므로 admin 메뉴가 모두 숨겨진 결과물을 렌더.
  // mounted 전에는 동일하게 숨겨서 hydration mismatch를 방지.
  const [mounted, setMounted] = useState(false);
  const [session] = useState(() => getSession());

  // SSR/CSR hydration 일치를 위한 mount gate
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => setMounted(true), []);

  const [openModules, setOpenModules] = useState<Record<string, boolean>>(() => {
    const init: Record<string, boolean> = {};
    NAV_MODULES.forEach((m) => {
      init[m.module] = moduleActive(pathname, m) || (m.defaultOpen ?? false);
    });
    return init;
  });

  const [openSections, setOpenSections] = useState<Record<string, boolean>>(() => {
    const init: Record<string, boolean> = {};
    NAV_MODULES.forEach((m) =>
      m.sections.forEach((s) => {
        init[s.group] = sectionActive(pathname, s) || (s.defaultOpen ?? false);
      })
    );
    return init;
  });

  const toggleModule  = (module: string)  => setOpenModules((p)  => ({ ...p, [module]:  !p[module]  }));
  const toggleSection = (group: string)   => setOpenSections((p) => ({ ...p, [group]:   !p[group]   }));

  function navigate(label: string, href: string) {
    addTab(label, href);
    router.push(href);
  }

  const { editMode, setEditMode, canEdit } = useWidgetLayout();

  useEffect(() => { setEditMode(false); }, [pathname, setEditMode]);

  return (
    <nav className="app__side" style={{ display: "flex", flexDirection: "column" }}>
      {/* Dashboard — 최상단 */}
      <button
        className={`side-item${pathname === DASHBOARD_HREF ? " is-active" : ""}`}
        onClick={() => navigate("Dashboard", DASHBOARD_HREF)}
      >
        <span className="side-item__icon"><LayoutDashboard size={14} /></span>
        Dashboard
      </button>

      {/* 최상위 모듈 (FMS, BMS …) */}
      {NAV_MODULES.map((mod) => {
        const modActive   = moduleActive(pathname, mod);
        const modOpen     = openModules[mod.module];

        return (
          <div key={mod.module} className="side-group">
            <button
              className={`side-group__label side-group__label--toggle${modActive ? " is-active" : ""}`}
              onClick={() => toggleModule(mod.module)}
            >
              <span style={{ flex: 1 }}>{mod.module}</span>
              <ChevronRight
                size={11}
                style={{
                  flexShrink: 0,
                  color: modActive ? "var(--accent)" : "var(--ink-4)",
                  transform: modOpen ? "rotate(90deg)" : undefined,
                  transition: "transform 160ms ease",
                }}
              />
            </button>

            {modOpen && mod.sections.map((section) => {
              const secActive = sectionActive(pathname, section);
              const secOpen   = openSections[section.group];

              return (
                <div key={section.group}>
                  <button
                    className={`side-item${secActive ? " is-active" : ""}`}
                    style={{ paddingLeft: 12 }}
                    onClick={() => toggleSection(section.group)}
                  >
                    <span className="side-item__icon"><section.icon size={13} /></span>
                    <span style={{ flex: 1, textAlign: "left" }}>{section.group}</span>
                    <ChevronRight
                      size={11}
                      style={{
                        flexShrink: 0,
                        color: secActive ? "var(--accent)" : "var(--ink-4)",
                        transform: secOpen ? "rotate(90deg)" : undefined,
                        transition: "transform 160ms ease",
                        marginRight: 4,
                      }}
                    />
                  </button>

                  {secOpen && section.children
                    // mounted 전에는 session=null로 취급해 SSR 결과와 일치시킴
                    .filter((leaf) => !leaf.requiredPermission || (mounted && hasPermission(session, leaf.requiredPermission)))
                    .map((leaf) => {
                    const active = leafActive(pathname, leaf);
                    return (
                      <button
                        key={leaf.href}
                        className={`side-item${active ? " is-active" : ""}`}
                        style={{ paddingLeft: 32, fontSize: "var(--fs-xs)" }}
                        onClick={() => navigate(`${section.group} ${leaf.label}`, leaf.href)}
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
        );
      })}

      {/* ── 하단: 위젯 편집 토글 ── */}
      <div style={{ flex: 1 }} />
      {pathname.includes("/entry") && (
        <div style={{ padding: "8px 4px", borderTop: "1px solid var(--border)" }}>
          <button
            className={`side-edit-btn${editMode ? " is-active" : ""}${!canEdit ? " is-disabled" : ""}`}
            onClick={() => canEdit && setEditMode(!editMode)}
            disabled={!canEdit}
            title={canEdit ? "위젯 편집 모드 on/off" : "Main / Freight 탭에서만 사용 가능"}
          >
            <LayoutGrid size={14} style={{ flexShrink: 0 }} />
            <span style={{ overflow: "hidden", textOverflow: "ellipsis" }}>Entry 위젯 편집</span>
          </button>
        </div>
      )}
    </nav>
  );
}
