"use client";

import { useQuery } from "@tanstack/react-query";
import { termsUseCases } from "@/application/terms/use-cases";
import type { TermsType } from "@/domain/terms";

interface Props {
  type: TermsType;
}

const TYPE_LABEL: Record<TermsType, string> = {
  TOS: "서비스 이용약관",
  PRIVACY: "개인정보처리방침",
  MARKETING: "마케팅 수신동의",
};

export function TermsHistoryPanel({ type }: Props) {
  const { data, isLoading, error } = useQuery({
    queryKey: ["admin-terms", "history", type],
    queryFn: () =>
      termsUseCases.search({ type, scope: "ALL", version: "", summary: "" }, 1, 100),
    staleTime: Infinity,
    gcTime: Infinity,
    refetchOnMount: false,
  });

  if (isLoading) {
    return <div style={{ padding: "8px 0", color: "var(--ink-3)" }}>이력 로딩 중...</div>;
  }

  if (error) {
    return (
      <div style={{ padding: "8px 0" }}>
        <span className="text-error">이력을 불러오지 못했습니다.</span>
      </div>
    );
  }

  // BE가 type ASC, version DESC 정렬을 보장하므로 클라이언트 재정렬 불필요
  const rows = data?.content ?? [];

  return (
    <div
      style={{
        marginTop: 12,
        border: "1px solid var(--border)",
        borderRadius: 4,
        overflow: "hidden",
      }}
    >
      <div
        style={{
          padding: "6px 10px",
          background: "var(--surface-2, #f8f9fa)",
          borderBottom: "1px solid var(--border)",
          fontSize: "var(--fs-sm)",
          fontWeight: 600,
        }}
      >
        {TYPE_LABEL[type]} 버전 이력
      </div>
      {rows.length === 0 ? (
        <div style={{ padding: "12px 10px", color: "var(--ink-3)", fontSize: "var(--fs-sm)" }}>
          이력이 없습니다.
        </div>
      ) : (
        <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "var(--fs-sm)" }}>
          <thead>
            <tr style={{ background: "var(--surface-2, #f8f9fa)" }}>
              <th style={{ padding: "4px 8px", textAlign: "center", borderBottom: "1px solid var(--border)", width: 60 }}>버전</th>
              <th style={{ padding: "4px 8px", textAlign: "left", borderBottom: "1px solid var(--border)" }}>적용일시</th>
              <th style={{ padding: "4px 8px", textAlign: "left", borderBottom: "1px solid var(--border)" }}>요약</th>
              <th style={{ padding: "4px 8px", textAlign: "center", borderBottom: "1px solid var(--border)", width: 72 }}>상태</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id} style={{ borderBottom: "1px solid var(--border)" }}>
                <td style={{ padding: "4px 8px", textAlign: "center" }}>{row.version}</td>
                <td style={{ padding: "4px 8px" }}>{row.effectiveAt}</td>
                <td style={{ padding: "4px 8px" }}>{row.summary ?? "—"}</td>
                <td style={{ padding: "4px 8px", textAlign: "center" }}>
                  {row.deletedAt ? (
                    <span style={{ color: "var(--danger, #dc2626)" }}>삭제됨</span>
                  ) : (
                    <span style={{ color: "var(--success, #16a34a)" }}>활성</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
