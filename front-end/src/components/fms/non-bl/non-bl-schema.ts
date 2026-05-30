import { z } from "zod";

export const CONTAINER_INFO_SCHEMA = z.object({
  id: z.number(),
  cno: z.string().optional(),
  contType: z.string().optional(),
  sealNo1: z.string().optional(),
  sealNo2: z.string().optional(),
  sealNo3: z.string().optional(),
  pkg: z.number().optional(),
  pkgUnit: z.string().optional(),
  grossWt: z.number().optional(),
  cbm: z.number().optional(),
});

export const DIM_SCHEMA = z.object({
  id: z.number(),
  length: z.string().optional(),
  width: z.string().optional(),
  height: z.string().optional(),
  qty: z.string().optional(),
  cbm: z.string().optional(),
  volWt: z.string().optional(),
});

export const NON_BL_SCHEMA = z.object({
  // 툴바
  nonBlNo:  z.string().optional(),
  workDiv:  z.string().optional(),
  bound:    z.string().optional(),
  status:   z.string().optional(),
  refNo:    z.string().optional(),

  // Party
  actualCustomerCode: z.string().optional(),
  actualCustomerName: z.string().optional(),
  shipperCode:        z.string().optional(),
  shipperName:        z.string().optional(),
  consigneeCode:      z.string().optional(),
  consigneeName:      z.string().optional(),
  notifyCode:         z.string().optional(),
  notifyName:         z.string().optional(),
  settlePartnerCode:  z.string().optional(),
  settlePartnerName:  z.string().optional(),

  // Schedule
  linerCode:     z.string().optional(),
  linerName:     z.string().optional(),
  vesselName:    z.string().optional(),
  voyNo:         z.string().optional(),
  etd:           z.string().optional(),
  eta:           z.string().optional(),
  polCode:       z.string().optional(),
  polName:       z.string().optional(),
  podCode:       z.string().optional(),
  podName:       z.string().optional(),
  finalDestCode: z.string().optional(),
  finalDestName: z.string().optional(),
  finalEta:      z.string().optional(),

  // Cargo
  mainItem:  z.string().optional(),
  hsCode:    z.string().optional(),
  hsCodeName: z.string().optional(),
  cargoQty:   z.number().optional(),
  pkgUnit:    z.string().optional(),
  weightUnit: z.string().optional(),
  grossWt:    z.number().optional(),
  volWt:     z.number().optional(),
  totalCbm:  z.number().optional(),
  rton:      z.number().optional(),

  // Document
  salesClass:    z.string().optional(),
  salesManCode:  z.string().optional(),
  salesManName:  z.string().optional(),
  operatorCode:  z.string().optional(),
  operatorName:  z.string().optional(),
  teamCode:      z.string().optional(),
  teamName:      z.string().optional(),

  // Dimension 단위 (form schema로 승격)
  dimensionDivisor: z.string().optional(),

  // Remark
  remark: z.string().optional(),

  // Freight (FreightTab 공유용)
  freightSelling: z.array(z.any()).optional(),
  freightBuying:  z.array(z.any()).optional(),

  // Container & Dimension grids
  containers: z.array(CONTAINER_INFO_SCHEMA).optional(),
  dimensions: z.array(DIM_SCHEMA).optional(),
});

export type NonBlFormValues = z.infer<typeof NON_BL_SCHEMA>;

export const EMPTY_CONTAINER_ROW: Omit<z.infer<typeof CONTAINER_INFO_SCHEMA>, "id"> = {
  cno: "", contType: "", sealNo1: "", sealNo2: "", sealNo3: "",
  pkg: 0, pkgUnit: "", grossWt: 0, cbm: 0,
};

export const EMPTY_DIM_ROW: Omit<z.infer<typeof DIM_SCHEMA>, "id"> = {
  length: "", width: "", height: "", qty: "", cbm: "", volWt: "",
};
