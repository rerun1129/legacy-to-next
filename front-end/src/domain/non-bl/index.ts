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
  mainItemName?: string;
  hsCode?: string;
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
}

export type UpdateNonBlRequest = Partial<CreateNonBlRequest>;
