"use client";

import { useState } from "react";

// Copy 훅: 키별로 복사 상태 추적
export function useCopy() {
  const [copied, setCopied] = useState<string | null>(null);
  const copy = (key: string, text: string) => {
    navigator.clipboard.writeText(text);
    setCopied(key);
    setTimeout(() => setCopied(null), 1500);
  };
  return { copied, copy };
}

export function CopyBtn({
  id,
  text,
  copied,
  copy,
}: {
  id: string;
  text: string;
  copied: string | null;
  copy: (k: string, t: string) => void;
}) {
  return (
    <button
      onClick={() => copy(id, text)}
      style={{
        marginLeft: 6,
        fontSize: 10,
        padding: "1px 6px",
        cursor: "pointer",
        border: "1px solid #ccc",
        borderRadius: 3,
        background: copied === id ? "#d1fae5" : "#fff",
      }}
    >
      {copied === id ? "복사됨 ✓" : "Copy"}
    </button>
  );
}

export const sectionStyle: React.CSSProperties = {
  borderTop: "1px solid #ddd",
  padding: "12px 16px",
};

export const preStyle: React.CSSProperties = {
  margin: "4px 0 0",
  padding: "6px 8px",
  background: "#f0f0f0",
  borderRadius: 4,
  fontSize: 10,
  fontFamily: "monospace",
  whiteSpace: "pre",
  overflowX: "auto",
};

export const rowStyle: React.CSSProperties = {
  display: "flex",
  alignItems: "flex-start",
  gap: 16,
  marginBottom: 12,
};

export const labelStyle: React.CSSProperties = {
  fontSize: 10,
  color: "#666",
  marginBottom: 4,
};

export const warningBadge: React.CSSProperties = {
  display: "inline-block",
  marginLeft: 6,
  padding: "1px 5px",
  background: "#fee2e2",
  color: "#b91c1c",
  borderRadius: 3,
  fontSize: 9,
  fontWeight: 600,
};
