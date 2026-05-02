export type JobDiv = 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL';
export type Bound = 'EXP' | 'IMP';

export interface HouseBlRow {
  id: number;
  hblNo: string | null;
  jobDiv: JobDiv;
  bound: Bound;
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
  jobDiv: JobDiv;
  bound: Bound;
  page?: number;
  size?: number;
}

export interface CreateHouseBlRequest {
  jobDiv: JobDiv;
  bound: Bound;
  hblNo?: string;
  shipmentType: 'HOUSE' | 'DIRECT';
  freightTerm: 'PREPAID' | 'COLLECT';
  shipperCode?: string;
  consigneeCode?: string;
  notifyCode?: string;
  polCode?: string;
  podCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  operatorCode?: string;
  teamCode?: string;
  salesManCode?: string;
  masterBlId?: number;
}

export type UpdateHouseBlRequest = Partial<CreateHouseBlRequest>;
