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
  remark?: string;
}

export interface TruckBlDetail {
  id: number;
  hblNo?: string;
  jobDiv: string;
  bound: string;
  shipmentType?: string;
  freightTerm?: string;
  shipperCode?: string;
  consigneeCode?: string;
  notifyCode?: string;
  settlePartnerCode?: string;
  polCode?: string;
  podCode?: string;
  deliveryCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  actualCustomerCode?: string;
  operatorCode?: string;
  teamCode?: string;
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
  truckOrders?: TruckOrderDetail[];
  desc?: DescDetail;
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
  remark?: string;
}

// ── Create / Update Request ────────────────────────────────────

export interface CreateTruckBlRequest {
  hblNo?: string;
  bound?: string;
  shipperCode?: string;
  consigneeCode?: string;
  notifyCode?: string;
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
  desc?: TruckDescRequest;
  truckOrders?: TruckOrderCreateRequest[];
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
  docPartnerCode: string;
  docPartnerName: string;
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
