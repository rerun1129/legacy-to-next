"use client";

import { GripVertical, X } from "lucide-react";

interface Props {
  label:       string;
  editMode:    boolean;
  canHide:     boolean;
  onHide:      () => void;
  onDragStart: (e: React.MouseEvent) => void;
  children:    React.ReactNode;
}

export function FieldWidgetContainer({
  label, editMode, canHide, onHide, onDragStart, children,
}: Props) {
  return (
    <div className={`field-widget-container${editMode ? " is-edit" : ""}`}>
      {editMode && (
        <div className="field-widget-container__bar" onMouseDown={onDragStart}>
          <GripVertical size={11} className="field-widget-container__grip" />
          <span className="field-widget-container__label">{label}</span>
          {canHide && (
            <button
              className="field-widget-container__close"
              title="숨기기"
              onMouseDown={e => e.stopPropagation()}
              onClick={onHide}
            >
              <X size={10} />
            </button>
          )}
        </div>
      )}
      {children}
    </div>
  );
}
