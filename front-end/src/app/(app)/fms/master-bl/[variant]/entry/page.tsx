import { getMasterVariant, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { MasterBLEntry } from "@/components/fms/master-bl/master-bl-entry";

export function generateStaticParams() {
  return BL_VARIANT_KEYS.map((v) => ({ variant: v }));
}

type Props = {
  params: Promise<{ variant: string }>;
};

export default async function MasterBLEntryPage({ params }: Props) {
  const { variant: variantKey } = await params;
  getMasterVariant(variantKey); // variant 유효성 사전 검증 (invalid key는 fallback)
  return <MasterBLEntry variantKey={variantKey} />;
}
