import type { JobDiv, Bound } from '@/domain/house-bl';

export type { JobDiv, Bound };

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

// §BE-sync — SeaDescView (BE SeaDetailResponse.SeaDescView nested)
export interface MasterBlSeaDescView {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

// §BE-sync — SeaDetailResponse 16 필드 (BE Phase 2 SeaDetailProjection 1:1 정합)
// §6.49 ⑰ — enum 필드는 string | null로 완화 (BE 검증 일원화, FE는 ComboBox useEnumOptions로 동적 fetch)
export interface MasterBlSeaDetail {
  loadType: string | null;
  linerCode: string | null;
  vesselCode: string | null;
  vesselName: string | null;
  voyageNo: string | null;
  onboardDate: string | null;
  vesselNationality: string | null;
  serviceTerm: string | null;
  blType: string | null;
  porCode: string | null;
  finalDestCode: string | null;
  rton: number | null;
  lineBkgNo: string | null;
  issueDate: string | null;
  desc: MasterBlSeaDescView | null;
  remark: string | null;
}

export interface MasterBlDetail extends MasterBlRow {
  shipmentType: string | null;
  // §6.49 ⑰ — freightTerm enum literal → string | null 완화 (BE 검증 일원화)
  freightTerm: string | null;
  pkgQty: number | null;
  weightUnit?: string;
  grossWeightKg: number | null;
  cbm: number | null;
  consolidatedHouseBls: ConsolidatedHouseBlSummary[];
  consoledSeaContainers: ConsoledSeaContainer[];
  updatedAt: string | null;
  remark?: string;
  teamCode: string | null;
  // party code + address (§BE Phase 2 — CustomerCode VO 기반 노출)
  shipperCode: string | null;
  shipperAddress: string | null;
  consigneeCode: string | null;
  consigneeAddress: string | null;
  notifyCode: string | null;
  notifyAddress: string | null;
  // §BE-sync — seaDetail nested (BE Phase 2 SeaDetailResponse 정합)
  seaDetail: MasterBlSeaDetail | null;
}

// §BE-sync — ConsoledSeaContainerView (BE consoledSeaContainers 전체 필드 정합)
// SEA Master에 콘솔된 모든 컨테이너를 row 단위로 그대로 반환, 표시 전용
export interface ConsoledSeaContainer {
  houseBlId: number;
  containerNo: string | null;
  containerType: string | null;
  sealNo1: string | null;
  sealNo2: string | null;
  sealNo3: string | null;
  pkgQty: number | null;
  pkgUnit: string | null;
  grossWeightKg: string | null;
  cbm: string | null;
  vgmKg: string | null;
}

// §BE-sync — ConsoledHouseBlSummaryView (BE consolidatedHouseBls 전체 필드 정합)
// AIR 전용 필드(chargeWeightKg)는 SEA에서 null 반환 — FE 표시 제외 가능
export interface ConsolidatedHouseBlSummary {
  id: number;
  hblNo: string | null;
  shipperCode: string | null;
  consigneeCode: string | null;
  docPartnerCode: string | null;
  pkgQty: string | null;
  pkgUnit: string | null;
  weightUnit: string | null;
  grossWeightKg: string | null;
  cbm: string | null;
  etd: string | null;
  eta: string | null;
  vesselName: string | null;
  voyageNo: string | null;
  polCode: string | null;
  podCode: string | null;
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

// §BE-sync — CreateMasterBlRequest.SeaDetailRequest (BE Phase 3 정합)
export interface SeaDetailRequest {
  loadType?: string;
  linerCode?: string;
  vesselCode?: string;
  vesselName?: string;
  voyageNo?: string;
  onboardDate?: string;
  vesselNationality?: string;
  serviceTerm?: string;
  blType?: string;
  porCode?: string;
  finalDestCode?: string;
  rton?: number;
  lineBkgNo?: string;
  issueDate?: string;
  desc?: DescRequest;
}

export interface DescRequest {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
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
  shipmentType?: string;
  // §6.49 ⑰ — freightTerm은 BE 검증 일원화, FE는 string으로 완화
  freightTerm?: string;
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
  weightUnit?: string;
  grossWeightKg?: number;
  cbm?: number;
  hsCode?: string;
  mainItemName?: string;
  settlePartnerCode?: string;
  operatorCode?: string;
  teamCode?: string;
  remark?: string;
  seaDetail?: SeaDetailRequest;
  desc?: DescRequest;
  dims?: DimRequest[];
  scheduleLegs?: ScheduleLegRequest[];
  airCharges?: AirChargeRequest[];
}

export type UpdateMasterBlRequest = Partial<CreateMasterBlRequest>;
