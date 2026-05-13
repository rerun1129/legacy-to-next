import type { BLVariantConfig } from '@/lib/bl-variants';
import type { CreateHouseBlRequest, UpdateHouseBlRequest } from '@/domain/house-bl';
import type { HouseBlFormValues } from './house-bl-schema';

/** 문자열 → number 변환. 빈 문자열이면 undefined 반환. */
function toNum(v: string | undefined): number | undefined {
  if (!v || v.trim() === '') return undefined;
  const n = Number(v);
  return Number.isNaN(n) ? undefined : n;
}

/** 빈 문자열 → undefined 정규화 */
function toStr(v: string | undefined): string | undefined {
  return v?.trim() || undefined;
}

/** form 값을 CreateHouseBlRequest로 매핑한다. */
export function buildHouseBlRequest(
  values: HouseBlFormValues,
  variant: BLVariantConfig,
): CreateHouseBlRequest {
  const jobDiv = variant.mode as 'SEA' | 'AIR' | 'TRUCK' | 'NON_BL';
  // TRUCK/NON_BL은 bound가 없으므로 기본값 EXP 사용 (백엔드 @NotNull 요건)
  const bound = (variant.direction ?? 'EXP') as 'EXP' | 'IMP';

  const base: CreateHouseBlRequest = {
    jobDiv,
    bound,
    hblNo: toStr(values.hbl),
    shipmentType:     (values.sType as 'HOUSE' | 'DIRECT') || 'HOUSE',
    freightTerm:      (values.freightTerm as 'PREPAID' | 'COLLECT') || 'PREPAID',
    shipperCode:      toStr(values.shipperCode),
    shipperAddress:   toStr(values.shipperAddress),
    consigneeCode:    toStr(values.consigneeCode),
    consigneeAddress: toStr(values.consigneeAddress),
    notifyCode:       toStr(values.notifyCode),
    notifyAddress:    toStr(values.notifyAddress),
    docPartnerCode:   toStr(values.docPartnerCode),
    docPartnerAddress: toStr(values.docPartnerAddress),
    settlePartnerCode: toStr(values.settlePartnerCode),
    polCode:          toStr(values.pol),
    podCode:          toStr(values.pod),
    etd:              toStr(values.etd),
    eta:              toStr(values.eta),
    pkgQty:           toNum(values.pkgQty),
    pkgUnit:          toStr(values.pkgUnit),
    weightUnit:       toStr(values.weightUnit),
    grossWeightKg:    toNum(values.grossWeightKg),
    cbm:              toNum(values.cbm),
    actualCustomerCode: toStr(values.actualCustomerCode),
    operatorCode:     toStr(values.operatorCode),
    teamCode:         toStr(values.teamCode),
    salesManCode:     toStr(values.salesManCode),
    masterBlId:       toNum(values.masterBlId),
    incoterms:        toStr(values.incoterms),
    salesClass:       toStr(values.salesClass),
    mainItemName:     toStr(values.mainItemName),
    hsCode:           toStr(values.hsCode),
    mblNo:            toStr(values.mblNo),
    masterRefNo:      toStr(values.masterRefNo),
    desc:             values.desc,
  };

  // SEA 전용 확장 필드
  if (jobDiv === 'SEA' && values.seaDetail) {
    const sd = values.seaDetail;
    base.seaDetail = {
      loadType:                toStr(sd.loadType),
      linerCode:               toStr(sd.linerCode),
      vesselCode:              toStr(sd.vesselCode),
      vesselName:              toStr(sd.vesselName),
      voyageNo:                toStr(sd.voyageNo),
      onboardDate:             toStr(sd.onboardDate),
      porCode:                 toStr(sd.porCode),
      finalDestCode:           toStr(sd.finalDestCode),
      issueDate:               toStr(sd.issueDate),
      noOfBl:                  toNum(sd.noOfBl),
      issuePlace:              toStr(sd.issuePlace),
      doDate:                  toStr(sd.doDate),
      payableAt:               toStr(sd.payableAt),
      triangle:                sd.triangle,
      serviceTerm:             toStr(sd.serviceTerm),
      vesselNationality:       toStr(sd.vesselNationality),
      rton:                    toNum(sd.rton),
      sayInformation:          toStr(sd.sayInformation),
      noOfContainerOrPackages: toStr(sd.noOfContainerOrPackages),
      blType:                  toStr(sd.blType),
      deliveryCode:            toStr(sd.deliveryCode),
    };
  }

  // SEA 전용 — containers
  if (jobDiv === 'SEA') {
    base.containers = values.containers?.map(c => ({
      // §6.28 — id가 있으면 그대로 포함(UPDATE merge-by-id), 없으면 undefined(신규 INSERT)
      id:            c.id,
      containerNo:   toStr(c.containerNo),
      containerType: toStr(c.containerType),
      lengthFeet:    toNum(c.lengthFeet),
      sealNo1:       toStr(c.sealNo1),
      sealNo2:       toStr(c.sealNo2),
      sealNo3:       toStr(c.sealNo3),
      sealNo4:       toStr(c.sealNo4),
      sealNo5:       toStr(c.sealNo5),
      sealNo6:       toStr(c.sealNo6),
      pkgQty:        toNum(c.pkgQty),
      pkgUnit:       toStr(c.pkgUnit),
      grossWeightKg: toNum(c.grossWeightKg),
      netWeightKg:   toNum(c.netWeightKg),
      cbm:           toNum(c.cbm),
      vgmKg:         toNum(c.vgmKg),
      soc:           c.soc,
      seq:           toNum(c.seq),
    }));
  }

  // AIR 전용 — scheduleLegs, airCharges, dims
  if (jobDiv === 'AIR') {
    base.scheduleLegs = values.scheduleLegs?.map(s => ({
      // §6.28 — id가 있으면 그대로 포함(UPDATE merge-by-id), 없으면 undefined(신규 INSERT)
      id:        s.id,
      toCode:    toStr(s.toCode),
      byCarrier: toStr(s.byCarrier),
      flightNo:  toStr(s.flightNo),
      onBoardDt: toStr(s.onBoardDt),
      onBoardTm: toStr(s.onBoardTm),
      arrivalDt: toStr(s.arrivalDt),
      arrivalTm: toStr(s.arrivalTm),
    }));

    base.airCharges = values.airCharges?.map(a => ({
      freightCode:    toStr(a.freightCode),
      currencyCode:   toStr(a.currencyCode),
      per:            toStr(a.per),
      freightTerm:    toStr(a.freightTerm),
      grossWeightKg:  toNum(a.grossWeightKg),
      rateClass:      toStr(a.rateClass),
      chargeWeightKg: toNum(a.chargeWeightKg),
      rate:           toNum(a.rate),
    }));

    base.dims = values.dims?.map(d => ({
      lengthCm:       toNum(d.lengthCm),
      widthCm:        toNum(d.widthCm),
      heightCm:       toNum(d.heightCm),
      quantity:       toNum(d.quantity),
      cbm:            toNum(d.cbm),
      volumeWeightKg: toNum(d.volumeWeightKg),
    }));
  }

  // TRUCK 전용 — truckOrders
  if (jobDiv === 'TRUCK') {
    base.truckOrders = values.truckOrders?.map(t => ({
      truckOrderNo:  toStr(t.truckOrderNo),
      pkgQty:        toNum(t.pkgQty),
      pkgUnit:       toStr(t.pkgUnit),
      grossWeightKg: toNum(t.grossWeightKg),
      cbm:           toNum(t.cbm),
      truckNo:       toStr(t.truckNo),
      truckType:     toStr(t.truckType),
      driver:        toStr(t.driver),
      mobileNo:      toStr(t.mobileNo),
      containerNo:   toStr(t.containerNo),
      containerType: toStr(t.containerType),
      sealNo1:       toStr(t.sealNo1),
      sealNo2:       toStr(t.sealNo2),
      sealNo3:       toStr(t.sealNo3),
    }));
  }

  return base;
}

/** update 시 사용하는 페이로드 빌더 — hblNo는 식별 키이므로 payload에서 제외 */
export function buildHouseBlUpdateRequest(
  values: HouseBlFormValues,
  variant: BLVariantConfig,
): UpdateHouseBlRequest {
  const { hblNo: _hblNo, ...rest } = buildHouseBlRequest(values, variant);
  return rest;
}
