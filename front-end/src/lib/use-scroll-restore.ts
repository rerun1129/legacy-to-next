"use client";

import { useEffect, useLayoutEffect, useRef, type RefObject } from "react";

const MAX_RESTORE_FRAMES = 60;

/**
 * Scroll container 의 scrollTop 을 sessionStorage 에 기억·복원.
 *
 * - 저장: scroll 이벤트 + 100ms debounce
 * - 복원: requestAnimationFrame 폴링 (TanStack Virtual 등 lazy mount 대응)
 * - 새로고침 시 자동 초기화 (sessionStorage 의 자연 동작)
 */
export function useScrollRestore(
  scrollRef: RefObject<HTMLDivElement | null>,
  storageKey: string | undefined,
  enabled: boolean = true,
): void {
  const restoredRef = useRef(false);

  // 저장: scroll 이벤트 (debounced)
  useEffect(() => {
    if (!storageKey) return;
    if (typeof window === "undefined") return;
    const el = scrollRef.current;
    if (!el) return;
    let timer: ReturnType<typeof setTimeout> | null = null;
    const handler = () => {
      if (timer) clearTimeout(timer);
      timer = setTimeout(() => {
        const top = el.scrollTop;
        if (top > 0) {
          sessionStorage.setItem(storageKey, String(top));
        } else {
          sessionStorage.removeItem(storageKey);
        }
      }, 100);
    };
    el.addEventListener("scroll", handler, { passive: true });
    return () => {
      el.removeEventListener("scroll", handler);
      if (timer) clearTimeout(timer);
    };
  }, [scrollRef, storageKey]);

  // 복원: RAF 폴링으로 scrollHeight 충분할 때까지 대기 후 적용
  useLayoutEffect(() => {
    if (!enabled || !storageKey || restoredRef.current) return;
    if (typeof window === "undefined") return;
    const el = scrollRef.current;
    if (!el) return;
    const saved = sessionStorage.getItem(storageKey);
    if (saved === null) {
      restoredRef.current = true;
      return;
    }
    const top = Number(saved);
    if (!Number.isFinite(top) || top <= 0) {
      restoredRef.current = true;
      return;
    }

    let cancelled = false;
    let frames = 0;

    const attempt = () => {
      if (cancelled || restoredRef.current) return;
      // scrollHeight 가 saved top 이상으로 자랄 때까지 대기 후 적용
      // (top + clientHeight 까지 보장하지 않음 — 데이터 끝에 있어도 OK)
      if (el.scrollHeight > top) {
        el.scrollTop = top;
        restoredRef.current = true;
        return;
      }
      frames++;
      if (frames >= MAX_RESTORE_FRAMES) {
        // 1초 후에도 충분히 안 자랐으면 포기 (데이터가 적은 경우 등)
        restoredRef.current = true;
        return;
      }
      requestAnimationFrame(attempt);
    };

    requestAnimationFrame(attempt);

    return () => {
      cancelled = true;
    };
  }, [scrollRef, storageKey, enabled]);
}
