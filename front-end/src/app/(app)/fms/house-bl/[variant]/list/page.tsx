import {FileText, RotateCcw, Search} from "lucide-react";
import { ListFilter } from "@/components/fms/house-bl/list-filter";
import { HouseBLListGrid } from "@/components/fms/house-bl/house-bl-list-grid";
import { getBLVariant, getPageTitle, BL_VARIANT_KEYS } from "@/lib/bl-variants";

export function generateStaticParams() {
  return BL_VARIANT_KEYS.map((v) => ({ variant: v }));
}

interface Props { params: Promise<{ variant: string }> }

export default async function HouseBLListPage({ params }: Props) {
  const { variant: variantKey } = await params;
  const variant = getBLVariant(variantKey);

  return (
    <div className="app__main--list" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><FileText size={14} /></div>
          {getPageTitle(variant, 'House', 'List')}
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
      <ListFilter />
      <HouseBLListGrid variantKey={variantKey} />
    </div>
  );
}
