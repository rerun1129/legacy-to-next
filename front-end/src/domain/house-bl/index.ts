export interface HouseBlRow {
  id: number;
  hblNo: string | null;
  jobDiv: 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL';
  bound: 'EXP' | 'IMP';
  polCode: string | null;
  podCode: string | null;
  etd: string | null;
  eta: string | null;
  shipperCode: string | null;
  consigneeCode: string | null;
  pkgQty: number | null;
  pkgUnit: string | null;
  createdAt: string;
}

export interface HouseBlDetail extends HouseBlRow {
  shipmentType: 'HOUSE' | 'DIRECT' | null;
  blType: 'OBL' | 'SWB' | 'SURRENDER' | null;
  freightTerm: 'PREPAID' | 'COLLECT' | null;
  notifyCode: string | null;
  deliveryCode: string | null;
  grossWeightKg: number | null;
  cbm: number | null;
  operatorCode: string | null;
  teamCode: string | null;
  salesManCode: string | null;
  masterBlId: number | null;
  updatedAt: string | null;
}

export interface HouseBlFilter {
  jobDiv?: string;
  bound?: 'EXP' | 'IMP';
  page?: number;
  size?: number;
}
