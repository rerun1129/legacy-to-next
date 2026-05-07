export interface SeaHouseRow {
  id: number
  hblNo: string
  bound: string
  mblNo: string
  masterRefNo: string
  shipmentType: string
  loadType: string | null
  etd: string
  eta: string
  polCode: string
  podCode: string
  deliveryCode: string | null
  vesselName: string | null
  voyageNo: string | null
  shipperCode: string
  shipperName: string
  consigneeCode: string
  consigneeName: string
  notifyCode: string
  notifyName: string
  settlePartnerCode: string
  settlePartnerName: string
  docPartnerCode: string
  docPartnerName: string
  linerCode: string
  linerName: string
  freightTerm: string
  incoterms: string
  actualCustomerCode: string
  actualCustomerName: string
  pkgQty: string | number
  pkgUnit: string
  grossWeightKg: string
  rton: string
  cbm: number | null
  cntr20Qty: number | null
  cntr40Qty: number | null
  teuQty: number | null
  salesManCode: string
  teamCode: string
}

export interface SeaHouseFilter {
  bound: string;
  dateKind: string;
  dateFrom: string;
  dateTo: string;
  masterBlKind: string;
  masterBlValue: string;
  hblNo: string;
  partyKind: string;
  partyCode: string;
  partyName: string;
  actualCustomerCode: string;
  actualCustomerName: string;
  partnerKind: string | null;
  partnerCode: string;
  partnerName: string;
  linerCode: string;
  linerName: string;
  portKind: string;
  portCode: string;
  portName: string;
  vesselName: string;
  voyageNo: string;
  shipmentType: string;
  teamCode: string;
  teamName: string;
  operatorCode: string;
  operatorName: string;
  salesClass: string;
  salesManCode: string;
  salesManName: string;
  incoterms: string;
  loadType: string;
}
