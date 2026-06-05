import { getTranslations } from "next-intl/server";
import { ReceiptText } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { FinancialDocumentListClient } from "@/components/bms/financial-document/financial-document-list-client";
import { INVOICE_LIST_CONFIG } from "@/components/bms/financial-document/financial-document-list-config";

/** BS-01 Invoice 조회 리스트 */
export default async function InvoiceListPage() {
  const t = await getTranslations("bms.list.title");
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_INVOICE">
      <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><ReceiptText size={14} /></div>
            {t("invoice")}
          </div>
        </div>
        <FinancialDocumentListClient config={INVOICE_LIST_CONFIG} />
      </div>
    </AdminGuard>
  );
}
