"use client";

import { useRouter } from "next/navigation";
import { LogOut } from "lucide-react";
import { authUseCases } from "@/application/auth/use-cases";
import { clearSession, getSession } from "@/lib/admin-session";
import { useTabs } from "@/lib/use-tabs";

export function AdminLogoutButton() {
  const router = useRouter();
  const clearTabs = useTabs((s) => s.clearTabs);

  const handleClick = async () => {
    const session = getSession();
    if (session) {
      try {
        await authUseCases.logout(session.refreshToken);
      } catch {
        // best-effort — 실패해도 로컬 세션 정리
      }
    }
    clearTabs();
    clearSession();
    router.replace("/login");
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      className="admin-logout-btn"
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: 6,
        padding: "4px 10px",
        fontSize: "var(--fs-xs)",
        border: "1px solid var(--border)",
        borderRadius: 4,
        background: "var(--surface)",
        cursor: "pointer",
      }}
    >
      <LogOut size={12} /> 로그아웃
    </button>
  );
}
