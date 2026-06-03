export type JobDiv = 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL';
export type Bound = 'EXP' | 'IMP';

export interface HouseBlRow {
  id: number;
  hblNo: string | null;
  jobDiv: JobDiv;
  bound: Bound;
  polCode: string | null;
  podCode: string | null;
  etd: string | null;
  eta: string | null;
  shipperCode: string | null;
  consigneeCode: string | null;
  pkgQty: number | null;
  pkgUnit: string | null;
  weightUnit?: string;
  createdAt: string;
}

// §BE-sync — SeaContainerView / SeaDescView (BE SeaDetailResponse nested 필드)
export interface HouseBlSeaContainerView {
  id: number;
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

export interface HouseBlSeaDescView {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

// §BE-sync — AirScheduleLegView / AirChargeView / AirDimView / AirDescView (BE AirDetailResponse nested)
export interface HouseBlAirScheduleLegView {
  id?: number;
  toCode?: string;
  byCarrier?: string;
  flightNo?: string;
  onBoardDt?: string;
  onBoardTm?: string;
  arrivalDt?: string;
  arrivalTm?: string;
}

export interface HouseBlAirChargeView {
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

export interface HouseBlAirDimView {
  id?: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

export interface HouseBlAirDescView {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

export interface HouseBlDetail extends HouseBlRow {
  shipmentType: 'HOUSE' | 'DIRECT' | null;
  blType: string | null;
  loadType?: string;
  seaDetail?: {
    linerCode?: string;
    linerName?: string;
    vesselCode?: string;
    vesselName?: string;
    voyageNo?: string;
    onboardDate?: string;
    porCode?: string;
    finalDestCode?: string;
    issueDate?: string;
    noOfBl?: string;
    issuePlace?: string;
    // §BE-sync — SeaDetailResponse name 4종 (issuePlaceName, payableAtName, deliveryName, linerName)
    issuePlaceName?: string;
    doDate?: string;
    payableAt?: string;
    payableAtName?: string;
    triangle?: boolean;
    serviceTerm?: string;
    vesselNationality?: string;
    rton?: number;
    sayInformation?: string;
    noOfContainerOrPackages?: string;
    deliveryName?: string;
    // §BE-sync — BE SeaDetailResponse.containers / .desc (seaDetail nested)
    containers?: HouseBlSeaContainerView[];
    desc?: HouseBlSeaDescView;
  } | null;
  // §BE-sync — BE AirDetailResponse (airDetail nested). §6.49 ⑰: AIR 확장 필드는 FE string 완화(BE에서 enum 검증)
  airDetail?: {
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
    scheduleLegs?: HouseBlAirScheduleLegView[];
    airCharges?: HouseBlAirChargeView[];
    dims?: HouseBlAirDimView[];
    desc?: HouseBlAirDescView;
  } | null;
  freightTerm: 'PREPAID' | 'COLLECT' | null;
  notifyCode: string | null;
  deliveryCode: string | null;
  grossWeightKg: number | null;
  cbm: number | null;
  operatorCode: string | null;
  teamCode: string | null;
  salesManCode: string | null;
  masterBlId: number | null;
  updatedAt: string | null;
  docPartnerCode: string | null;
  actualCustomerCode: string | null;
  // §6.48 ⑧ — BE 860abfb에서 노출된 party address 4필드
  shipperAddress: string | null;
  consigneeAddress: string | null;
  notifyAddress: string | null;
  docPartnerAddress: string | null;
  // §BE-sync — HouseBlDetailResponse name 필드 (8종 + salesManName, operatorName)
  shipperName?: string;
  consigneeName?: string;
  notifyName?: string;
  docPartnerName?: string;
  settlePartnerName?: string;
  actualCustomerName?: string;
  polName?: string;
  podName?: string;
  salesManName?: string;
  operatorName?: string;
  // §BE-sync — BE 조회 시 admin.team 조인 응답. 표시 전용.
  teamName?: string | null;
  linerCode?: string;
  linerName?: string;
  vesselName?: string;
  voyageNo?: string;
  finalDestCode?: string;
  finalDestName?: string;
  finalEta?: string;
  volumeWeightKg?: number;
  rton?: number;
  remark?: string;
  incoterms?: string;
  salesClass?: string;
  mblNo?: string;
  masterRefNo?: string;
  settlePartnerCode?: string;
  hsCode?: string;
  hsCodeName?: string;
  // §BE-sync — FreightResponse (Freight 탭 응답, 없으면 null)
  freight?: FreightDetailView | null;
}

// §BE-sync — FreightLineResponse (BE 계산값 포함 라인 응답)
export interface FreightLineView {
  id?: number;
  freightCode?: string;
  per?: string;
  qty?: number;
  price?: number;
  currency?: string;
  customerCode?: string;
  taxType?: string;
  performanceDt?: string;
  // 계산값 (저장 후 BE 산정)
  financialDocType?: string;
  exchangeRate?: number;
  usdExchangeRate?: number;
  settleAmount?: number;
  localAmount?: number;
  settleTaxAmount?: number;
  localTaxAmount?: number;
  usdAmount?: number;
  // BE 발행 후 채워지는 서류 번호 필드 (readOnly)
  taxNo?: string;
  slipNo?: string;
  financialDocumentNo?: string;
}

// §BE-sync — FreightResponse (환율 헤더 + selling/buying 라인)
export interface FreightDetailView {
  sellRateDt?: string;
  sellRateCurrencyCode?: string;
  sellRate?: number;
  buyRateDt?: string;
  buyRateCurrencyCode?: string;
  buyRate?: number;
  usdRateDt?: string;
  usdRate?: number;
  selling: FreightLineView[];
  buying: FreightLineView[];
}

export interface HouseBlFilter {
  jobDiv: JobDiv;
  bound: Bound;
  hblNo?: string;
  mblNo?: string;
  shipperCode?: string;
  consigneeCode?: string;
  polCode?: string;
  podCode?: string;
  etdFrom?: string;
  etdTo?: string;
  page?: number;
  size?: number;
}
