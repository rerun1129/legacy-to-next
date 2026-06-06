import { getTranslations } from "next-intl/server";
import { FileSpreadsheet } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { FreightLineIssueListClient } from "@/components/bms/freight-line-issue/freight-line-issue-list-client";
import { SLIP_ISSUE_CONFIG } from "@/components/bms/freight-line-issue/freight-line-issue-list-config";

/** BS-E2 전표 발급 화면 */
export default async function SlipIssuePage() {
  const t = await getTranslations("bms.issue.title");
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_SLIP">
      <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><FileSpreadsheet size={14} /></div>
            {t("slip")}
          </div>
        </div>
        <FreightLineIssueListClient config={SLIP_ISSUE_CONFIG} />
      </div>
    </AdminGuard>
  );
}
