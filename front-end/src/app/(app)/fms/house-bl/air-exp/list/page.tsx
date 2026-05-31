import { getTranslations } from "next-intl/server";
import { Plane } from "lucide-react";
import { AirHouseListClient } from "@/components/fms/air-house/air-house-list-client";

export default async function AirExpListPage() {
  const t = await getTranslations("fms.airHouse.list");
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Plane size={14} /></div>
          {t("title.exp")}
        </div>
      </div>
      <AirHouseListClient bound="EXP" />
    </div>
  );
}
