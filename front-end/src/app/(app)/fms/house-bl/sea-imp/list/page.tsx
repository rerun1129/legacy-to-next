import { Ship } from "lucide-react";
import { SeaHouseListClient } from "@/components/fms/sea-house/sea-house-list-client";

export default function SeaImpListPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Ship size={14} /></div>
          Sea Import List
        </div>
      </div>
      <SeaHouseListClient bound="IMP" />
    </div>
  );
}
