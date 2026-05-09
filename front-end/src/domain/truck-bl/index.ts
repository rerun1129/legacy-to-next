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
}

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
