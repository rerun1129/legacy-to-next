"use client";

import { useState } from "react";
import { Pagination } from "@/components/shared/pagination";

const sectionStyle: React.CSSProperties = {
  marginBottom: 32,
  padding: 20,
  border: "1px solid #e5e7eb",
  borderRadius: 8,
};

const labelStyle: React.CSSProperties = {
  fontSize: 11,
  color: "#6b7280",
  marginBottom: 12,
  fontWeight: 600,
  textTransform: "uppercase",
  letterSpacing: "0.05em",
};

export function PaginationSection() {
  const [page, setPage] = useState(1);

  return (
    <div style={{ fontFamily: "inherit", fontSize: 12, maxWidth: 960, margin: "0 auto", padding: 24 }}>
      <h1 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12 }}>Pagination Preview</h1>

      <section style={sectionStyle}>
        <div style={labelStyle}>A. 기본 페이지네이션 (totalPages: 10)</div>
        <div style={{ maxWidth: 400, marginLeft: "auto" }}>
          <Pagination currentPage={page} totalPages={10} onPageChange={setPage} />
        </div>
        <div style={{ marginTop: 8, color: "#6b7280" }}>현재 페이지: {page}</div>
      </section>

      <section style={sectionStyle}>
        <div style={labelStyle}>B. disabled 상태</div>
        <div style={{ maxWidth: 400, marginLeft: "auto" }}>
          <Pagination currentPage={3} totalPages={10} onPageChange={() => undefined} disabled />
        </div>
      </section>

      <section style={sectionStyle}>
        <div style={labelStyle}>C. totalPages &lt;= 1 이면 렌더 안 함</div>
        <div style={{ padding: "8px 0", color: "#6b7280" }}>
          아래 영역은 비어 있어야 합니다:
        </div>
        <Pagination currentPage={1} totalPages={1} onPageChange={() => undefined} />
      </section>
    </div>
  );
}
