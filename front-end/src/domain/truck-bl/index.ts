export interface TruckBlRow {
  id: number;
  truckBlNo: string;
  bound: string;
  etd: string;
  eta: string;
  pol: string;
  pod: string;
  truckerCode: string;
  truckerName: string;
  shipperCode: string;
  shipperName: string;
  consigneeCode: string;
  consigneeName: string;
  notifyCode: string;
  notifyName: string;
  docPartnerCode: string;
  docPartnerName: string;
  pkgQty: string;
  pkgUnit: string;
  grossWt: string;
  cbm: string;
  teamCode: string;
  teamName: string;
}

// ── Truck Order / Desc 응답 전용 타입 (BE TruckBlDetailResponse 중첩 record) ──
export interface TruckOrderDetail {
  id?: number;
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

export interface DescDetail {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

export interface TruckBlDimView {
  id?: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

export interface TruckBlDimRequest {
  id?: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

export interface TruckBlDetail {
  id: number;
  hblNo?: string;
  jobDiv: string;
  bound: string;
  shipmentType?: string;
  freightTerm?: string;
  shipperCode?: string;
  shipperAddr?: string;
  consigneeCode?: string;
  consigneeAddr?: string;
  notifyCode?: string;
  notifyAddr?: string;
  settlePartnerCode?: string;
  docPartnerCode?: string;
  docPartnerAddress?: string;
  polCode?: string;
  podCode?: string;
  deliveryCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  weightUnit?: string;
  actualCustomerCode?: string;
  operatorCode?: string;
  teamCode?: string;
  // §BE-sync — BE 조회 시 admin.team 조인 응답. 표시 전용.
  teamName?: string | null;
  salesManCode?: string;
  incoterms?: string;
  createdAt?: string;
  updatedAt?: string;
  truckerCode?: string;
  truckerPic?: string;
  chargeWeightKg?: number;
  pickupDate?: string;
  pickupTm?: string;
  etdTm?: string;
  etaTm?: string;
  loadType?: string;
  serviceTerm?: string;
  voyageNo?: string;
  vesselName?: string;
  remark?: string;
  volumeDivisor?: string | null;
  hsCode?: string;
  hsCodeName?: string;
  truckOrders?: TruckOrderDetail[];
  desc?: DescDetail;
  dims?: TruckBlDimView[];
  // §BE-sync — Freight 탭 응답
  freight?: TruckBlFreightView | null;
}

// ── Freight 타입
export interface TruckBlFreightLineView {
  id?: number;
  freightCode?: string;
  per?: string;
  qty?: number;
  price?: number;
  currency?: string;
  customerCode?: string;
  taxType?: string;
  performanceDt?: string;
  financialDocType?: string;
  exchangeRate?: number;
  usdExchangeRate?: number;
  settleAmount?: number;
  localAmount?: number;
  settleTaxAmount?: number;
  localTaxAmount?: number;
  usdAmount?: number;
  taxNo?: string;
  slipNo?: string;
  financialDocumentNo?: string;
}

export interface TruckBlFreightView {
  sellRateDt?: string;
  sellRateCurrencyCode?: string;
  sellRate?: number;
  buyRateDt?: string;
  buyRateCurrencyCode?: string;
  buyRate?: number;
  usdRateDt?: string;
  usdRate?: number;
  selling: TruckBlFreightLineView[];
  buying: TruckBlFreightLineView[];
}

// ── Truck Order 자식 Request 타입 ─────────────────────────────
export interface TruckOrderCreateRequest {
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

export interface TruckOrderUpdateRequest extends TruckOrderCreateRequest {
  id?: number;
}

// ── Marks/Description 패널 Request 타입 ─────────────────────
export interface TruckDescRequest {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

// ── Create / Update Request ────────────────────────────────────

export interface CreateTruckBlRequest {
  hblNo?: string;
  bound?: string;
  shipperCode?: string;
  shipperAddress?: string;
  consigneeCode?: string;
  consigneeAddress?: string;
  notifyCode?: string;
  notifyAddress?: string;
  settlePartnerCode?: string;
  docPartnerAddress?: string;
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
  salesClass?: string;
  mainItemName?: string;
  hsCode?: string;
  incoterms?: string;
  truckerCode?: string;
  truckerPic?: string;
  chargeWeightKg?: number;
  pickupDate?: string;
  pickupTm?: string;
  etdTm?: string;
  etaTm?: string;
  loadType?: string;
  serviceTerm?: string;
  voyageNo?: string;
  remark?: string;
  volumeDivisor?: string;
  desc?: TruckDescRequest;
  truckOrders?: TruckOrderCreateRequest[];
  dims?: TruckBlDimRequest[];
  // §Freight 탭 — 환율 헤더 + 매출/매입 라인
  sellRateDt?: string;
  sellRateCurrencyCode?: string;
  sellRate?: string;
  buyRateDt?: string;
  buyRateCurrencyCode?: string;
  buyRate?: string;
  usdRateDt?: string;
  usdRate?: string;
  freightSelling?: TruckBlFreightLineRequest[];
  freightBuying?: TruckBlFreightLineRequest[];
}

export interface TruckBlFreightLineRequest {
  id?: number;
  freightCode?: string;
  per?: string;
  qty?: string;
  price?: string;
  currency?: string;
  customerCode?: string;
  taxType?: string;
  performanceDt?: string;
  exchangeRate?: string;
  usdExchangeRate?: string;
  settleAmount?: string;
  localAmount?: string;
  localTaxAmount?: string;
  usdAmount?: string;
  financialDocType?: string;
}

/** Update 요청은 hblNo를 제외한 나머지 필드 (B/L No 변경은 PUT /{id}/hbl-no 전용) */
export type UpdateTruckBlRequest = Omit<CreateTruckBlRequest, 'hblNo'> & {
  truckOrders?: TruckOrderUpdateRequest[];
};

export interface TruckBlFilter {
  bound: string;
  dateFrom: string;
  dateTo: string;
  truckBlNo: string;
  truckerCode: string;
  truckerName: string;
  partyCode: string;
  partyName: string;
  portCode: string;
  portName: string;
  partnerKind: string | null;
  partnerCode: string;
  partnerName: string;
  operatorCode: string;
  operatorName: string;
  teamCode: string;
  teamName: string;
  page?: number;
  size?: number;
  dateKind?: 'ETD' | 'ETA';
  partyKind?: 'SHIPPER' | 'CONSIGNEE' | 'NOTIFY';
  portKind?: 'POL' | 'POD';
}
