"use client";

import { useEffect, useLayoutEffect, useMemo, useRef, useState } from "react";
import { useWidgetLayout }      from "@/lib/use-widget-layout";
import { useFieldLayout }       from "@/lib/use-field-layout";
import { FieldWidgetContainer } from "./field-widget-container";

export interface FieldWidgetDef {
  key:           string;
  label:         string;
  render:        () => React.ReactNode;
  alwaysVisible?: boolean;
}

interface DragState {
  key:         string;
  origIndex:   number;
  deltaY:      number;
  insertIndex: number;
  itemHeight:  number;
}

interface Props {
  panelScope: string;
  fields:     FieldWidgetDef[];
}

export function FieldWidgetList({ panelScope, fields }: Props) {
  const { editMode }  = useWidgetLayout();
  const defaultOrder  = useMemo(() => fields.map(f => f.key), [fields]);

  const { getFieldLayout, initFieldLayout, reorderFields, hideField, showField, resetFieldLayout } =
    useFieldLayout();

  const initFieldLayoutRef = useRef(initFieldLayout);
  useLayoutEffect(() => { initFieldLayoutRef.current = initFieldLayout; });

  useEffect(() => {
    initFieldLayoutRef.current(panelScope, defaultOrder);
  }, [panelScope, defaultOrder]);

  const layout = getFieldLayout(panelScope, defaultOrder);

  const visibleDefs = layout.order
    .map(k => fields.find(f => f.key === k))
    .filter((f): f is FieldWidgetDef => !!f);

  const hiddenDefs = fields.filter(f => layout.hidden.includes(f.key));

  // ── Drag state ──────────────────────────────────────────────
  const [dragState, setDragState] = useState<DragState | null>(null);
  const containerRef              = useRef<HTMLDivElement>(null);

  // tab DnD 와 동일한 알고리즘 — 수직(Y) 방향 적용
  // i < origIndex && remainingIndex >= insertIndex → 아래로 shift (itemHeight)
  // i > origIndex && remainingIndex <  insertIndex → 위로  shift (-itemHeight)
  function getItemStyle(index: number): React.CSSProperties {
    if (!dragState || Math.abs(dragState.deltaY) <= 4) return {};
    const { origIndex, deltaY, insertIndex, itemHeight } = dragState;

    if (index === origIndex) {
      return { transform: `translateY(${deltaY}px)`, transition: "none", position: "relative", zIndex: 10 };
    }

    const ri = index < origIndex ? index : index - 1;
    let shift = 0;
    if (index < origIndex && ri >= insertIndex) shift =  itemHeight;
    else if (index > origIndex && ri < insertIndex) shift = -itemHeight;

    return { transform: `translateY(${shift}px)`, transition: "transform 150ms ease" };
  }

  function handleDragStart(e: React.MouseEvent, key: string) {
    if (e.button !== 0) return;
    e.preventDefault();

    const container = containerRef.current!;
    const origIndex = visibleDefs.findIndex(f => f.key === key);
    const itemEls   = Array.from(container.querySelectorAll<HTMLElement>(".field-widget-item"));
    const containerTop = container.getBoundingClientRect().top;

    const rects      = itemEls.map(el => el.getBoundingClientRect());
    const centers    = rects.map(r => r.top + r.height / 2 - containerTop);
    const itemHeight = rects[origIndex]?.height ?? 60;
    const startY     = e.clientY;

    // 클로저 내 가변 변수 — setState 콜백 안에서 다른 setState 호출 방지
    let latestInsert = origIndex;
    let latestDeltaY = 0;

    const onMove = (ev: MouseEvent) => {
      const deltaY = ev.clientY - startY;
      latestDeltaY = deltaY;
      // 마우스 Y 좌표 기준으로 위치 결정 (위/아래 대칭, 스크롤 시 cTop 재계산)
      const cTop   = container.getBoundingClientRect().top;
      const mouseY = ev.clientY - cTop;
      const rest   = centers.filter((_, i) => i !== origIndex);
      let   insertIdx = 0;
      for (let i = 0; i < rest.length; i++) {
        if (mouseY > rest[i]) insertIdx = i + 1;
      }
      latestInsert = insertIdx;
      setDragState({ key, origIndex, deltaY, insertIndex: insertIdx, itemHeight });
    };

    const onUp = () => {
      if (latestInsert !== origIndex && Math.abs(latestDeltaY) > 4) {
        reorderFields(panelScope, origIndex, latestInsert);
      }
      setDragState(null);
      window.removeEventListener("mousemove", onMove);
      window.removeEventListener("mouseup",   onUp);
    };

    setDragState({ key, origIndex, deltaY: 0, insertIndex: origIndex, itemHeight });
    window.addEventListener("mousemove", onMove);
    window.addEventListener("mouseup",   onUp);
  }

  return (
    <div className="field-widget-list">
      <div ref={containerRef}>
        {visibleDefs.map((def, index) => (
          <div
            key={def.key}
            className={`field-widget-item${dragState?.key === def.key ? " is-dragging" : ""}`}
            style={getItemStyle(index)}
          >
            <FieldWidgetContainer
              label={def.label}
              editMode={editMode}
              canHide={!def.alwaysVisible}
              onHide={() => hideField(panelScope, def.key)}
              onDragStart={ev => handleDragStart(ev, def.key)}
            >
              {def.render()}
            </FieldWidgetContainer>
          </div>
        ))}
      </div>

      {editMode && hiddenDefs.length > 0 && (
        <div className="field-widget-hidden-bar">
          {hiddenDefs.map(def => (
            <button
              type="button"
              key={def.key}
              className="field-widget-hidden-pill"
              onClick={() => showField(panelScope, def.key)}
            >
              + {def.label}
            </button>
          ))}
          <button
            type="button"
            className="field-widget-hidden-pill field-widget-hidden-pill--reset"
            onClick={() => resetFieldLayout(panelScope, defaultOrder)}
          >
            초기화
          </button>
        </div>
      )}
    </div>
  );
}
