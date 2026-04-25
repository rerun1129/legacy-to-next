import { Download, Layers } from "lucide-react";
import { getMasterVariant, BL_VARIANT_KEYS } from "@/lib/bl-variants";
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
          Master B/L List
        </div>
        <div className="page-head__actions">
          <button className="btn btn--sm"><Download size={12} />Export</button>
        </div>
      </div>

      <ListFilter />

      <div style={{ flex: 1, overflow: "auto", margin: "10px 14px 0", display: "flex", flexDirection: "column" }}>
        <MasterBlGrid variantKey={variantKey} isSea={variant.mode === "SEA"} />
      </div>

      <div className="footbar">
        <span style={{ color: "var(--ink-4)", fontSize: "var(--fs-xs)" }}>MBL No 더블클릭 → Entry</span>
        <span style={{ marginLeft: "auto" }}>2 records</span>
      </div>
    </div>
  );
}
