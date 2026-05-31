export interface AirHouseRow {
  id: number
  hblNo: string
  bound: string
  mblNo: string
  shipmentType: string
  etd: string
  eta: string
  grossWeightKg: string
  chargeWeightKg: string
  pkgQty: string | number
  pkgUnit: string
  polCode: string
  podCode: string
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
  airlineCode: string
  airlineName: string
  masterRefNo: string
  freightTerm: string
  incoterms: string
  actualCustomerCode: string
  actualCustomerName: string
  salesManCode: string
  teamCode: string
  teamName: string
}

export interface AirHouseFilter {
  bound: string;
  dateKind: string;
  dateFrom: string;
  dateTo: string;
  masterAwbKind: string;
  masterAwbValue: string;
  hblNo: string;
  partyKind: string;
  partyCode: string;
  partyName: string;
  actualCustomerCode: string;
  actualCustomerName: string;
  settlePartnerCode: string;
  settlePartnerName: string;
  airlineCode: string;
  airlineName: string;
  portKind: string;
  portCode: string;
  portName: string;
  shipmentType: string;
  teamCode: string;
  teamName: string;
  operatorCode: string;
  operatorName: string;
  salesClass: string;
  salesManCode: string;
  salesManName: string;
  incoterms: string;
}
