"use client";

import { Search } from "lucide-react";
import { FieldItemGrid, type FieldItemDef } from "@/components/widget/field-item-grid";

const DOCUMENT_ITEMS: FieldItemDef[] = [
  {
    key: "sales-class",
    render: () => (
      <div className="li">
        <span className="li__label">Sales Class</span>
        <div className="li__input">
          <select style={{ flex: 1, height: 22, padding: "0 4px", fontSize: 10, border: "1px solid var(--border)", borderRadius: 4, background: "var(--surface-0)", color: "var(--ink)" }}>
            <option value="">Select...</option>
            <option value="DOM">DOM</option>
            <option value="INT">INT</option>
          </select>
        </div>
      </div>
    ),
  },
  {
    key: "sales-man",
    render: () => (
      <div className="li">
        <span className="li__label">Sales Man</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={12} style={{ flexShrink: 0, color: "var(--ink-muted)", cursor: "pointer" }} />
          <input placeholder="Name" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "operator",
    render: () => (
      <div className="li">
        <span className="li__label is-required">Operator</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={12} style={{ flexShrink: 0, color: "var(--ink-muted)", cursor: "pointer" }} />
          <input placeholder="Name" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
  {
    key: "team",
    render: () => (
      <div className="li">
        <span className="li__label is-required">Team</span>
        <div className="li__input" style={{ gap: 4 }}>
          <input placeholder="Code" style={{ width: 72, height: 22, padding: "0 8px", fontSize: 10, fontFamily: "var(--font-mono)" }} />
          <Search size={12} style={{ flexShrink: 0, color: "var(--ink-muted)", cursor: "pointer" }} />
          <input placeholder="Name" style={{ flex: 1, height: 22, padding: "0 8px", fontSize: 10 }} />
        </div>
      </div>
    ),
  },
];

export function NonBLDocumentPanel() {
  return (
    <div className="panel" style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      <div className="panel__head">
        <div className="panel__title-accent" />
        <span className="panel__title">Document</span>
      </div>
      <div className="panel__body" style={{ overflow: "auto", flex: 1 }}>
        <FieldItemGrid itemScope="nonbl-document-panel" items={DOCUMENT_ITEMS} />
      </div>
    </div>
  );
}
