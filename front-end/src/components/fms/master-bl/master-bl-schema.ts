import { z } from "zod";

const DATE8 = z.string().regex(/^\d{8}$/).optional().or(z.literal(""));

const DESC_SCHEMA = z.object({
  marks:        z.string().optional(),
  description:  z.string().optional(),
  descClause1:  z.string().optional(),
  descClause2:  z.string().optional(),
  remark:       z.string().optional(),
}).optional();

const DIM_SCHEMA = z.object({
  lengthCm:       z.number().min(0).optional(),
  widthCm:        z.number().min(0).optional(),
  heightCm:       z.number().min(0).optional(),
  quantity:       z.number().min(0).optional(),
  cbm:            z.number().min(0).optional(),
  volumeWeightKg: z.number().min(0).optional(),
});

const SCHEDULE_LEG_SCHEMA = z.object({
  toCode:    z.string().min(1),
  byCarrier: z.string().optional(),
  flightNo:  z.string().optional(),
  onBoardDt: z.string().regex(/^\d{8}$/),
  onBoardTm: z.string().optional(),
  arrivalDt: z.string().regex(/^\d{8}$/),
  arrivalTm: z.string().optional(),
});

const AIR_CHARGE_SCHEMA = z.object({
  freightCode:    z.string().optional(),
  currencyCode:   z.string().optional(),
  per:            z.string().optional(),
  freightTerm:    z.string().optional(),
  grossWeightKg:  z.number().min(0).optional(),
  rateClass:      z.string().optional(),
  chargeWeightKg: z.number().min(0).optional(),
  rate:           z.number().min(0).optional(),
});

const HOUSE_BL_REF_SCHEMA = z.object({
  id:         z.number(),
  hblNo:      z.string().optional(),
  shipper:    z.string().optional(),
  consignee:  z.string().optional(),
  pkg:        z.string().optional(),
  gw:         z.string().optional(),
  cbm:        z.string().optional(),
  status:     z.string().optional(),
});

// freightTerm 값이 null일 수 있으므로 detail reset 시 기본값 fallback 처리
export const MASTER_BL_SCHEMA = z.object({
  jobDiv:       z.enum(["SEA", "AIR", "TRUCK", "NON_BL"]),
  bound:        z.enum(["EXP", "IMP"]),
  mblNo:        z.string().max(35).optional(),
  masterRefNo:  z.string().max(35).optional(),
  freightTerm:  z.enum(["", "PREPAID", "COLLECT"]),

  shipperCode:      z.string().max(20).optional(),
  shipperAddress:   z.string().optional(),
  consigneeCode:    z.string().max(20).optional(),
  consigneeAddress: z.string().optional(),
  notifyCode:       z.string().max(20).optional(),
  notifyAddress:    z.string().optional(),

  polCode: z.string().max(5).optional(),
  podCode: z.string().max(5).optional(),
  etd:     DATE8,
  eta:     DATE8,

  pkgQty:           z.number().min(0).optional(),
  pkgUnit:          z.string().optional(),
  weightUnit:       z.string().optional(),
  grossWeightKg:    z.number().min(0).optional(),
  cbm:              z.number().min(0).optional(),
  hsCode:           z.string().optional(),
  mainItemName:     z.string().optional(),
  settlePartnerCode: z.string().optional(),
  operatorCode:     z.string().optional(),

  // Coder 추가 필드
  shipmentType:   z.string().optional(),
  mblStatus:      z.string().optional(),
  cargoMainItem:  z.string().optional(),
  cargoHsCode:    z.string().optional(),
  rTon:           z.number().min(0).optional(),
  volWeight:      z.number().min(0).optional(),
  chargeWeight:   z.number().min(0).optional(),
  rateClass:      z.string().optional(),
  settlePartner:  z.string().optional(),
  coLoadAgent:    z.string().optional(),
  coLoadType:     z.string().optional(),
  flightType:     z.string().optional(),
  securityStatus: z.string().optional(),
  teamCode:       z.string().optional(),

  seaDetail: z.object({
    loadType:          z.string().optional(),
    linerCode:         z.string().optional(),
    linerName:         z.string().optional(),
    vesselCode:        z.string().optional(),
    vesselName:        z.string().optional(),
    voyageNo:          z.string().optional(),
    onboardDate:       DATE8,
    vesselNationality: z.string().optional(),
    serviceTerm:       z.string().optional(),
    blType:            z.string().optional(),
    porCode:           z.string().max(5).optional(),
    finalDestCode:     z.string().max(5).optional(),
    rton:              z.number().min(0).optional(),
    lineBkgNo:         z.string().max(35).optional(),
    issueDate:         DATE8,
    freightTermDetail: z.string().optional(),
    polName:           z.string().optional(),
    podName:           z.string().optional(),
    deliveryCode:      z.string().optional(),
    deliveryName:      z.string().optional(),
    signature:         z.string().optional(),
    issuePlace:        z.string().optional(),
  }).optional(),

  desc:         DESC_SCHEMA,
  dims:         z.array(DIM_SCHEMA).optional(),
  scheduleLegs: z.array(SCHEDULE_LEG_SCHEMA).optional(),
  airCharges:   z.array(AIR_CHARGE_SCHEMA).optional(),
  houseBls:     z.array(HOUSE_BL_REF_SCHEMA).optional(),
});

export type MasterBlFormValues = z.infer<typeof MASTER_BL_SCHEMA>;

// toolbar 라벨 → RHF field path 매핑
export const TOOLBAR_TO_FIELD: Partial<Record<string, string>> = {
  "Master Ref":    "masterRefNo",
  "MBL No":        "mblNo",
  "MAWB No":       "mblNo",
  "Line Bkg. No":  "seaDetail.lineBkgNo",
  "Load Type":     "seaDetail.loadType",
  "Service Term":  "seaDetail.serviceTerm",
  "B/L Type":      "seaDetail.blType",
  "Shipment Type": "shipmentType",
  "Status":        "mblStatus",
};

export function createEmptyMasterBlFormValues(): MasterBlFormValues {
  return {
  jobDiv: "SEA",
  bound: "EXP",
  freightTerm: "",
  mblNo: "",
  masterRefNo: "",
  shipperCode: "",
  shipperAddress: "",
  consigneeCode: "",
  consigneeAddress: "",
  notifyCode: "",
  notifyAddress: "",
  polCode: "",
  podCode: "",
  etd: "",
  eta: "",
  pkgUnit: "",
  weightUnit: "",
  hsCode: "",
  mainItemName: "",
  settlePartnerCode: "",
  operatorCode: "",
  shipmentType: "",
  mblStatus: "",
  cargoMainItem: "",
  cargoHsCode: "",
  rateClass: "",
  settlePartner: "",
  coLoadAgent: "",
  coLoadType: "",
  flightType: "",
  securityStatus: "",
  teamCode: "",
  seaDetail: {
    loadType: "",
    linerCode: "",
    linerName: "",
    vesselCode: "",
    vesselName: "",
    voyageNo: "",
    onboardDate: "",
    vesselNationality: "",
    serviceTerm: "",
    blType: "",
    porCode: "",
    finalDestCode: "",
    lineBkgNo: "",
    issueDate: "",
    freightTermDetail: "",
    polName: "",
    podName: "",
    deliveryCode: "",
    deliveryName: "",
    signature: "",
    issuePlace: "",
  },
  desc: {
    marks: "",
    description: "",
    descClause1: "",
    descClause2: "",
    remark: "",
  },
  dims: [],
  scheduleLegs: [],
  airCharges: [],
  houseBls: [],
  };
}

export const MASTER_BL_DEFAULT_VALUES: MasterBlFormValues = createEmptyMasterBlFormValues();
