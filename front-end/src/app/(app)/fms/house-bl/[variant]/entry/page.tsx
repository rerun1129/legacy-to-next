import { getBLVariant, BL_VARIANT_KEYS } from "@/lib/bl-variants";
import { HouseBLEntry } from "@/components/fms/house-bl/house-bl-entry";

export function generateStaticParams() {
  return BL_VARIANT_KEYS.map((v) => ({ variant: v }));
}

interface Props {
  params: Promise<{ variant: string }>;
  searchParams: Promise<{ id?: string }>;
}

export default async function HouseBLEntryPage({ params, searchParams }: Props) {
  const { variant: variantKey } = await params;
  const { id: idStr } = await searchParams;
  // 유효하지 않은 key는 getBLVariant가 sea-exp로 fallback 처리
  const variant = getBLVariant(variantKey);
  const id = idStr ? Number(idStr) : undefined;
  return <HouseBLEntry variant={variant} id={id} />;
}
