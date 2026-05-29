import { useMemo } from "react";
import type { MenuRow } from "@/domain/access/menu";
import type { ButtonRow } from "@/domain/access/button";
import type { ButtonFilter } from "./button-list-filter";

/**
 * 버튼 클라이언트 필터 훅.
 * activeFilter 기준으로 버튼을 필터링하고,
 * 매칭 버튼이 있는 메뉴(+ 조상 메뉴)만 추출한다.
 */
export function useButtonFilter(
  buttonContent: ButtonRow[],
  menuContent: MenuRow[],
  activeFilter: ButtonFilter | null,
): { filteredButtons: ButtonRow[]; filteredMenus: MenuRow[] } {
  const filteredButtons = useMemo(() => {
    if (!activeFilter || buttonContent.length === 0) return [];
    const codeQ = activeFilter.buttonCode.trim().toLowerCase();
    const modQ = activeFilter.moduleCode;

    return buttonContent.filter((btn) => {
      if (codeQ && !btn.buttonCode.toLowerCase().includes(codeQ)) return false;
      if (activeFilter.status === "ACTIVE" && !btn.active) return false;
      if (activeFilter.status === "INACTIVE" && btn.active) return false;
      if (modQ) {
        const menu = menuContent.find((m) => m.id === btn.menuId);
        if (!menu || menu.moduleCode !== modQ) return false;
      }
      return true;
    });
  }, [buttonContent, menuContent, activeFilter]);

  const filteredMenus = useMemo(() => {
    if (!activeFilter || menuContent.length === 0) return [];
    const matchingMenuIds = new Set(filteredButtons.map((btn) => btn.menuId));

    // 매칭 버튼을 가진 메뉴와 그 조상 메뉴를 포함
    const includedIds = new Set<number>();
    function includeAncestors(menuId: number) {
      if (includedIds.has(menuId)) return;
      includedIds.add(menuId);
      const menu = menuContent.find((m) => m.id === menuId);
      if (menu?.parentId != null) includeAncestors(menu.parentId);
    }
    matchingMenuIds.forEach(includeAncestors);

    return menuContent.filter((m) => includedIds.has(m.id));
  }, [menuContent, activeFilter, filteredButtons]);

  return { filteredButtons, filteredMenus };
}
