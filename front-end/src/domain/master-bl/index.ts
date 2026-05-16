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

// §BE-sync — MasterBlDetailResponse.DescView (root desc nested, BE 보강 완료)
export interface MasterBlDescView {
  marks: string | null;
  description: string | null;
  descClause1: string | null;
  descClause2: string | null;
}

// §BE-sync — SeaDetailResponse 15 필드 (desc root 승격으로 제거됨)
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
  remark: string | null;
}

// §BE-sync — AirDetailResponse 18 필드 (BE Phase 2 AirDetailResponse record mirror)
// BigDecimal(chargeWeightKg/volumeWeightKg)은 BE Jackson → number
// handlingInfoCode/handlingInfoText: BE 필드명 그대로 (form schema는 handlingInformationCode/Text 사용)
export interface MasterBlAirDetail {
  airlineCode: string | null;
  chargeWeightKg: number | null;
  volumeWeightKg: number | null;
  rateClass: string | null;
  currencyCode: string | null;
  declaredValueCarriage: string | null;
  declaredValueCustoms: string | null;
  insurance: string | null;
  accountInformation: string | null;
  securityStatus: string | null;
  flightType: string | null;
  issueDate: string | null;
  issuePlace: string | null;
  signature: string | null;
  otherTerm: string | null;
  handlingInfoCode: string | null;
  handlingInfoText: string | null;
  remark: string | null;
}

// §BE-sync — DimView (id merge-by-id, 포장 치수 응답, BigDecimal → number)
export interface Dim {
  id?: number | null;
  lengthCm: number | null;
  widthCm: number | null;
  heightCm: number | null;
  quantity: number | null;
  cbm: number | null;
  volumeWeightKg: number | null;
}

// §BE-sync — ScheduleLegView (id merge-by-id, 구간 스케줄 응답)
export interface ScheduleLeg {
  id?: number | null;
  toCode: string | null;
  byCarrier: string | null;
  flightNo: string | null;
  onBoardDt: string | null;
  onBoardTm: string | null;
  arrivalDt: string | null;
  arrivalTm: string | null;
}

// §BE-sync — AirChargeView (id merge-by-id, AIR Charge 응답, BigDecimal → number)
export interface AirCharge {
  id?: number | null;
  freightCode: string | null;
  currencyCode: string | null;
  per: string | null;
  freightTerm: string | null;
  grossWeightKg: number | null;
  rateClass: string | null;
  chargeWeightKg: number | null;
  rate: number | null;
}

export interface MasterBlDetail extends MasterBlRow {
  shipmentType: string | null;
  // §6.49 ⑰ — freightTerm enum literal → string | null 완화 (BE 검증 일원화)
  freightTerm: string | null;
  pkgQty: number | null;
  pkgUnit?: string;
  weightUnit?: string;
  grossWeightKg: number | null;
  cbm: number | null;
  // §BE 보강 — root 승격 cargo 식별 필드
  mainItemName: string | null;
  hsCode: string | null;
  settlePartnerCode: string | null;
  desc: MasterBlDescView | null;
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
  // §BE-sync — seaDetail nested (BE Phase 2 SeaDetailResponse 정합, desc root 승격)
  seaDetail: MasterBlSeaDetail | null;
  // §BE Phase 2 — airDetail nested (AIR 전용, SEA에서 null)
  airDetail?: MasterBlAirDetail | null;
  // §BE Phase 2 — dims/scheduleLegs/airCharges (AIR 전용 배열, 미존재 시 빈 배열)
  dims?: Dim[];
  scheduleLegs?: ScheduleLeg[];
  airCharges?: AirCharge[];
}

// §BE-sync — ConsoledSeaContainerView (BE consoledSeaContainers 전체 필드 정합)
// SEA Master에 콘솔된 모든 컨테이너를 row 단위로 그대로 반환, 표시 전용
// BigDecimal(grossWeightKg/cbm/vgmKg)은 BE Jackson 기본 직렬화 → number
export interface ConsoledSeaContainer {
  houseBlId: number;
  containerNo: string | null;
  containerType: string | null;
  sealNo1: string | null;
  sealNo2: string | null;
  sealNo3: string | null;
  pkgQty: number | null;
  pkgUnit: string | null;
  grossWeightKg: number | null;
  cbm: number | null;
  vgmKg: number | null;
}

// §BE-sync — ConsoledHouseBlSummaryView (BE consolidatedHouseBls 전체 필드 정합)
// houseBlId 키 통일 (BE Integer → number, BigDecimal → number)
// AIR 전용 필드(chargeWeightKg)는 SEA에서 null 반환 — FE 표시 제외 가능
export interface ConsolidatedHouseBlSummary {
  houseBlId: number;
  hblNo: string | null;
  shipperCode: string | null;
  consigneeCode: string | null;
  docPartnerCode: string | null;
  pkgQty: number | null;
  pkgUnit: string | null;
  weightUnit: string | null;
  grossWeightKg: number | null;
  cbm: number | null;
  etd: string | null;
  eta: string | null;
  vesselName: string | null;
  voyageNo: string | null;
  polCode: string | null;
  podCode: string | null;
  chargeWeightKg?: number | null;
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

// §BE-sync — CreateMasterBlRequest.SeaDetailRequest (BE Phase 3 정합, desc root 승격으로 제거됨)
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
}

export interface DescRequest {
  marks?: string;
  description?: string;
  descClause1?: string;
  descClause2?: string;
}

export interface DimRequest {
  id?: number;
  lengthCm?: number;
  widthCm?: number;
  heightCm?: number;
  quantity?: number;
  cbm?: number;
  volumeWeightKg?: number;
}

export interface ScheduleLegRequest {
  id?: number;
  toCode: string;
  byCarrier?: string;
  flightNo?: string;
  onBoardDt: string;
  onBoardTm?: string;
  arrivalDt: string;
  arrivalTm?: string;
}

export interface AirChargeRequest {
  id?: number;
  freightCode?: string;
  currencyCode?: string;
  per?: string;
  freightTerm?: string;
  grossWeightKg?: number;
  rateClass?: string;
  chargeWeightKg?: number;
  rate?: number;
}

// §BE-sync — CreateMasterBlRequest.AirDetailRequest (BE Phase 2 AirDetailRequest record mirror)
// handlingInfoCode/handlingInfoText: BE 필드명 기준 (§6.49 ⑮)
export interface AirDetailRequest {
  airlineCode?: string;
  chargeWeightKg?: number;
  volumeWeightKg?: number;
  rateClass?: string;
  currencyCode?: string;
  declaredValueCarriage?: string;
  declaredValueCustoms?: string;
  insurance?: string;
  accountInformation?: string;
  securityStatus?: string;
  flightType?: string;
  issueDate?: string;
  issuePlace?: string;
  signature?: string;
  otherTerm?: string;
  handlingInfoCode?: string;
  handlingInfoText?: string;
  remark?: string;
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
  // §BE Phase 2 — AIR 전용 확장 필드
  airDetail?: AirDetailRequest;
  desc?: DescRequest;
  dims?: DimRequest[];
  scheduleLegs?: ScheduleLegRequest[];
  airCharges?: AirChargeRequest[];
}

export type UpdateMasterBlRequest = Partial<CreateMasterBlRequest>;
