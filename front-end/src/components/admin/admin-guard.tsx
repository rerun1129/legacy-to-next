"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useTranslations } from "next-intl";
import { getSession, hasMenuAccess, clearSession, firstAccessibleRoute } from "@/lib/admin-session";
import { useTabs } from "@/lib/use-tabs";
import { toast } from "@/lib/toast-store";

interface Props {
  children: React.ReactNode;
  requiredMenuCode?: string;
}

export function AdminGuard({ children, requiredMenuCode }: Props) {
  const router = useRouter();
  const pathname = usePathname();
  const t = useTranslations("admin.guard");
  // SSR 단계에서는 session을 읽지 않고, mounted 후에만 실제 세션을 평가.
  // mounted=false 동안 SSR 결과(return null)와 CSR 초기 렌더를 일치시켜 hydration mismatch 방지.
  const [mounted, setMounted] = useState(false);
  const [session] = useState(() => getSession());

  const authorized = session !== null && (
    requiredMenuCode ? hasMenuAccess(session, requiredMenuCode) : true
  );

  // SSR/CSR hydration 일치를 위한 mount gate
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => setMounted(true), []);

  useEffect(() => {
    if (!mounted) return;
    if (!session) {
      // 쿠키는 있지만 localStorage가 비어있는 desync 상태를 정리해 무한 리다이렉트 방지
      clearSession();
      router.replace("/login");
      return;
    }
    if (!authorized) {
      // 세션은 있으나 해당 페이지 권한이 부족한 경우 — 로그인 해제 없이 접근 가능 라우트로 이동
      const target = firstAccessibleRoute(session);
      if (target) {
        // redirect 전 권한 없는 경로 탭을 제거해 잔존 탭을 통한 무한 redirect 방지
        useTabs.getState().removeTab(pathname);
        toast.error(t("noAccess"));
        router.replace(target);
      } else {
        // 어떤 라우트도 접근 불가한 비정상 세션이면 정리 후 로그인으로
        useTabs.getState().removeTab(pathname);
        clearSession();
        router.replace("/login");
      }
    }
  }, [mounted, session, authorized, router, pathname, t]);

  if (!mounted || !authorized) return null;
  return <>{children}</>;
}
