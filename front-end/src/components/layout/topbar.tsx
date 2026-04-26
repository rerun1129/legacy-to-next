"use client";

import { useEffect, useRef } from "react";
import { usePathname, useRouter } from "next/navigation";
import { Bell, HelpCircle, Search, ChevronLeft, LogOut } from "lucide-react";
import { useTabs } from "@/lib/use-tabs";

const SIDEBAR_W = 220;

interface Props {
  onToggleSidebar: () => void;
  sidebarCollapsed: boolean;
}

export function Topbar({ onToggleSidebar, sidebarCollapsed }: Props) {
  const pathname   = usePathname();
  const router     = useRouter();
  const { tabs, removeTab, initFromPath } = useTabs();
  const tabsRef    = useRef<HTMLDivElement>(null);

  // CSR 마운트 시 현재 경로로 탭 초기화 (Hydration mismatch 방지)
  useEffect(() => {
    initFromPath(pathname);
  }, [pathname, initFromPath]);

  // 활성 탭을 탭 스트립 내에서 보이도록 스크롤
  useEffect(() => {
    const container = tabsRef.current;
    if (!container) return;
    const activeTab = container.querySelector<HTMLElement>(".app__tab.is-active");
    activeTab?.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "nearest" });
  }, [pathname, tabs]);

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

      {/* ── Brand + Toggle ────────────────────────────────────── */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          flexShrink: 0,
          overflow: "hidden",
          borderRight: "1px solid var(--border)",
          padding: "0 4px",
          width: sidebarCollapsed ? 68 : SIDEBAR_W,
          minWidth: sidebarCollapsed ? 68 : SIDEBAR_W,
          transition: "width 180ms ease, min-width 180ms ease",
        }}
      >
        <div
          className="app__brand"
          style={{ flex: 1, overflow: "hidden", cursor: "pointer" }}
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
        <button
          className="topbar-icon"
          onClick={onToggleSidebar}
          title={sidebarCollapsed ? "Show sidebar" : "Hide sidebar"}
          style={{ flexShrink: 0 }}
        >
          <ChevronLeft style={{ transform: sidebarCollapsed ? "rotate(180deg)" : undefined, transition: "transform 180ms ease" }} />
        </button>
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
              <span style={{ maxWidth: 240, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
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
          title="Logout"
          style={{ margin: 4 }}
        >
          <LogOut />
        </button>
      </div>
    </header>
  );
}
