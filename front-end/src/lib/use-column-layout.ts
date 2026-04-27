"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import { arrayMove } from "@dnd-kit/sortable";
import type { GridColumn } from "@/components/shared/grid-list";

type ColumnPrefs = {
  order: string[];
  widths: Record<string, number>;
  hidden: string[];
};

type ColumnLayoutStore = {
  layouts: Record<string, ColumnPrefs>;
  resize(gridId: string, key: string, width: number): void;
  setOrder(gridId: string, order: string[]): void;
  hide(gridId: string, key: string): void;
  show(gridId: string, key: string): void;
  reset(gridId: string): void;
};

const useColumnLayoutStore = create<ColumnLayoutStore>()(
  persist(
    (set) => ({
      layouts: {},

      resize(gridId, key, width) {
        set((s) => {
          const prefs = s.layouts[gridId] ?? { order: [], widths: {}, hidden: [] };
          return {
            layouts: {
              ...s.layouts,
              [gridId]: { ...prefs, widths: { ...prefs.widths, [key]: width } },
            },
          };
        });
      },

      setOrder(gridId, order) {
        set((s) => {
          const prefs = s.layouts[gridId] ?? { order: [], widths: {}, hidden: [] };
          return {
            layouts: { ...s.layouts, [gridId]: { ...prefs, order } },
          };
        });
      },

      hide(gridId, key) {
        set((s) => {
          const prefs = s.layouts[gridId] ?? { order: [], widths: {}, hidden: [] };
          if (prefs.hidden.includes(key)) return s;
          return {
            layouts: {
              ...s.layouts,
              [gridId]: { ...prefs, hidden: [...prefs.hidden, key] },
            },
          };
        });
      },

      show(gridId, key) {
        set((s) => {
          const prefs = s.layouts[gridId] ?? { order: [], widths: {}, hidden: [] };
          return {
            layouts: {
              ...s.layouts,
              [gridId]: { ...prefs, hidden: prefs.hidden.filter((k) => k !== key) },
            },
          };
        });
      },

      reset(gridId) {
        set((s) => {
          const { [gridId]: _removed, ...rest } = s.layouts;
          return { layouts: rest };
        });
      },
    }),
    { name: "fms.columnPrefs.v1" }
  )
);

/**
 * 저장된 컬럼 순서/너비/숨김 설정을 defaultColumns에 reconcile하여 반환.
 * - 저장에 없는 새 key는 order 끝에 추가
 * - defaultColumns에 없는 key는 order/hidden에서 제거
 * - required:true 컬럼이 hidden에 있으면 자동 복구
 */
export function useColumnLayout<T>(
  gridId: string,
  defaultColumns: GridColumn<T>[]
): {
  visibleColumns: GridColumn<T>[];
  hiddenColumns: GridColumn<T>[];
  resizeColumn: (key: string, width: number) => void;
  reorderColumn: (activeKey: string, overKey: string) => void;
  hideColumn: (key: string) => void;
  showColumn: (key: string) => void;
  resetLayout: () => void;
} {
  const store = useColumnLayoutStore();
  const raw = store.layouts[gridId];

  const defaultKeys = defaultColumns.map((c) => String(c.key));

  // reconcile: 저장된 order를 현재 defaultColumns 기준으로 정합성 유지
  const savedOrder: string[] = raw?.order ?? [];
  const savedHidden: string[] = raw?.hidden ?? [];
  const savedWidths: Record<string, number> = raw?.widths ?? {};

  // required 컬럼이 hidden에 있으면 제거 (자동 복구)
  const requiredKeys = defaultColumns
    .filter((c) => c.isRequired)
    .map((c) => String(c.key));
  const effectiveHidden = savedHidden.filter(
    (k) => !requiredKeys.includes(k) && defaultKeys.includes(k)
  );

  // defaultColumns에 있지만 savedOrder에 없는 key → order 끝에 append
  const reconciledOrder = [
    ...savedOrder.filter((k) => defaultKeys.includes(k)),
    ...defaultKeys.filter((k) => !savedOrder.includes(k)),
  ];

  const colMap = new Map(defaultColumns.map((c) => [String(c.key), c]));

  const visibleColumns = reconciledOrder
    .filter((k) => !effectiveHidden.includes(k))
    .map((k) => {
      const col = colMap.get(k);
      if (!col) return null;
      const savedWidth = savedWidths[k];
      if (savedWidth !== undefined) {
        return { ...col, width: savedWidth, minWidth: savedWidth };
      }
      return col;
    })
    .filter((c): c is GridColumn<T> => c !== null);

  // required:true 컬럼은 hiddenColumns에서 제외
  const hiddenColumns = effectiveHidden
    .map((k) => colMap.get(k))
    .filter((c): c is GridColumn<T> => c !== undefined && !c.isRequired);

  return {
    visibleColumns,
    hiddenColumns,
    resizeColumn: (key, width) => store.resize(gridId, key, width),
    reorderColumn: (activeKey, overKey) => {
      const from = reconciledOrder.indexOf(activeKey);
      const to = reconciledOrder.indexOf(overKey);
      if (from === -1 || to === -1) return;
      store.setOrder(gridId, arrayMove(reconciledOrder, from, to));
    },
    hideColumn: (key) => {
      const col = colMap.get(key);
      // required 컬럼은 숨김 불가
      if (col?.isRequired) return;
      store.hide(gridId, key);
    },
    showColumn: (key) => store.show(gridId, key),
    resetLayout: () => store.reset(gridId),
  };
}
