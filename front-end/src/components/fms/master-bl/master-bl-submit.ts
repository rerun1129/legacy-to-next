import type { BLVariantConfig } from '@/lib/bl-variants';
import type { CreateMasterBlRequest, UpdateMasterBlRequest, SeaDetailRequest } from '@/domain/master-bl';
import type { MasterBlFormValues } from './master-bl-schema';

/** 문자열 → number 변환. 빈 문자열이면 undefined 반환. */
function toNum(v: string | number | undefined | null): number | undefined {
  if (v == null) return undefined;
  if (typeof v === 'number') return v;
  if (v.trim() === '') return undefined;
  const n = Number(v);
  return Number.isNaN(n) ? undefined : n;
}

/** 빈 문자열 → undefined 정규화 */
function toStr(v: string | undefined | null): string | undefined {
  return v?.trim() || undefined;
}

/** form seaDetail → BE SeaDetailRequest 변환. desc는 root 승격으로 seaDetail에서 제거됨 (§BE 보강). */
function buildSeaDetailRequest(
  sd: NonNullable<MasterBlFormValues['seaDetail']>,
): SeaDetailRequest {
  return {
    loadType:          toStr(sd.loadType),
    linerCode:         toStr(sd.linerCode),
    vesselCode:        toStr(sd.vesselCode),
    vesselName:        toStr(sd.vesselName),
    voyageNo:          toStr(sd.voyageNo),
    onboardDate:       toStr(sd.onboardDate),
    vesselNationality: toStr(sd.vesselNationality),
    serviceTerm:       toStr(sd.serviceTerm),
    blType:            toStr(sd.blType),
    porCode:           toStr(sd.porCode),
    finalDestCode:     toStr(sd.finalDestCode),
    rton:              toNum(sd.rton),
    lineBkgNo:         toStr(sd.lineBkgNo),
    issueDate:         toStr(sd.issueDate),
  };
}

/** form 값을 CreateMasterBlRequest로 매핑한다. Status 필드 미포함 (BE 미지원, 사용자 결정 2026-05-15). */
export function buildCreateMasterBlPayload(
  values: MasterBlFormValues,
  variant: BLVariantConfig,
): CreateMasterBlRequest {
  const jobDiv = variant.mode as 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL';
  const bound = (variant.direction ?? 'EXP') as 'EXP' | 'IMP';

  const base: CreateMasterBlRequest = {
    jobDiv,
    bound,
    mblNo:             toStr(values.mblNo),
    masterRefNo:       toStr(values.masterRefNo),
    shipmentType:      toStr(values.shipmentType),
    freightTerm:       toStr(values.freightTerm as string | undefined),
    shipperCode:       toStr(values.shipperCode),
    shipperAddress:    toStr(values.shipperAddress),
    consigneeCode:     toStr(values.consigneeCode),
    consigneeAddress:  toStr(values.consigneeAddress),
    notifyCode:        toStr(values.notifyCode),
    notifyAddress:     toStr(values.notifyAddress),
    polCode:           toStr(values.polCode),
    podCode:           toStr(values.podCode),
    etd:               toStr(values.etd),
    eta:               toStr(values.eta),
    pkgQty:            toNum(values.pkgQty),
    pkgUnit:           toStr(values.pkgUnit),
    weightUnit:        toStr(values.weightUnit),
    grossWeightKg:     toNum(values.grossWeightKg),
    cbm:               toNum(values.cbm),
    hsCode:            toStr(values.hsCode),
    mainItemName:      toStr(values.mainItemName),
    settlePartnerCode: toStr(values.settlePartnerCode),
    operatorCode:      toStr(values.operatorCode),
    teamCode:          toStr(values.teamCode),
    remark:            toStr(values.remark),
    desc: values.desc,
    dims:         values.dims && values.dims.length > 0 ? values.dims : undefined,
    scheduleLegs: values.scheduleLegs && values.scheduleLegs.length > 0 ? values.scheduleLegs : undefined,
    airCharges:   values.airCharges && values.airCharges.length > 0 ? values.airCharges : undefined,
  };

  // SEA 전용 — seaDetail nested
  if (jobDiv === 'SEA' && values.seaDetail) {
    base.seaDetail = buildSeaDetailRequest(values.seaDetail);
  }

  return base;
}

/** update 시 사용하는 페이로드 빌더 — mblNo는 식별 키이므로 payload에서 제외 */
export function buildUpdateMasterBlPayload(
  _id: number,
  values: MasterBlFormValues,
  variant: BLVariantConfig,
): UpdateMasterBlRequest {
  const { mblNo: _mblNo, ...rest } = buildCreateMasterBlPayload(values, variant);
  return rest;
}
