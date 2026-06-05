import type { FinancialDocumentSearchRow } from "@/application/bms/financial-document/ports";
import { useTabs } from "@/lib/use-tabs";
import { getSession, hasMenuAccess } from "@/lib/admin-session";
import { toast } from "@/lib/toast-store";
import { listFilterStore } from "@/lib/use-list-filter-store";

// router.push 시그니처만 필요 — 컴포넌트 외부에서 호출하므로 최소 타입으로 선언
interface RouterLike {
  push(href: string): void;
}

interface DocTarget {
  href: string;
  menuCode: string;
}

function resolveDocTarget(documentType: string): DocTarget | null {
  switch (documentType) {
    case "INVOICE":
      return { href: "/bms/invoice/list", menuCode: "MENU_BMS_INVOICE" };
    case "PAYMENT":
      return { href: "/bms/payment/list", menuCode: "MENU_BMS_PAYMENT" };
    case "DEBIT":
    case "CREDIT":
      return { href: "/bms/dc-note/list", menuCode: "MENU_BMS_DC_NOTE" };
    default:
      return null;
  }
}

/**
 * BMS Document Quick Search 결과 선택 시 해당 List 화면으로 이동.
 * documentNo를 inject 슬롯에 넣어 List가 즉시 조회하도록 한다.
 * 권한 없으면 toast.error(noAccessMessage) 안내 후 중단.
 */
export function openBmsDocumentList(
  row: FinancialDocumentSearchRow,
  router: RouterLike,
  noAccessMessage: string,
): void {
  const target = resolveDocTarget(row.documentType);
  if (!target) {
    toast.error("Unsupported document type: " + row.documentType);
    return;
  }

  const session = getSession();
  if (!hasMenuAccess(session, target.menuCode)) {
    toast.error(noAccessMessage);
    return;
  }

  listFilterStore.getState().setInject(target.href, { documentNoLike: row.documentNo });
  useTabs.getState().addTab(target.href, target.href);
  router.push(target.href);
}
