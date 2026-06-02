"use client";

import { useCallback, useRef, useState, useEffect, useLayoutEffect, useMemo } from "react";
import GridLayout, { type Layout } from "react-grid-layout";
import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";
import { useWidgetLayout } from "@/lib/use-widget-layout";
import { useFieldLayout } from "@/lib/use-field-layout";
import { setLayoutPersistPaused } from "@/lib/backend-layout-storage";
import { EntryScopeProvider } from "@/lib/entry-scope-context";
import { WidgetContainer } from "./widget-container";
import { WidgetPalette }   from "./widget-palette";
import type { WidgetDef, AnyVariantConfig } from "./widget-registry";
import { WIDGET_REGISTRY, REGISTRY_MAP as DEFAULT_MAP, getDefaultPositions as getSeaDefaults } from "./widget-registry";

const COLS       = 6;
const ROW_HEIGHT = 150;
const GAP        = 8;

interface Props {
  scope:    string;
  variant?: AnyVariantConfig;
  registry: WidgetDef[];       // 엔트리 타입별 레지스트리
  active?:  boolean;           // false이면 WidgetPalette 포탈 렌더 억제
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

export function WidgetGrid({ scope, variant, registry, active = true }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [containerWidth, setContainerWidth] = useState(0);

  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    setContainerWidth(el.clientWidth);
    const ro = new ResizeObserver(([entry]) => setContainerWidth(entry.contentRect.width));
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  const { editMode, initLayout, getLayout, moveWidget, resizeWidget, hideWidget, showWidget, resetLayout, beginEdit, commitEdit, revertEdit } = useWidgetLayout();

  // react-grid-layout v2의 layout prop은 초기값(uncontrolled)이라 마운트 후 스토어 변경이 자동 반영되지 않음.
  // 외부에서 레이아웃을 재설정(롤백·편집 종료)할 때 key를 바꿔 GridLayout을 remount시킨다.
  const [gridEpoch, setGridEpoch] = useState(0);
  const wasEditRef = useRef(editMode);

  // 편집 모드 종료(true→false) 시에만 epoch bump — 진입 시는 불필요한 remount/깜빡임 방지
  useEffect(() => {
    if (wasEditRef.current && !editMode) setGridEpoch(e => e + 1);
    wasEditRef.current = editMode;
  }, [editMode]);

  const registryMap = buildRegistryMap(registry);
  const defaults    = useMemo(
    () => registry.map(d => ({ key: d.key, ...d.defaultPosition })),
    [registry]
  );
  const rawLayout = getLayout(scope, defaults);

  // registry에 추가된 새 위젯 key가 저장된 레이아웃(visible+hidden 모두)에 없으면 visible에 자동 포함
  const layout = useMemo(() => {
    const known = new Set([...rawLayout.visible.map(w => w.key), ...rawLayout.hidden]);
    const missing = defaults.filter(d => !known.has(d.key));
    if (!missing.length) return rawLayout;
    return { ...rawLayout, visible: [...rawLayout.visible, ...missing] };
  }, [rawLayout, defaults]);

  const initLayoutRef = useRef(initLayout);
  useLayoutEffect(() => { initLayoutRef.current = initLayout; });

  useEffect(() => {
    initLayoutRef.current(scope, defaults);
  }, [scope, defaults]);

  // 편집 모드 진입 시 위젯·필드 스냅샷 저장, 종료 시 revert
  // (저장 버튼은 commitEdit으로 스냅샷을 미리 비워 두 revert를 no-op으로 만듦)
  useEffect(() => {
    if (!editMode || !active) return;
    beginEdit(scope);
    useFieldLayout.getState().beginEdit();
    return () => {
      revertEdit(scope);
      useFieldLayout.getState().revertEdit();
    };
  }, [editMode, active, scope, beginEdit, revertEdit]);

  const handleLayoutChange = useCallback((next: Layout) => {
    next.forEach(item => {
      const key  = item.i;
      const prev = layout.visible.find(w => w.key === key);
      if (!prev) return;
      if (prev.col !== item.x || prev.row !== item.y) moveWidget(scope, key, item.x, item.y);
      if (prev.colSpan !== item.w || prev.rowSpan !== item.h) resizeWidget(scope, key, item.w, item.h);
    });
  }, [layout.visible, moveWidget, resizeWidget, scope]);

  const maxRow     = layout.visible.length > 0 ? Math.max(...layout.visible.map(w => w.row + w.rowSpan)) : 4;
  const overlayRows = maxRow + 2;

  return (
    <EntryScopeProvider scope={scope}>
    <div ref={containerRef} className="widget-grid-root" style={{ position: "relative" }}>
      {editMode && containerWidth > 0 && (
        <div className="widget-grid-overlay" style={{
          position: "absolute", top: 0, left: 0, right: 0,
          height: overlayRows * (ROW_HEIGHT + GAP) - GAP,
          display: "grid", gridTemplateColumns: `repeat(${COLS}, 1fr)`, gridAutoRows: ROW_HEIGHT,
          gap: GAP, pointerEvents: "none", zIndex: 0,
        }}>
          {/* 순수 장식 셀(CSS 배경, state 없음) — 동적 크기이지만 state 유실 위험 없으므로 A5 예외 */}
          {Array.from({ length: overlayRows * COLS }).map((_, i) => <div key={`cell-${i}`} className="widget-grid-cell-bg" />)}
        </div>
      )}

      {containerWidth > 0 && (
        <GridLayout
          key={`${scope}-${gridEpoch}`}
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
            return (
              <div key={w.key} style={{ display: "flex", flexDirection: "column" }}>
                <WidgetContainer label={def.label} editMode={editMode} onHide={() => hideWidget(scope, w.key)}>
                  <Component variant={variant} isExp={variant?.direction === "EXP"} />
                </WidgetContainer>
              </div>
            );
          })}
        </GridLayout>
      )}

      {editMode && active && (
        <WidgetPalette
          scope={scope}
          registry={registry}
          hidden={layout.hidden}
          onShow={(key) => showWidget(scope, key)}
          onReset={() => {
            resetLayout(scope, defaults);             // 위젯 기본값(paused → 미영속)
            useFieldLayout.getState().resetByPrefix(`${scope}::`); // 현재 화면 필드 레이아웃만 초기화(paused → 미영속)
            setGridEpoch(e => e + 1);                 // 위젯 그리드(uncontrolled) remount로 기본값 반영
          }}
          onClose={() => useWidgetLayout.getState().setEditMode(false)}
          onSave={() => {
            commitEdit(scope);                              // 1. 위젯 스냅샷 비움(paused → 미영속)
            setLayoutPersistPaused(false);                  // 2. 영속 재개
            useFieldLayout.getState().commitEdit();         // 3. 필드 스냅샷 비움 + set() → 필드 영속(재개됨)
            useWidgetLayout.getState().setEditMode(false);  // 4. 편집 종료 + set(editMode) → 위젯 영속; cleanup의 두 revert는 스냅샷 비워져 no-op
          }}
        />
      )}
    </div>
    </EntryScopeProvider>
  );
}

// 하위 호환 — 기존 house-bl sea 레지스트리 그대로 재내보내기
export { WIDGET_REGISTRY, DEFAULT_MAP, getSeaDefaults };
