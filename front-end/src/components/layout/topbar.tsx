"use client";

import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { usePathname, useRouter } from "next/navigation";
import { ChevronLeft, LogOut, Moon, Sun } from "lucide-react";
import { useTranslations } from "next-intl";
import { useTabs, inferLabelFromPath } from "@/lib/use-tabs";
import { useTheme } from "@/lib/use-theme";
import { authUseCases } from "@/application/auth/use-cases";
import { clearSession, getSession } from "@/lib/admin-session";
import { LanguageToggle } from "./language-toggle";

const SIDEBAR_W = 220;

interface Props {
  onToggleSidebar: () => void;
  sidebarCollapsed: boolean;
}

interface DragState {
  tabId: string;
  origIndex: number;
  deltaX: number;
  insertIndex: number;
  tabWidth: number;
}

export function Topbar({ onToggleSidebar, sidebarCollapsed }: Props) {
  const pathname = usePathname();
  const router   = useRouter();
  const { tabs, removeTab, initFromPath, reorderTabs, closeOtherTabs, closeTabsToRight, closeTabsToLeft, clearTabs } = useTabs();
  const tabsRef  = useRef<HTMLDivElement>(null);

  const [ctxMenu, setCtxMenu]     = useState<{ x: number; y: number; tabId: string } | null>(null);
  const [dragState, setDragState] = useState<DragState | null>(null);
  const didDragRef = useRef(false);

  const { dark, toggle: toggleTheme } = useTheme();

  const t  = useTranslations('shell.topbar');
  const tt = useTranslations('shell.tabs');

  const displayTabs = tabs.filter(tab => tab.href !== "/dashboard");

  useEffect(() => { initFromPath(pathname); }, [pathname, initFromPath]);

  useEffect(() => {
    const container = tabsRef.current;
    if (!container) return;
    const activeTab = container.querySelector<HTMLElement>(".app__tab.is-active");
    activeTab?.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "nearest" });
  }, [pathname, tabs]);

  useEffect(() => {
    const el = tabsRef.current;
    if (!el) return;
    function onWheel(e: WheelEvent) {
      if (el!.scrollWidth <= el!.clientWidth) return;
      e.preventDefault();
      el!.scrollLeft += e.deltaX !== 0 ? e.deltaX : e.deltaY;
    }
    el.addEventListener("wheel", onWheel, { passive: false });
    return () => el.removeEventListener("wheel", onWheel);
  }, []);

  useEffect(() => {
    if (!ctxMenu) return;
    function onMouseDown() { setCtxMenu(null); }
    window.addEventListener("mousedown", onMouseDown);
    return () => window.removeEventListener("mousedown", onMouseDown);
  }, [ctxMenu]);

  // ── Tab close ────────────────────────────────────────────
  function handleClose(e: React.MouseEvent, id: string) {
    e.stopPropagation();
    const isActive = id === pathname;
    const target   = removeTab(id);
    if (isActive) router.push(target);
  }

  // ── Context menu ─────────────────────────────────────────
  function handleContextMenu(e: React.MouseEvent, tabId: string) {
    e.preventDefault();
    setCtxMenu({ x: e.clientX, y: e.clientY, tabId });
  }

  function handleCloseOthers(tabId: string) {
    if (pathname !== tabId) router.push(tabId);
    closeOtherTabs(tabId);
    setCtxMenu(null);
  }

  function handleCloseRight(tabId: string) {
    const tabIdx  = tabs.findIndex(t => t.id === tabId);
    const currIdx = tabs.findIndex(t => t.id === pathname);
    if (currIdx > tabIdx) router.push(tabId);
    closeTabsToRight(tabId);
    setCtxMenu(null);
  }

  function handleCloseLeft(tabId: string) {
    const tabIdx  = tabs.findIndex(t => t.id === tabId);
    const currIdx = tabs.findIndex(t => t.id === pathname);
    if (currIdx < tabIdx) router.push(tabId);
    closeTabsToLeft(tabId);
    setCtxMenu(null);
  }

  // ── Drag animation ───────────────────────────────────────
  //
  // Algorithm: keep tabs in their original flex positions, apply translateX.
  // For tab at original index `i`, remaining index ri = i < origIndex ? i : i-1
  //   - i < origIndex AND ri >= insertIndex  →  shift right by tabWidth  (make room)
  //   - i > origIndex AND ri < insertIndex   →  shift left by tabWidth   (fill gap)
  //   - i === origIndex                      →  follow cursor (deltaX)
  function getTabStyle(index: number): React.CSSProperties {
    if (!dragState || Math.abs(dragState.deltaX) <= 4) return {};
    const { origIndex, deltaX, insertIndex, tabWidth } = dragState;

    if (index === origIndex) {
      return { transform: `translateX(${deltaX}px)`, transition: "none", position: "relative", zIndex: 10 };
    }

    const ri = index < origIndex ? index : index - 1;
    let shift = 0;
    if (index < origIndex && ri >= insertIndex)  shift = tabWidth;
    else if (index > origIndex && ri < insertIndex) shift = -tabWidth;

    return { transform: `translateX(${shift}px)`, transition: "transform 150ms ease" };
  }

  function handleTabMouseDown(e: React.MouseEvent, tabId: string) {
    if (e.button !== 0) return;

    const container  = tabsRef.current!;
    const origIndex  = displayTabs.findIndex(t => t.id === tabId);
    const containerLeft = container.getBoundingClientRect().left;
    const allEls     = Array.from(container.querySelectorAll<HTMLElement>(".app__tab"));
    const centers    = allEls.map(el => {
      const r = el.getBoundingClientRect();
      return r.left + r.width / 2 - containerLeft;
    });
    const tabWidth   = allEls[origIndex]?.getBoundingClientRect().width ?? 100;
    const startX     = e.clientX;

    // 클로저 내 가변 변수로 최신값 추적 — setState 콜백 안에서 다른 setState 호출 방지
    let latestDeltaX    = 0;
    let latestInsertIdx = origIndex;

    const handleMove = (ev: MouseEvent) => {
      const deltaX = ev.clientX - startX;
      latestDeltaX = deltaX;
      if (Math.abs(deltaX) > 4) didDragRef.current = true;

      const dragCenter = centers[origIndex] + deltaX;
      const rest = centers.filter((_, i) => i !== origIndex);
      let insertIdx = 0;
      for (let i = 0; i < rest.length; i++) {
        if (dragCenter > rest[i]) insertIdx = i + 1;
      }
      latestInsertIdx = insertIdx;
      setDragState({ tabId, origIndex, deltaX, insertIndex: insertIdx, tabWidth });
    };

    const handleUp = () => {
      if (latestInsertIdx !== origIndex && Math.abs(latestDeltaX) > 4) {
        reorderTabs(tabId, latestInsertIdx);
      }
      setDragState(null);
      window.removeEventListener("mousemove", handleMove);
      window.removeEventListener("mouseup", handleUp);
    };

    window.addEventListener("mousemove", handleMove);
    window.addEventListener("mouseup", handleUp);
  }

  function handleTabClick(e: React.MouseEvent, href: string) {
    if (didDragRef.current) { didDragRef.current = false; return; }
    router.push(href);
  }

  const handleLogout = async () => {
    const session = getSession();
    if (session) {
      try {
        await authUseCases.logout(session.refreshToken);
      } catch {
        // best-effort — 실패해도 로컬 세션 정리
      }
    }
    clearTabs();
    clearSession();
    router.replace("/login");
  };

  const isDragging = dragState !== null && Math.abs(dragState.deltaX) > 4;

  return (
    <>
      <header className="app__topbar" style={{ overflow: "hidden" }}>

        {/* ── Brand + Toggle ─────────────────────────────────── */}
        <div
          style={{
            display: "flex", alignItems: "center", flexShrink: 0, overflow: "hidden",
            borderRight: "1px solid var(--border)", padding: "0 4px",
            width: sidebarCollapsed ? 68 : SIDEBAR_W, minWidth: sidebarCollapsed ? 68 : SIDEBAR_W,
            transition: "width 180ms ease, min-width 180ms ease",
          }}
        >
          <div className="app__brand" style={{ flex: 1, overflow: "hidden", cursor: "pointer" }} onClick={() => router.push("/dashboard")}>
            <div className="app__brand-mark">FMS</div>
            <span style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap", opacity: sidebarCollapsed ? 0 : 1, transition: "opacity 160ms ease" }}>
              FreightOS
            </span>
          </div>
          <button type="button" className="topbar-icon" onClick={onToggleSidebar} title={sidebarCollapsed ? t('showSidebar') : t('hideSidebar')} style={{ flexShrink: 0 }}>
            <ChevronLeft style={{ transform: sidebarCollapsed ? "rotate(180deg)" : undefined, transition: "transform 180ms ease" }} />
          </button>
        </div>

        {/* ── Tab strip ──────────────────────────────────────── */}
        <div ref={tabsRef} className={`app__tabs${isDragging ? " is-dragging-tabs" : ""}`}>
          {displayTabs.map((tab, index) => {
            const active    = pathname === tab.href;
            const dragged   = isDragging && dragState?.tabId === tab.id;
            // Translate from pathname at render time so locale switches retranslate open tabs.
            const tabLabel  = tt.has(tab.href) ? tt(tab.href) : inferLabelFromPath(tab.href);
            return (
              <button
                key={tab.id}
                className={`app__tab${active ? " is-active" : ""}${dragged ? " is-dragging" : ""}`}
                style={getTabStyle(index)}
                title={tabLabel}
                onMouseDown={(e) => handleTabMouseDown(e, tab.id)}
                onClick={(e) => handleTabClick(e, tab.href)}
                onContextMenu={(e) => handleContextMenu(e, tab.id)}
              >
                <span style={{ maxWidth: 240, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                  {tabLabel}
                </span>
                <span className="app__tab-close" onClick={(e) => handleClose(e, tab.id)}>×</span>
              </button>
            );
          })}
        </div>

        {/* ── Right icons ────────────────────────────────────── */}
        <div className="app__topbar-right" style={{ flexShrink: 0 }}>
          <div className="topbar-icons">
            <button type="button" className="topbar-icon" title={dark ? t('lightMode') : t('darkMode')} onClick={toggleTheme}>
              {dark ? <Sun /> : <Moon />}
            </button>
            <LanguageToggle />
          </div>
          <button type="button" className="topbar-icon" title={t('logout')} style={{ margin: 4 }} onClick={handleLogout}><LogOut /></button>
        </div>
      </header>

      {/* ── Tab context menu ───────────────────────────────────── */}
      {ctxMenu && createPortal(
        <div
          className="tab-context-menu"
          style={{ left: ctxMenu.x, top: ctxMenu.y }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          <button type="button" onClick={() => handleCloseOthers(ctxMenu.tabId)}>{t('closeOtherTabs')}</button>
          <button type="button" onClick={() => handleCloseRight(ctxMenu.tabId)}>{t('closeTabsToRight')}</button>
          <button type="button" onClick={() => handleCloseLeft(ctxMenu.tabId)}>{t('closeTabsToLeft')}</button>
        </div>,
        document.body
      )}

    </>
  );
}
