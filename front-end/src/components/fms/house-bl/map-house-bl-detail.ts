import type { HouseBlDetail } from "@/domain/house-bl";
import { createEmptyHouseBlFormValues } from "./house-bl-defaults";
import type { HouseBlFormValues } from "./house-bl-schema";

/** BE detail 응답을 form 값으로 매핑. 본 함수 시그니처 변경 시 form schema와 동시 정합 필수(§6.49 ⑧). */
export function mapHouseBlDetailToForm(detail: HouseBlDetail): HouseBlFormValues {
  return {
    ...createEmptyHouseBlFormValues(),
    // toolbar
    hbl:         detail.hblNo ?? "",
    mbl:         detail.masterBlId != null ? String(detail.masterBlId) : "",
    sType:       detail.shipmentType ?? "",
    lType:       detail.loadType ?? "",
    etd:         detail.etd ?? "",
    eta:         detail.eta ?? "",
    pol:         detail.polCode ?? "",
    pod:         detail.podCode ?? "",
    freightTerm: (detail.freightTerm ?? "") as "" | "PREPAID" | "COLLECT",
    expImp:      detail.bound,
    // party
    shipperCode:        detail.shipperCode    ?? "",
    shipperAddress:     detail.shipperAddress ?? "",
    consigneeCode:      detail.consigneeCode  ?? "",
    consigneeAddress:   detail.consigneeAddress ?? "",
    notifyCode:         detail.notifyCode     ?? "",
    notifyAddress:      detail.notifyAddress  ?? "",
    docPartnerCode:     detail.docPartnerCode ?? "",
    docPartnerAddress:  detail.docPartnerAddress ?? "",
    // cargo summary
    pkgQty:          detail.pkgQty    != null ? String(detail.pkgQty)    : "",
    pkgUnit:         detail.pkgUnit   ?? "",
    weightUnit:      detail.weightUnit ?? "",
    grossWeightKg:   detail.grossWeightKg != null ? String(detail.grossWeightKg) : "",
    cbm:             detail.cbm        != null ? String(detail.cbm)        : "",
    volumeWeightKg:  detail.volumeWeightKg != null ? String(detail.volumeWeightKg) : "",
    // performance
    actualCustomerCode: detail.actualCustomerCode ?? "",
    operatorCode:        detail.operatorCode  ?? "",
    teamCode:            detail.teamCode      ?? "",
    salesManCode:        detail.salesManCode  ?? "",
    // schedule — linerCode/linerName은 toolbar 표시용 본체 필드 (SEA: seaDetail에도 중복 있음)
    linerCode:  detail.linerCode  ?? "",
    linerName:  detail.linerName  ?? "",
    // trade
    incoterms:  detail.incoterms  ?? "",
    salesClass: detail.salesClass ?? "",
    // remark (본체 — SEA: HouseBlSea.remark, 화면: sea-remark-panel)
    remark: detail.remark ?? "",
    // SEA nested detail — BE Phase A-1에서 추가된 seaDetail 서브 엔티티 매핑
    seaDetail: {
      loadType:                detail.loadType                          ?? "",
      linerCode:               detail.seaDetail?.linerCode             ?? "",
      vesselCode:              detail.seaDetail?.vesselCode            ?? "",
      vesselName:              detail.seaDetail?.vesselName            ?? "",
      voyageNo:                detail.seaDetail?.voyageNo              ?? "",
      onboardDate:             detail.seaDetail?.onboardDate           ?? "",
      porCode:                 detail.seaDetail?.porCode               ?? "",
      finalDestCode:           detail.seaDetail?.finalDestCode         ?? "",
      issueDate:               detail.seaDetail?.issueDate             ?? "",
      noOfBl:                  detail.seaDetail?.noOfBl                ?? "",
      issuePlace:              detail.seaDetail?.issuePlace            ?? "",
      issuePlaceName:          "",
      doDate:                  detail.seaDetail?.doDate                ?? "",
      payableAt:               detail.seaDetail?.payableAt             ?? "",
      payableAtName:           "",
      triangle:                detail.seaDetail?.triangle              ?? false,
      serviceTerm:             detail.seaDetail?.serviceTerm           ?? "",
      vesselNationality:       detail.seaDetail?.vesselNationality     ?? "",
      rton:                    detail.seaDetail?.rton != null ? String(detail.seaDetail.rton) : "",
      sayInformation:          detail.seaDetail?.sayInformation        ?? "",
      noOfContainerOrPackages: detail.seaDetail?.noOfContainerOrPackages ?? "",
      blType:                  detail.blType                           ?? "",
      deliveryCode:            detail.deliveryCode                     ?? "",
      polName:                 "",
      podName:                 "",
      deliveryName:            "",
      freightTermDetail:       "",
      signature:               "",
    },
    // §BE-sync — BE SeaDetailResponse.containers / .desc (seaDetail nested 경로)
    containers: detail.seaDetail?.containers?.map(c => ({
      id:            c.id,
      containerNo:   c.containerNo   ?? "",
      containerType: c.containerType ?? "",
      lengthFeet:    c.lengthFeet    != null ? String(c.lengthFeet)    : "",
      sealNo1:       c.sealNo1       ?? "",
      sealNo2:       c.sealNo2       ?? "",
      sealNo3:       c.sealNo3       ?? "",
      sealNo4:       c.sealNo4       ?? "",
      sealNo5:       c.sealNo5       ?? "",
      sealNo6:       c.sealNo6       ?? "",
      pkgQty:        c.pkgQty        != null ? String(c.pkgQty)        : "",
      pkgUnit:       c.pkgUnit       ?? "",
      grossWeightKg: c.grossWeightKg != null ? String(c.grossWeightKg) : "",
      netWeightKg:   c.netWeightKg   != null ? String(c.netWeightKg)   : "",
      cbm:           c.cbm           != null ? String(c.cbm)           : "",
      vgmKg:         c.vgmKg         != null ? String(c.vgmKg)         : "",
      soc:           c.soc           ?? false,
      seq:           c.seq           != null ? String(c.seq)           : "",
    })) ?? [],
    desc: {
      marks:        detail.seaDetail?.desc?.marks        ?? "",
      description:  detail.seaDetail?.desc?.description  ?? "",
      descClause1:  detail.seaDetail?.desc?.descClause1  ?? "",
      descClause2:  detail.seaDetail?.desc?.descClause2  ?? "",
      // remark는 본체 필드 — desc에는 저장하지 않으므로 빈 문자열 유지
      remark:       "",
    },
  };
}
