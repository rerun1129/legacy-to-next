import { useEnumOptions } from "@/application/enums/use-enum";

// Sea House list-filter에서 사용하는 enum 4종 통합 로딩.
// air-house-list-filter 등 재사용 가능.
export function useSeaHouseEnums() {
  const shipmentType = useEnumOptions("ShipmentType");
  const salesClass   = useEnumOptions("SalesClass");
  const incoterms    = useEnumOptions("Incoterms");
  const loadType     = useEnumOptions("LoadType");
  return { shipmentType, salesClass, incoterms, loadType };
}
