import { getTranslations } from "next-intl/server";
import { Plane } from "lucide-react";
import { AirMasterListClient } from "@/components/fms/air-master/air-master-list-client";

export default async function AirMasterExpListPage() {
  const t = await getTranslations("fms.airMaster.list");
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Plane size={14} /></div>
          {t("title.exp")}
        </div>
      </div>
      <AirMasterListClient bound="EXP" />
    </div>
  );
}
