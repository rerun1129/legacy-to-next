"use client";

import { useTranslations } from "next-intl";
import { buildButtonColumnHeaders } from "./button-grid-columns";

// 체크박스(28px) + 들여쓰기/토글 영역(20px) + 메뉴 indent 스페이서(20px) 스페이서
// 버튼 행 기준: paddingLeft(8) + checkbox(20) + gap(4) + leaf-indent(20) = 52px 앞쪽 고정
// 헤더는 이에 맞춰 체크박스+토글 스페이서를 배치
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
 * ButtonTreeView 상단 컬럼 라벨 헤더.
 * BUTTON_COLUMN_DEFS의 width와 ButtonRowCells 셀 width가 일치해야 정렬이 맞음.
 * 버튼 행(leaf)을 기준으로 정렬하며, 메뉴 노드 헤더는 read-only이므로 헤더와 완전 정렬이 어렵다.
 * (메뉴 노드는 menuCode+label 읽기전용 표시, 버튼 행은 인라인 편집 셀)
 */
export function ButtonTreeHeader() {
  // useTranslations는 early-return 이전에 무조건 호출
  const tCols = useTranslations("admin.button.cols");
  const columns = buildButtonColumnHeaders(tCols);

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
      {/* 토글/leaf-indent 스페이서 */}
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
