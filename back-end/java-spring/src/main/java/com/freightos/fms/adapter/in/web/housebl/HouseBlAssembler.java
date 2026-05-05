package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.SearchHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.HouseBlFilter;
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

    public HouseBlFilter toFilter(SearchHouseBlRequest req) {
        return HouseBlFilter.of(req.jobDiv(), req.bound(), req.hblNo(), req.mblNo(),
                        req.shipperCode(), req.consigneeCode(), req.polCode(), req.podCode(),
                        req.etdFrom(), req.etdTo(), req.vessel(), req.voyage(),
                        req.linerCode(), req.operatorCode(), req.teamCode(), req.partyCode(), req.portCode())
                .withKinds(req.dateKind(), req.partyKind(), req.portKind());
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
            case NON_BL -> {
                WorkDivision wd = req.workDivision() != null
                        ? WorkDivision.valueOf(req.workDivision())
                        : WorkDivision.SEA;
                yield HouseBlNonBl.create(wd, req.bound());
            }
        };

        applyCommonCreate(entity, req);
        applySeaCreate(entity, req.seaDetail());
        applyNonBlCreate(entity, req);
        sub.applyAllCreate(entity, req);
        return entity;
    }

    /**
     * UPDATE 요청을 기존 엔티티에 반영한다.
     * null 필드는 기존 값을 유지한다 (PATCH 의미론).
     * 본체 공통 필드는 엔티티의 update() 메서드에 위임하고, SEA/NON_BL/sub 분기는 어셈블러 책임으로 유지한다.
     */
    public void applyToEntity(UpdateHouseBlRequest req, HouseBl entity) {
        entity.update(toUpdateFields(req));
        applySeaUpdate(entity, req.seaDetail());
        applyNonBlUpdate(entity, req);
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

    // ── 공통 필드 record 변환 (UPDATE) ───────────────────────────────────────

    private HouseBl.HouseBlUpdateFields toUpdateFields(UpdateHouseBlRequest req) {
        String deliveryCode = req.seaDetail() != null ? req.seaDetail().deliveryCode() : null;
        return new HouseBl.HouseBlUpdateFields(
                req.hblNo()             != null ? BlNumber.of(req.hblNo())                  : null,
                req.shipmentType(),
                req.freightTerm(),
                req.shipperCode()       != null ? CustomerCode.of(req.shipperCode())         : null,
                req.consigneeCode()     != null ? CustomerCode.of(req.consigneeCode())       : null,
                req.notifyCode()        != null ? CustomerCode.of(req.notifyCode())          : null,
                req.docPartnerCode()    != null ? CustomerCode.of(req.docPartnerCode())      : null,
                req.polCode()           != null ? PortCode.of(req.polCode())                 : null,
                req.podCode()           != null ? PortCode.of(req.podCode())                 : null,
                req.etd()               != null ? BlDate.of(req.etd())                       : null,
                req.eta()               != null ? BlDate.of(req.eta())                       : null,
                req.pkgQty()            != null ? Quantity.of(req.pkgQty())                  : null,
                req.pkgUnit(),
                req.grossWeightKg()     != null ? Weight.of(req.grossWeightKg())             : null,
                req.cbm()               != null ? Volume.of(req.cbm())                       : null,
                req.actualCustomerCode() != null ? CustomerCode.of(req.actualCustomerCode()) : null,
                req.operatorCode()      != null ? EmployeeCode.of(req.operatorCode())        : null,
                req.teamCode()          != null ? TeamCode.of(req.teamCode())                : null,
                req.salesManCode()      != null ? EmployeeCode.of(req.salesManCode())        : null,
                req.settlePartnerCode() != null ? CustomerCode.of(req.settlePartnerCode())   : null,
                req.incoterms()         != null ? Incoterms.fromCode(req.incoterms())        : null,
                req.salesClass()        != null ? SalesClass.fromCode(req.salesClass())      : null,
                req.mainItemName(),
                req.hsCode(),
                req.masterBlId()
        );
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
                    serviceTerm != null ? ServiceTerm.fromLabel(serviceTerm) : sea.getServiceTerm(),
                    weightUnit  != null ? WeightUnit.fromCode(weightUnit)   : sea.getWeightUnit(),
                    rton        != null ? Rton.of(rton)                     : sea.getRton(),
                    sayInfo     != null ? sayInfo                           : sea.getSayInformation(),
                    noOfCtnr    != null ? noOfCtnr                         : sea.getNoOfContainerOrPackages());
        }
    }

    // ── Non B/L 확장 필드 매핑 ────────────────────────────────────────

    private void applyNonBlCreate(HouseBl entity, CreateHouseBlRequest req) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        nonBl.updateNonBlFields(
                BlNumber.of(req.originalBlRef()),
                Rton.of(req.rton()),
                Weight.of(req.volumeWeightKg()));
        nonBl.updateScheduleFields(
                req.linerCode(), req.linerName(), req.vesselName(), req.voyageNo(),
                req.finalDestCode(), req.finalDestName(), req.finalEta());
    }

    private void applyNonBlUpdate(HouseBl entity, UpdateHouseBlRequest req) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        nonBl.updateNonBlFields(
                BlNumber.of(req.originalBlRef()),
                Rton.of(req.rton()),
                Weight.of(req.volumeWeightKg()));
        nonBl.updateScheduleFields(
                req.linerCode(), req.linerName(), req.vesselName(), req.voyageNo(),
                req.finalDestCode(), req.finalDestName(), req.finalEta());
    }
}
