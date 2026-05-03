package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl.WorkDivision;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.housebl.enums.SalesClass;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import org.springframework.stereotype.Component;

/**
 * 도메인 엔티티를 House B/L 응답 DTO로 변환한다.
 * 컨트롤러는 매핑을 직접 호출하지 않고 본 어셈블러에 위임한다.
 * sub 엔티티 생성 로직은 {@link HouseBlSubAssembler}에 위임한다.
 */
@Component
public class HouseBlAssembler {

    private final HouseBlSubAssembler sub;

    public HouseBlAssembler(HouseBlSubAssembler sub) {
        this.sub = sub;
    }

    public PagedResult<HouseBlSummaryResponse> toSummaryPage(PagedResult<HouseBlSummary> source) {
        return source.map(HouseBlSummaryResponse::from);
    }

    public HouseBlDetailResponse toDetail(HouseBl source) {
        return HouseBlDetailResponse.from(source);
    }

    /**
     * CREATE 요청을 도메인 엔티티로 변환한다.
     * jobDiv에 따라 적합한 서브클래스를 생성한다.
     */
    public HouseBl toEntity(CreateHouseBlRequest req) {
        HouseBl entity = switch (req.jobDiv()) {
            case SEA    -> HouseBlSea.create(req.bound());
            case AIR    -> HouseBlAir.create(req.bound());
            case TRUCK  -> HouseBlTruck.create(req.bound());
            case NON_BL -> HouseBlNonBl.create(WorkDivision.SEA, req.bound());
        };

        applyCommonCreate(entity, req);
        applySeaCreate(entity, req.seaDetail());
        sub.applyAllCreate(entity, req);
        return entity;
    }

    /**
     * UPDATE 요청을 기존 엔티티에 반영한다.
     * null 필드는 기존 값을 유지한다 (PATCH 의미론).
     */
    public void applyToEntity(UpdateHouseBlRequest req, HouseBl entity) {
        applyCommonUpdate(entity, req);
        applySeaUpdate(entity, req.seaDetail());
        sub.applyAllUpdate(entity, req);
    }

    // ── 공통 필드 매핑 (CREATE) ───────────────────────────────────────

    private void applyCommonCreate(HouseBl entity, CreateHouseBlRequest req) {
        if (req.hblNo() != null) entity.assignHblNo(BlNumber.of(req.hblNo()));
        entity.updateBlStatus(req.shipmentType(), req.freightTerm());
        entity.assignParties(
                CustomerCode.of(req.shipperCode()), CustomerCode.of(req.consigneeCode()),
                CustomerCode.of(req.notifyCode()), CustomerCode.of(req.docPartnerCode()),
                PortCode.of(req.seaDetail() != null ? req.seaDetail().deliveryCode() : null)
        );
        entity.updateSchedule(
                PortCode.of(req.polCode()), PortCode.of(req.podCode()),
                BlDate.of(req.etd()), BlDate.of(req.eta())
        );
        entity.updateCargoSummary(new CargoSummary(
                Quantity.of(req.pkgQty()), req.pkgUnit(),
                Weight.of(req.grossWeightKg()), Volume.of(req.cbm())
        ));
        entity.assignOperator(
                CustomerCode.of(req.actualCustomerCode()), EmployeeCode.of(req.operatorCode()),
                TeamCode.of(req.teamCode()), EmployeeCode.of(req.salesManCode())
        );
        if (req.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(req.settlePartnerCode()));
        entity.updateTradeInfo(
                Incoterms.fromCode(req.incoterms()), SalesClass.fromCode(req.salesClass()),
                req.mainItemName(), req.hsCode()
        );
        if (req.masterBlId() != null) entity.linkToMaster(req.masterBlId());
    }

    // ── 공통 필드 매핑 (UPDATE) ───────────────────────────────────────

    private void applyCommonUpdate(HouseBl entity, UpdateHouseBlRequest req) {
        if (req.hblNo() != null) entity.assignHblNo(BlNumber.of(req.hblNo()));
        if (req.shipmentType() != null || req.freightTerm() != null) {
            entity.updateBlStatus(
                    req.shipmentType() != null ? req.shipmentType() : entity.getShipmentType(),
                    req.freightTerm()  != null ? req.freightTerm()  : entity.getFreightTerm());
        }
        if (req.shipperCode() != null || req.consigneeCode() != null
                || req.notifyCode() != null || req.docPartnerCode() != null) {
            String deliveryCode = req.seaDetail() != null ? req.seaDetail().deliveryCode() : null;
            entity.assignParties(
                    req.shipperCode()    != null ? CustomerCode.of(req.shipperCode())    : entity.getShipperCode(),
                    req.consigneeCode()  != null ? CustomerCode.of(req.consigneeCode())  : entity.getConsigneeCode(),
                    req.notifyCode()     != null ? CustomerCode.of(req.notifyCode())     : entity.getNotifyCode(),
                    req.docPartnerCode() != null ? CustomerCode.of(req.docPartnerCode()) : entity.getDocPartnerCode(),
                    deliveryCode != null ? PortCode.of(deliveryCode) : entity.getDeliveryCode()
            );
        }
        if (req.polCode() != null || req.podCode() != null || req.etd() != null || req.eta() != null) {
            entity.updateSchedule(
                    req.polCode() != null ? PortCode.of(req.polCode()) : entity.getPolCode(),
                    req.podCode() != null ? PortCode.of(req.podCode()) : entity.getPodCode(),
                    req.etd()     != null ? BlDate.of(req.etd())       : entity.getEtd(),
                    req.eta()     != null ? BlDate.of(req.eta())       : entity.getEta());
        }
        if (req.pkgQty() != null || req.pkgUnit() != null
                || req.grossWeightKg() != null || req.cbm() != null) {
            entity.updateCargoSummary(new CargoSummary(
                    req.pkgQty()        != null ? Quantity.of(req.pkgQty())      : entity.getPkgQty(),
                    req.pkgUnit()       != null ? req.pkgUnit()                  : entity.getPkgUnit(),
                    req.grossWeightKg() != null ? Weight.of(req.grossWeightKg()) : entity.getGrossWeightKg(),
                    req.cbm()           != null ? Volume.of(req.cbm())           : entity.getCbm()));
        }
        if (req.operatorCode() != null || req.teamCode() != null || req.salesManCode() != null) {
            entity.assignOperator(
                    req.actualCustomerCode() != null
                            ? CustomerCode.of(req.actualCustomerCode()) : entity.getActualCustomerCode(),
                    req.operatorCode() != null ? EmployeeCode.of(req.operatorCode()) : entity.getOperatorCode(),
                    req.teamCode()     != null ? TeamCode.of(req.teamCode())          : entity.getTeamCode(),
                    req.salesManCode() != null ? EmployeeCode.of(req.salesManCode()) : entity.getSalesManCode());
        }
        if (req.settlePartnerCode() != null) entity.assignSettlePartner(CustomerCode.of(req.settlePartnerCode()));
        if (req.incoterms() != null || req.salesClass() != null
                || req.mainItemName() != null || req.hsCode() != null) {
            entity.updateTradeInfo(
                    req.incoterms()    != null ? Incoterms.fromCode(req.incoterms()) : entity.getIncoterms(),
                    req.salesClass()   != null ? SalesClass.fromCode(req.salesClass()) : entity.getSalesClass(),
                    req.mainItemName() != null ? req.mainItemName() : entity.getMainItemName(),
                    req.hsCode()       != null ? req.hsCode()       : entity.getHsCode());
        }
        if (req.masterBlId() != null) entity.linkToMaster(req.masterBlId());
    }

    // ── SEA 확장 필드 매핑 ────────────────────────────────────────────

    private void applySeaCreate(HouseBl entity, CreateHouseBlRequest.SeaDetailRequest s) {
        if (s == null || !(entity instanceof HouseBlSea sea)) return;
        sea.updateSeaSchedule(LinerCode.of(s.linerCode()),
                VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo()), BlDate.of(s.onboardDate()));
        sea.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                PortCode.of(s.porCode()), PortCode.of(s.finalDestCode()),
                BlDate.of(s.issueDate()), NoOfBl.fromNumber(s.noOfBl()),
                PortCode.of(s.issuePlace()), BlDate.of(s.doDate()), PortCode.of(s.payableAt()),
                Boolean.TRUE.equals(s.triangle()), s.loadType() != null ? LoadType.valueOf(s.loadType()) : null));
        applySeaCargoTerms(sea, s.serviceTerm(), s.weightUnit(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    private void applySeaUpdate(HouseBl entity, UpdateHouseBlRequest.SeaDetailRequest s) {
        if (s == null || !(entity instanceof HouseBlSea sea)) return;
        sea.updateSeaSchedule(
                s.linerCode()  != null ? LinerCode.of(s.linerCode())                                    : sea.getLinerCode(),
                s.vesselName() != null ? VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo())  : sea.getVesselVoyage(),
                s.onboardDate() != null ? BlDate.of(s.onboardDate())                                    : sea.getOnboardDate());
        if (s.porCode() != null || s.finalDestCode() != null || s.issueDate() != null
                || s.noOfBl() != null || s.issuePlace() != null || s.doDate() != null
                || s.payableAt() != null || s.triangle() != null || s.loadType() != null) {
            sea.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                    s.porCode()       != null ? PortCode.of(s.porCode())           : sea.getPorCode(),
                    s.finalDestCode() != null ? PortCode.of(s.finalDestCode())     : sea.getFinalDestCode(),
                    s.issueDate()     != null ? BlDate.of(s.issueDate())           : sea.getIssueDate(),
                    s.noOfBl()        != null ? NoOfBl.fromNumber(s.noOfBl())      : sea.getNoOfBl(),
                    s.issuePlace()    != null ? PortCode.of(s.issuePlace())        : sea.getIssuePlace(),
                    s.doDate()        != null ? BlDate.of(s.doDate())              : sea.getDoDate(),
                    s.payableAt()     != null ? PortCode.of(s.payableAt())         : sea.getPayableAt(),
                    s.triangle()      != null ? s.triangle()                       : sea.isTriangle(),
                    s.loadType()      != null ? LoadType.valueOf(s.loadType())     : sea.getLoadType()));
        }
        applySeaCargoTerms(sea, s.serviceTerm(), s.weightUnit(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    private void applySeaCargoTerms(HouseBlSea sea, String serviceTerm, String weightUnit,
                                     java.math.BigDecimal rton, String sayInfo, String noOfCtnr) {
        if (serviceTerm != null || weightUnit != null || rton != null || sayInfo != null || noOfCtnr != null) {
            sea.updateSeaCargoTerms(
                    serviceTerm != null ? ServiceTerm.fromCode(serviceTerm) : sea.getServiceTerm(),
                    weightUnit  != null ? WeightUnit.fromCode(weightUnit)   : sea.getWeightUnit(),
                    rton        != null ? Rton.of(rton)                     : sea.getRton(),
                    sayInfo     != null ? sayInfo                           : sea.getSayInformation(),
                    noOfCtnr    != null ? noOfCtnr                         : sea.getNoOfContainerOrPackages());
        }
    }
}
