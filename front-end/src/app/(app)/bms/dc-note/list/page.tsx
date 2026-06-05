import { AdminGuard } from "@/components/admin/admin-guard";
import { FinancialDocumentListClient } from "@/components/bms/financial-document/financial-document-list-client";
import { DC_NOTE_LIST_CONFIG } from "@/components/bms/financial-document/financial-document-list-config";

/** BS-03 D/C Note 조회 리스트 (Debit + Credit 합본) */
export default function DcNoteListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_DC_NOTE">
      <FinancialDocumentListClient config={DC_NOTE_LIST_CONFIG} />
    </AdminGuard>
  );
}
