import { getTranslations } from "next-intl/server";
import { FileText } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { FinancialDocumentListClient } from "@/components/bms/financial-document/financial-document-list-client";
import { DC_NOTE_LIST_CONFIG } from "@/components/bms/financial-document/financial-document-list-config";

/** BS-03 D/C Note 조회 리스트 (Debit + Credit 합본) */
export default async function DcNoteListPage() {
  const t = await getTranslations("bms.list.title");
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_DC_NOTE">
      <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><FileText size={14} /></div>
            {t("dcNote")}
          </div>
        </div>
        <FinancialDocumentListClient config={DC_NOTE_LIST_CONFIG} />
      </div>
    </AdminGuard>
  );
}
