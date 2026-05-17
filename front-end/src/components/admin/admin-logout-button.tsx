"use client";

import { useRouter } from "next/navigation";
import { LogOut } from "lucide-react";
import { clearSession } from "@/lib/admin-session";

export function AdminLogoutButton() {
  const router = useRouter();
  const handleClick = () => {
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
