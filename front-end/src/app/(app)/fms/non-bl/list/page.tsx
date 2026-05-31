import { getTranslations } from "next-intl/server";
import { Package } from "lucide-react";
import { NonBlListClient } from "@/components/fms/non-bl/non-bl-list-client";

export default async function NonBLListPage() {
  const t = await getTranslations("fms.nonBl.list");
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          {t("title")}
        </div>
      </div>
      <NonBlListClient />
    </div>
  );
}
