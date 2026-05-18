"use client";

import { useState } from "react";
import { CodeMasterListGrid } from "./code-master-list-grid";
import { CodeDetailListGrid } from "./code-detail-list-grid";
import { CodeMasterEntryModal } from "./code-master-entry-modal";
import type { CodeMasterEntryModalState } from "./code-master-entry-modal";

export function CodeListClient() {
  const [selectedMasterId, setSelectedMasterId] = useState<number | null>(null);
  const [editModalState, setEditModalState] = useState<CodeMasterEntryModalState | null>(null);

  return (
    // ResizablePanel 미사용 — 프로젝트에 shadcn ui 컴포넌트 없음, 고정+가변 그리드 레이아웃 사용
    <div
      style={{
        display: "grid",
        gridTemplateColumns: "400px 1fr",
        gap: 12,
        flex: 1,
        minHeight: 0,
        overflow: "hidden",
      }}
    >
      <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        <CodeMasterListGrid
          selectedId={selectedMasterId}
          onSelect={setSelectedMasterId}
          onRowDoubleClick={(id) => setEditModalState({ mode: "edit", id })}
        />
      </div>
      <div style={{ minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        {/* key로 master 변경 시 컴포넌트 remount → currentPage 등 내부 state 자연 초기화 */}
        <CodeDetailListGrid key={selectedMasterId ?? "none"} masterId={selectedMasterId} />
      </div>

      <CodeMasterEntryModal
        state={editModalState}
        onClose={() => setEditModalState(null)}
        onSaved={() => setEditModalState(null)}
      />
    </div>
  );
}
