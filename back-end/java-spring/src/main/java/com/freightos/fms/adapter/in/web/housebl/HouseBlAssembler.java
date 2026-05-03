package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import org.springframework.stereotype.Component;

/**
 * 도메인 엔티티를 House B/L 응답 DTO로 변환한다.
 * 컨트롤러는 매핑을 직접 호출하지 않고 본 어셈블러에 위임한다.
 */
@Component
public class HouseBlAssembler {

    public PagedResult<HouseBlSummaryResponse> toSummaryPage(PagedResult<HouseBlSummary> source) {
        return source.map(HouseBlSummaryResponse::from);
    }

    public HouseBlDetailResponse toDetail(HouseBl source) {
        return HouseBlDetailResponse.from(source);
    }

    /**
     * CREATE 요청을 도메인 엔티티로 변환한다.
     * jobDiv에 따라 적합한 서브클래스를 생성하며, TRUCK/NON_BL은 본 사이클 미구현.
     */
    public HouseBl toEntity(CreateHouseBlRequest req) {
        HouseBl entity = switch (req.jobDiv()) {
            case SEA   -> HouseBlSea.create(req.bound());
            case AIR   -> HouseBlAir.create(req.bound());
            case TRUCK, NON_BL -> throw new UnsupportedOperationException(
                    "jobDiv=" + req.jobDiv() + " 은(는) 본 사이클 미구현");
        };

        if (req.hblNo() != null) {
            entity.assignHblNo(BlNumber.of(req.hblNo()));
        }

        entity.updateBlStatus(req.shipmentType(), req.freightTerm());

        entity.assignParties(
                CustomerCode.of(req.shipperCode()),
                CustomerCode.of(req.consigneeCode()),
                CustomerCode.of(req.notifyCode()),
                null,
                null
        );

        entity.updateSchedule(
                PortCode.of(req.polCode()),
                PortCode.of(req.podCode()),
                BlDate.of(req.etd()),
                BlDate.of(req.eta())
        );

        entity.updateCargoSummary(new CargoSummary(
                Quantity.of(req.pkgQty()),
                req.pkgUnit(),
                Weight.of(req.grossWeightKg()),
                Volume.of(req.cbm())
        ));

        entity.assignOperator(
                null,
                EmployeeCode.of(req.operatorCode()),
                TeamCode.of(req.teamCode()),
                EmployeeCode.of(req.salesManCode())
        );

        if (req.masterBlId() != null) {
            entity.linkToMaster(req.masterBlId());
        }

        return entity;
    }

    /**
     * UPDATE 요청을 기존 엔티티에 반영한다.
     * null 필드는 기존 값을 유지한다 (PATCH 의미론).
     */
    public void applyToEntity(UpdateHouseBlRequest req, HouseBl entity) {
        if (req.hblNo() != null) {
            entity.assignHblNo(BlNumber.of(req.hblNo()));
        }

        if (req.shipmentType() != null || req.freightTerm() != null) {
            entity.updateBlStatus(
                    req.shipmentType() != null ? req.shipmentType() : entity.getShipmentType(),
                    req.freightTerm()  != null ? req.freightTerm()  : entity.getFreightTerm()
            );
        }

        if (req.shipperCode() != null || req.consigneeCode() != null || req.notifyCode() != null) {
            entity.assignParties(
                    req.shipperCode()   != null ? CustomerCode.of(req.shipperCode())   : entity.getShipperCode(),
                    req.consigneeCode() != null ? CustomerCode.of(req.consigneeCode()) : entity.getConsigneeCode(),
                    req.notifyCode()    != null ? CustomerCode.of(req.notifyCode())    : entity.getNotifyCode(),
                    entity.getDocPartnerCode(),
                    entity.getDeliveryCode()
            );
        }

        if (req.polCode() != null || req.podCode() != null
                || req.etd() != null || req.eta() != null) {
            entity.updateSchedule(
                    req.polCode() != null ? PortCode.of(req.polCode()) : entity.getPolCode(),
                    req.podCode() != null ? PortCode.of(req.podCode()) : entity.getPodCode(),
                    req.etd()     != null ? BlDate.of(req.etd())       : entity.getEtd(),
                    req.eta()     != null ? BlDate.of(req.eta())       : entity.getEta()
            );
        }

        if (req.pkgQty() != null || req.pkgUnit() != null
                || req.grossWeightKg() != null || req.cbm() != null) {
            entity.updateCargoSummary(new CargoSummary(
                    req.pkgQty()        != null ? Quantity.of(req.pkgQty())     : entity.getPkgQty(),
                    req.pkgUnit()       != null ? req.pkgUnit()                 : entity.getPkgUnit(),
                    req.grossWeightKg() != null ? Weight.of(req.grossWeightKg()) : entity.getGrossWeightKg(),
                    req.cbm()           != null ? Volume.of(req.cbm())           : entity.getCbm()
            ));
        }

        if (req.operatorCode() != null || req.teamCode() != null || req.salesManCode() != null) {
            entity.assignOperator(
                    entity.getActualCustomerCode(),
                    req.operatorCode()  != null ? EmployeeCode.of(req.operatorCode())  : entity.getOperatorCode(),
                    req.teamCode()      != null ? TeamCode.of(req.teamCode())           : entity.getTeamCode(),
                    req.salesManCode()  != null ? EmployeeCode.of(req.salesManCode())  : entity.getSalesManCode()
            );
        }

        if (req.masterBlId() != null) {
            entity.linkToMaster(req.masterBlId());
        }
    }
}
