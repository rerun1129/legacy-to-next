import { getBLVariant, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { HouseBLEntry } from "@/components/fms/house-bl/house-bl-entry";

export function generateStaticParams() {
  return BL_VARIANT_KEYS.map((v) => ({ variant: v }));
}

interface Props { params: Promise<{ variant: string }> }

export default async function HouseBLEntryPage({ params }: Props) {
  const { variant: variantKey } = await params;
  const variant = getBLVariant(variantKey);
  return <HouseBLEntry variant={variant} />;
}
