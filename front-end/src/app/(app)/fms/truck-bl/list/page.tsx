import { Truck } from "lucide-react";
import { TruckBlListClient } from "@/components/fms/truck-bl/truck-bl-list-client";

export default function TruckBLListPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Truck size={14} /></div>
          Truck B/L List
        </div>
      </div>
      <TruckBlListClient />
    </div>
  );
}
