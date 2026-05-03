import { z } from 'zod';

// ── Sub-entity schemas ────────────────────────────────────

const DATE8 = z.string().regex(/^\d{8}$/).or(z.literal('')).optional();

export const DESC_SCHEMA = z.object({
  marks:        z.string().optional(),
  description:  z.string().optional(),
  descClause1:  z.string().optional(),
  descClause2:  z.string().optional(),
  remark:       z.string().optional(),
});

export const DIM_SCHEMA = z.object({
  lengthCm:      z.string().optional(),
  widthCm:       z.string().optional(),
  heightCm:      z.string().optional(),
  quantity:      z.string().optional(),
  cbm:           z.string().optional(),
  volumeWeightKg: z.string().optional(),
});

export const CONTAINER_SCHEMA = z.object({
  containerNo:   z.string().max(20).optional(),
  containerType: z.string().optional(),
  lengthFeet:    z.string().optional(),
  sealNo1:       z.string().optional(),
  sealNo2:       z.string().optional(),
  sealNo3:       z.string().optional(),
  sealNo4:       z.string().optional(),
  sealNo5:       z.string().optional(),
  sealNo6:       z.string().optional(),
  pkgQty:        z.string().optional(),
  pkgUnit:       z.string().optional(),
  grossWeightKg: z.string().optional(),
  netWeightKg:   z.string().optional(),
  cbm:           z.string().optional(),
  vgmKg:         z.string().optional(),
  soc:           z.boolean().optional(),
  seq:           z.string().optional(),
});

export const SCHEDULE_LEG_SCHEMA = z.object({
  toCode:    z.string().max(5).optional(),
  byCarrier: z.string().optional(),
  flightNo:  z.string().optional(),
  onBoardDt: DATE8,
  onBoardTm: z.string().optional(),
  arrivalDt: DATE8,
  arrivalTm: z.string().optional(),
});

export const LICENSE_SCHEMA = z.object({
  licenseNo:            z.string().optional(),
  pkgQty:               z.string().optional(),
  pkgUnit:              z.string().optional(),
  grossWeightKg:        z.string().optional(),
  combinedPackingMark:  z.string().optional(),
  combinedPackingQty:   z.string().optional(),
  combinedPackingUnit:  z.string().optional(),
  partialShipment:      z.boolean().optional(),
  partialShipmentSeq:   z.string().optional(),
  hsnNo:                z.string().optional(),
});

export const TRUCK_ORDER_SCHEMA = z.object({
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

export const AIR_CHARGE_SCHEMA = z.object({
  freightCode:    z.string().optional(),
  currencyCode:   z.string().optional(),
  per:            z.string().optional(),
  freightTerm:    z.string().optional(),
  grossWeightKg:  z.string().optional(),
  rateClass:      z.string().optional(),
  chargeWeightKg: z.string().optional(),
  rate:           z.string().optional(),
});

// House-BL Item/HS 그리드 행
const ITEM_HS_SCHEMA = z.object({
  id:    z.number(),
  hs:    z.string().optional(),
  desc:  z.string().optional(),
  qty:   z.string().optional(),
  unit:  z.string().optional(),
  value: z.string().optional(),
  cur:   z.string().optional(),
});

// Freight Selling/Buying 공통 행
const FREIGHT_ROW_SCHEMA = z.object({
  id:     z.number(),
  code:   z.string().optional(),
  desc:   z.string().optional(),
  qty:    z.string().optional(),
  unit:   z.string().optional(),
  amount: z.string().optional(),
  cur:    z.string().optional(),
});

// Co-Load B/L 그리드 행
const CO_LOAD_SCHEMA = z.object({
  id:        z.number(),
  hblNo:     z.string().optional(),
  shipper:   z.string().optional(),
  consignee: z.string().optional(),
  pkg:       z.string().optional(),
  gw:        z.string().optional(),
  cbm:       z.string().optional(),
});

// EDI / Korea License (수출신고필증)
const KOREA_LICENSE_SCHEMA = z.object({
  id:        z.number(),
  licenseNo: z.string().optional(),
  amount:    z.string().optional(),
  cur:       z.string().optional(),
});

export const SEA_DETAIL_SCHEMA = z.object({
  loadType:                z.string().optional(),
  linerCode:               z.string().optional(),
  vesselCode:              z.string().optional(),
  vesselName:              z.string().optional(),
  voyageNo:                z.string().optional(),
  onboardDate:             DATE8,
  porCode:                 z.string().max(5).optional(),
  finalDestCode:           z.string().max(5).optional(),
  issueDate:               DATE8,
  noOfBl:                  z.string().optional(),
  issuePlace:              z.string().max(5).optional(),
  doDate:                  DATE8,
  payableAt:               z.string().max(5).optional(),
  triangle:                z.boolean().optional(),
  serviceTerm:             z.string().optional(),
  vesselNationality:       z.string().optional(),
  weightUnit:              z.string().optional(),
  rton:                    z.string().optional(),
  sayInformation:          z.string().optional(),
  noOfContainerOrPackages: z.string().optional(),
  blType:                  z.string().optional(),
  deliveryCode:            z.string().max(5).optional(),
});

// ── Root form schema ──────────────────────────────────────

export const HOUSE_BL_SCHEMA = z.object({
  // toolbar fields
  hbl:    z.string().max(35),
  mbl:    z.string().max(35),
  mblNo:  z.string().max(35).optional(),
  masterRefNo: z.string().max(35).optional(),
  sType:  z.string(),
  lType:  z.string(),
  etd:    z.string().regex(/^\d{8}$/).or(z.literal('')).optional(),
  eta:    z.string().regex(/^\d{8}$/).or(z.literal('')).optional(),
  pol:    z.string().max(5).optional(),
  pod:    z.string().max(5).optional(),
  settle: z.enum(['PREPAID', 'COLLECT']),
  expImp: z.enum(['EXP', 'IMP']).nullable(),

  // party fields
  shipperCode:      z.string().max(20).optional(),
  shipperAddress:   z.string().optional(),
  consigneeCode:    z.string().max(20).optional(),
  consigneeAddress: z.string().optional(),
  notifyCode:       z.string().max(20).optional(),
  notifyAddress:    z.string().optional(),
  docPartnerCode:   z.string().max(20).optional(),
  docPartnerAddress: z.string().optional(),
  settlePartnerCode: z.string().max(20).optional(),

  // cargo summary
  pkgQty:        z.string().optional(),
  pkgUnit:       z.string().optional(),
  grossWeightKg: z.string().optional(),
  cbm:           z.string().optional(),

  // performance
  actualCustomerCode: z.string().max(20).optional(),
  operatorCode:       z.string().optional(),
  teamCode:           z.string().optional(),
  salesManCode:       z.string().optional(),

  // trade
  masterBlId:   z.string().optional(),
  incoterms:    z.string().optional(),
  salesClass:   z.string().optional(),
  mainItemName: z.string().optional(),
  hsCode:       z.string().optional(),

  // truck toolbar
  truckBlNo:        z.string().optional(),
  truckSettle:      z.string().optional(),
  truckFreightTerm: z.string().optional(),
  truckStatus:      z.string().optional(),

  // truck schedule / location names
  polName:     z.string().optional(),
  podName:     z.string().optional(),

  // truck cargo
  chargeWeightKg: z.string().optional(),

  // truck document
  pickupDate:   z.string().regex(/^\d{8}$/).or(z.literal('')).optional(),
  truckerCode:  z.string().optional(),
  truckerName:  z.string().optional(),
  truckerPic:   z.string().optional(),

  // truck performance
  customerPic: z.string().optional(),

  // SEA detail
  seaDetail: SEA_DETAIL_SCHEMA.optional(),

  // sub-entities
  desc:         DESC_SCHEMA.optional(),
  dims:         z.array(DIM_SCHEMA).optional(),
  containers:   z.array(CONTAINER_SCHEMA).optional(),
  scheduleLegs: z.array(SCHEDULE_LEG_SCHEMA).optional(),
  licenses:     z.array(LICENSE_SCHEMA).optional(),
  truckOrders:  z.array(TRUCK_ORDER_SCHEMA).optional(),
  airCharges:   z.array(AIR_CHARGE_SCHEMA).optional(),
  itemHs:         z.array(ITEM_HS_SCHEMA).optional(),
  freightSelling: z.array(FREIGHT_ROW_SCHEMA).optional(),
  freightBuying:  z.array(FREIGHT_ROW_SCHEMA).optional(),
  coLoadBls:      z.array(CO_LOAD_SCHEMA).optional(),
  koreaLicenses:  z.array(KOREA_LICENSE_SCHEMA).optional(),
});

export type HouseBlFormValues = z.infer<typeof HOUSE_BL_SCHEMA>;
