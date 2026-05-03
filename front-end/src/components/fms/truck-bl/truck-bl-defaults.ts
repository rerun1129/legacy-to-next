import type { HouseBlFormValues } from "@/components/fms/house-bl/house-bl-schema";

export function createEmptyTruckBlFormValues(): Partial<HouseBlFormValues> {
  return {
    freightSelling: [],
    freightBuying: [],
    truckOrders: [],
    truckBlNo:        "",
    truckSettle:      "",
    incoterms:        "",
    truckFreightTerm: "",
    truckStatus:      "",
  };
}
