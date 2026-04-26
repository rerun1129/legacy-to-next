"use client";

import { useState } from "react";
import { createPortal } from "react-dom";
import { RotateCcw, GripHorizontal, ChevronUp, ChevronDown, X } from "lucide-react";
import type { WidgetKey } from "@/lib/use-widget-layout";
import type { WidgetDef }  from "./widget-registry";

interface Props {
  scope:    string;
  registry: WidgetDef[];
  hidden:   WidgetKey[];
  onShow:   (key: WidgetKey) => void;
  onReset:  () => void;
  onClose:  () => void;
}

export function WidgetPalette({ registry, hidden, onShow, onReset, onClose }: Props) {
  const [pos, setPos]           = useState({ x: 24, y: 80 });
  const [collapsed, setCollapsed] = useState(false);

  const hiddenSet = new Set(hidden);
  const defMap    = Object.fromEntries(registry.map(d => [d.key, d]));

  function handleDragStart(e: React.MouseEvent) {
    e.preventDefault();
    const startMouseX = e.clientX;
    const startMouseY = e.clientY;
    const startX      = pos.x;
    const startY      = pos.y;

    function onMove(ev: MouseEvent) {
      setPos({
        x: startX + ev.clientX - startMouseX,
        y: startY + ev.clientY - startMouseY,
      });
    }
    function onUp() {
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup",   onUp);
    }
    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup",   onUp);
  }

  const palette = (
    <div className="widget-palette" style={{ left: pos.x, top: pos.y }}>
      {/* 헤더 — 드래그 핸들 */}
      <div
        className="widget-palette__head"
        onMouseDown={handleDragStart}
        style={{ cursor: "grab" }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
          <GripHorizontal size={13} style={{ color: "var(--ink-4)", flexShrink: 0 }} />
          <span className="widget-palette__title">위젯 팔레트</span>
        </div>
        <div style={{ display: "flex", gap: 2 }}>
          <button
            className="widget-palette__icon-btn"
            onClick={onReset}
            title="기본값으로 재설정"
            onMouseDown={e => e.stopPropagation()}
          >
            <RotateCcw size={12} />
          </button>
          <button
            className="widget-palette__icon-btn"
            onClick={() => setCollapsed(c => !c)}
            title={collapsed ? "펼치기" : "접기"}
            onMouseDown={e => e.stopPropagation()}
          >
            {collapsed ? <ChevronDown size={12} /> : <ChevronUp size={12} />}
          </button>
          <button
            className="widget-palette__icon-btn"
            onClick={onClose}
            title="닫기"
            onMouseDown={e => e.stopPropagation()}
          >
            <X size={12} />
          </button>
        </div>
      </div>

      {/* 바디 — 접기 시 숨김 */}
      {!collapsed && (
        <div className="widget-palette__body">
          {registry.map(({ key }) => {
            const def      = defMap[key];
            const isHidden = hiddenSet.has(key);
            return (
              <div
                key={key}
                className={`widget-palette__card${isHidden ? "" : " is-visible"}`}
                title={isHidden ? "클릭하여 그리드에 추가" : "현재 표시 중"}
                onClick={() => isHidden && onShow(key)}
              >
                <span>{def.label}</span>
                {isHidden
                  ? <span className="widget-palette__badge">숨김</span>
                  : <span className="widget-palette__badge widget-palette__badge--on">표시 중</span>
                }
              </div>
            );
          })}
        </div>
      )}
    </div>
  );

  return createPortal(palette, document.body);
}
