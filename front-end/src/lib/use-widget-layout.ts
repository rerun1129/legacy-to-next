"use client";

import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { backendLayoutStorage, setLayoutPersistPaused } from "./backend-layout-storage";

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
  snapshots:  Record<string, PageLayout>;
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
  beginEdit:     (scope: string) => void;
  commitEdit:    (scope: string) => void;
  revertEdit:    (scope: string) => void;
}

export const useWidgetLayout = create<LayoutStore>()(
  persist(
    (set, get) => ({
      layouts:   {},
      snapshots: {},
      editMode:  false,
      canEdit:   true,

      setEditMode(on) { setLayoutPersistPaused(on); set({ editMode: on }); },
      setCanEdit(on)  { if (!on) setLayoutPersistPaused(false); set({ canEdit: on, ...(!on && { editMode: false }) }); },

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

      // React Strict Mode 재마운트 보호: 스냅샷이 이미 있으면 no-op
      beginEdit(scope) {
        const { layouts, snapshots } = get();
        if (snapshots[scope]) return;
        const current = layouts[scope];
        if (!current) return;
        set(s => ({
          snapshots: { ...s.snapshots, [scope]: structuredClone(current) },
        }));
      },

      // 변경 사항 확정 — 스냅샷만 삭제, 레이아웃은 그대로 유지
      commitEdit(scope) {
        set(s => {
          const next = { ...s.snapshots };
          delete next[scope];
          return { snapshots: next };
        });
      },

      // 편집 취소 — 스냅샷이 있으면 레이아웃을 되돌리고 스냅샷 삭제. 없으면 no-op
      revertEdit(scope) {
        const { snapshots } = get();
        const snap = snapshots[scope];
        if (!snap) return;
        set(s => {
          const nextSnapshots = { ...s.snapshots };
          delete nextSnapshots[scope];
          return {
            layouts:   { ...s.layouts, [scope]: snap },
            snapshots: nextSnapshots,
          };
        });
      },
    }),
    {
      name: "fms.widgetLayouts.v1",
      storage: createJSONStorage(() => backendLayoutStorage),
      // snapshots는 편집 중 임시 데이터이므로 영속 대상 제외
      // editMode/canEdit는 UI 세션 플래그이므로 서버 영속화 대상에서 제외
      partialize: (state) => ({ layouts: state.layouts }),
    }
  )
);
