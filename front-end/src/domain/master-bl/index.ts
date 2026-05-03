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
  page?: number;
  size?: number;
}

export interface CreateMasterBlRequest {
  jobDiv: JobDiv;
  bound: Bound;
  mblNo?: string;
  masterRefNo?: string;
  freightTerm: 'PREPAID' | 'COLLECT';
  shipperCode?: string;
  consigneeCode?: string;
  polCode?: string;
  podCode?: string;
  etd?: string;
  eta?: string;
  pkgQty?: number;
  grossWeightKg?: number;
  cbm?: number;
  operatorCode?: string;
}

export type UpdateMasterBlRequest = Partial<CreateMasterBlRequest>;
