import type { JobDiv, Bound } from './types';

// ── Sub-entity request types ────────────────────────────────

export interface SeaDetailRequest {
  loadType?: string;
  linerCode?: string;
  vesselCode?: string;
  vesselName?: string;
  voyageNo?: string;
  onboardDate?: string;
  porCode?: string;
  finalDestCode?: string;
  issueDate?: string;
  noOfBl?: string;
  issuePlace?: string;
  doDate?: string;
  payableAt?: string;
  triangle?: boolean;
  serviceTerm?: string;
  vesselNationality?: string;
  rton?: number;
  sayInformation?: string;
  noOfContainerOrPackages?: string;
  blType?: string;
  deliveryCode?: string;
}

export interface DescRequest {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

export interface DimRequest {
  // §6.28 — UPDATE 분기에서 row id 포함 필수 (신규 생성 시 undefined)
  id?: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

export interface ContainerRequest {
  // §6.28 — UPDATE 분기에서 row id 포함 필수 (신규 생성 시 undefined)
  id?: number;
  containerNo?: string;
  containerType?: string;
  lengthFeet?: number;
  sealNo1?: string;
  sealNo2?: string;
  sealNo3?: string;
  sealNo4?: string;
  sealNo5?: string;
  sealNo6?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  netWeightKg?: number;
  cbm?: number;
  vgmKg?: number;
  soc?: boolean;
  seq?: number;
}

export interface ScheduleLegRequest {
  // §6.28 — UPDATE 분기에서 row id 포함 필수 (신규 생성 시 undefined)
  id?: number;
  toCode?: string;
  byCarrier?: string;
  flightNo?: string;
  onBoardDt?: string;
  onBoardTm?: string;
  arrivalDt?: string;
  arrivalTm?: string;
}

export interface TruckOrderRequest {
  truckOrderNo?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  truckNo?: string;
  truckType?: string;
  driver?: string;
  mobileNo?: string;
  containerNo?: string;
  containerType?: string;
  sealNo1?: string;
  sealNo2?: string;
  sealNo3?: string;
}

export interface AirChargeRequest {
  // §6.28 — UPDATE 분기에서 row id 포함 필수 (신규 생성 시 undefined)
  id?: number;
  freightCode?: string;
  currencyCode?: string;
  per?: string;
  freightTerm?: string;
  grossWeightKg?: number;
  rateClass?: string;
  chargeWeightKg?: number;
  rate?: number;
}

// §6.49 ⑰ — AIR 확장 필드는 FE string 완화(enum 검증은 BE 일원화)
export interface AirDetailRequest {
  airlineCode?: string;
  chargeWeightKg?: number;
  volumeWeightKg?: number;
  rateClass?: string;
  currencyCode?: string;
  declaredValueCarriage?: string;
  declaredValueCustoms?: string;
  insurance?: string;
  accountInformation?: string;
  otherTerm?: string;
  issueDate?: string;
  issuePlace?: string;
  signature?: string;
  fhd?: string;
  handlingInformationCode?: string;
  handlingInformationDesc?: string;
  originOfGoods?: string;
  cargoType?: string;
}

// ── Freight request types ──────────────────────────────────

export interface FreightLineRequest {
  // §BE — id 있으면 merge-by-id, 없으면 신규 INSERT
  id?: number;
  freightCode?: string;
  per?: string;
  qty?: string;
  price?: string;
  currency?: string;
  customerCode?: string;
  taxType?: string;
  performanceDt?: string;
  // 계산값 — FE 산정 후 BE에 그대로 전달 (BE는 재계산 없이 저장)
  exchangeRate?: string;
  usdExchangeRate?: string;
  settleAmount?: string;
  localAmount?: string;
  localTaxAmount?: string;   // FE vat → BE localTaxAmount
  usdAmount?: string;
  financialDocType?: string;
}

// ── Create / Update request ────────────────────────────────

export interface CreateHouseBlRequest {
  jobDiv: JobDiv;
  bound: Bound;
  hblNo?: string;
  shipmentType: 'HOUSE' | 'DIRECT';
  freightTerm: 'PREPAID' | 'COLLECT';
  shipperCode?: string;
  shipperAddress?: string;
  consigneeCode?: string;
  consigneeAddress?: string;
  notifyCode?: string;
  notifyAddress?: string;
  docPartnerCode?: string;
  docPartnerAddress?: string;
  settlePartnerCode?: string;
  polCode?: string;
  podCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  weightUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  actualCustomerCode?: string;
  operatorCode?: string;
  teamCode?: string;
  salesManCode?: string;
  masterBlId?: number;
  incoterms?: string;
  salesClass?: string;
  mainItemName?: string;
  hsCode?: string;
  mblNo?: string;
  masterRefNo?: string;
  remark?: string;
  seaDetail?: SeaDetailRequest;
  airDetail?: AirDetailRequest;
  desc?: DescRequest;
  dims?: DimRequest[];
  containers?: ContainerRequest[];
  scheduleLegs?: ScheduleLegRequest[];
  truckOrders?: TruckOrderRequest[];
  airCharges?: AirChargeRequest[];
  // §Freight 탭 — 환율 헤더 + 매출/매입 라인
  sellRateDt?: string;
  sellRateCurrencyCode?: string;
  sellRate?: string;
  buyRateDt?: string;
  buyRateCurrencyCode?: string;
  buyRate?: string;
  usdRateDt?: string;
  usdRate?: string;
  freightSelling?: FreightLineRequest[];
  freightBuying?: FreightLineRequest[];
}

export type UpdateHouseBlRequest = Partial<CreateHouseBlRequest>;
