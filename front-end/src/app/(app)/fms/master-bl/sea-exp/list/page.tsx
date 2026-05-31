import { getTranslations } from "next-intl/server";
import { Ship } from "lucide-react";
import { SeaMasterListClient } from "@/components/fms/sea-master/sea-master-list-client";

export default async function SeaMasterExpListPage() {
  const t = await getTranslations("fms.seaMaster.list");
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Ship size={14} /></div>
          {t("title.exp")}
        </div>
      </div>
      <SeaMasterListClient bound="EXP" />
    </div>
  );
}
