"use client";

import { useCallback, useRef, useState, useEffect } from "react";
import GridLayout, { type Layout } from "react-grid-layout";
import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { useCurrentUser }  from "@/lib/use-current-user";
import { WidgetContainer } from "./widget-container";
import { WidgetPalette }   from "./widget-palette";
import type { WidgetDef }  from "./widget-registry";
import { WIDGET_REGISTRY, REGISTRY_MAP as DEFAULT_MAP, getDefaultPositions as seaDefaults } from "./widget-registry";

const COLS       = 6;
const ROW_HEIGHT = 150;
const GAP        = 8;

interface Props {
  scope:    string;
  variant:  unknown;           // 각 패널 컴포넌트가 자체 타입 처리
  registry: WidgetDef[];       // 엔트리 타입별 레지스트리
}

function buildRegistryMap(registry: WidgetDef[]) {
  return Object.fromEntries(registry.map(d => [d.key, d])) as Record<string, WidgetDef>;
}

function toRGLLayout(visible: { key: string; col: number; row: number; colSpan: number; rowSpan: number }[], map: Record<string, WidgetDef>) {
  return visible.map(w => ({
    i: w.key, x: w.col, y: w.row, w: w.colSpan, h: w.rowSpan,
    minW: map[w.key]?.minColSpan ?? 1,
    minH: map[w.key]?.minRowSpan ?? 1,
  }));
}

export function WidgetGrid({ scope, variant, registry }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [containerWidth, setContainerWidth] = useState(0);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const el = containerRef.current;
    if (!el) return;
    setContainerWidth(el.clientWidth);
    const ro = new ResizeObserver(([entry]) => setContainerWidth(entry.contentRect.width));
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  const currentUserId = useCurrentUser(s => s.currentUserId);
  const userScope = `${currentUserId}.${scope}`;

  const { editMode, initLayout, getLayout, moveWidget, resizeWidget, hideWidget, showWidget, resetLayout } = useWidgetLayout();

  const registryMap = buildRegistryMap(registry);
  const defaults    = registry.map(d => ({ key: d.key, ...d.defaultPosition }));
  const layout      = getLayout(userScope, defaults);

  useEffect(() => { initLayout(userScope, defaults); }, [userScope]); // eslint-disable-line

  const handleLayoutChange = useCallback((next: Layout) => {
    next.forEach(item => {
      const key  = item.i;
      const prev = layout.visible.find(w => w.key === key);
      if (!prev) return;
      if (prev.col !== item.x || prev.row !== item.y) moveWidget(userScope, key, item.x, item.y);
      if (prev.colSpan !== item.w || prev.rowSpan !== item.h) resizeWidget(userScope, key, item.w, item.h);
    });
  }, [layout.visible, moveWidget, resizeWidget, scope]); // eslint-disable-line

  if (!mounted) return <div ref={containerRef} className="widget-grid-root" />;

  const maxRow     = layout.visible.length > 0 ? Math.max(...layout.visible.map(w => w.row + w.rowSpan)) : 4;
  const overlayRows = maxRow + 2;

  return (
    <div ref={containerRef} className="widget-grid-root" style={{ position: "relative" }}>
      {editMode && containerWidth > 0 && (
        <div className="widget-grid-overlay" style={{
          position: "absolute", top: 0, left: 0, right: 0,
          height: overlayRows * (ROW_HEIGHT + GAP) - GAP,
          display: "grid", gridTemplateColumns: `repeat(${COLS}, 1fr)`, gridAutoRows: ROW_HEIGHT,
          gap: GAP, pointerEvents: "none", zIndex: 0,
        }}>
          {Array.from({ length: overlayRows * COLS }).map((_, i) => <div key={i} className="widget-grid-cell-bg" />)}
        </div>
      )}

      {containerWidth > 0 && (
        <GridLayout
          layout={toRGLLayout(layout.visible, registryMap)}
          width={containerWidth}
          style={{ position: "relative", zIndex: 1 }}
          gridConfig={{ cols: COLS, rowHeight: ROW_HEIGHT, margin: [GAP, GAP], containerPadding: [0, 0] }}
          dragConfig={{ enabled: editMode, handle: ".widget-container__bar", cancel: "button" }}
          resizeConfig={{ enabled: editMode, handles: ["n", "s", "e", "w", "se", "sw"] }}
          onLayoutChange={handleLayoutChange}
        >
          {layout.visible.map(w => {
            const def = registryMap[w.key];
            if (!def) return null;
            const Component = def.component;
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const v = variant as any;
            return (
              <div key={w.key} style={{ display: "flex", flexDirection: "column" }}>
                <WidgetContainer label={def.label} editMode={editMode} onHide={() => hideWidget(userScope, w.key)}>
                  <Component variant={v} isExp={v?.direction === "EXP"} />
                </WidgetContainer>
              </div>
            );
          })}
        </GridLayout>
      )}

      {editMode && (
        <WidgetPalette
          scope={scope}
          registry={registry}
          hidden={layout.hidden}
          onShow={(key) => showWidget(userScope, key)}
          onReset={() => resetLayout(userScope, defaults)}
          onClose={() => useWidgetLayout.getState().setEditMode(false)}
        />
      )}
    </div>
  );
}

// 하위 호환 — 기존 house-bl sea 레지스트리 그대로 재내보내기
export { WIDGET_REGISTRY, DEFAULT_MAP, seaDefaults };
