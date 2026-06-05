import { AdminGuard } from "@/components/admin/admin-guard";
import { FinancialDocumentListClient } from "@/components/bms/financial-document/financial-document-list-client";
import { INVOICE_LIST_CONFIG } from "@/components/bms/financial-document/financial-document-list-config";

/** BS-01 Invoice 조회 리스트 */
export default function InvoiceListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_INVOICE">
      <FinancialDocumentListClient config={INVOICE_LIST_CONFIG} />
    </AdminGuard>
  );
}
