import type { TruckBlDetail, TruckBlFreightLineView } from "@/domain/truck-bl";
import { createEmptyTruckBlFormValues } from "./truck-bl-defaults";
import type { TruckBlFormValues } from "./truck-bl-schema";
import type { FreightRow } from "@/components/fms/house-bl/house-bl-schema";

/** TruckBlFreightLineView → FreightRow 변환 */
function mapFreightLine(l: TruckBlFreightLineView): FreightRow {
  return {
    id:                  l.id,
    // RHF useFieldArray가 id를 UUID로 덮으므로 freight_line_id를 별도 필드에 보존
    freightLineId:       l.id,
    freightCode:         l.freightCode  ?? "",
    freightName:         l.freightName  ?? "",
    per:                 l.per          ?? "",
    qty:                 l.qty          != null ? String(l.qty)             : "",
    price:               l.price        != null ? String(l.price)           : "",
    currency:            l.currency     ?? "",
    exchangeRate:        l.exchangeRate != null ? String(l.exchangeRate)    : "",
    customerCode:        l.customerCode ?? "",
    customerName:        l.customerName ?? "",
    taxType:             l.taxType      ?? "",
    performanceDt:       l.performanceDt ?? "",
    settleAmount:        l.settleAmount  != null ? String(l.settleAmount)  : "",
    localAmount:         l.localAmount   != null ? String(l.localAmount)   : "",
    vat:                 l.localTaxAmount != null ? String(l.localTaxAmount) : "",
    usdExchangeRate:     l.usdExchangeRate != null ? String(l.usdExchangeRate) : "",
    usdAmount:           l.usdAmount     != null ? String(l.usdAmount)     : "",
    financialDocType:    l.financialDocType ?? "",
    taxNo:               l.taxNo              ?? "",
    slipNo:              l.slipNo             ?? "",
    financialDocumentNo: l.financialDocumentNo ?? "",
  };
}

/**
 * BE detail 응답을 form 값으로 매핑.
 * use-truck-bl-entry.ts의 useEffect inline form.reset 블록을 추출한 함수 — 동작 100% 동일.
 * polName/podName은 CodeBox가 런타임에 채우므로 항상 "".
 */
export function mapTruckBlDetailToForm(detail: TruckBlDetail): TruckBlFormValues {
  return {
    ...createEmptyTruckBlFormValues(),
    truckBlNo:          detail.hblNo             ?? "",
    bound:              detail.bound              ?? "",
    loadType:           detail.loadType           ?? "",
    serviceTerm:        detail.serviceTerm        ?? "",
    shipperCode:        detail.shipperCode        ?? "",
    shipperAddr:        detail.shipperAddr        ?? "",
    consigneeCode:      detail.consigneeCode      ?? "",
    consigneeAddr:      detail.consigneeAddr      ?? "",
    notifyCode:         detail.notifyCode         ?? "",
    notifyAddr:         detail.notifyAddr         ?? "",
    docPartnerCode:     detail.docPartnerCode     ?? "",
    docPartnerAddress:  detail.docPartnerAddress  ?? "",
    polCode:            detail.polCode            ?? "",
    polName:            "",
    podCode:            detail.podCode            ?? "",
    podName:            "",
    etd:                detail.etd                ?? "",
    eta:                detail.eta                ?? "",
    voyNo:              detail.voyageNo           ?? "",
    vesselName:         detail.vesselName         ?? "",
    pkgQty:             detail.pkgQty             != null ? detail.pkgQty : undefined,
    pkgUnit:            detail.pkgUnit            ?? "",
    weightUnit:         detail.weightUnit         ?? "",
    grossWeightKg:      detail.grossWeightKg      != null ? detail.grossWeightKg : undefined,
    cbm:                detail.cbm                != null ? detail.cbm : undefined,
    chargeWeightKg:     detail.chargeWeightKg     != null ? detail.chargeWeightKg : undefined,
    actualCustomerCode: detail.actualCustomerCode ?? "",
    operatorCode:       detail.operatorCode       ?? "",
    teamCode:           detail.teamCode           ?? "",
    teamName:           detail.teamName           ?? "",
    salesManCode:       detail.salesManCode       ?? "",
    settlePartnerCode:  detail.settlePartnerCode  ?? "",
    truckerCode:        detail.truckerCode        ?? "",
    truckerPic:         detail.truckerPic         ?? "",
    pickupDate:         detail.pickupDate         ?? "",
    hsCode:             detail.hsCode             ?? "",
    hsCodeName:         detail.hsCodeName         ?? "",
    truckOrders: detail.truckOrders?.map((o) => ({
      id:            o.id,
      truckOrderNo:  o.truckOrderNo   ?? "",
      pkgQty:        o.pkgQty         != null ? String(o.pkgQty)         : "",
      pkgUnit:       o.pkgUnit        ?? "",
      grossWeightKg: o.grossWeightKg  != null ? String(o.grossWeightKg)  : "",
      cbm:           o.cbm            != null ? String(o.cbm)            : "",
      truckNo:       o.truckNo        ?? "",
      truckType:     o.truckType      ?? "",
      driver:        o.driver         ?? "",
      mobileNo:      o.mobileNo       ?? "",
      containerNo:   o.containerNo    ?? "",
      containerType: o.containerType  ?? "",
      sealNo1:       o.sealNo1        ?? "",
      sealNo2:       o.sealNo2        ?? "",
      sealNo3:       o.sealNo3        ?? "",
    })) ?? [],
    marks:       detail.desc?.marks       ?? "",
    description: detail.desc?.description ?? "",
    descClause1: detail.desc?.descClause1 ?? "",
    descClause2: detail.desc?.descClause2 ?? "",
    remark:      detail.remark            ?? "",
    dimensionDivisor: detail.volumeDivisor ?? "",
    dimensions: detail.dims?.map((d, idx) => ({
      id:     d.id ?? idx,
      length: d.lengthCm       != null ? String(d.lengthCm)       : "",
      width:  d.widthCm        != null ? String(d.widthCm)        : "",
      height: d.heightCm       != null ? String(d.heightCm)       : "",
      qty:    d.quantity       != null ? String(d.quantity)       : "",
      cbm:    d.cbm            != null ? String(d.cbm)            : "",
      volWt:  d.volumeWeightKg != null ? String(d.volumeWeightKg) : "",
    })) ?? [],
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
  };
}
