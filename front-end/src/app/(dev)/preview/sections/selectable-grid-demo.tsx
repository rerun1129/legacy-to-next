"use client";

import { useState } from "react";
import { GridList, type GridColumn } from "@/components/shared/grid-list";

interface DemoRow {
  id: number;
  name: string;
  code: string;
  status: string;
}

const DEMO_DATA: DemoRow[] = [
  { id: 1, name: "고객사 A",  code: "CUST-001", status: "활성" },
  { id: 2, name: "고객사 B",  code: "CUST-002", status: "비활성" },
  { id: 3, name: "고객사 C",  code: "CUST-003", status: "활성" },
  { id: 4, name: "고객사 D",  code: "CUST-004", status: "활성" },
  { id: 5, name: "고객사 E",  code: "CUST-005", status: "비활성" },
];

const COLUMNS: GridColumn<DemoRow>[] = [
  { key: "id",     label: "ID",     width: 60,  align: "right" },
  { key: "name",   label: "이름",   width: 160 },
  { key: "code",   label: "코드",   width: 120 },
  { key: "status", label: "상태",   width: 80 },
];

export function SelectableGridDemo() {
  const [selectedKeys, setSelectedKeys] = useState<Set<number>>(new Set());

  function handleDelete() {
    alert(`삭제 대상 ID: ${[...selectedKeys].join(", ") || "(없음)"}`);
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
      <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
        <span style={{ fontSize: 11, color: "var(--ink-3, #555)" }}>
          선택됨: <strong style={{ fontFamily: "monospace" }}>{selectedKeys.size}</strong>건
        </span>
        <button
          style={{
            padding: "3px 10px", fontSize: 11, borderRadius: 4, cursor: "pointer",
            border: "1px solid var(--border, #ccc)",
            background: selectedKeys.size > 0 ? "var(--danger, #ef4444)" : "var(--surface-2, #f9f9f9)",
            color: selectedKeys.size > 0 ? "#fff" : "var(--ink-4, #aaa)",
          }}
          disabled={selectedKeys.size === 0}
          onClick={handleDelete}
        >
          선택 삭제
        </button>
        <button
          style={{
            padding: "3px 10px", fontSize: 11, borderRadius: 4, cursor: "pointer",
            border: "1px solid var(--border, #ccc)",
            background: "var(--surface, #fff)",
            color: "var(--ink-3, #555)",
          }}
          onClick={() => setSelectedKeys(new Set())}
        >
          선택 해제
        </button>
      </div>
      <div style={{ height: 200 }}>
        <GridList<DemoRow>
          columns={COLUMNS}
          data={DEMO_DATA}
          rowKey={(row) => row.id}
          selectable
          selectedKeys={selectedKeys as ReadonlySet<number>}
          onSelectionChange={(next) => setSelectedKeys(next as Set<number>)}
        />
      </div>
    </div>
  );
}
