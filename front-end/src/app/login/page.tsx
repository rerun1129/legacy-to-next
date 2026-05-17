"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { ADMIN_API_URL } from "@/lib/api-base";
import { setSession } from "@/lib/admin-session";
import { toast } from "@/lib/toast-store";

interface LoginForm {
  username: string;
  password: string;
}

export default function LoginPage() {
  const router = useRouter();
  const { register, handleSubmit, formState: { isSubmitting } } = useForm<LoginForm>();
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (data: LoginForm) => {
    setError(null);
    const authHeader = "Basic " + btoa(`${data.username}:${data.password}`);
    try {
      const res = await fetch(`${ADMIN_API_URL}/api/admin/code/search`, {
        method: "POST",
        headers: {
          Authorization: authHeader,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ page: 0, size: 1 }),
      });
      if (res.status === 401 || res.status === 403) {
        toast.error("로그인 실패: 사용자 또는 비밀번호가 올바르지 않습니다.");
        setError("auth");
        return;
      }
      if (!res.ok) {
        toast.error(`서버 오류 (${res.status})`);
        return;
      }
      setSession({ authHeader, role: "ADMIN" });
      router.replace("/admin/code/list");
    } catch (e) {
      console.error(e);
      toast.error("네트워크 오류");
    }
  };

  return (
    <div style={{
      minHeight: "100vh",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      background: "var(--bg, #f8fafc)",
    }}>
      <form
        onSubmit={handleSubmit(onSubmit)}
        style={{
          width: 360,
          padding: 24,
          background: "var(--surface, white)",
          border: "1px solid var(--border, #e5e7eb)",
          borderRadius: 8,
          display: "flex",
          flexDirection: "column",
          gap: 12,
        }}
      >
        <h1 style={{ fontSize: 18, fontWeight: 600, marginBottom: 4 }}>Admin Login</h1>
        <label style={{ display: "flex", flexDirection: "column", gap: 4, fontSize: 13 }}>
          ID
          <input
            type="text"
            autoComplete="username"
            {...register("username")}
            style={{ padding: "6px 8px", border: "1px solid var(--border, #e5e7eb)", borderRadius: 4 }}
          />
        </label>
        <label style={{ display: "flex", flexDirection: "column", gap: 4, fontSize: 13 }}>
          Password
          <input
            type="password"
            autoComplete="current-password"
            {...register("password")}
            style={{ padding: "6px 8px", border: "1px solid var(--border, #e5e7eb)", borderRadius: 4 }}
          />
        </label>
        <button
          type="submit"
          disabled={isSubmitting}
          style={{
            marginTop: 8,
            padding: "8px 12px",
            background: "var(--accent, #2563eb)",
            color: "white",
            border: "none",
            borderRadius: 4,
            cursor: "pointer",
            opacity: isSubmitting ? 0.6 : 1,
          }}
        >
          {isSubmitting ? "로그인 중..." : "로그인"}
        </button>
        {error && <span style={{ color: "var(--danger, #dc2626)", fontSize: 12 }}>로그인에 실패했습니다.</span>}
      </form>
    </div>
  );
}
