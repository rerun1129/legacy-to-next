/**
 * blKind + variantKey → { panelKey: 폼 필드 경로 배열 } 해석.
 *
 * Phase A: House SEA(sea-exp / sea-imp).
 * Phase B: House AIR(air-exp / air-imp) + Master SEA(sea-exp / sea-imp) + Master AIR(air-exp / air-imp).
 * Phase B-3: Truck (variant 없음 — variantKey 무시).
 * Phase B-4: Non-BL (variant 없음 — variantKey 무시).
 */
export type { PanelKey, PanelFieldsMap } from "./types";
import { HOUSE_SEA_PANEL_FIELDS } from "./house-sea";
import { HOUSE_AIR_PANEL_FIELDS } from "./house-air";
import { MASTER_SEA_PANEL_FIELDS } from "./master-sea";
import { MASTER_AIR_EXP_PANEL_FIELDS, MASTER_AIR_IMP_PANEL_FIELDS } from "./master-air";
import { TRUCK_PANEL_FIELDS } from "./truck";
import { NON_PANEL_FIELDS } from "./non";
import type { PanelFieldsMap } from "./types";

export function getPanelFieldsMap(
  blKind: "house" | "master" | "truck" | "non",
  variantKey: string,
): PanelFieldsMap {
  if (blKind === "house") {
    if (variantKey === "sea-exp" || variantKey === "sea-imp") return HOUSE_SEA_PANEL_FIELDS;
    if (variantKey === "air-exp" || variantKey === "air-imp") return HOUSE_AIR_PANEL_FIELDS;
    return {};
  }
  if (blKind === "master") {
    if (variantKey === "sea-exp" || variantKey === "sea-imp") return MASTER_SEA_PANEL_FIELDS;
    if (variantKey === "air-exp") return MASTER_AIR_EXP_PANEL_FIELDS;
    if (variantKey === "air-imp") return MASTER_AIR_IMP_PANEL_FIELDS;
    return {};
  }
  // Truck은 variant 없음 — variantKey 무시
  if (blKind === "truck") return TRUCK_PANEL_FIELDS;
  // Non-BL은 variant 없음 — variantKey 무시
  if (blKind === "non") return NON_PANEL_FIELDS;
  return {};
}
