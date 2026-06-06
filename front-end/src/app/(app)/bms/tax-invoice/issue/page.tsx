import { getTranslations } from "next-intl/server";
import { Receipt } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { FreightLineIssueListClient } from "@/components/bms/freight-line-issue/freight-line-issue-list-client";
import { TAX_INVOICE_ISSUE_CONFIG } from "@/components/bms/freight-line-issue/freight-line-issue-list-config";

/** BS-E1 세금계산서 발급 화면 */
export default async function TaxInvoiceIssuePage() {
  const t = await getTranslations("bms.issue.title");
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_TAX_INVOICE">
      <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><Receipt size={14} /></div>
            {t("taxInvoice")}
          </div>
        </div>
        <FreightLineIssueListClient config={TAX_INVOICE_ISSUE_CONFIG} />
      </div>
    </AdminGuard>
  );
}
