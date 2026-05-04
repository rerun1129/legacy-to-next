"use client";

import { useState } from "react";
import {
  Search,
  Save,
  Plus,
  Minus,
  X,
  Trash2,
  LogOut,
  Bell,
  Settings,
  Loader2,
} from "lucide-react";

// Copy 훅: 키별로 복사 상태 추적
function useCopy() {
  const [copied, setCopied] = useState<string | null>(null);
  const copy = (key: string, text: string) => {
    navigator.clipboard.writeText(text);
    setCopied(key);
    setTimeout(() => setCopied(null), 1500);
  };
  return { copied, copy };
}

const sectionStyle: React.CSSProperties = {
  borderTop: "1px solid #ddd",
  padding: "12px 16px",
};

const preStyle: React.CSSProperties = {
  margin: "4px 0 0",
  padding: "6px 8px",
  background: "#f0f0f0",
  borderRadius: 4,
  fontSize: 10,
  fontFamily: "monospace",
  whiteSpace: "pre",
  overflowX: "auto",
};

const rowStyle: React.CSSProperties = {
  display: "flex",
  alignItems: "flex-start",
  gap: 16,
  marginBottom: 12,
};

const labelStyle: React.CSSProperties = {
  fontSize: 10,
  color: "#666",
  marginBottom: 4,
};

const warningBadge: React.CSSProperties = {
  display: "inline-block",
  marginLeft: 6,
  padding: "1px 5px",
  background: "#fee2e2",
  color: "#b91c1c",
  borderRadius: 3,
  fontSize: 9,
  fontWeight: 600,
};

function CopyBtn({ id, text, copied, copy }: { id: string; text: string; copied: string | null; copy: (k: string, t: string) => void }) {
  return (
    <button
      onClick={() => copy(id, text)}
      style={{ marginLeft: 6, fontSize: 10, padding: "1px 6px", cursor: "pointer", border: "1px solid #ccc", borderRadius: 3, background: copied === id ? "#d1fae5" : "#fff" }}
    >
      {copied === id ? "복사됨 ✓" : "Copy"}
    </button>
  );
}

// 그룹 ①: Standard .btn 변형
function StandardBtnGroup() {
  const { copied, copy } = useCopy();

  const specimens: { id: string; label: string; node: React.ReactNode; snippet: string }[] = [
    {
      id: "btn-default",
      label: ".btn",
      node: <button className="btn">기본</button>,
      snippet: `<button className="btn">기본</button>`,
    },
    {
      id: "btn-primary",
      label: ".btn.btn--primary",
      node: <button className="btn btn--primary">저장</button>,
      snippet: `<button className="btn btn--primary">저장</button>`,
    },
    {
      id: "btn-ghost",
      label: ".btn.btn--ghost",
      node: <button className="btn btn--ghost">초기화</button>,
      snippet: `<button className="btn btn--ghost">초기화</button>`,
    },
    {
      id: "btn-danger",
      label: ".btn.btn--danger",
      node: <button className="btn btn--danger">삭제</button>,
      snippet: `<button className="btn btn--danger">삭제</button>`,
    },
    {
      id: "btn-sm",
      label: ".btn.btn--sm",
      node: <button className="btn btn--sm">기본(sm)</button>,
      snippet: `<button className="btn btn--sm">기본(sm)</button>`,
    },
    {
      id: "btn-sm-primary",
      label: ".btn.btn--sm.btn--primary",
      node: <button className="btn btn--sm btn--primary">저장(sm)</button>,
      snippet: `<button className="btn btn--sm btn--primary">저장(sm)</button>`,
    },
    {
      id: "btn-sm-ghost",
      label: ".btn.btn--sm.btn--ghost",
      node: <button className="btn btn--sm btn--ghost">초기화(sm)</button>,
      snippet: `<button className="btn btn--sm btn--ghost">초기화(sm)</button>`,
    },
    {
      id: "btn-sm-danger",
      label: ".btn.btn--sm.btn--danger",
      node: <button className="btn btn--sm btn--danger">삭제(sm)</button>,
      snippet: `<button className="btn btn--sm btn--danger">삭제(sm)</button>`,
    },
    {
      id: "btn-icon",
      label: ".btn.btn--icon",
      node: <button className="btn btn--icon"><Search size={12} /></button>,
      snippet: `<button className="btn btn--icon"><Search size={12} /></button>`,
    },
    {
      id: "btn-sm-icon",
      label: ".btn.btn--sm.btn--icon",
      node: <button className="btn btn--sm btn--icon"><X size={12} /></button>,
      snippet: `<button className="btn btn--sm btn--icon"><X size={12} /></button>`,
    },
    {
      id: "btn-busy",
      label: ".btn.btn--primary.is-busy",
      node: <button className="btn btn--primary is-busy">저장<Loader2 size={12} className="spin" /></button>,
      snippet: `<button className="btn btn--primary is-busy">저장<Loader2 size={12} className="spin" /></button>`,
    },
    {
      id: "btn-disabled",
      label: ".btn disabled",
      node: <button className="btn" disabled>비활성</button>,
      snippet: `<button className="btn" disabled>비활성</button>`,
    },
  ];

  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>① Standard .btn 변형</div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
        {specimens.map((s) => (
          <div key={s.id} style={{ minWidth: 160 }}>
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

// 그룹 ②: CSS 미정의 변형
function UndefinedCssBtnGroup() {
  const { copied, copy } = useCopy();

  const specimens: { id: string; cls: string; snippet: string }[] = [
    {
      id: "undef-secondary",
      cls: "btn--secondary",
      snippet: `<button className="btn btn--secondary">Secondary</button>`,
    },
    {
      id: "undef-success",
      cls: "btn--success",
      snippet: `<button className="btn btn--success">Success</button>`,
    },
    {
      id: "undef-xs",
      cls: "btn--xs",
      snippet: `<button className="btn btn--xs">Extra Small</button>`,
    },
  ];

  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>
        ② CSS 미정의 변형
        <span style={{ marginLeft: 8, fontSize: 10, color: "#b91c1c", fontWeight: 400 }}>
          (시각적으로 default와 동일하게 보임)
        </span>
      </div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
        {specimens.map((s) => (
          <div key={s.id} style={{ minWidth: 180 }}>
            <div style={labelStyle}>
              .{s.cls}
              <span style={warningBadge}>⚠ CSS 미정의</span>
            </div>
            <div>
              <button className={`btn ${s.cls}`}>{s.cls.replace("btn--", "")}</button>
            </div>
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

// 그룹 ③: 아이콘 + 라벨 조합
function IconLabelBtnGroup() {
  const { copied, copy } = useCopy();

  const specimens: { id: string; label: string; node: React.ReactNode; snippet: string }[] = [
    {
      id: "icon-save",
      label: "Save + 라벨",
      node: <button className="btn btn--sm btn--primary"><Save size={12} />저장</button>,
      snippet: `<button className="btn btn--sm btn--primary"><Save size={12} />저장</button>`,
    },
    {
      id: "icon-search",
      label: "Search + 라벨",
      node: <button className="btn btn--sm"><Search size={12} />검색</button>,
      snippet: `<button className="btn btn--sm"><Search size={12} />검색</button>`,
    },
    {
      id: "icon-plus",
      label: "Plus + 라벨",
      node: <button className="btn btn--sm btn--ghost"><Plus size={12} />행 추가</button>,
      snippet: `<button className="btn btn--sm btn--ghost"><Plus size={12} />행 추가</button>`,
    },
    {
      id: "icon-trash",
      label: "Trash + 라벨",
      node: <button className="btn btn--sm btn--danger"><Trash2 size={12} />삭제</button>,
      snippet: `<button className="btn btn--sm btn--danger"><Trash2 size={12} />삭제</button>`,
    },
    {
      id: "icon-kbd",
      label: "kbd 슬롯",
      node: (
        <button className="btn btn--sm btn--primary">
          저장<span className="btn__kbd">⌘S</span>
        </button>
      ),
      snippet: `<button className="btn btn--sm btn--primary">저장<span className="btn__kbd">⌘S</span></button>`,
    },
  ];

  return (
    <div style={sectionStyle}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>③ 아이콘 + 라벨 조합</div>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
        {specimens.map((s) => (
          <div key={s.id} style={{ minWidth: 180 }}>
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

// 그룹 ④: Topbar Icons
function TopbarIconGroup() {
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
function PartyBlockBtnGroup() {
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
function FieldItemBtnGroup() {
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
      // opacity 0 주의: 실제 사용 시 부모 hover 상태에서 opacity 1로 전환됨
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

export function ButtonsSection() {
  return (
    <div style={{ fontFamily: "inherit", fontSize: 12, maxWidth: 960, margin: "0 auto", padding: 24 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Buttons Preview</h1>
      <StandardBtnGroup />
      <UndefinedCssBtnGroup />
      <IconLabelBtnGroup />
      <TopbarIconGroup />
      <PartyBlockBtnGroup />
      <FieldItemBtnGroup />
    </div>
  );
}
