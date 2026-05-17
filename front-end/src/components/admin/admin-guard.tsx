"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getSession } from "@/lib/admin-session";

export function AdminGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [session] = useState(() => getSession());
  const authorized = session?.role === "ADMIN";

  useEffect(() => {
    if (!authorized) router.replace("/login");
  }, [authorized, router]);

  if (!authorized) return null;
  return <>{children}</>;
}
