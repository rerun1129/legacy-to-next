"use client";

// grid 액션 7종 팩토리 — use-field-layout.ts 단일 store를 유지한 채 파일만 분리.
// type-only 순환 import는 런타임 사이클을 만들지 않음.
import type { StoreApi } from "zustand";
import type { FieldLayoutStore } from "./use-field-layout";
import { nextRowId, buildRowsWithFull } from "./field-layout-helpers";

type SetState = StoreApi<FieldLayoutStore>["setState"];
type GetState = StoreApi<FieldLayoutStore>["getState"];

type GridActions = Pick<
  FieldLayoutStore,
  | "initItemRows"
  | "swapFields"
  | "moveItemToSlot"
  | "addItemRow"
  | "deleteItemRow"
  | "setRowMode"
  | "setSplitCol"
>;

export function createGridActions(set: SetState, get: GetState): GridActions {
  return {
    // FieldItemGrid 전용: cols 개씩 split 행으로 초기화 (fullKeys는 단독 행 + rowMode "full"로 시드)
    initItemRows(scope, items, cols = 2, fullKeys = []) {
      const layout = get().layouts[scope];
      if (layout?.itemRows) {
        // layout.cols가 명시된 경우에만 cols 변경을 감지해 재배치
        // layout.cols === undefined 인 레거시 데이터는 재배치 대상에서 제외 (기존 커스터마이즈 보존)
        if (layout.cols !== undefined && layout.cols !== cols) {
          const flat = layout.itemRows.flat();
          const itemsSet = new Set(items);
          // 기존 flat 순서(사용자 정렬 결과)를 보존하되, items에 없는 stale 키는 드롭
          const preserved = flat.filter(k => itemsSet.has(k));
          // items에만 있는 신규 키는 뒤에 추가
          const missing = items.filter(k => !preserved.includes(k));
          const ordered = [...preserved, ...missing];
          const { rows, rowModes } = buildRowsWithFull(ordered, cols, new Set(fullKeys));
          const rowIds = rows.map(() => nextRowId());
          set(s => ({
            layouts: {
              ...s.layouts,
              [scope]: {
                ...s.layouts[scope]!,
                itemRows: rows,
                rowIds,
                cols,
                // 행 인덱스 의미가 무너지므로 행 단위 모드/분할 설정 리셋 (fullKeys 시드 적용)
                rowModes,
                splitCols: {},
              },
            },
          }));
          return;
        }
        // rowIds 중복 감지 시 자동 재생성
        if (layout.rowIds) {
          const seen = new Set<string>();
          const hasDup = layout.rowIds.some(id => seen.size === seen.add(id).size);
          if (hasDup) {
            const fresh = layout.itemRows.map(() => nextRowId());
            set(s => ({ layouts: { ...s.layouts, [scope]: { ...s.layouts[scope]!, rowIds: fresh } } }));
          }
        }
        return;
      }
      const { rows, rowModes } = buildRowsWithFull(items, cols, new Set(fullKeys));
      const rowIds = rows.map(() => nextRowId());
      set(s => {
        const base = s.layouts[scope] ?? { order: items, hidden: [] };
        return { layouts: { ...s.layouts, [scope]: { ...base, itemRows: rows, rowIds, cols, rowModes } } };
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
  };
}
