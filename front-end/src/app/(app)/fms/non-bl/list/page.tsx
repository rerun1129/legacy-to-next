import { Package } from "lucide-react";
import { NonBlListClient } from "@/components/fms/non-bl/non-bl-list-client";

export default function NonBLListPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L List
        </div>
      </div>
      <NonBlListClient />
    </div>
  );
}
