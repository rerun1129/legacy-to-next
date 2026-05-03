import type { JobDiv, Bound } from '@/domain/house-bl';

export interface MasterBlRow {
  id: number;
  mblNo: string | null;
  masterRefNo: string | null;
  jobDiv: JobDiv;
  bound: Bound;
  shipperCode: string | null;
  consigneeCode: string | null;
  polCode: string | null;
  podCode: string | null;
  etd: string | null;
  eta: string | null;
  operatorCode: string | null;
  createdAt: string;
}

export interface MasterBlDetail extends MasterBlRow {
  freightTerm: 'PREPAID' | 'COLLECT' | null;
  pkgQty: number | null;
  grossWeightKg: number | null;
  cbm: number | null;
  consolidatedHouseBls: ConsolidatedHouseBlSummary[];
  updatedAt: string | null;
}

export interface ConsolidatedHouseBlSummary {
  id: number;
  hblNo: string | null;
  shipperCode: string | null;
  consigneeCode: string | null;
}

export interface MasterBlFilter {
  jobDiv?: JobDiv;
  bound: Bound;
  mblNo?: string;
  shipperCode?: string;
  consigneeCode?: string;
  polCode?: string;
  podCode?: string;
  etdFrom?: string;
  etdTo?: string;
  page?: number;
  size?: number;
}

export interface SeaDetailRequest {
  loadType?: string;
  linerCode?: string;
  vesselCode?: string;
  vesselName?: string;
  voyageNo?: string;
  onboardDate?: string;
  vesselNationality?: string;
  weightUnit?: string;
  serviceTerm?: string;
  blType?: string;
  porCode?: string;
  finalDestCode?: string;
  rton?: number;
  lineBkgNo?: string;
  issueDate?: string;
}

export interface DescRequest {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
  remark?: string;
}

export interface DimRequest {
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

export interface ScheduleLegRequest {
  toCode: string;
  byCarrier?: string;
  flightNo?: string;
  onBoardDt: string;
  onBoardTm?: string;
  arrivalDt: string;
  arrivalTm?: string;
}

export interface AirChargeRequest {
  freightCode?: string;
  currencyCode?: string;
  per?: string;
  freightTerm?: string;
  grossWeightKg?: number;
  rateClass?: string;
  chargeWeightKg?: number;
  rate?: number;
}

export interface CreateMasterBlRequest {
  jobDiv: JobDiv;
  bound: Bound;
  mblNo?: string;
  masterRefNo?: string;
  freightTerm: 'PREPAID' | 'COLLECT';
  shipperCode?: string;
  shipperAddress?: string;
  consigneeCode?: string;
  consigneeAddress?: string;
  notifyCode?: string;
  notifyAddress?: string;
  polCode?: string;
  podCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  pkgUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  hsCode?: string;
  mainItemName?: string;
  settlePartnerCode?: string;
  operatorCode?: string;
  seaDetail?: SeaDetailRequest;
  desc?: DescRequest;
  dims?: DimRequest[];
  scheduleLegs?: ScheduleLegRequest[];
  airCharges?: AirChargeRequest[];
}

export type UpdateMasterBlRequest = Partial<CreateMasterBlRequest>;
