import { z } from "zod";
import { FREIGHT_ROW_SCHEMA } from "@/components/fms/house-bl/house-bl-schema";

const DATE8 = z.string().regex(/^\d{8}$/).or(z.literal('')).optional();

// ── Dimension 그리드 행 ────────────────────────────────────
export const TRUCK_DIM_SCHEMA = z.object({
  // validation은 BE SSOT — Copy 신규 행은 id 없이 BE INSERT, 기존 행은 id로 BE merge
  id: z.number().optional(),
  length: z.string().optional(),
  width:  z.string().optional(),
  height: z.string().optional(),
  qty:    z.string().optional(),
  cbm:    z.string().optional(),
  volWt:  z.string().optional(),
});

// ── 자식 그리드 행 ─────────────────────────────────────────
export const TRUCK_ORDER_SCHEMA = z.object({
  // DB 식별자 — Update 시 BE merge-by-id 에 사용 (§6.28)
  id:            z.number().optional(),
  truckOrderNo:  z.string().optional(),
  pkgQty:        z.string().optional(),
  pkgUnit:       z.string().optional(),
  grossWeightKg: z.string().optional(),
  cbm:           z.string().optional(),
  truckNo:       z.string().optional(),
  truckType:     z.string().optional(),
  driver:        z.string().optional(),
  mobileNo:      z.string().optional(),
  containerNo:   z.string().optional(),
  containerType: z.string().optional(),
  sealNo1:       z.string().optional(),
  sealNo2:       z.string().optional(),
  sealNo3:       z.string().optional(),
});

// ── Truck B/L 메인 스키마 ─────────────────────────────────
// validation은 BE SSOT — 모든 필드 optional (§6.25)
export const TRUCK_BL_SCHEMA = z.object({
  // 툴바
  truckBlNo:   z.string().optional(),
  bound:       z.string().optional(),
  loadType:    z.string().optional(),
  serviceTerm: z.string().optional(),

  // Party
  shipperCode:      z.string().optional(),
  shipperName:      z.string().optional(),
  shipperAddr:      z.string().optional(),
  consigneeCode:    z.string().optional(),
  consigneeName:    z.string().optional(),
  consigneeAddr:    z.string().optional(),
  notifyCode:       z.string().optional(),
  notifyName:       z.string().optional(),
  notifyAddr:       z.string().optional(),
  docPartnerCode:   z.string().optional(),
  docPartnerName:   z.string().optional(),
  docPartnerAddress: z.string().optional(),

  // Schedule
  vesselName: z.string().optional(),
  voyNo:      z.string().optional(),
  etd:        z.string().optional(),
  eta:        z.string().optional(),
  polCode:    z.string().optional(),
  polName:    z.string().optional(),
  podCode:    z.string().optional(),
  podName:    z.string().optional(),

  // Cargo — 수치 필드는 number, 단위는 string
  pkgQty:         z.number().optional(),
  pkgUnit:        z.string().optional(),
  weightUnit:     z.string().optional(),
  grossWeightKg:  z.number().optional(),
  cbm:            z.number().optional(),
  chargeWeightKg: z.number().optional(),
  hsCode:         z.string().optional(),
  hsCodeName:     z.string().optional(),

  // Document
  pickupDate:  z.string().optional(),
  truckerCode: z.string().optional(),
  truckerName: z.string().optional(),
  truckerPic:  z.string().optional(),

  // Performance
  actualCustomerCode: z.string().optional(),
  actualCustomerName: z.string().optional(),
  settlePartnerCode:  z.string().optional(),
  settlePartnerName:  z.string().optional(),
  salesManCode:       z.string().optional(),
  salesManName:       z.string().optional(),
  operatorCode:       z.string().optional(),
  operatorName:       z.string().optional(),
  teamCode:           z.string().optional(),
  teamName:           z.string().optional(),

  // Marks & Description
  marks:        z.string().optional(),
  description:  z.string().optional(),
  descClause1:  z.string().optional(),
  descClause2:  z.string().optional(),
  remark:       z.string().optional(),

  // Freight (FreightTab 공유용)
  freightSelling: z.array(FREIGHT_ROW_SCHEMA).optional(),
  freightBuying:  z.array(FREIGHT_ROW_SCHEMA).optional(),
  sellRateDt:           DATE8,
  sellRateCurrencyCode: z.string().optional(),
  sellRate:             z.string().optional(),
  buyRateDt:            DATE8,
  buyRateCurrencyCode:  z.string().optional(),
  buyRate:              z.string().optional(),
  usdRateDt:            DATE8,
  usdRate:              z.string().optional(),

  // Dimension 단위 (form schema로 승격)
  dimensionDivisor: z.string().optional(),

  // 자식 그리드
  truckOrders: z.array(TRUCK_ORDER_SCHEMA).optional(),
  dimensions:  z.array(TRUCK_DIM_SCHEMA).optional(),
});

export type TruckBlFormValues = z.infer<typeof TRUCK_BL_SCHEMA>;

export const EMPTY_TRUCK_DIM_ROW: Omit<z.infer<typeof TRUCK_DIM_SCHEMA>, "id"> = {
  length: "", width: "", height: "", qty: "", cbm: "", volWt: "",
};

export const EMPTY_TRUCK_ORDER_ROW: z.infer<typeof TRUCK_ORDER_SCHEMA> = {
  truckOrderNo:  "",
  pkgQty:        "",
  pkgUnit:       "",
  grossWeightKg: "",
  cbm:           "",
  truckNo:       "",
  truckType:     "",
  driver:        "",
  mobileNo:      "",
  containerNo:   "",
  containerType: "",
  sealNo1:       "",
  sealNo2:       "",
  sealNo3:       "",
};
