import type { CreateNonBlRequest, UpdateNonBlRequest } from '@/domain/non-bl';
import type { NonBlFormValues } from './non-bl-schema';

/** 문자열 → number 변환. 빈 문자열이면 undefined 반환. */
function toNum(v: string | number | undefined): number | undefined {
  if (v === undefined || v === null) return undefined;
  if (typeof v === 'number') return Number.isNaN(v) ? undefined : v;
  const trimmed = v.trim();
  if (trimmed === '') return undefined;
  const n = Number(trimmed);
  return Number.isNaN(n) ? undefined : n;
}

/** 빈 문자열 → undefined 정규화 */
function toStr(v: string | undefined): string | undefined {
  return v?.trim() || undefined;
}

/** NonBlFormValues를 CreateNonBlRequest로 변환한다. */
export function buildNonBlRequest(values: NonBlFormValues): CreateNonBlRequest {
  const containers = values.containers?.map((c) => ({
    id:            c.id,
    containerNo:   toStr(c.cno),
    containerType: toStr(c.contType),
    sealNo1:       toStr(c.sealNo1),
    sealNo2:       toStr(c.sealNo2),
    sealNo3:       toStr(c.sealNo3),
    pkgQty:        toNum(c.pkg),
    pkgUnit:       toStr(c.pkgUnit),
    grossWeightKg: toNum(c.grossWt),
    cbm:           toNum(c.cbm),
  }));

  const dims = values.dimensions?.map((d) => ({
    id:             d.id,
    lengthCm:       toNum(d.length),
    widthCm:        toNum(d.width),
    heightCm:       toNum(d.height),
    quantity:       toNum(d.qty),
    cbm:            toNum(d.cbm),
    volumeWeightKg: toNum(d.volWt),
  }));

  return {
    hblNo: toStr(values.nonBlNo),
    bound:              values.bound ?? '',
    workDivision:       toStr(values.workDiv),
    shipperCode:        toStr(values.shipperCode),
    consigneeCode:      toStr(values.consigneeCode),
    notifyCode:         toStr(values.notifyCode),
    settlePartnerCode:  toStr(values.settlePartnerCode),
    actualCustomerCode: toStr(values.actualCustomerCode),
    polCode:            toStr(values.polCode),
    podCode:            toStr(values.podCode),
    etd:                toStr(values.etd),
    eta:                toStr(values.eta),
    linerCode:          toStr(values.linerCode),
    linerName:          toStr(values.linerName),
    vesselName:         toStr(values.vesselName),
    voyageNo:           toStr(values.voyNo),
    finalDestCode:      toStr(values.finalDestCode),
    finalDestName:      toStr(values.finalDestName),
    finalEta:           toStr(values.finalEta),
    mainItemName:       toStr(values.mainItem),
    hsCode:             toStr(values.hsCode),
    pkgQty:             toNum(values.cargoQty),
    pkgUnit:            toStr(values.pkgUnit),
    weightUnit:         toStr(values.weightUnit),
    grossWeightKg:      toNum(values.grossWt),
    cbm:                toNum(values.totalCbm),
    rton:               toNum(values.rton),
    volumeWtKg:         toNum(values.volWt),
    operatorCode:       toStr(values.operatorCode),
    salesManCode:       toStr(values.salesManCode),
    teamCode:           toStr(values.teamCode),
    salesClass:         toStr(values.salesClass),
    // form 필드명 dimensionDivisor → BE Request 키 volumeDivisor
    volumeDivisor:      values.dimensionDivisor ?? undefined,
    originalBlRef:      toStr(values.refNo),
    remark:             toStr(values.remark),
    containers:         containers && containers.length > 0 ? containers : undefined,
    dims:               dims && dims.length > 0 ? dims : undefined,
  };
}

/** update 시 사용하는 페이로드 빌더 — hblNo는 식별 키이므로 payload에서 제외 */
export function buildNonBlUpdateRequest(values: NonBlFormValues): UpdateNonBlRequest {
  const { hblNo: _hblNo, ...rest } = buildNonBlRequest(values);
  return rest;
}
