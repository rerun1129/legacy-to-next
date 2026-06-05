import { getTranslations } from "next-intl/server";
import { Wallet } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { FinancialDocumentListClient } from "@/components/bms/financial-document/financial-document-list-client";
import { PAYMENT_LIST_CONFIG } from "@/components/bms/financial-document/financial-document-list-config";

/** BS-02 Payment 조회 리스트 */
export default async function PaymentListPage() {
  const t = await getTranslations("bms.list.title");
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_PAYMENT">
      <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Wallet size={14} /></div>
            {t("payment")}
          </div>
        </div>
        <FinancialDocumentListClient config={PAYMENT_LIST_CONFIG} />
      </div>
    </AdminGuard>
  );
}
