"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getSession, hasPermission } from "@/lib/admin-session";
import type { Permission } from "@/domain/permission";

interface Props {
  children: React.ReactNode;
  requiredPermission?: Permission;
}

export function AdminGuard({ children, requiredPermission }: Props) {
  const router = useRouter();
  const [session] = useState(() => getSession());

  const authorized = session !== null && (
    requiredPermission
      ? hasPermission(session, requiredPermission)
      : true
  );

  useEffect(() => {
    if (!session) {
      router.replace("/login");
      return;
    }
    if (!authorized) {
      router.replace("/login");
    }
  }, [session, authorized, router]);

  if (!authorized) return null;
  return <>{children}</>;
}
