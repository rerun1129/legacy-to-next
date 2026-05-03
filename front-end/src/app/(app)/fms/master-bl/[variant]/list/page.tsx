import { Layers } from "lucide-react";
import { getMasterVariant, getPageTitle, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { MasterBLListClient } from "@/components/fms/master-bl/master-bl-list-client";

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
      </div>
      <MasterBLListClient variantKey={variantKey} variant={variant} />
    </div>
  );
}
