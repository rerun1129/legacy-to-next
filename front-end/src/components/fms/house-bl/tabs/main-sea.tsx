import type { BLVariantConfig } from "@/lib/bl-variants";
import { WidgetGrid }      from "@/components/widget/widget-grid";
import { WIDGET_REGISTRY } from "@/components/widget/widget-registry";
import { TruckOrderPanel } from "@/components/fms/house-bl/panels/truck-order-panel";

interface Props { variant: BLVariantConfig }

export function MainTabSea({ variant }: Props) {
  // TRUCK 전용: 트럭오더 그리드만 렌더링
  if (variant.mode === "TRUCK") {
    return (
      <div className="tab-content" style={{ padding: 8 }}>
        <TruckOrderPanel />
      </div>
    );
  }

  // NON_BL: 기본 SEA 위젯 레이아웃 사용 (컨테이너 불필요이나 기존 위젯 재활용)
  const scope = `house-bl-entry.main.${variant.key}`;
  return <WidgetGrid scope={scope} variant={variant} registry={WIDGET_REGISTRY} />;
}
