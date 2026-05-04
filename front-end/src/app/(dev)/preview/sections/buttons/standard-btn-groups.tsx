"use client";

import { Search, Save, Plus, Trash2, X, Loader2 } from "lucide-react";
import { useCopy, CopyBtn, sectionStyle, preStyle, labelStyle, warningBadge } from "./_shared";

// 그룹 ①: Standard .btn 변형
export function StandardBtnGroup() {
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
export function UndefinedCssBtnGroup() {
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
export function IconLabelBtnGroup() {
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
