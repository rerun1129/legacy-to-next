"use client";

// NOTE: 단일 store 유지·파일만 분리 — 액션 상호호출 없음(전부 set/get/순수헬퍼만 사용).
// getFieldLayout의 정합 보정 로직은 이 파일에 그대로 유지.

import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { backendLayoutStorage } from "./backend-layout-storage";
import { createGridActions } from "./field-layout-grid-actions";

export interface FieldLayout {
  order:      string[];          // 섹션 순서 (FieldWidgetList 전용)
  hidden:     string[];
  rowModes?:  Record<number, "full" | "split">;
  splitCols?: Record<number, number>;
  itemRows?:  string[][];        // FieldItemGrid 전용: 명시적 행 배치
  rowIds?:    string[];          // FieldItemGrid 전용: 행마다 stable React key
  cols?:      number;            // FieldItemGrid 전용: 행당 최대 열 수 (기본 2)
}

export interface FieldLayoutStore {
  layouts:          Record<string, FieldLayout>;
  snapshot:         Record<string, FieldLayout> | null;
  getFieldLayout:   (scope: string, defaults: string[]) => FieldLayout;
  initFieldLayout:  (scope: string, defaults: string[]) => void;
  initItemRows:     (scope: string, items: string[], cols?: number, fullKeys?: string[]) => void;
  reorderFields:    (scope: string, fromIdx: number, toIdx: number) => void;
  swapFields:       (scope: string, key1: string, key2: string) => void;
  moveItemToSlot:   (scope: string, key: string, rowIdx: number, slotIdx: number) => void;
  addItemRow:       (scope: string) => void;
  deleteItemRow:    (scope: string, rowIdx: number) => void;
  hideField:        (scope: string, key: string) => void;
  showField:        (scope: string, key: string) => void;
  resetFieldLayout: (scope: string, defaults: string[]) => void;
  setRowMode:       (scope: string, rowIdx: number, mode: "full" | "split") => void;
  setSplitCol:      (scope: string, rowIdx: number, col: number) => void;
  beginEdit:        () => void;
  commitEdit:       () => void;
  revertEdit:       () => void;
  resetAll:         () => void;
  resetByPrefix:    (prefix: string) => void;
}

export const useFieldLayout = create<FieldLayoutStore>()(
  persist(
    (set, get) => ({
      layouts: {},
      snapshot: null,

      // === Read ===
      getFieldLayout(scope, defaults) {
        const layout = get().layouts[scope];
        if (!layout) return { order: defaults, hidden: [] };
        // 보정: defaults에 있지만 visible(itemRows or order)에도 hidden에도 없는 key를 hidden에 자동 포함
        const visibleSet = new Set(layout.itemRows ? layout.itemRows.flat() : layout.order);
        const hiddenSet  = new Set(layout.hidden);
        const missing    = defaults.filter(k => !visibleSet.has(k) && !hiddenSet.has(k));
        const patched    = missing.length > 0
          ? { ...layout, hidden: [...layout.hidden, ...missing] }
          : layout;
        // itemRows가 있으면 flat으로 order 동기화 — 단 defaults 모두 포함 시만 (stale 방지)
        let result = patched;
        if (patched.itemRows) {
          const flat    = patched.itemRows.flat();
          const flatSet = new Set(flat);
          const valid   = defaults.every(k => flatSet.has(k) || hiddenSet.has(k));
          if (valid) result = { ...patched, order: flat };
          // stale itemRows 감지: 무시하고 order 사용
        }
        // rowIds 중복 감지 시 제거 → field-item-grid의 r${rowIdx} fallback 사용 (initItemRows에서 복구)
        if (result.rowIds) {
          const seen = new Set<string>();
          if (result.rowIds.some(id => seen.size === seen.add(id).size)) {
            const { rowIds: _dup, ...noRowIds } = result;
            return noRowIds;
          }
        }
        return result;
      },

      // === Init ===
      initFieldLayout(scope, defaults) {
        if (get().layouts[scope]) return;
        set(s => ({ layouts: { ...s.layouts, [scope]: { order: defaults, hidden: [] } } }));
      },

      // === Reorder ===
      reorderFields(scope, fromIdx, toIdx) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout) return s;
          const next = [...layout.order];
          const [moved] = next.splice(fromIdx, 1);
          next.splice(Math.max(0, Math.min(toIdx, next.length)), 0, moved);
          return { layouts: { ...s.layouts, [scope]: { ...layout, order: next } } };
        });
      },

      // === Visibility ===
      hideField(scope, key) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout) return s;
          const newHidden = [...layout.hidden, key];
          const newOrder  = layout.order.filter(k => k !== key);
          const newRows   = layout.itemRows
            ? layout.itemRows.map(r => r.filter(k => k !== key))
            : undefined;
          return {
            layouts: {
              ...s.layouts,
              [scope]: { ...layout, order: newOrder, hidden: newHidden, ...(newRows ? { itemRows: newRows } : {}) },
            },
          };
        });
      },

      showField(scope, key) {
        set(s => {
          const layout   = s.layouts[scope];
          if (!layout) return s;
          const newHidden = layout.hidden.filter(k => k !== key);
          const newOrder  = [...layout.order, key];
          let newRows     = layout.itemRows;
          if (newRows) {
            const last = newRows[newRows.length - 1];
            const lastMode = (layout.rowModes ?? {})[newRows.length - 1] ?? "split";
            if (last && last.length < (lastMode === "split" ? (layout.cols ?? 2) : 1)) {
              newRows = [...newRows.slice(0, -1), [...last, key]];
            } else {
              newRows = [...newRows, [key]];
            }
          }
          return {
            layouts: {
              ...s.layouts,
              [scope]: { ...layout, order: newOrder, hidden: newHidden, ...(newRows ? { itemRows: newRows } : {}) },
            },
          };
        });
      },

      // === Reset ===
      resetFieldLayout(scope, defaults) {
        set(s => ({
          layouts: { ...s.layouts, [scope]: { order: defaults, hidden: [] } },
        }));
      },

      // === Edit lifecycle ===
      // StrictMode/다중 그리드 보호: 스냅샷이 이미 있으면 no-op
      beginEdit() { if (get().snapshot) return; set({ snapshot: structuredClone(get().layouts) }); },
      // 저장 확정: 스냅샷만 비움(변경 유지). set()이 persist를 트리거함.
      commitEdit() { set({ snapshot: null }); },
      // 취소: 스냅샷이 있으면 layouts 복원 후 비움
      revertEdit() { const snap = get().snapshot; if (!snap) return; set({ layouts: snap, snapshot: null }); },
      // 롤백: 전체 필드 레이아웃 초기화. 스냅샷은 보존(닫기 시 pre-edit로 복원되게).
      resetAll() { set({ layouts: {} }); },
      // 현재 화면(scope::) 필드 레이아웃만 초기화 — prefix 이하 키만 제거.
      resetByPrefix(prefix) {
        set(s => ({
          layouts: Object.fromEntries(
            Object.entries(s.layouts).filter(([k]) => !k.startsWith(prefix))
          ),
        }));
      },

      // === Grid actions (파일 분리, 단일 store 유지) ===
      ...createGridActions(set, get),
    }),
    {
      name: "fms.fieldLayouts.v2",
      storage: createJSONStorage(() => backendLayoutStorage),
      // snapshot은 편집 중 임시 데이터이므로 영속 대상 제외
      partialize: (s) => ({ layouts: s.layouts }),
    }
  )
);
