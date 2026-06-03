import type { BLVariantConfig } from '@/lib/bl-variants';
import type { CreateMasterBlRequest, UpdateMasterBlRequest, SeaDetailRequest, AirDetailRequest, MasterBlFreightLineRequest } from '@/domain/master-bl';
import type { MasterBlFormValues } from './master-bl-schema';
import type { FreightRow } from '@/components/fms/house-bl/house-bl-schema';

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

/** FreightRow 배열 → MasterBlFreightLineRequest 배열 변환 */
function buildFreightLines(rows: FreightRow[] | undefined): MasterBlFreightLineRequest[] | undefined {
  if (!rows || rows.length === 0) return undefined;
  return rows.map((r) => ({
    id:               r.id,
    freightCode:      toStr(r.freightCode),
    per:              toStr(r.per),
    qty:              toStr(r.qty),
    price:            toStr(r.price),
    currency:         toStr(r.currency),
    customerCode:     toStr(r.customerCode),
    taxType:          toStr(r.taxType),
    performanceDt:    toStr(r.performanceDt),
    exchangeRate:     toStr(r.exchangeRate),
    usdExchangeRate:  toStr(r.usdExchangeRate),
    settleAmount:     toStr(r.settleAmount),
    localAmount:      toStr(r.localAmount),
    localTaxAmount:   toStr(r.vat),
    usdAmount:        toStr(r.usdAmount),
    financialDocType: toStr(r.financialDocType),
  }));
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

/**
 * form airDetail → BE AirDetailRequest 변환.
 * form은 handlingInformationCode/Text, BE는 handlingInfoCode/Text 필드명 사용 (§BE Phase 2 정합).
 */
function buildAirDetailRequest(
  ad: NonNullable<MasterBlFormValues['airDetail']>,
): AirDetailRequest {
  return {
    airlineCode:           toStr(ad.airlineCode),
    chargeWeightKg:        ad.chargeWeightKg ?? undefined,
    volumeWeightKg:        ad.volumeWeightKg ?? undefined,
    rateClass:             toStr(ad.rateClass),
    currencyCode:          toStr(ad.currencyCode),
    declaredValueCarriage: toStr(ad.declaredValueCarriage),
    declaredValueCustoms:  toStr(ad.declaredValueCustoms),
    insurance:             toStr(ad.insurance),
    accountInformation:    toStr(ad.accountInformation),
    securityStatus:        toStr(ad.securityStatus),
    flightType:            toStr(ad.flightType),
    issueDate:             toStr(ad.issueDate),
    issuePlace:            toStr(ad.issuePlace),
    signature:             toStr(ad.signature),
    otherTerm:             toStr(ad.otherTerm),
    handlingInfoCode:      toStr(ad.handlingInformationCode),
    handlingInfoText:      toStr(ad.handlingInformationText),
    remark:                toStr(ad.remark),
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
    // §Freight 탭 — 환율 헤더 + 매출/매입 라인
    sellRateDt:          toStr(values.sellRateDt),
    sellRateCurrencyCode: toStr(values.sellRateCurrencyCode),
    sellRate:            toStr(values.sellRate),
    buyRateDt:           toStr(values.buyRateDt),
    buyRateCurrencyCode: toStr(values.buyRateCurrencyCode),
    buyRate:             toStr(values.buyRate),
    usdRateDt:           toStr(values.usdRateDt),
    usdRate:             toStr(values.usdRate),
    freightSelling: buildFreightLines(values.freightSelling),
    freightBuying:  buildFreightLines(values.freightBuying),
  };

  // SEA 전용 — seaDetail nested
  if (jobDiv === 'SEA' && values.seaDetail) {
    base.seaDetail = buildSeaDetailRequest(values.seaDetail);
  }

  // AIR 전용 — airDetail nested
  if (jobDiv === 'AIR' && values.airDetail) {
    base.airDetail = buildAirDetailRequest(values.airDetail);
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
