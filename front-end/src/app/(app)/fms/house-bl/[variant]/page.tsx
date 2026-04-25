import { Download, FileText, Columns3 } from "lucide-react";
import { ListFilter } from "@/components/fms/house-bl/list-filter";
import { ListGrid }   from "@/components/fms/house-bl/list-grid";
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
          <button className="btn btn--sm"><Columns3 size={12} />Columns</button>
          <button className="btn btn--sm"><Download size={12} />Export</button>
        </div>
      </div>

      <ListFilter />
      <ListGrid variantKey={variantKey} />

      <div className="footbar">
        <div className="footbar__shortcuts">
          <span className="footbar__shortcut"><kbd className="kbd">⌘E</kbd> Export</span>
          <span className="footbar__shortcut" style={{ color: "var(--ink-4)" }}>HBL No 더블클릭 → Entry</span>
        </div>
        <span>8 records</span>
      </div>
    </div>
  );
}
