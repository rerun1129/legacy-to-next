"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";

export type WidgetKey =
  | "party" | "schedule" | "trade"
  | "container" | "marks" | "description" | "item-hs";

export interface WidgetPosition {
  key: WidgetKey;
  col: number;     // 0..5
  row: number;
  colSpan: number; // 1..6
  rowSpan: number;
}

export interface PageLayout {
  visible: WidgetPosition[];
  hidden:  WidgetKey[];
}

interface LayoutStore {
  layouts:    Record<string, PageLayout>;
  editMode:   boolean;
  setEditMode:   (on: boolean) => void;
  getLayout:     (scope: string, defaults: WidgetPosition[]) => PageLayout;
  moveWidget:    (scope: string, key: WidgetKey, col: number, row: number) => void;
  resizeWidget:  (scope: string, key: WidgetKey, colSpan: number, rowSpan: number) => void;
  hideWidget:    (scope: string, key: WidgetKey) => void;
  showWidget:    (scope: string, key: WidgetKey, col?: number, row?: number) => void;
  resetLayout:   (scope: string, defaults: WidgetPosition[]) => void;
}

export const useWidgetLayout = create<LayoutStore>()(
  persist(
    (set, get) => ({
      layouts:  {},
      editMode: false,

      setEditMode(on) {
        set({ editMode: on });
      },

      getLayout(scope, defaults) {
        // 렌더 중 set() 호출 금지 — 저장된 레이아웃이 없으면 defaults를 폴백으로 반환만 함
        return get().layouts[scope] ?? { visible: defaults, hidden: [] };
      },

      moveWidget(scope, key, col, row) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout) return s;
          return {
            layouts: {
              ...s.layouts,
              [scope]: {
                ...layout,
                visible: layout.visible.map(w =>
                  w.key === key ? { ...w, col, row } : w
                ),
              },
            },
          };
        });
      },

      resizeWidget(scope, key, colSpan, rowSpan) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout) return s;
          return {
            layouts: {
              ...s.layouts,
              [scope]: {
                ...layout,
                visible: layout.visible.map(w =>
                  w.key === key ? { ...w, colSpan, rowSpan } : w
                ),
              },
            },
          };
        });
      },

      hideWidget(scope, key) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout) return s;
          return {
            layouts: {
              ...s.layouts,
              [scope]: {
                visible: layout.visible.filter(w => w.key !== key),
                hidden:  [...layout.hidden, key],
              },
            },
          };
        });
      },

      showWidget(scope, key, col = 0, row = 0) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout) return s;
          return {
            layouts: {
              ...s.layouts,
              [scope]: {
                visible: [...layout.visible, { key, col, row, colSpan: 3, rowSpan: 2 }],
                hidden:  layout.hidden.filter(k => k !== key),
              },
            },
          };
        });
      },

      resetLayout(scope, defaults) {
        set(s => ({
          layouts: {
            ...s.layouts,
            [scope]: { visible: defaults, hidden: [] },
          },
        }));
      },
    }),
    { name: "fms.widgetLayouts.v1" }
  )
);
