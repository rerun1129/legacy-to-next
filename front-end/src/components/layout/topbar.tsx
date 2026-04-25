"use client";

import { useEffect, useRef } from "react";
import { usePathname, useRouter } from "next/navigation";
import { Bell, HelpCircle, Search, ChevronLeft } from "lucide-react";
import { useTabs } from "@/lib/use-tabs";

const SIDEBAR_W = 220;

interface Props {
  onToggleSidebar: () => void;
  sidebarCollapsed: boolean;
}

export function Topbar({ onToggleSidebar, sidebarCollapsed }: Props) {
  const pathname   = usePathname();
  const router     = useRouter();
  const { tabs, removeTab } = useTabs();
  const tabsRef    = useRef<HTMLDivElement>(null);

  // 마우스 휠 → 가로 스크롤 변환
  useEffect(() => {
    const el = tabsRef.current;
    if (!el) return;

    function onWheel(e: WheelEvent) {
      // 이미 가로 스크롤이 가능한 상태일 때만 처리
      const canScroll = el!.scrollWidth > el!.clientWidth;
      if (!canScroll) return;
      e.preventDefault();
      // deltaY(세로 휠)를 그대로 scrollLeft에 더함
      // deltaX가 있으면(트랙패드 가로 스와이프) 우선 적용
      el!.scrollLeft += e.deltaX !== 0 ? e.deltaX : e.deltaY;
    }

    el.addEventListener("wheel", onWheel, { passive: false });
    return () => el.removeEventListener("wheel", onWheel);
  }, []);

  function handleClose(e: React.MouseEvent, id: string) {
    e.stopPropagation();
    const isActive = id === pathname;
    const target   = removeTab(id);
    if (isActive) router.push(target);
  }

  return (
    <header className="app__topbar" style={{ overflow: "hidden" }}>

      {/* ── Brand ─────────────────────────────────────────────── */}
      <div
        className="app__brand"
        style={{
          width: sidebarCollapsed ? 100 : SIDEBAR_W,
          minWidth: sidebarCollapsed ? 100 : SIDEBAR_W,
          flexShrink: 0,
          transition: "width 180ms ease, min-width 180ms ease",
          borderRight: "1px solid var(--border)",
          paddingRight: 12,
          overflow: "hidden",
          cursor: "pointer",
        }}
        onClick={() => router.push("/dashboard")}
      >
        <div className="app__brand-mark">FMS</div>
        <span
          style={{
            overflow: "hidden",
            textOverflow: "ellipsis",
            whiteSpace: "nowrap",
            opacity: sidebarCollapsed ? 0 : 1,
            transition: "opacity 160ms ease",
          }}
        >
          FreightOS
        </span>
      </div>

      {/* ── Tab strip ─────────────────────────────────────────── */}
      <div ref={tabsRef} className="app__tabs">
        {tabs.filter((tab) => tab.href !== "/dashboard").map((tab) => {
          const active = pathname === tab.href;
          return (
            <button
              key={tab.id}
              className={`app__tab${active ? " is-active" : ""}`}
              onClick={() => router.push(tab.href)}
              title={tab.label}
            >
              <span style={{ maxWidth: 160, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                {tab.label}
              </span>
              <span className="app__tab-close" onClick={(e) => handleClose(e, tab.id)}>×</span>
            </button>
          );
        })}
      </div>

      {/* ── Right icons ───────────────────────────────────────── */}
      <div className="app__topbar-right" style={{ flexShrink: 0 }}>
        <div className="topbar-icons">
          <button className="topbar-icon" title="Search (⌘K)"><Search /></button>
          <button className="topbar-icon" title="Notifications">
            <Bell />
            <span className="topbar-icon__dot" />
          </button>
          <button className="topbar-icon" title="Help"><HelpCircle /></button>
        </div>
        <div className="app__user">
          <div className="app__user-avatar">KY</div>
          <span>김영선</span>
        </div>
        <button
          className="topbar-icon"
          onClick={onToggleSidebar}
          title={sidebarCollapsed ? "Show sidebar" : "Hide sidebar"}
          style={{ marginLeft: 4 }}
        >
          <ChevronLeft style={{ transform: sidebarCollapsed ? "rotate(180deg)" : undefined, transition: "transform 180ms ease" }} />
        </button>
      </div>
    </header>
  );
}
