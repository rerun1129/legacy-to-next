"use client";

import { useEffect, useRef, useState } from "react";
import { GripHorizontal, X, Plus, Trash2 } from "lucide-react";
import { useWidgetLayout }  from "@/lib/use-widget-layout";
import { useCurrentUser }   from "@/lib/use-current-user";
import { useFieldLayout }   from "@/lib/use-field-layout";

export interface FieldItemDef {
  key:    string;
  label?: string;
  render: () => React.ReactNode;
}

interface DragState {
  dragKey:      string;
  dragOverKey:  string | null;  // 아이템 key 또는 "__slot_R_S" (빈 슬롯)
  deltaX:       number;
  deltaY:       number;
  itemRects:    Record<string, { left: number; top: number }>;
}

interface Props {
  itemScope:        string;
  items:            FieldItemDef[];
  cols?:            number;   // 행당 최대 열 수, 기본 2
  showRowControls?: boolean;  // false 이면 행 추가/삭제 버튼 숨김 (Party 패널 등)
}

function inferLabel(key: string) {
  return key.split("-").map(w => w[0]?.toUpperCase() + w.slice(1)).join(" ");
}

// 빈 슬롯 key 형식: "slot-R-S" (대시 구분 — 언더스코어는 파싱 오류 위험)
const slotKey  = (row: number, slot: number) => `slot-${row}-${slot}`;
const isSlotKey = (k: string | null | undefined): k is string => !!k && k.startsWith("slot-");
const parseSlot = (k: string): [number, number] => {
  const [, r, s] = k.split("-");   // ["slot", "R", "S"]
  return [Number(r), Number(s)];
};

export function FieldItemGrid({ itemScope, items, cols = 2, showRowControls = true }: Props) {
  const { editMode }      = useWidgetLayout();
  const { currentUserId } = useCurrentUser();
  const fullScope         = `${currentUserId}.${itemScope}`;
  const defaultOrder      = items.map(i => i.key);

  const {
    getFieldLayout, initFieldLayout, initItemRows,
    swapFields, moveItemToSlot, hideField, showField,
    addItemRow, deleteItemRow, setRowMode, setSplitCol,
  } = useFieldLayout();

  useEffect(() => {
    initFieldLayout(fullScope, defaultOrder);
  }, [fullScope]); // eslint-disable-line

  useEffect(() => {
    initItemRows(fullScope, defaultOrder, cols);
  }, [fullScope]); // eslint-disable-line

  const layout    = getFieldLayout(fullScope, defaultOrder);
  const rowModes  = layout.rowModes  ?? {};
  const splitCols = layout.splitCols ?? {};
  const itemRows  = layout.itemRows  ?? (() => {
    const rows: string[][] = [];
    for (let i = 0; i < defaultOrder.length; i += cols) rows.push(defaultOrder.slice(i, i + cols));
    return rows.filter(r => r.length > 0);
  })();
  const hiddenItems = items.filter(i => layout.hidden.includes(i.key));

  // ── Drag ────────────────────────────────────────────────
  const [dragState, setDragState] = useState<DragState | null>(null);
  const containerRef              = useRef<HTMLDivElement>(null);

  function getItemStyle(key: string): React.CSSProperties {
    if (!dragState || (Math.abs(dragState.deltaX) <= 4 && Math.abs(dragState.deltaY) <= 4)) return {};
    const { dragKey, dragOverKey, deltaX, deltaY, itemRects } = dragState;
    if (key === dragKey) {
      return { transform: `translate(${deltaX}px,${deltaY}px)`, transition: "none", position: "relative", zIndex: 10, opacity: 0.85 };
    }
    // 타겟이 아이템 셀일 때만 슬라이드 (빈 슬롯 타겟은 슬라이드 없음)
    if (key === dragOverKey && !isSlotKey(dragOverKey ?? "")) {
      const dr = itemRects[dragKey], or = itemRects[key];
      if (dr && or) return { transform: `translate(${dr.left - or.left}px,${dr.top - or.top}px)`, transition: "transform 150ms ease" };
    }
    return {};
  }

  function handleDragStart(e: React.MouseEvent, key: string) {
    if (e.button !== 0) return;
    const container = containerRef.current!;
    const cr        = container.getBoundingClientRect();

    // 아이템 셀과 빈 슬롯 모두 수집
    const cells  = Array.from(container.querySelectorAll<HTMLElement>(".field-item-cell:not(.is-empty-slot)"));
    const slots  = Array.from(container.querySelectorAll<HTMLElement>(".is-empty-slot"));

    const itemRects: Record<string, { left: number; top: number }> = {};
    const centers:   Record<string, { x: number; y: number }>       = {};

    cells.forEach(cell => {
      const k = cell.dataset.key;
      if (!k) return;
      const r = cell.getBoundingClientRect();
      itemRects[k] = { left: r.left - cr.left, top: r.top - cr.top };
      centers[k]   = { x: r.left + r.width / 2 - cr.left, y: r.top + r.height / 2 - cr.top };
    });
    slots.forEach(slot => {
      const k = slot.dataset.slotKey;
      if (!k) return;
      const r = slot.getBoundingClientRect();
      centers[k] = { x: r.left + r.width / 2 - cr.left, y: r.top + r.height / 2 - cr.top };
    });

    const startX = e.clientX, startY = e.clientY;
    let latestOver: string | null = null, latestDX = 0, latestDY = 0;

    const onMove = (ev: MouseEvent) => {
      latestDX = ev.clientX - startX; latestDY = ev.clientY - startY;
      const mx = ev.clientX - cr.left, my = ev.clientY - cr.top;
      let closest: string | null = null, minDist = Infinity;
      Object.entries(centers).forEach(([k, c]) => {
        if (k === key) return;
        const d = Math.hypot(mx - c.x, my - c.y);
        if (d < minDist) { minDist = d; closest = k; }
      });
      latestOver = closest;
      setDragState({ dragKey: key, dragOverKey: closest, deltaX: latestDX, deltaY: latestDY, itemRects });
    };

    const onUp = () => {
      if (latestOver && (Math.abs(latestDX) > 4 || Math.abs(latestDY) > 4)) {
        if (isSlotKey(latestOver)) {
          const [rowIdx, slotIdx] = parseSlot(latestOver);
          moveItemToSlot(fullScope, key, rowIdx, slotIdx);
        } else {
          swapFields(fullScope, key, latestOver);
        }
      }
      setDragState(null);
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup",   onUp);
    };

    setDragState({ dragKey: key, dragOverKey: null, deltaX: 0, deltaY: 0, itemRects });
    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup",   onUp);
  }

  // ── Render ──────────────────────────────────────────────
  return (
    <div ref={containerRef}>
      {itemRows.map((keys, rowIdx) => {
        const mode     = rowModes[rowIdx]  ?? "split";
        const splitCol = splitCols[rowIdx] ?? 1;
        const isEmpty  = keys.length === 0;
        const isSingle = mode === "split" && keys.length === 1;
        const slotCount = mode === "full" ? 1 : cols;

        // 각 열 위치에 아이템 key 또는 null(빈 슬롯) 배치
        const slots: (string | null)[] = Array(slotCount).fill(null);
        if (mode === "full") {
          if (keys[0]) slots[0] = keys[0];
        } else if (isSingle) {
          slots[Math.min(splitCol - 1, slotCount - 1)] = keys[0];
        } else {
          keys.slice(0, slotCount).forEach((k, i) => { slots[i] = k; });
        }

        return (
          <div key={keys.length > 0 ? keys.join('|') : `empty-row-${rowIdx}`} style={{ marginBottom: 2 }}>
            {/* 행 컨트롤 바 */}
            {editMode && (
              <div className="field-item-row-bar">
                <div style={{ flex: 1 }} />
                {isSingle && (
                  <button className="field-item-row-btn" onMouseDown={e => e.stopPropagation()}
                    onClick={() => setSplitCol(fullScope, rowIdx, (splitCol % cols) + 1)}>
                    {splitCol === 1 ? "← 좌측" : splitCol === cols ? "→ 우측" : `${splitCol}번째`}
                  </button>
                )}
                <button className="field-item-row-btn field-item-row-btn--mode"
                  onMouseDown={e => e.stopPropagation()}
                  onClick={() => setRowMode(fullScope, rowIdx, mode === "full" ? "split" : "full")}>
                  {mode === "full" ? "전체" : "분할"}
                </button>
                {showRowControls && (
                  <button
                    className={`field-item-row-btn field-item-row-btn--delete${!isEmpty ? " is-disabled" : ""}`}
                    onMouseDown={e => e.stopPropagation()}
                    onClick={() => isEmpty && deleteItemRow(fullScope, rowIdx)}
                    title={isEmpty ? "행 삭제" : "아이템을 모두 이동 후 삭제 가능"}
                    disabled={!isEmpty}
                  >
                    <Trash2 size={9} />
                  </button>
                )}
              </div>
            )}

            {/* 행 그리드 */}
            <div
              className={`field-item-grid${editMode ? " is-edit" : ""}`}
              style={{ gridTemplateColumns: mode === "full" ? "1fr" : `repeat(${cols}, 1fr)` }}
            >
              {slots.map((k, slotIdx) => {
                if (k === null) {
                  const sk = slotKey(rowIdx, slotIdx);
                  return (
                    <div key={sk}
                      className={`field-item-cell field-item-cell-placeholder is-empty-slot${dragState?.dragOverKey === sk ? " is-drag-over" : ""}`}
                      data-slot-key={sk}
                    />
                  );
                }
                const item = items.find(i => i.key === k);
                if (!item) return null;
                return (
                  <div
                    key={k}
                    data-key={k}
                    className={[
                      "field-item-cell",
                      dragState?.dragKey    === k ? "is-dragging"  : "",
                      dragState?.dragOverKey === k ? "is-drag-over" : "",
                    ].filter(Boolean).join(" ")}
                    style={getItemStyle(k)}
                  >
                    {editMode && (
                      <>
                        <div className="field-item-handle" onMouseDown={ev => handleDragStart(ev, k)}>
                          <GripHorizontal size={9} />
                        </div>
                        <button className="field-item-cell-close"
                          onMouseDown={e => e.stopPropagation()}
                          onClick={() => hideField(fullScope, k)} title="숨기기">
                          <X size={8} />
                        </button>
                      </>
                    )}
                    {item.render()}
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}

      {/* 행 추가 버튼 */}
      {editMode && showRowControls && (
        <button className="field-item-add-row" onClick={() => addItemRow(fullScope)}>
          <Plus size={10} /> 행 추가
        </button>
      )}

      {/* 숨긴 항목 복구 */}
      {editMode && hiddenItems.length > 0 && (
        <div className="field-item-hidden-bar">
          {hiddenItems.map(item => (
            <button key={item.key} className="field-widget-hidden-pill"
              onClick={() => showField(fullScope, item.key)}>
              + {item.label ?? inferLabel(item.key)}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
