"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";

export type WidgetKey = string; // 엔트리 타입별로 다른 위젯 키 사용 가능

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
  canEdit:    boolean;
  setEditMode:   (on: boolean) => void;
  setCanEdit:    (on: boolean) => void;
  initLayout:    (scope: string, defaults: WidgetPosition[]) => void;
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
      canEdit:  true,

      setEditMode(on) { set({ editMode: on }); },
      setCanEdit(on)  { set({ canEdit: on, ...(!on && { editMode: false }) }); },

      // useEffect 에서만 호출 — 레이아웃이 없을 때 초기값을 스토어에 저장
      initLayout(scope, defaults) {
        if (get().layouts[scope]) return;
        set(s => ({
          layouts: { ...s.layouts, [scope]: { visible: defaults, hidden: [] } },
        }));
      },

      getLayout(scope, defaults) {
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
