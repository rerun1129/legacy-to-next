import { FileText, RotateCcw, Search } from "lucide-react";
import { getBLVariant, getPageTitle, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { HouseBLListClient } from "@/components/fms/house-bl/house-bl-list-client";

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
      </div>
      <HouseBLListClient variantKey={variantKey} />
    </div>
  );
}
