import { z } from "zod";

const DATE8 = z.string().regex(/^\d{8}$/).optional().or(z.literal(""));

// §BE 보강 — desc root 승격 (seaDetail.desc 제거, root desc로 통일)
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

// §BE-sync — ConsoledSeaContainerView 전체 필드 정합 (표시 전용, zodResolver 미사용 — BE SSOT)
// BigDecimal(grossWeightKg/cbm/vgmKg)은 BE Jackson 기본 직렬화 → number
const CONSOLED_SEA_CONTAINER_SCHEMA = z.object({
  houseBlId:     z.number(),
  containerNo:   z.string().nullable().optional(),
  containerType: z.string().nullable().optional(),
  sealNo1:       z.string().nullable().optional(),
  sealNo2:       z.string().nullable().optional(),
  sealNo3:       z.string().nullable().optional(),
  pkgQty:        z.number().nullable().optional(),
  pkgUnit:       z.string().nullable().optional(),
  grossWeightKg: z.number().nullable().optional(),
  cbm:           z.number().nullable().optional(),
  vgmKg:         z.number().nullable().optional(),
});

// §BE-sync — ConsoledHouseBlSummaryView 전체 필드 정합 (houseBlId 키 통일, pkgQty=number)
// BigDecimal(grossWeightKg/cbm)은 BE Jackson 기본 직렬화 → number
const HOUSE_BL_REF_SCHEMA = z.object({
  houseBlId:      z.number(),
  hblNo:          z.string().nullable().optional(),
  shipperCode:    z.string().nullable().optional(),
  consigneeCode:  z.string().nullable().optional(),
  docPartnerCode: z.string().nullable().optional(),
  pkgQty:         z.number().nullable().optional(),
  pkgUnit:        z.string().nullable().optional(),
  weightUnit:     z.string().nullable().optional(),
  grossWeightKg:  z.number().nullable().optional(),
  cbm:            z.number().nullable().optional(),
  etd:            z.string().nullable().optional(),
  eta:            z.string().nullable().optional(),
  vesselName:     z.string().nullable().optional(),
  voyageNo:       z.string().nullable().optional(),
  polCode:        z.string().nullable().optional(),
  podCode:        z.string().nullable().optional(),
});

// §BE-sync — AirDetailProjection 18 필드 (BE Phase 2 AirDetailRequest/AirDetailProjection 정합)
// 모두 nullable optional, null → undefined transform (BE SSOT — zodResolver 미사용)
// handlingInfoCode/handlingInfoText: BE AirDetailRequest 필드명 그대로 매핑
export const AIR_DETAIL_FORM_SCHEMA = z.object({
  airlineCode:            z.string().nullable().optional().transform((v) => v ?? undefined),
  chargeWeightKg:         z.number().min(0).nullable().optional(),
  volumeWeightKg:         z.number().min(0).nullable().optional(),
  rateClass:              z.string().nullable().optional().transform((v) => v ?? undefined),
  currencyCode:           z.string().nullable().optional().transform((v) => v ?? undefined),
  declaredValueCarriage:  z.string().nullable().optional().transform((v) => v ?? undefined),
  declaredValueCustoms:   z.string().nullable().optional().transform((v) => v ?? undefined),
  insurance:              z.string().nullable().optional().transform((v) => v ?? undefined),
  accountInformation:     z.string().nullable().optional().transform((v) => v ?? undefined),
  securityStatus:         z.string().nullable().optional().transform((v) => v ?? undefined),
  flightType:             z.string().nullable().optional().transform((v) => v ?? undefined),
  issueDate:              DATE8,
  issuePlace:             z.string().nullable().optional().transform((v) => v ?? undefined),
  signature:              z.string().nullable().optional().transform((v) => v ?? undefined),
  otherTerm:              z.string().nullable().optional().transform((v) => v ?? undefined),
  handlingInformationCode: z.string().nullable().optional().transform((v) => v ?? undefined),
  handlingInformationText: z.string().nullable().optional().transform((v) => v ?? undefined),
  remark:                 z.string().nullable().optional().transform((v) => v ?? undefined),
});

// §BE-sync — SeaDetailResponse 16 필드 (BE Phase 2 SeaDetailProjection 정합)
// §6.49 ⑰ — enum 필드(loadType/serviceTerm/blType)는 z.string().nullable()로 완화 (BE 검증 일원화)
// seaDetail.deliveryCode 자연 제거 (Master 도메인 미보유 — Phase 0/1 사용자 결정)
export const SEA_DETAIL_FORM_SCHEMA = z.object({
  loadType:          z.string().nullable().optional().transform((v) => v ?? undefined),
  linerCode:         z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselCode:        z.string().nullable().optional().transform((v) => v ?? undefined),
  vesselName:        z.string().nullable().optional().transform((v) => v ?? undefined),
  voyageNo:          z.string().nullable().optional().transform((v) => v ?? undefined),
  onboardDate:       DATE8,
  vesselNationality: z.string().nullable().optional().transform((v) => v ?? undefined),
  serviceTerm:       z.string().nullable().optional().transform((v) => v ?? undefined),
  blType:            z.string().nullable().optional().transform((v) => v ?? undefined),
  porCode:           z.string().max(5).nullable().optional().transform((v) => v ?? undefined),
  finalDestCode:     z.string().max(5).nullable().optional().transform((v) => v ?? undefined),
  rton:              z.string().nullable().optional().transform((v) => v ?? undefined),
  lineBkgNo:         z.string().max(35).nullable().optional().transform((v) => v ?? undefined),
  issueDate:         DATE8,
  remark:            z.string().nullable().optional().transform((v) => v ?? undefined),
});

// freightTerm 값이 null일 수 있으므로 detail reset 시 기본값 fallback 처리
// §6.49 ⑰ — freightTerm enum literal → z.string().nullable() 완화 (BE 검증 일원화)
export const MASTER_BL_SCHEMA = z.object({
  jobDiv:       z.enum(["SEA", "AIR", "TRUCK", "NON_BL"]),
  bound:        z.enum(["EXP", "IMP"]),
  mblNo:        z.string().max(35).optional(),
  masterRefNo:  z.string().max(35).optional(),
  freightTerm:  z.string().nullable().optional().transform((v) => v ?? undefined),

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

  pkgQty:            z.number().min(0).optional(),
  pkgUnit:           z.string().optional(),
  weightUnit:        z.string().optional(),
  grossWeightKg:     z.number().min(0).optional(),
  cbm:               z.number().min(0).optional(),
  hsCode:            z.string().optional(),
  mainItemName:      z.string().optional(),
  settlePartnerCode: z.string().optional(),
  operatorCode:      z.string().optional(),

  shipmentType:   z.string().optional(),
  rTon:           z.number().min(0).optional(),
  volWeight:      z.number().min(0).optional(),
  chargeWeight:   z.number().min(0).optional(),
  rateClass:      z.string().optional(),
  coLoadAgent:    z.string().optional(),
  coLoadType:     z.string().optional(),
  flightType:     z.string().optional(),
  securityStatus: z.string().optional(),
  teamCode:       z.string().optional(),
  remark:         z.string().optional(),

  // §BE-sync — seaDetail nested (BE Phase 2 SeaDetailResponse 16 필드 정합)
  seaDetail: SEA_DETAIL_FORM_SCHEMA.optional(),
  // §BE-sync — airDetail nested (BE Phase 2 AirDetailRequest 18 필드 정합)
  airDetail: AIR_DETAIL_FORM_SCHEMA.optional(),

  desc:         DESC_SCHEMA,
  dims:         z.array(DIM_SCHEMA).optional(),
  scheduleLegs: z.array(SCHEDULE_LEG_SCHEMA).optional(),
  airCharges:   z.array(AIR_CHARGE_SCHEMA).optional(),
  houseBls:              z.array(HOUSE_BL_REF_SCHEMA).optional(),
  consoledSeaContainers: z.array(CONSOLED_SEA_CONTAINER_SCHEMA).default([]),
});

export type MasterBlFormValues = z.infer<typeof MASTER_BL_SCHEMA>;

// toolbar 라벨 → RHF field path 매핑
// Status 필드 제거 (BE 미지원, 사용자 결정 2026-05-15)
export const TOOLBAR_TO_FIELD: Partial<Record<string, string>> = {
  "Master Ref":    "masterRefNo",
  "MBL No":        "mblNo",
  "MAWB No":       "mblNo",
  "Line Bkg. No":  "seaDetail.lineBkgNo",
  "Load Type":     "seaDetail.loadType",
  "Service Term":  "seaDetail.serviceTerm",
  "B/L Type":      "seaDetail.blType",
  "Shipment Type": "shipmentType",
};
