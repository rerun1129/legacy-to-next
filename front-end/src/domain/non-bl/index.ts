export interface NonBlRow {
  id: number;
  nonBlNo: string;
  bound: string;
  etd: string;
  eta: string;
  pol: string;
  pod: string;
  vesselName: string;
  voyNo: string;
  shipperCode: string;
  shipperName: string;
  consigneeCode: string;
  consigneeName: string;
  notifyCode: string;
  notifyName: string;
  settlePartnerCode: string;
  settlePartnerName: string;
  linerCode: string;
  linerName: string;
  actualCustomerCode: string;
  actualCustomerName: string;
  pkgQty: string;
  pkgUnit: string;
  grossWt: string;
  cbm: string;
  teamCode: string;
  teamName: string;
}

export interface NonBlFilter {
  bound: string;
  dateFrom: string;
  dateTo: string;
  linerCode: string;
  linerName: string;
  nonBlNo: string;
  partyCode: string;
  partyName: string;
  portCode: string;
  portName: string;
  vessel: string;
  voyage: string;
  operatorCode: string;
  operatorName: string;
  teamCode: string;
  teamName: string;
  page?: number;
  size?: number;
  dateKind?: 'ETD' | 'ETA';
  partyKind?: 'SHIPPER' | 'CONSIGNEE' | 'NOTIFY' | 'SETTLE_PARTNER';
  portKind?: 'POL' | 'POD';
}

// ── 자식 View 타입 ─────────────────────────────────────────────

export interface NonBlContainerView {
  id?: number;
  containerNo?: string;
  containerType?: string;
  sealNo1?: string;
  sealNo2?: string;
  sealNo3?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
}

export interface NonBlDimView {
  id?: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

// ── 단건 조회 응답 타입 ────────────────────────────────────────

export interface NonBlDetail {
  id: number;
  hblNo?: string;
  bound: string;
  shipperCode?: string;
  consigneeCode?: string;
  notifyCode?: string;
  settlePartnerCode?: string;
  actualCustomerCode?: string;
  polCode?: string;
  podCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  weightUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  operatorCode?: string;
  salesManCode?: string;
  teamCode?: string;
  // §BE-sync — BE 조회 시 admin.team 조인 응답. 표시 전용.
  teamName?: string | null;
  mainItemName?: string;
  hsCode?: string;
  hsCodeName?: string;
  workDivision?: string;
  originalBlRef?: string;
  rton?: number;
  volumeWtKg?: number;
  linerCode?: string;
  linerName?: string;
  vesselName?: string;
  voyageNo?: string;
  finalDestCode?: string;
  finalDestName?: string;
  finalEta?: string;
  volumeDivisor?: string | null;
  salesClass?: string | null;
  createdAt?: string;
  updatedAt?: string;
  remark?: string;
  containers: NonBlContainerView[];
  dims: NonBlDimView[];
  // §BE-sync — Freight 탭 응답
  freight?: NonBlFreightView | null;
}

// ── Freight 타입 (house-bl FreightDetailView 재활용 불가 — 별도 import 금지)
export interface NonBlFreightLineView {
  id?: number;
  freightCode?: string;
  freightName?: string;
  per?: string;
  qty?: number;
  price?: number;
  currency?: string;
  customerCode?: string;
  customerName?: string;
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
  // BE amend 진입을 위한 서류 PK — 발행 전은 null/undefined
  financialDocumentId?: number;
}

export interface NonBlFreightView {
  sellRateDt?: string;
  sellRateCurrencyCode?: string;
  sellRate?: number;
  buyRateDt?: string;
  buyRateCurrencyCode?: string;
  buyRate?: number;
  usdRateDt?: string;
  usdRate?: number;
  selling: NonBlFreightLineView[];
  buying: NonBlFreightLineView[];
}

// ── 자식 Request 타입 ─────────────────────────────────────────

export interface NonBlContainerRequest {
  id?: number;
  containerNo?: string;
  containerType?: string;
  sealNo1?: string;
  sealNo2?: string;
  sealNo3?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
}

export interface NonBlDimRequest {
  id?: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

// ── Create / Update Request ────────────────────────────────────

export interface CreateNonBlRequest {
  hblNo?: string;
  bound: string;
  workDivision?: string;
  shipperCode?: string;
  consigneeCode?: string;
  notifyCode?: string;
  settlePartnerCode?: string;
  actualCustomerCode?: string;
  polCode?: string;
  podCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  weightUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  operatorCode?: string;
  salesManCode?: string;
  teamCode?: string;
  mainItemName?: string;
  hsCode?: string;
  rton?: number;
  volumeWtKg?: number;
  linerCode?: string;
  linerName?: string;
  vesselName?: string;
  voyageNo?: string;
  finalDestCode?: string;
  finalDestName?: string;
  finalEta?: string;
  salesClass?: string;
  volumeDivisor?: string;
  originalBlRef?: string;
  remark?: string;
  containers?: NonBlContainerRequest[];
  dims?: NonBlDimRequest[];
  // §Freight 탭 — 환율 헤더 + 매출/매입 라인
  sellRateDt?: string;
  sellRateCurrencyCode?: string;
  sellRate?: string;
  buyRateDt?: string;
  buyRateCurrencyCode?: string;
  buyRate?: string;
  usdRateDt?: string;
  usdRate?: string;
  freightSelling?: NonBlFreightLineRequest[];
  freightBuying?: NonBlFreightLineRequest[];
}

export interface NonBlFreightLineRequest {
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

export type UpdateNonBlRequest = Partial<CreateNonBlRequest>;
