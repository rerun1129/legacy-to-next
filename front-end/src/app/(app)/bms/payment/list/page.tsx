import { AdminGuard } from "@/components/admin/admin-guard";
import { FinancialDocumentListClient } from "@/components/bms/financial-document/financial-document-list-client";
import { PAYMENT_LIST_CONFIG } from "@/components/bms/financial-document/financial-document-list-config";

/** BS-02 Payment 조회 리스트 */
export default function PaymentListPage() {
  return (
    <AdminGuard requiredMenuCode="MENU_BMS_PAYMENT">
      <FinancialDocumentListClient config={PAYMENT_LIST_CONFIG} />
    </AdminGuard>
  );
}
