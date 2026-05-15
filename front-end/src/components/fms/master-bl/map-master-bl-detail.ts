import type { MasterBlDetail } from "@/domain/master-bl";
import { createEmptyMasterBlFormValues } from "./master-bl-defaults";
import type { MasterBlFormValues } from "./master-bl-schema";

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
    grossWeightKg: detail.grossWeightKg != null ? detail.grossWeightKg : undefined,
    cbm:           detail.cbm           != null ? detail.cbm : undefined,
    weightUnit:    detail.weightUnit    ?? "",
    // performance
    operatorCode: detail.operatorCode ?? "",
    teamCode:     detail.teamCode      ?? "",
    // remark (root 본체)
    remark: detail.remark ?? "",
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
      desc: {
        marks:        detail.seaDetail?.desc?.marks        ?? "",
        description:  detail.seaDetail?.desc?.description  ?? "",
        descClause1:  detail.seaDetail?.desc?.descClause1  ?? "",
        descClause2:  detail.seaDetail?.desc?.descClause2  ?? "",
      },
      remark: detail.seaDetail?.remark ?? undefined,
    },
    // §BE-sync — consolidatedHouseBls → houseBls (ConsoledHouseBlSummaryView 전체 필드 매핑)
    houseBls: (detail.consolidatedHouseBls ?? []).map((hbl) => ({
      id:             hbl.id,
      hblNo:          hbl.hblNo,
      shipperCode:    hbl.shipperCode,
      consigneeCode:  hbl.consigneeCode,
      docPartnerCode: hbl.docPartnerCode,
      pkgQty:         hbl.pkgQty,
      pkgUnit:        hbl.pkgUnit,
      weightUnit:     hbl.weightUnit,
      grossWeightKg:  hbl.grossWeightKg,
      cbm:            hbl.cbm,
      etd:            hbl.etd,
      eta:            hbl.eta,
      vesselName:     hbl.vesselName,
      voyageNo:       hbl.voyageNo,
      polCode:        hbl.polCode,
      podCode:        hbl.podCode,
    })),
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
