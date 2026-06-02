"use client";
import { useState, useSyncExternalStore } from "react";

// hydration mount 가드: SSR·하이드레이션 첫 렌더에서는 false(서버와 동일),
// mount 후에만 실제 localStorage 값을 반영해 hydration mismatch를 방지한다.
const subscribeNoop = () => () => {};

export function useTheme() {
  const mounted = useSyncExternalStore(subscribeNoop, () => true, () => false);

  const [dark, setDark] = useState<boolean>(() => {
    if (typeof window === "undefined") return false;
    const isDark = localStorage.getItem("theme") === "dark";
    if (isDark) document.documentElement.setAttribute("data-theme", "dark");
    return isDark;
  });

  function toggle() {
    const next = !dark;
    setDark(next);
    if (next) {
      document.documentElement.setAttribute("data-theme", "dark");
      localStorage.setItem("theme", "dark");
    } else {
      document.documentElement.removeAttribute("data-theme");
      localStorage.setItem("theme", "light");
    }
  }

  // mount 전(SSR·하이드레이션)에는 false를 반환해 서버 렌더와 일치시킨다.
  return { dark: mounted ? dark : false, toggle };
}
