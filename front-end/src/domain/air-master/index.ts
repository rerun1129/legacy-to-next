export interface AirMasterRow {
  id: number
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
  airlineCode: string
  airlineName: string
  houseBlCount: number
  operatorCode: string
  masterRefNo: string
  freightTerm: string
  teamCode: string
  teamName: string
}

export interface AirMasterFilter {
  bound: string;
  dateKind: string;
  dateFrom: string;
  dateTo: string;
  masterAwbKind: string;
  masterAwbValue: string;
  partyKind: string;
  partyCode: string;
  partyName: string;
  airlineCode: string;
  airlineName: string;
  portKind: string;
  portCode: string;
  portName: string;
  shipmentType: string;
  teamCode: string;
  teamName: string;
}
