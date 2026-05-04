"use client";

import { Bell, Settings, LogOut, Search, Plus, X } from "lucide-react";
import { useCopy, CopyBtn, sectionStyle, preStyle, labelStyle } from "./_shared";

// 그룹 ④: Topbar Icons
export function TopbarIconGroup() {
  const { copied, copy } = useCopy();

  const specimens: { id: string; label: string; node: React.ReactNode; snippet: string }[] = [
    {
      id: "topbar-bell",
      label: "Bell",
      node: <button className="topbar-icon"><Bell size={14} /></button>,
      snippet: `<button className="topbar-icon"><Bell size={14} /></button>`,
    },
    {
      id: "topbar-settings",
      label: "Settings",
      node: <button className="topbar-icon"><Settings size={14} /></button>,
      snippet: `<button className="topbar-icon"><Settings size={14} /></button>`,
    },
    {
      id: "topbar-logout",
      label: "LogOut",
      node: <button className="topbar-icon"><LogOut size={14} /></button>,
      snippet: `<button className="topbar-icon"><LogOut size={14} /></button>`,
    },
    {
      id: "topbar-search",
      label: "Search",
      node: <button className="topbar-icon"><Search size={14} /></button>,
      snippet: `<button className="topbar-icon"><Search size={14} /></button>`,
    },
  ];

  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>④ Topbar Icons (.topbar-icon)</div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
        {specimens.map((s) => (
          <div key={s.id} style={{ minWidth: 120 }}>
            <div style={labelStyle}>{s.label}</div>
            <div>{s.node}</div>
            <div style={{ display: "flex", alignItems: "center" }}>
              <pre style={preStyle}>{s.snippet}</pre>
              <CopyBtn id={s.id} text={s.snippet} copied={copied} copy={copy} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// 그룹 ⑤: Party Block 헤더 버튼
export function PartyBlockBtnGroup() {
  const { copied, copy } = useCopy();

  const specimens: { id: string; label: string; snippet: string }[] = [
    {
      id: "party-clear",
      label: "Clear",
      snippet: `<button className="party-block__head-btn">Clear</button>`,
    },
    {
      id: "party-order",
      label: "To Order",
      snippet: `<button className="party-block__head-btn">To Order</button>`,
    },
    {
      id: "party-same",
      label: "Same as Cne.",
      snippet: `<button className="party-block__head-btn">Same as Cne.</button>`,
    },
  ];

  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>⑤ Party Block 헤더 (.party-block__head-btn)</div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
        {specimens.map((s) => (
          <div key={s.id} style={{ minWidth: 140 }}>
            <div style={labelStyle}>{s.label}</div>
            <div><button className="party-block__head-btn">{s.label}</button></div>
            <div style={{ display: "flex", alignItems: "center" }}>
              <pre style={preStyle}>{s.snippet}</pre>
              <CopyBtn id={s.id} text={s.snippet} copied={copied} copy={copy} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// 그룹 ⑥: Field Item Grid 버튼들
export function FieldItemBtnGroup() {
  const { copied, copy } = useCopy();

  const specimens: { id: string; label: string; node: React.ReactNode; snippet: string }[] = [
    {
      id: "field-row-btn-a",
      label: ".field-item-row-btn A",
      node: <button className="field-item-row-btn">A</button>,
      snippet: `<button className="field-item-row-btn">A</button>`,
    },
    {
      id: "field-row-btn-b",
      label: ".field-item-row-btn B",
      node: <button className="field-item-row-btn">B</button>,
      snippet: `<button className="field-item-row-btn">B</button>`,
    },
    {
      id: "field-row-btn-mode",
      label: ".field-item-row-btn--mode",
      node: <button className="field-item-row-btn field-item-row-btn--mode">ITEM</button>,
      snippet: `<button className="field-item-row-btn field-item-row-btn--mode">ITEM</button>`,
    },
    {
      id: "field-row-btn-delete",
      label: ".field-item-row-btn--delete",
      node: <button className="field-item-row-btn field-item-row-btn--delete"><X size={10} /></button>,
      snippet: `<button className="field-item-row-btn field-item-row-btn--delete"><X size={10} /></button>`,
    },
    {
      id: "field-add-row",
      label: ".field-item-add-row",
      node: (
        <button className="field-item-add-row">
          <Plus size={8} />행 추가
        </button>
      ),
      snippet: `<button className="field-item-add-row"><Plus size={8} />행 추가</button>`,
    },
    {
      id: "field-hidden-pill",
      label: ".field-widget-hidden-pill",
      node: <button className="field-widget-hidden-pill">+2개 숨김</button>,
      snippet: `<button className="field-widget-hidden-pill">+2개 숨김</button>`,
    },
    {
      id: "field-cell-close",
      label: ".field-item-cell-close",
      node: (
        <div style={{ position: "relative", display: "inline-block", width: 40, height: 20, background: "#eee", borderRadius: 2 }}>
          <button className="field-item-cell-close" style={{ opacity: 1 }}><X size={8} /></button>
        </div>
      ),
      // opacity 0 기본값 — 부모 hover 시 visible
      snippet: `{/* opacity 0 기본값 — 부모 hover 시 visible */}\n<button className="field-item-cell-close"><X size={8} /></button>`,
    },
  ];

  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>⑥ Field Item Grid 버튼들</div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
        {specimens.map((s) => (
          <div key={s.id} style={{ minWidth: 160 }}>
            <div style={labelStyle}>{s.label}</div>
            <div style={{ minHeight: 24 }}>{s.node}</div>
            <div style={{ display: "flex", alignItems: "center" }}>
              <pre style={preStyle}>{s.snippet}</pre>
              <CopyBtn id={s.id} text={s.snippet} copied={copied} copy={copy} />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
