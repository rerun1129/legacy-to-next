"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface FieldLayout {
  order:      string[];          // 섹션 순서 (FieldWidgetList 전용)
  hidden:     string[];
  rowModes?:  Record<number, "full" | "split">;
  splitCols?: Record<number, number>;
  itemRows?:  string[][];        // FieldItemGrid 전용: 명시적 행 배치
  rowIds?:    string[];          // FieldItemGrid 전용: 행마다 stable React key
  cols?:      number;            // FieldItemGrid 전용: 행당 최대 열 수 (기본 2)
}

// 모듈-레벨 단조 증가 카운터 — stable React key 생성용 (Math.random/crypto 미사용)
let _rowSeq = 0;
function nextRowId() { return `r${++_rowSeq}`; }

interface FieldLayoutStore {
  layouts:          Record<string, FieldLayout>;
  getFieldLayout:   (scope: string, defaults: string[]) => FieldLayout;
  initFieldLayout:  (scope: string, defaults: string[]) => void;
  initItemRows:     (scope: string, items: string[], cols?: number) => void;
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
}

export const useFieldLayout = create<FieldLayoutStore>()(
  persist(
    (set, get) => ({
      layouts: {},

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
        // itemRows 가 있으면 order 를 flat 으로 동기화해서 반환
        if (patched.itemRows) {
          return { ...patched, order: patched.itemRows.flat() };
        }
        return patched;
      },

      initFieldLayout(scope, defaults) {
        if (get().layouts[scope]) return;
        set(s => ({ layouts: { ...s.layouts, [scope]: { order: defaults, hidden: [] } } }));
      },

      // FieldItemGrid 전용: cols 개씩 split 행으로 초기화
      initItemRows(scope, items, cols = 2) {
        const layout = get().layouts[scope];
        if (layout?.itemRows) return;
        const rows: string[][] = [];
        for (let i = 0; i < items.length; i += cols) rows.push(items.slice(i, i + cols));
        const rowIds = rows.map(() => nextRowId());
        set(s => {
          const base = s.layouts[scope] ?? { order: items, hidden: [] };
          return { layouts: { ...s.layouts, [scope]: { ...base, itemRows: rows, rowIds, cols } } };
        });
      },

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

      swapFields(scope, key1, key2) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout) return s;
          if (layout.itemRows) {
            const rows = layout.itemRows.map(r => [...r]);
            let p1: [number, number] | null = null, p2: [number, number] | null = null;
            rows.forEach((row, ri) => row.forEach((k, ci) => {
              if (k === key1) p1 = [ri, ci];
              if (k === key2) p2 = [ri, ci];
            }));
            if (p1 && p2) { rows[p1[0]][p1[1]] = key2; rows[p2[0]][p2[1]] = key1; }
            return { layouts: { ...s.layouts, [scope]: { ...layout, itemRows: rows } } };
          }
          const order = [...layout.order];
          const i1 = order.indexOf(key1), i2 = order.indexOf(key2);
          if (i1 === -1 || i2 === -1) return s;
          [order[i1], order[i2]] = [order[i2], order[i1]];
          return { layouts: { ...s.layouts, [scope]: { ...layout, order } } };
        });
      },

      // 아이템을 특정 행/슬롯으로 이동 (빈 슬롯 대상)
      moveItemToSlot(scope, key, rowIdx, slotIdx) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout?.itemRows) return s;
          const rows = layout.itemRows.map(r => r.filter(k => k !== key));
          const target = [...(rows[rowIdx] ?? [])];
          target.splice(slotIdx, 0, key);
          rows[rowIdx] = target;
          return { layouts: { ...s.layouts, [scope]: { ...layout, itemRows: rows } } };
        });
      },

      addItemRow(scope) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout?.itemRows) return s;
          return {
            layouts: {
              ...s.layouts,
              [scope]: {
                ...layout,
                itemRows: [...layout.itemRows, []],
                rowIds:   [...(layout.rowIds ?? layout.itemRows.map(() => nextRowId())), nextRowId()],
              },
            },
          };
        });
      },

      deleteItemRow(scope, rowIdx) {
        set(s => {
          const layout = s.layouts[scope];
          if (!layout?.itemRows) return s;
          const rows = layout.itemRows.filter((_, i) => i !== rowIdx);

          // rowModes / splitCols 에서 해당 행 제거 후 idx > rowIdx 를 -1 시프트
          const shiftModes: Record<number, "full" | "split"> = {};
          Object.entries(layout.rowModes ?? {}).forEach(([k, v]) => {
            const idx = Number(k);
            if (idx === rowIdx) return;
            shiftModes[idx > rowIdx ? idx - 1 : idx] = v as "full" | "split";
          });
          const shiftCols: Record<number, number> = {};
          Object.entries(layout.splitCols ?? {}).forEach(([k, v]) => {
            const idx = Number(k);
            if (idx === rowIdx) return;
            shiftCols[idx > rowIdx ? idx - 1 : idx] = v as 1 | 2;
          });
          const rowIds = (layout.rowIds ?? layout.itemRows.map(() => nextRowId()))
            .filter((_, i) => i !== rowIdx);
          return {
            layouts: {
              ...s.layouts,
              [scope]: { ...layout, itemRows: rows, rowIds, rowModes: shiftModes, splitCols: shiftCols },
            },
          };
        });
      },

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

      resetFieldLayout(scope, defaults) {
        set(s => ({
          layouts: { ...s.layouts, [scope]: { order: defaults, hidden: [] } },
        }));
      },

      setRowMode(scope, rowIdx, mode) {
        set(s => {
          const layout = s.layouts[scope];
          const base   = layout ?? { order: [], hidden: [] };

          let newItemRows  = base.itemRows;
          let newRowModes  = { ...(base.rowModes  ?? {}) };
          let newSplitCols = { ...(base.splitCols ?? {}) };

          // split → full 전환 시, 2개 아이템이 있으면 두 번째를 아래로 분리
          if (mode === "full" && base.itemRows) {
            const cur = base.itemRows[rowIdx];
            if (cur && cur.length >= 2) {
              const rows  = base.itemRows.map(r => [...r]);
              const extra = rows[rowIdx].splice(1);       // 두 번째 이후 추출 (rows[rowIdx] 는 첫 번째만 남음)
              const nextRow = rows[rowIdx + 1];
              const nextEmpty = nextRow !== undefined && nextRow.length === 0;

              if (nextEmpty) {
                // 아래 행이 비어있으면 거기로 이동 (행 추가 없음, 인덱스 시프트 없음)
                rows[rowIdx + 1] = extra;
                newItemRows = rows;
                newRowModes[rowIdx] = mode;
              } else {
                // 아래 행이 없거나 차있으면 새 행 삽입 + 인덱스 시프트
                rows.splice(rowIdx + 1, 0, extra);
                newItemRows = rows;

                const sm: Record<number, "full" | "split"> = {};
                Object.entries(newRowModes).forEach(([k, v]) => {
                  const i = Number(k);
                  sm[i > rowIdx ? i + 1 : i] = v as "full" | "split";
                });
                sm[rowIdx] = mode;
                newRowModes = sm;

                const sc: Record<number, 1 | 2> = {};
                Object.entries(newSplitCols).forEach(([k, v]) => {
                  const i = Number(k);
                  sc[i > rowIdx ? i + 1 : i] = v as 1 | 2;
                });
                newSplitCols = sc;
              }
            } else {
              newRowModes[rowIdx] = mode;
            }
          } else {
            newRowModes[rowIdx] = mode;
          }

          return {
            layouts: {
              ...s.layouts,
              [scope]: {
                ...base,
                rowModes:  newRowModes,
                splitCols: newSplitCols,
                ...(newItemRows ? { itemRows: newItemRows } : {}),
              },
            },
          };
        });
      },

      setSplitCol(scope, rowIdx, col: number) {
        set(s => {
          const layout    = s.layouts[scope];
          const base      = layout ?? { order: [], hidden: [] };
          const splitCols = { ...(base.splitCols ?? {}), [rowIdx]: col };
          return { layouts: { ...s.layouts, [scope]: { ...base, splitCols } } };
        });
      },
    }),
    { name: "fms.fieldLayouts.v1" }
  )
);
