"use client";

import { usePathname, useRouter } from "next/navigation";
import { useState, useEffect, useSyncExternalStore } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  LayoutDashboard, FileText, Layers, Truck, Package,
  ChevronRight, List, FilePlus, LayoutGrid,
} from "lucide-react";
import { useTabs } from "@/lib/use-tabs";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { getSession, hasMenuAccess } from "@/lib/admin-session";
import { sidebarMenuPort } from "@/lib/ports";
import { SidebarAdminTree } from "./sidebar-admin-tree";


// ─── Types ──────────────────────────────────────────────────
interface NavLeaf {
  label: string;
  href: string;
  icon: React.ComponentType<{ size?: number }>;
  requiredMenuCode?: string;
}

interface NavSection {
  group: string;
  icon: React.ComponentType<{ size?: number }>;
  children: NavLeaf[];
  defaultOpen?: boolean;
  requiredMenuCode?: string;
}

interface NavModule {
  module: string;
  defaultOpen?: boolean;
  sections: NavSection[];
}

// ─── FMS Nav data (정적) ────────────────────────────────────
const DASHBOARD_HREF = "/dashboard";

const FMS_NAV_MODULE: NavModule = {
  module: "FMS", defaultOpen: true,
  sections: [
    {
      group: "House B/L", icon: FileText, defaultOpen: true, requiredMenuCode: "MENU_FMS_HOUSE_BL",
      children: [
        { label: "Sea Export List",  href: "/fms/house-bl/sea-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_EXP_LIST" },
        { label: "Sea Export Entry", href: "/fms/house-bl/sea-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_EXP_ENTRY" },
        { label: "Sea Import List",  href: "/fms/house-bl/sea-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_IMP_LIST" },
        { label: "Sea Import Entry", href: "/fms/house-bl/sea-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_IMP_ENTRY" },
        { label: "Air Export List",  href: "/fms/house-bl/air-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_EXP_LIST" },
        { label: "Air Export Entry", href: "/fms/house-bl/air-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_EXP_ENTRY" },
        { label: "Air Import List",  href: "/fms/house-bl/air-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_IMP_LIST" },
        { label: "Air Import Entry", href: "/fms/house-bl/air-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_IMP_ENTRY" },
      ],
    },
    {
      group: "Master B/L", icon: Layers, defaultOpen: false, requiredMenuCode: "MENU_FMS_MASTER_BL",
      children: [
        { label: "Sea Export List",  href: "/fms/master-bl/sea-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_EXP_LIST" },
        { label: "Sea Export Entry", href: "/fms/master-bl/sea-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_EXP_ENTRY" },
        { label: "Sea Import List",  href: "/fms/master-bl/sea-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_IMP_LIST" },
        { label: "Sea Import Entry", href: "/fms/master-bl/sea-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_IMP_ENTRY" },
        { label: "Air Export List",  href: "/fms/master-bl/air-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_EXP_LIST" },
        { label: "Air Export Entry", href: "/fms/master-bl/air-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_EXP_ENTRY" },
        { label: "Air Import List",  href: "/fms/master-bl/air-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_IMP_LIST" },
        { label: "Air Import Entry", href: "/fms/master-bl/air-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_IMP_ENTRY" },
      ],
    },
    {
      group: "Truck B/L", icon: Truck, defaultOpen: false, requiredMenuCode: "MENU_FMS_TRUCK_BL",
      children: [
        { label: "List",  href: "/fms/truck-bl/list",  icon: List,     requiredMenuCode: "MENU_FMS_TRUCK_BL_LIST" },
        { label: "Entry", href: "/fms/truck-bl/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_TRUCK_BL_ENTRY" },
      ],
    },
    {
      group: "Non B/L", icon: Package, defaultOpen: false, requiredMenuCode: "MENU_FMS_NON_BL",
      children: [
        { label: "List",  href: "/fms/non-bl/list",  icon: List,     requiredMenuCode: "MENU_FMS_NON_BL_LIST" },
        { label: "Entry", href: "/fms/non-bl/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_NON_BL_ENTRY" },
      ],
    },
  ],
};

// ─── useSyncExternalStore helpers for hydration mount gate ──
// SSR snapshot → false, CSR snapshot → true. setState 없이 hydration guard 구현.
const subscribeNoop = () => () => {};
const getServerSnapshot = () => false;
const getClientSnapshot = () => true;

// ─── Helpers ────────────────────────────────────────────────
function leafActive(pathname: string, leaf: NavLeaf) {
  return pathname === leaf.href || pathname.startsWith(leaf.href + "/");
}

function sectionActive(pathname: string, s: NavSection) {
  return s.children.some((c) => leafActive(pathname, c));
}

// ─── Component ──────────────────────────────────────────────
export function Sidebar() {
  const pathname = usePathname();
  const router   = useRouter();
  const addTab   = useTabs((s) => s.addTab);
  // SSR 시 session=null이므로 admin 메뉴가 모두 숨겨진 결과물을 렌더.
  // mounted 전에는 동일하게 숨겨서 hydration mismatch를 방지.
  const mounted = useSyncExternalStore(subscribeNoop, getClientSnapshot, getServerSnapshot);
  const [session] = useState(() => getSession());

  const [openModules, setOpenModules] = useState<Record<string, boolean>>(() => ({
    [FMS_NAV_MODULE.module]:
      FMS_NAV_MODULE.sections.some((s) => sectionActive(pathname, s)) ||
      (FMS_NAV_MODULE.defaultOpen ?? false),
    Admin: false,
  }));

  const [openSections, setOpenSections] = useState<Record<string, boolean>>(() => {
    const init: Record<string, boolean> = {};
    FMS_NAV_MODULE.sections.forEach((s) => {
      init[s.group] = sectionActive(pathname, s) || (s.defaultOpen ?? false);
    });
    return init;
  });

  const toggleModule  = (module: string) => setOpenModules((p) => ({ ...p, [module]: !p[module] }));
  const toggleSection = (group: string)  => setOpenSections((p) => ({ ...p, [group]:  !p[group]  }));

  function navigate(label: string, href: string) {
    addTab(label, href);
    router.push(href);
  }

  const { editMode, setEditMode, canEdit } = useWidgetLayout();

  useEffect(() => { setEditMode(false); }, [pathname, setEditMode]);

  // Admin 동적 메뉴 fetch — mount 전에는 SSR 무력화
  const { data: adminMenuData, isLoading: adminMenuLoading } = useQuery({
    queryKey: ["sidebar-menu", "accessible"],
    queryFn: () => sidebarMenuPort.fetchAccessibleAdminMenus(),
    enabled: mounted,
    staleTime: 5 * 60 * 1000,
  });

  const fmsModActive = FMS_NAV_MODULE.sections.some((s) => sectionActive(pathname, s));
  const fmsModOpen   = openModules[FMS_NAV_MODULE.module];
  const fmsAccessibleSections = FMS_NAV_MODULE.sections.filter(
    (s) => !s.requiredMenuCode || (mounted && hasMenuAccess(session, s.requiredMenuCode))
  );

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

      {/* FMS 모듈 — 정적, 접근 가능한 섹션이 1개 이상일 때만 렌더 */}
      {fmsAccessibleSections.length > 0 && (
        <div className="side-group">
          <button
            className={`side-group__label side-group__label--toggle${fmsModActive ? " is-active" : ""}`}
            onClick={() => toggleModule(FMS_NAV_MODULE.module)}
          >
            <span style={{ flex: 1 }}>{FMS_NAV_MODULE.module}</span>
            <ChevronRight
              size={11}
              style={{
                flexShrink: 0,
                color: fmsModActive ? "var(--accent)" : "var(--ink-4)",
                transform: fmsModOpen ? "rotate(90deg)" : undefined,
                transition: "transform 160ms ease",
              }}
            />
          </button>

          {fmsModOpen && fmsAccessibleSections.map((section) => {
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
                  .filter((leaf) => !leaf.requiredMenuCode || (mounted && hasMenuAccess(session, leaf.requiredMenuCode)))
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
      )}

      {/* Admin 모듈 — BE fetch 동적 렌더 */}
      <SidebarAdminTree
        data={adminMenuData}
        isLoading={mounted && adminMenuLoading}
        pathname={pathname}
        openSections={openSections}
        onToggleSection={toggleSection}
        onNavigate={navigate}
      />

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
