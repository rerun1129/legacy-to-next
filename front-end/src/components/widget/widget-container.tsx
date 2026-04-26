"use client";

import { GripVertical, X } from "lucide-react";

interface Props {
  label:    string;
  editMode: boolean;
  onHide:   () => void;
  children: React.ReactNode;
}

export function WidgetContainer({ label, editMode, onHide, children }: Props) {
  return (
    <div className={`widget-container${editMode ? " is-edit" : ""}`}>
      {editMode && (
        <div className="widget-container__bar">
          <span className="widget-container__handle" title="드래그하여 이동">
            <GripVertical size={14} />
          </span>
          <span className="widget-container__label">{label}</span>
          <button
            className="widget-container__close"
            title="숨기기"
            onClick={onHide}
          >
            <X size={12} />
          </button>
        </div>
      )}
      <div className="widget-container__body" style={{ flex: 1, minHeight: 0, overflow: "hidden" }}>
        {children}
      </div>
    </div>
  );
}
