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
