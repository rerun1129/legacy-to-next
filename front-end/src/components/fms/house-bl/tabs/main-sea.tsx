import type { BLVariantConfig } from "@/lib/bl-variants";
import { WidgetGrid } from "@/components/widget/widget-grid";

interface Props { variant: BLVariantConfig }

export function MainTabSea({ variant }: Props) {
  const scope = `house-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} />;
}
