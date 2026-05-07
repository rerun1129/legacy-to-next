export interface SeaMasterRow {
  id: number
  bound: string
  mblNo: string
  shipmentType: string
  etd: string
  eta: string
  grossWeightKg: string
  rton: string
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
  linerCode: string
  linerName: string
  houseBlCount: number
  operatorCode: string
  masterRefNo: string
  freightTerm: string
  teamCode: string
  vesselName: string | null
  voyageNo: string | null
  loadType: string | null
  cbm: number | null
}

export interface SeaMasterFilter {
  bound: string;
  dateKind: string;
  dateFrom: string;
  dateTo: string;
  masterBlKind: string;
  masterBlValue: string;
  partyKind: string;
  partyCode: string;
  partyName: string;
  linerCode: string;
  linerName: string;
  portKind: string;
  portCode: string;
  portName: string;
  vesselName: string;
  voyageNo: string;
  shipmentType: string;
  loadType: string;
}
