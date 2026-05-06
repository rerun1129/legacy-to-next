import { Plane } from "lucide-react";
import { AirMasterListClient } from "@/components/fms/air-master/air-master-list-client";

export default function AirMasterImpListPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Plane size={14} /></div>
          Master B/L Air Import List
        </div>
      </div>
      <AirMasterListClient bound="IMP" />
    </div>
  );
}
