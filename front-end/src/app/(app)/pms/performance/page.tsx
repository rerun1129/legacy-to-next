import { getTranslations } from "next-intl/server";
import { BarChart3 } from "lucide-react";
import { AdminGuard } from "@/components/admin/admin-guard";
import { PmsPerformanceListClient } from "@/components/pms/performance/pms-performance-list-client";

/** PS-01 실적 조회 화면 */
export default async function PmsPerformancePage() {
  const t = await getTranslations("pms.performance");
  return (
    <AdminGuard requiredMenuCode="MENU_PMS_PERFORMANCE">
      <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
        <div className="page-head">
          <div className="page-head__title">
            <div className="page-head__title-icon"><BarChart3 size={14} /></div>
            {t("title")}
          </div>
        </div>
        <PmsPerformanceListClient />
      </div>
    </AdminGuard>
  );
}
