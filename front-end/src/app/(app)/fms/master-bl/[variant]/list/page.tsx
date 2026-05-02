import {Layers, RotateCcw, Search} from "lucide-react";
import { getMasterVariant, getPageTitle, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { ListFilter }   from "@/components/fms/house-bl/list-filter";
import { MasterBlGrid } from "@/components/fms/master-bl/master-bl-grid";

export function generateStaticParams() {
  return BL_VARIANT_KEYS.map((v) => ({ variant: v }));
}

interface Props { params: Promise<{ variant: string }> }

export default async function MasterBLListPage({ params }: Props) {
  const { variant: variantKey } = await params;
  const variant = getMasterVariant(variantKey);

  return (
    <div style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0, overflow: "hidden" }}>
      <div className="page-head">
        <div className="page-head__title">
          <div className="page-head__title-icon"><Layers size={14} /></div>
          {getPageTitle(variant, 'Master', 'List')}
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

      <div style={{ flex: 1, overflow: "auto", margin: "10px 14px 0", display: "flex", flexDirection: "column" }}>
        <MasterBlGrid variantKey={variantKey} variant={variant} />
      </div>
    </div>
  );
}
