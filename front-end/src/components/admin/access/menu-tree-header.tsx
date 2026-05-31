"use client";

import { useTranslations } from "next-intl";
import { buildMenuColumnHeaders } from "./menu-grid-columns";

// 체크박스(28px) + 토글(20px) 스페이서 — TreeRow 레이아웃과 동기화
const CHECKBOX_W = 28;
const TOGGLE_W = 20;

const thStyle: React.CSSProperties = {
  fontSize: 10,
  fontWeight: 600,
  textTransform: "uppercase",
  letterSpacing: "0.06em",
  color: "var(--ink-4)",
  textAlign: "left",
  padding: "6px 4px",
  background: "var(--surface-2)",
  borderBottom: "1px solid var(--border)",
  whiteSpace: "nowrap",
  flexShrink: 0,
};

/**
 * MenuTreeView 상단 컬럼 라벨 헤더.
 * MENU_COLUMN_DEFS의 width와 TreeRow 셀 width가 일치해야 정렬이 맞음.
 */
export function MenuTreeHeader() {
  // useTranslations는 early-return 이전에 무조건 호출
  const tCols = useTranslations("admin.menu.cols");
  const columns = buildMenuColumnHeaders(tCols);

  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        position: "sticky",
        top: 0,
        zIndex: 1,
        background: "var(--surface-2)",
        borderBottom: "1px solid var(--border)",
      }}
    >
      {/* 체크박스 스페이서 */}
      <div style={{ ...thStyle, width: CHECKBOX_W, padding: "6px 0" }} />
      {/* 토글 스페이서 */}
      <div style={{ ...thStyle, width: TOGGLE_W, padding: "6px 0" }} />
      {/* 컬럼 헤더 */}
      {columns.map((col) => (
        <div key={col.key} style={{ ...thStyle, width: col.width, minWidth: col.width }}>
          {col.label}
        </div>
      ))}
    </div>
  );
}
