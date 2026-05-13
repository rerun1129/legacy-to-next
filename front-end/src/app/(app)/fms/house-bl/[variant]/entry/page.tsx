import { Suspense } from "react";
import { getBLVariant, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { HouseBLEntry } from "@/components/fms/house-bl/house-bl-entry";

export function generateStaticParams() {
  return BL_VARIANT_KEYS.map((v) => ({ variant: v }));
}

interface Props {
  params: Promise<{ variant: string }>;
}

export default async function HouseBLEntryPage({ params }: Props) {
  const { variant: variantKey } = await params;
  // 유효하지 않은 key는 getBLVariant가 sea-exp로 fallback 처리
  const variant = getBLVariant(variantKey);
  return (
    <Suspense fallback={<div>Loading…</div>}>
      <HouseBLEntry variant={variant} />
    </Suspense>
  );
}
