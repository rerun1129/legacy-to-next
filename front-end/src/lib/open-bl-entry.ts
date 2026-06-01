import type { BlQuickSearchItem } from "@/domain/bl-quick-search";
import { entryFocusKeys, useEntryFocusStore } from "@/lib/use-entry-focus-store";
import { useTabs } from "@/lib/use-tabs";
import { getSession, hasMenuAccess } from "@/lib/admin-session";
import { toast } from "@/lib/toast-store";

// EntryDomain은 use-entry-focus-store 내 union 타입과 동일하게 선언 (export 미제공이므로 로컬 선언)
type EntryDomain = "nonBl" | "truckBl" | `houseBl:${string}` | `masterBl:${string}`;

// router.push 시그니처만 필요 — 컴포넌트 외부에서 호출하므로 최소 타입으로 선언
interface RouterLike {
  push(href: string): void;
}

interface EntryTarget {
  href: string;
  focusDomain: EntryDomain;
  menuCode: string;
}

function resolveTarget(item: BlQuickSearchItem): EntryTarget | null {
  const bound = item.bound === "EXP" ? "exp" : "imp";

  if (item.blType === "HOUSE") {
    if (item.jobDiv === "SEA") {
      const variant = `sea-${bound}`;
      return {
        href: `/fms/house-bl/${variant}/entry`,
        focusDomain: entryFocusKeys.houseBl(variant),
        menuCode: `MENU_FMS_HOUSE_BL_SEA_${item.bound}_ENTRY`,
      };
    }
    if (item.jobDiv === "AIR") {
      const variant = `air-${bound}`;
      return {
        href: `/fms/house-bl/${variant}/entry`,
        focusDomain: entryFocusKeys.houseBl(variant),
        menuCode: `MENU_FMS_HOUSE_BL_AIR_${item.bound}_ENTRY`,
      };
    }
    if (item.jobDiv === "TRUCK") {
      return {
        href: "/fms/truck-bl/entry",
        focusDomain: entryFocusKeys.truckBl,
        menuCode: "MENU_FMS_TRUCK_BL_ENTRY",
      };
    }
    if (item.jobDiv === "NON_BL") {
      return {
        href: "/fms/non-bl/entry",
        focusDomain: entryFocusKeys.nonBl,
        menuCode: "MENU_FMS_NON_BL_ENTRY",
      };
    }
  }

  if (item.blType === "MASTER") {
    if (item.jobDiv === "SEA") {
      const variant = `sea-${bound}`;
      return {
        href: `/fms/master-bl/${variant}/entry`,
        focusDomain: entryFocusKeys.masterBl(variant),
        menuCode: `MENU_FMS_MASTER_BL_SEA_${item.bound}_ENTRY`,
      };
    }
    if (item.jobDiv === "AIR") {
      const variant = `air-${bound}`;
      return {
        href: `/fms/master-bl/${variant}/entry`,
        focusDomain: entryFocusKeys.masterBl(variant),
        menuCode: `MENU_FMS_MASTER_BL_AIR_${item.bound}_ENTRY`,
      };
    }
  }

  return null;
}

/**
 * B/L Quick Search 결과 선택 시 해당 Entry 화면으로 이동.
 * 권한 없으면 toast.error(noAccessMessage) 안내 후 중단.
 * noAccessMessage를 호출자에서 주입받아 하드코딩 영문 메시지를 제거함.
 */
export function openBlEntry(item: BlQuickSearchItem, router: RouterLike, noAccessMessage: string): void {
  const target = resolveTarget(item);
  if (!target) {
    toast.error(`Unsupported B/L type: blType=${item.blType}, jobDiv=${item.jobDiv}`);
    return;
  }

  const session = getSession();
  if (!hasMenuAccess(session, target.menuCode)) {
    toast.error(noAccessMessage);
    return;
  }

  useEntryFocusStore.getState().setFocus(target.focusDomain, item.id);
  useTabs.getState().addTab(target.href, target.href);
  router.push(target.href);
}
