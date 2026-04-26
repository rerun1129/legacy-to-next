"use client";

import { useCallback, useRef, useState, useEffect } from "react";
import GridLayout, { type Layout } from "react-grid-layout";
import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";
import type { BLVariantConfig } from "@/lib/bl-variants";
import type { WidgetKey } from "@/lib/use-widget-layout";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { useCurrentUser }  from "@/lib/use-current-user";
import { WidgetContainer }  from "./widget-container";
import { WidgetPalette }    from "./widget-palette";
import { REGISTRY_MAP, getDefaultPositions } from "./widget-registry";

const COLS       = 6;
const ROW_HEIGHT = 150;
const GAP        = 8;

interface Props {
  scope:   string;
  variant: BLVariantConfig;
}

function toRGLLayout(visible: ReturnType<typeof getDefaultPositions>) {
  return visible.map(w => ({
    i:    w.key,
    x:    w.col,
    y:    w.row,
    w:    w.colSpan,
    h:    w.rowSpan,
    minW: REGISTRY_MAP[w.key].minColSpan,
    minH: REGISTRY_MAP[w.key].minRowSpan,
  }));
}

export function WidgetGrid({ scope, variant }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [containerWidth, setContainerWidth] = useState(0);
  const [mounted, setMounted] = useState(false);

  // 서버·클라이언트 초기 렌더를 빈 div로 일치시킨 뒤 mount 후 실제 렌더
  // (zustand persist localStorage 복원 + containerWidth 측정이 모두 mount 이후에 발생)
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

  const {
    editMode, getLayout,
    moveWidget, resizeWidget,
    hideWidget, showWidget, resetLayout,
  } = useWidgetLayout();

  const defaults = getDefaultPositions();
  const layout   = getLayout(userScope, defaults);

  const handleLayoutChange = useCallback((next: Layout) => {
    next.forEach(item => {
      const key  = item.i as WidgetKey;
      const prev = layout.visible.find(w => w.key === key);
      if (!prev) return;
      if (prev.col !== item.x || prev.row !== item.y) moveWidget(userScope, key, item.x, item.y);
      if (prev.colSpan !== item.w || prev.rowSpan !== item.h) resizeWidget(userScope, key, item.w, item.h);
    });
  }, [layout.visible, moveWidget, resizeWidget, scope]);

  if (!mounted) return <div ref={containerRef} className="widget-grid-root" />;

  // 편집 모드 격자 오버레이: visible 위젯의 최대 행 + 2 여유 행
  const maxRow = layout.visible.length > 0
    ? Math.max(...layout.visible.map(w => w.row + w.rowSpan))
    : 4;
  const overlayRows = maxRow + 2;

  return (
    <div ref={containerRef} className="widget-grid-root" style={{ position: "relative" }}>
      {editMode && containerWidth > 0 && (
        <div
          className="widget-grid-overlay"
          style={{
            position:            "absolute",
            top: 0, left: 0, right: 0,
            height:              overlayRows * (ROW_HEIGHT + GAP) - GAP,
            display:             "grid",
            gridTemplateColumns: `repeat(${COLS}, 1fr)`,
            gridAutoRows:        ROW_HEIGHT,
            gap:                 GAP,
            pointerEvents:       "none",
            zIndex:              0,
          }}
        >
          {Array.from({ length: overlayRows * COLS }).map((_, i) => (
            <div key={i} className="widget-grid-cell-bg" />
          ))}
        </div>
      )}

      {containerWidth > 0 && (
        <GridLayout
          layout={toRGLLayout(layout.visible)}
          width={containerWidth}
          style={{ position: "relative", zIndex: 1 }}
          gridConfig={{ cols: COLS, rowHeight: ROW_HEIGHT, margin: [GAP, GAP], containerPadding: [0, 0] }}
          dragConfig={{ enabled: editMode, handle: ".widget-container__bar", cancel: "button" }}
          resizeConfig={{ enabled: editMode, handles: ["n", "s", "e", "w", "se", "sw"] }}
          onLayoutChange={handleLayoutChange}
        >
          {layout.visible.map(w => {
            const def = REGISTRY_MAP[w.key];
            if (!def) return null;
            const Component = def.component;
            return (
              <div key={w.key} style={{ display: "flex", flexDirection: "column" }}>
                <WidgetContainer
                  label={def.label}
                  editMode={editMode}
                  onHide={() => hideWidget(userScope, w.key)}
                >
                  <Component variant={variant} isExp={variant.direction === "EXP"} />
                </WidgetContainer>
              </div>
            );
          })}
        </GridLayout>
      )}

      {editMode && (
        <WidgetPalette
          scope={scope}
          hidden={layout.hidden}
          onShow={(key) => showWidget(userScope, key)}
          onReset={() => resetLayout(userScope, defaults)}
          onClose={() => useWidgetLayout.getState().setEditMode(false)}
        />
      )}
    </div>
  );
}
