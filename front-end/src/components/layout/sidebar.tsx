"use client";

import { usePathname, useRouter } from "next/navigation";
import { useState, useEffect, useSyncExternalStore } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import {
  LayoutDashboard, FileText, Layers, Truck, Package,
  ChevronRight, List, FilePlus, LayoutGrid, Search,
  Receipt, Stamp, FileSpreadsheet, BarChart3,
} from "lucide-react";
import { useTabs } from "@/lib/use-tabs";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { getSession, hasMenuAccess } from "@/lib/admin-session";
import { sidebarMenuPort } from "@/lib/ports";
import { SidebarAdminTree } from "./sidebar-admin-tree";
import { QuickSearchPanel } from "./quick-search/quick-search-panel";


// ─── Types ──────────────────────────────────────────────────
interface NavLeaf {
  labelKey: string;
  href: string;
  icon: React.ComponentType<{ size?: number }>;
  requiredMenuCode?: string;
}

interface NavSection {
  sectionKey: string;
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
      sectionKey: "houseBl", icon: FileText, defaultOpen: true, requiredMenuCode: "MENU_FMS_HOUSE_BL",
      children: [
        { labelKey: "seaExportList",  href: "/fms/house-bl/sea-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_EXP_LIST" },
        { labelKey: "seaExportEntry", href: "/fms/house-bl/sea-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_EXP_ENTRY" },
        { labelKey: "seaImportList",  href: "/fms/house-bl/sea-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_IMP_LIST" },
        { labelKey: "seaImportEntry", href: "/fms/house-bl/sea-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_SEA_IMP_ENTRY" },
        { labelKey: "airExportList",  href: "/fms/house-bl/air-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_EXP_LIST" },
        { labelKey: "airExportEntry", href: "/fms/house-bl/air-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_EXP_ENTRY" },
        { labelKey: "airImportList",  href: "/fms/house-bl/air-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_IMP_LIST" },
        { labelKey: "airImportEntry", href: "/fms/house-bl/air-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_HOUSE_BL_AIR_IMP_ENTRY" },
      ],
    },
    {
      sectionKey: "masterBl", icon: Layers, defaultOpen: false, requiredMenuCode: "MENU_FMS_MASTER_BL",
      children: [
        { labelKey: "seaExportList",  href: "/fms/master-bl/sea-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_EXP_LIST" },
        { labelKey: "seaExportEntry", href: "/fms/master-bl/sea-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_EXP_ENTRY" },
        { labelKey: "seaImportList",  href: "/fms/master-bl/sea-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_IMP_LIST" },
        { labelKey: "seaImportEntry", href: "/fms/master-bl/sea-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_SEA_IMP_ENTRY" },
        { labelKey: "airExportList",  href: "/fms/master-bl/air-exp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_EXP_LIST" },
        { labelKey: "airExportEntry", href: "/fms/master-bl/air-exp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_EXP_ENTRY" },
        { labelKey: "airImportList",  href: "/fms/master-bl/air-imp/list",  icon: List,     requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_IMP_LIST" },
        { labelKey: "airImportEntry", href: "/fms/master-bl/air-imp/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_MASTER_BL_AIR_IMP_ENTRY" },
      ],
    },
    {
      sectionKey: "truckBl", icon: Truck, defaultOpen: false, requiredMenuCode: "MENU_FMS_TRUCK_BL",
      children: [
        { labelKey: "list",  href: "/fms/truck-bl/list",  icon: List,     requiredMenuCode: "MENU_FMS_TRUCK_BL_LIST" },
        { labelKey: "entry", href: "/fms/truck-bl/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_TRUCK_BL_ENTRY" },
      ],
    },
    {
      sectionKey: "nonBl", icon: Package, defaultOpen: false, requiredMenuCode: "MENU_FMS_NON_BL",
      children: [
        { labelKey: "list",  href: "/fms/non-bl/list",  icon: List,     requiredMenuCode: "MENU_FMS_NON_BL_LIST" },
        { labelKey: "entry", href: "/fms/non-bl/entry", icon: FilePlus, requiredMenuCode: "MENU_FMS_NON_BL_ENTRY" },
      ],
    },
  ],
};

// ─── BMS Nav data (정적) ────────────────────────────────────
const BMS_NAV_MODULE: NavModule = {
  module: "BMS", defaultOpen: false,
  sections: [
    {
      sectionKey: "financialDoc", icon: Receipt, defaultOpen: true, requiredMenuCode: "MENU_BMS_FINANCIAL",
      children: [
        { labelKey: "invoice",  href: "/bms/invoice/list",  icon: List, requiredMenuCode: "MENU_BMS_INVOICE"  },
        { labelKey: "payment",  href: "/bms/payment/list",  icon: List, requiredMenuCode: "MENU_BMS_PAYMENT"  },
        { labelKey: "dcNote",   href: "/bms/dc-note/list",  icon: List, requiredMenuCode: "MENU_BMS_DC_NOTE"  },
      ],
    },
    {
      // BMS_ISSUE parent — DB menu_code: BMS_ISSUE → FE: MENU_BMS_ISSUE
      sectionKey: "bmsIssue", icon: Stamp, defaultOpen: false, requiredMenuCode: "MENU_BMS_ISSUE",
      children: [
        { labelKey: "taxInvoiceIssue", href: "/bms/tax-invoice/issue", icon: FileSpreadsheet, requiredMenuCode: "MENU_BMS_TAX_INVOICE" },
        { labelKey: "slipIssue",       href: "/bms/slip/issue",        icon: Stamp,           requiredMenuCode: "MENU_BMS_SLIP"        },
      ],
    },
  ],
};

// ─── PMS Nav data (정적) ────────────────────────────────────
const PMS_NAV_MODULE: NavModule = {
  module: "PMS", defaultOpen: false,
  sections: [
    {
      sectionKey: "performance", icon: BarChart3, defaultOpen: true, requiredMenuCode: "MENU_PMS_PERFORMANCE",
      children: [
        { labelKey: "performance", href: "/pms/performance", icon: BarChart3, requiredMenuCode: "MENU_PMS_PERFORMANCE" },
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

// ─── Sidebar props ──────────────────────────────────────────
interface SidebarProps {
  quickSearchOpen: boolean;
  onOpenQuickSearch: () => void;
  onCloseQuickSearch: () => void;
}

// ─── Component ──────────────────────────────────────────────
export function Sidebar({ quickSearchOpen, onOpenQuickSearch, onCloseQuickSearch }: SidebarProps) {
  const pathname = usePathname();
  const router   = useRouter();
  const addTab   = useTabs((s) => s.addTab);
  const t        = useTranslations('shell.sidebar');
  // SSR 시 session=null이므로 admin 메뉴가 모두 숨겨진 결과물을 렌더.
  // mounted 전에는 동일하게 숨겨서 hydration mismatch를 방지.
  const mounted = useSyncExternalStore(subscribeNoop, getClientSnapshot, getServerSnapshot);
  const [session] = useState(() => getSession());

  const [openModules, setOpenModules] = useState<Record<string, boolean>>(() => ({
    [FMS_NAV_MODULE.module]:
      FMS_NAV_MODULE.sections.some((s) => sectionActive(pathname, s)) ||
      (FMS_NAV_MODULE.defaultOpen ?? false),
    [BMS_NAV_MODULE.module]:
      BMS_NAV_MODULE.sections.some((s) => sectionActive(pathname, s)) ||
      (BMS_NAV_MODULE.defaultOpen ?? false),
    [PMS_NAV_MODULE.module]:
      PMS_NAV_MODULE.sections.some((s) => sectionActive(pathname, s)) ||
      (PMS_NAV_MODULE.defaultOpen ?? false),
    Admin: false,
  }));

  const [openSections, setOpenSections] = useState<Record<string, boolean>>(() => {
    const init: Record<string, boolean> = {};
    FMS_NAV_MODULE.sections.forEach((s) => {
      init[s.sectionKey] = sectionActive(pathname, s) || (s.defaultOpen ?? false);
    });
    BMS_NAV_MODULE.sections.forEach((s) => {
      init[s.sectionKey] = sectionActive(pathname, s) || (s.defaultOpen ?? false);
    });
    PMS_NAV_MODULE.sections.forEach((s) => {
      init[s.sectionKey] = sectionActive(pathname, s) || (s.defaultOpen ?? false);
    });
    return init;
  });

  const toggleModule  = (module: string) => setOpenModules((p) => ({ ...p, [module]: !p[module] }));
  const toggleSection = (key: string)    => setOpenSections((p) => ({ ...p, [key]: !p[key] }));

  // Pass href as the label to addTab — the topbar resolves the display label
  // from the pathname via useTranslations('shell.tabs') at render time.
  function navigate(href: string) {
    addTab(href, href);
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

  const bmsModActive = BMS_NAV_MODULE.sections.some((s) => sectionActive(pathname, s));
  const bmsModOpen   = openModules[BMS_NAV_MODULE.module];
  const bmsAccessibleSections = BMS_NAV_MODULE.sections.filter(
    (s) => !s.requiredMenuCode || (mounted && hasMenuAccess(session, s.requiredMenuCode))
  );

  const pmsModActive = PMS_NAV_MODULE.sections.some((s) => sectionActive(pathname, s));
  const pmsModOpen   = openModules[PMS_NAV_MODULE.module];
  const pmsAccessibleSections = PMS_NAV_MODULE.sections.filter(
    (s) => !s.requiredMenuCode || (mounted && hasMenuAccess(session, s.requiredMenuCode))
  );

  if (quickSearchOpen) {
    return (
      <nav className="app__side" style={{ display: "flex", flexDirection: "column" }}>
        <QuickSearchPanel onBack={onCloseQuickSearch} />
      </nav>
    );
  }

  return (
    <nav className="app__side" style={{ display: "flex", flexDirection: "column" }}>
      {/* Dashboard — 최상단 */}
      <button
        className={`side-item${pathname === DASHBOARD_HREF ? " is-active" : ""}`}
        onClick={() => navigate(DASHBOARD_HREF)}
      >
        <span className="side-item__icon"><LayoutDashboard size={14} /></span>
        {t('dashboard')}
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

          {/* Quick Search 진입 버튼 — BL 메뉴 접근 보유 시에만 표시 */}
          {fmsModOpen && mounted && (
            hasMenuAccess(session, "MENU_FMS_HOUSE_BL") || hasMenuAccess(session, "MENU_FMS_MASTER_BL")
          ) && (
            <button
              type="button"
              className="side-item"
              style={{ paddingLeft: 12 }}
              onClick={onOpenQuickSearch}
            >
              <span className="side-item__icon"><Search size={13} /></span>
              <span style={{ flex: 1, textAlign: "left" }}>{t('quickSearch')}</span>
            </button>
          )}

          {fmsModOpen && fmsAccessibleSections.map((section) => {
            const secActive = sectionActive(pathname, section);
            const secOpen   = openSections[section.sectionKey];

            return (
              <div key={section.sectionKey}>
                <button
                  className={`side-item${secActive ? " is-active" : ""}`}
                  style={{ paddingLeft: 12 }}
                  onClick={() => toggleSection(section.sectionKey)}
                >
                  <span className="side-item__icon"><section.icon size={13} /></span>
                  <span style={{ flex: 1, textAlign: "left" }}>{t(section.sectionKey)}</span>
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
                      onClick={() => navigate(leaf.href)}
                    >
                      <span className="side-item__icon"><leaf.icon size={11} /></span>
                      {t(leaf.labelKey)}
                    </button>
                  );
                })}
              </div>
            );
          })}
        </div>
      )}

      {/* BMS 모듈 — 정적, 접근 가능한 섹션이 1개 이상일 때만 렌더 */}
      {bmsAccessibleSections.length > 0 && (
        <div className="side-group">
          <button
            className={`side-group__label side-group__label--toggle${bmsModActive ? " is-active" : ""}`}
            onClick={() => toggleModule(BMS_NAV_MODULE.module)}
          >
            <span style={{ flex: 1 }}>{BMS_NAV_MODULE.module}</span>
            <ChevronRight
              size={11}
              style={{
                flexShrink: 0,
                color: bmsModActive ? "var(--accent)" : "var(--ink-4)",
                transform: bmsModOpen ? "rotate(90deg)" : undefined,
                transition: "transform 160ms ease",
              }}
            />
          </button>

          {/* Quick Search 진입 버튼 — BMS 금융서류 접근 보유 시에만 표시 */}
          {bmsModOpen && mounted && hasMenuAccess(session, "MENU_BMS_FINANCIAL") && (
            <button
              type="button"
              className="side-item"
              style={{ paddingLeft: 12 }}
              onClick={onOpenQuickSearch}
            >
              <span className="side-item__icon"><Search size={13} /></span>
              <span style={{ flex: 1, textAlign: "left" }}>{t('quickSearch')}</span>
            </button>
          )}

          {bmsModOpen && bmsAccessibleSections.map((section) => {
            const secActive = sectionActive(pathname, section);
            const secOpen   = openSections[section.sectionKey];

            return (
              <div key={section.sectionKey}>
                <button
                  className={`side-item${secActive ? " is-active" : ""}`}
                  style={{ paddingLeft: 12 }}
                  onClick={() => toggleSection(section.sectionKey)}
                >
                  <span className="side-item__icon"><section.icon size={13} /></span>
                  <span style={{ flex: 1, textAlign: "left" }}>{t(section.sectionKey)}</span>
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
                  .filter((leaf) => !leaf.requiredMenuCode || (mounted && hasMenuAccess(session, leaf.requiredMenuCode)))
                  .map((leaf) => {
                  const active = leafActive(pathname, leaf);
                  return (
                    <button
                      key={leaf.href}
                      className={`side-item${active ? " is-active" : ""}`}
                      style={{ paddingLeft: 32, fontSize: "var(--fs-xs)" }}
                      onClick={() => navigate(leaf.href)}
                    >
                      <span className="side-item__icon"><leaf.icon size={11} /></span>
                      {t(leaf.labelKey)}
                    </button>
                  );
                })}
              </div>
            );
          })}
        </div>
      )}

      {/* PMS 모듈 — 정적, 접근 가능한 섹션이 1개 이상일 때만 렌더 */}
      {pmsAccessibleSections.length > 0 && (
        <div className="side-group">
          <button
            className={`side-group__label side-group__label--toggle${pmsModActive ? " is-active" : ""}`}
            onClick={() => toggleModule(PMS_NAV_MODULE.module)}
          >
            <span style={{ flex: 1 }}>{PMS_NAV_MODULE.module}</span>
            <ChevronRight
              size={11}
              style={{
                flexShrink: 0,
                color: pmsModActive ? "var(--accent)" : "var(--ink-4)",
                transform: pmsModOpen ? "rotate(90deg)" : undefined,
                transition: "transform 160ms ease",
              }}
            />
          </button>

          {pmsModOpen && pmsAccessibleSections.map((section) => {
            const secActive = sectionActive(pathname, section);
            const secOpen   = openSections[section.sectionKey];

            return (
              <div key={section.sectionKey}>
                <button
                  className={`side-item${secActive ? " is-active" : ""}`}
                  style={{ paddingLeft: 12 }}
                  onClick={() => toggleSection(section.sectionKey)}
                >
                  <span className="side-item__icon"><section.icon size={13} /></span>
                  <span style={{ flex: 1, textAlign: "left" }}>{t(section.sectionKey)}</span>
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
                  .filter((leaf) => !leaf.requiredMenuCode || (mounted && hasMenuAccess(session, leaf.requiredMenuCode)))
                  .map((leaf) => {
                  const active = leafActive(pathname, leaf);
                  return (
                    <button
                      key={leaf.href}
                      className={`side-item${active ? " is-active" : ""}`}
                      style={{ paddingLeft: 32, fontSize: "var(--fs-xs)" }}
                      onClick={() => navigate(leaf.href)}
                    >
                      <span className="side-item__icon"><leaf.icon size={11} /></span>
                      {t(leaf.labelKey)}
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
        onNavigate={(_label, href) => navigate(href)}
      />

      {/* ── 하단: 위젯 편집 토글 ── */}
      <div style={{ flex: 1 }} />
      {pathname.includes("/entry") && (
        <div style={{ padding: "8px 4px", borderTop: "1px solid var(--border)" }}>
          <button
            className={`side-edit-btn${editMode ? " is-active" : ""}${!canEdit ? " is-disabled" : ""}`}
            onClick={() => canEdit && setEditMode(!editMode)}
            disabled={!canEdit}
            title={canEdit ? t('widgetEditTitle') : t('widgetEditDisabledTitle')}
          >
            <LayoutGrid size={14} style={{ flexShrink: 0 }} />
            <span style={{ overflow: "hidden", textOverflow: "ellipsis" }}>{t('widgetEditMode')}</span>
          </button>
        </div>
      )}
    </nav>
  );
}
