import type { MasterBlDetail, MasterBlFreightLineView } from "@/domain/master-bl";
import { createEmptyMasterBlFormValues } from "./master-bl-defaults";
import type { MasterBlFormValues } from "./master-bl-schema";
import type { FreightRow } from "@/components/fms/house-bl/house-bl-schema";

/** MasterBlFreightLineView → FreightRow 변환 */
function mapFreightLine(l: MasterBlFreightLineView): FreightRow {
  return {
    id:               l.id,
    freightCode:      l.freightCode  ?? "",
    freightName:      "",
    per:              l.per          ?? "",
    qty:              l.qty          != null ? String(l.qty)           : "",
    price:            l.price        != null ? String(l.price)         : "",
    currency:         l.currency     ?? "",
    customerCode:     l.customerCode ?? "",
    customerName:     "",
    taxType:          l.taxType      ?? "",
    performanceDt:    l.performanceDt ?? "",
    settleAmount:     l.settleAmount  != null ? String(l.settleAmount)  : "",
    localAmount:      l.localAmount   != null ? String(l.localAmount)   : "",
    usdAmount:        l.usdAmount     != null ? String(l.usdAmount)     : "",
    financialDocType: l.financialDocType ?? "",
    remark:           "",
  };
}

/**
 * BE detail 응답을 form 값으로 매핑. 본 함수 시그니처 변경 시 form schema와 동시 정합 필수(§6.49 ⑧).
 * SEA nested → form seaDetail nested로 매핑. address 3 필드 포함.
 */
export function mapMasterBlDetailToForm(detail: MasterBlDetail): MasterBlFormValues {
  return {
    ...createEmptyMasterBlFormValues(),
    // toolbar
    jobDiv:       detail.jobDiv,
    bound:        detail.bound,
    mblNo:        detail.mblNo        ?? "",
    masterRefNo:  detail.masterRefNo  ?? "",
    shipmentType: detail.shipmentType ?? "",
    // §6.49 ⑰ — freightTerm string | null → form string (빈 문자열 fallback)
    freightTerm:  detail.freightTerm  ?? undefined,
    // party
    shipperCode:      detail.shipperCode      ?? "",
    shipperAddress:   detail.shipperAddress   ?? "",
    consigneeCode:    detail.consigneeCode     ?? "",
    consigneeAddress: detail.consigneeAddress  ?? "",
    notifyCode:       detail.notifyCode        ?? "",
    notifyAddress:    detail.notifyAddress     ?? "",
    // schedule (root 본체 필드)
    polCode: detail.polCode ?? "",
    podCode: detail.podCode ?? "",
    etd:     detail.etd     ?? "",
    eta:     detail.eta     ?? "",
    // cargo summary
    pkgQty:        detail.pkgQty        != null ? detail.pkgQty : undefined,
    pkgUnit:       detail.pkgUnit       ?? "",
    grossWeightKg: detail.grossWeightKg != null ? detail.grossWeightKg : undefined,
    cbm:           detail.cbm           != null ? detail.cbm : undefined,
    weightUnit:    detail.weightUnit    ?? "",
    // §BE 보강 — root 승격 cargo 식별 필드
    mainItemName:      detail.mainItemName      ?? "",
    hsCode:            detail.hsCode            ?? "",
    hsCodeName:        detail.hsCodeName        ?? "",
    settlePartnerCode: detail.settlePartnerCode ?? "",
    // performance
    operatorCode: detail.operatorCode ?? "",
    teamCode:     detail.teamCode      ?? "",
    teamName:     detail.teamName      ?? "",
    // remark (root 본체)
    remark: detail.remark ?? "",
    // §BE 보강 — root desc (marks/description/descClause1/descClause2 + remark panel 표시)
    desc: {
      marks:        detail.desc?.marks        ?? "",
      description:  detail.desc?.description  ?? "",
      descClause1:  detail.desc?.descClause1  ?? "",
      descClause2:  detail.desc?.descClause2  ?? "",
      remark:       detail.remark             ?? "",
    },
    // §BE-sync — seaDetail nested (BE Phase 2 SeaDetailProjection 16 필드 매핑)
    seaDetail: {
      loadType:          detail.seaDetail?.loadType          ?? undefined,
      linerCode:         detail.seaDetail?.linerCode         ?? undefined,
      vesselCode:        detail.seaDetail?.vesselCode        ?? undefined,
      vesselName:        detail.seaDetail?.vesselName        ?? undefined,
      voyageNo:          detail.seaDetail?.voyageNo          ?? undefined,
      onboardDate:       detail.seaDetail?.onboardDate       ?? "",
      vesselNationality: detail.seaDetail?.vesselNationality ?? undefined,
      serviceTerm:       detail.seaDetail?.serviceTerm       ?? undefined,
      blType:            detail.seaDetail?.blType            ?? undefined,
      porCode:           detail.seaDetail?.porCode           ?? undefined,
      finalDestCode:     detail.seaDetail?.finalDestCode     ?? undefined,
      rton:              detail.seaDetail?.rton != null ? String(detail.seaDetail.rton) : undefined,
      lineBkgNo:         detail.seaDetail?.lineBkgNo         ?? undefined,
      issueDate:         detail.seaDetail?.issueDate          ?? "",
      remark: detail.seaDetail?.remark ?? undefined,
    },
    // §BE Phase 2 — airDetail nested 18 필드 매핑 (BE handlingInfoCode/Text → form handlingInformationCode/Text)
    airDetail: {
      airlineCode:            detail.airDetail?.airlineCode            ?? undefined,
      chargeWeightKg:         detail.airDetail?.chargeWeightKg         ?? undefined,
      volumeWeightKg:         detail.airDetail?.volumeWeightKg         ?? undefined,
      rateClass:              detail.airDetail?.rateClass              ?? undefined,
      currencyCode:           detail.airDetail?.currencyCode           ?? undefined,
      declaredValueCarriage:  detail.airDetail?.declaredValueCarriage  ?? undefined,
      declaredValueCustoms:   detail.airDetail?.declaredValueCustoms   ?? undefined,
      insurance:              detail.airDetail?.insurance              ?? undefined,
      accountInformation:     detail.airDetail?.accountInformation     ?? undefined,
      securityStatus:         detail.airDetail?.securityStatus         ?? undefined,
      flightType:             detail.airDetail?.flightType             ?? undefined,
      issueDate:              detail.airDetail?.issueDate              ?? "",
      issuePlace:             detail.airDetail?.issuePlace             ?? undefined,
      signature:              detail.airDetail?.signature              ?? undefined,
      otherTerm:              detail.airDetail?.otherTerm              ?? undefined,
      handlingInformationCode: detail.airDetail?.handlingInfoCode      ?? undefined,
      handlingInformationText: detail.airDetail?.handlingInfoText      ?? undefined,
      remark:                 detail.airDetail?.remark                 ?? undefined,
    },
    // §BE Phase 2 — dims 배열 매핑 (default [])
    dims: (detail.dims ?? []).map((d) => ({
      id:             d.id             ?? undefined,
      lengthCm:       d.lengthCm       ?? undefined,
      widthCm:        d.widthCm        ?? undefined,
      heightCm:       d.heightCm       ?? undefined,
      quantity:       d.quantity       ?? undefined,
      cbm:            d.cbm            ?? undefined,
      volumeWeightKg: d.volumeWeightKg ?? undefined,
    })),
    // §BE Phase 2 — scheduleLegs 배열 매핑 (default [])
    scheduleLegs: (detail.scheduleLegs ?? []).map((leg) => ({
      id:        leg.id        ?? undefined,
      toCode:    leg.toCode    ?? "",
      byCarrier: leg.byCarrier ?? undefined,
      flightNo:  leg.flightNo  ?? undefined,
      onBoardDt: leg.onBoardDt ?? "",
      onBoardTm: leg.onBoardTm ?? undefined,
      arrivalDt: leg.arrivalDt ?? "",
      arrivalTm: leg.arrivalTm ?? undefined,
    })),
    // §BE Phase 2 — airCharges 배열 매핑 (default [])
    airCharges: (detail.airCharges ?? []).map((c) => ({
      id:             c.id             ?? undefined,
      freightCode:    c.freightCode    ?? undefined,
      currencyCode:   c.currencyCode   ?? undefined,
      per:            c.per            ?? undefined,
      freightTerm:    c.freightTerm    ?? undefined,
      grossWeightKg:  c.grossWeightKg  ?? undefined,
      rateClass:      c.rateClass      ?? undefined,
      chargeWeightKg: c.chargeWeightKg ?? undefined,
      rate:           c.rate           ?? undefined,
    })),
    // §BE-sync — consolidatedHouseBls → houseBls (ConsoledHouseBlSummaryView 전체 필드 매핑)
    houseBls: (detail.consolidatedHouseBls ?? []).map((hbl) => ({
      houseBlId:      hbl.houseBlId,
      hblNo:          hbl.hblNo,
      shipperCode:    hbl.shipperCode,
      consigneeCode:  hbl.consigneeCode,
      docPartnerCode: hbl.docPartnerCode,
      pkgQty:         hbl.pkgQty,
      pkgUnit:        hbl.pkgUnit,
      weightUnit:     hbl.weightUnit,
      grossWeightKg:  hbl.grossWeightKg,
      chargeWeightKg: hbl.chargeWeightKg ?? undefined,
      cbm:            hbl.cbm,
      etd:            hbl.etd,
      eta:            hbl.eta,
      vesselName:     hbl.vesselName,
      voyageNo:       hbl.voyageNo,
      polCode:        hbl.polCode,
      podCode:        hbl.podCode,
    })),
    // §A2 — Freight 탭: 환율 헤더 + 매출/매입 라인 + 계산값 바인딩
    sellRateDt:          detail.freight?.sellRateDt           ?? "",
    sellRateCurrencyCode: detail.freight?.sellRateCurrencyCode ?? "",
    sellRate:            detail.freight?.sellRate  != null ? String(detail.freight.sellRate)  : "",
    buyRateDt:           detail.freight?.buyRateDt            ?? "",
    buyRateCurrencyCode: detail.freight?.buyRateCurrencyCode   ?? "",
    buyRate:             detail.freight?.buyRate   != null ? String(detail.freight.buyRate)   : "",
    usdRateDt:           detail.freight?.usdRateDt            ?? "",
    usdRate:             detail.freight?.usdRate   != null ? String(detail.freight.usdRate)   : "",
    freightSelling: detail.freight?.selling?.map(mapFreightLine) ?? [],
    freightBuying:  detail.freight?.buying?.map(mapFreightLine)  ?? [],
    // §BE-sync — consoledSeaContainers (ConsoledSeaContainerView 전체 필드 매핑, 표시 전용)
    consoledSeaContainers: (detail.consoledSeaContainers ?? []).map((c) => ({
      houseBlId:     c.houseBlId,
      containerNo:   c.containerNo,
      containerType: c.containerType,
      sealNo1:       c.sealNo1,
      sealNo2:       c.sealNo2,
      sealNo3:       c.sealNo3,
      pkgQty:        c.pkgQty,
      pkgUnit:       c.pkgUnit,
      grossWeightKg: c.grossWeightKg,
      cbm:           c.cbm,
      vgmKg:         c.vgmKg,
    })),
  };
}
