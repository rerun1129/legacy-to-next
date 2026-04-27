"use client";
import { useState } from "react";

export function useTheme() {
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

  return { dark, toggle };
}
