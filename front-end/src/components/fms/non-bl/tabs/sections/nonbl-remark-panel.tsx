"use client";

import { LineNumberTextarea } from "@/components/shared/line-number-textarea";

export function NonBLRemarkPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Remark</span>
      </div>
      <div className="panel__body" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
        <LineNumberTextarea placeholder="Remark" style={{ flex: 1 }} />
      </div>
    </div>
  );
}
