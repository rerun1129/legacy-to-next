import { z } from "zod";

export const NON_BL_SCHEMA = z.object({
  // 툴바
  nonBlNo:  z.string().optional(),
  workDiv:  z.string().optional(),
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
  cargoQty:  z.number().optional(),
  cargoUnit: z.string().optional(),
  grossWt:   z.number().optional(),
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

  // Remark
  remark: z.string().optional(),

  // Freight (FreightTab 공유용)
  freightSelling: z.array(z.any()).optional(),
  freightBuying:  z.array(z.any()).optional(),
});

export type NonBlFormValues = z.infer<typeof NON_BL_SCHEMA>;
