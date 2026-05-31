import { getTranslations } from "next-intl/server";
import { Truck } from "lucide-react";
import { TruckBlListClient } from "@/components/fms/truck-bl/truck-bl-list-client";

export default async function TruckBLListPage() {
  const t = await getTranslations("fms.truckBl.list");
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Truck size={14} /></div>
          {t("title")}
        </div>
      </div>
      <TruckBlListClient />
    </div>
  );
}
