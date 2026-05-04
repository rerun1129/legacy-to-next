import { createEmptyHouseBlFormValues } from "@/components/fms/house-bl/house-bl-defaults";
import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

export function createEmptyTruckBlFormValues(): HouseBlFormValues {
  return createEmptyHouseBlFormValues();
}
