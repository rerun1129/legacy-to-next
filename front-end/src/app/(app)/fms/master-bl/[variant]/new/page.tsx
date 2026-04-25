import { getMasterVariant, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { MasterBLEntry } from "@/components/fms/master-bl/master-bl-entry";

export function generateStaticParams() {
  return BL_VARIANT_KEYS.map((v) => ({ variant: v }));
}

interface Props { params: Promise<{ variant: string }> }

export default async function MasterBLEntryPage({ params }: Props) {
  const { variant: variantKey } = await params;
  const variant = getMasterVariant(variantKey);
  return <MasterBLEntry variant={variant} />;
}
