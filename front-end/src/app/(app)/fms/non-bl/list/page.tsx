import {Package, RotateCcw, Search} from "lucide-react";
import { NonBlGrid } from "@/components/fms/non-bl/non-bl-grid";
import { NonBlListFilter } from "@/components/fms/non-bl/non-bl-list-filter";

export default function NonBLListPage() {
  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Package size={14} /></div>
          Non B/L List
        </div>
        <div className="page-head__actions">
            <div style={{ display: "flex", justifyContent: "flex-end", gap: 8, marginTop: 12 }}>
                <button className="btn btn--sm btn--ghost">
                    <RotateCcw size={12} />
                    Reset
                </button>
                <button className="btn btn--sm btn--primary">
                    <Search size={12} />
                    Search
                </button>
            </div>
        </div>
      </div>

      <NonBlListFilter />

      <div style={{ flex: 1, overflow: "auto", margin: "10px 14px 0", display: "flex", flexDirection: "column" }}>
        <NonBlGrid />
      </div>
    </div>
  );
}
