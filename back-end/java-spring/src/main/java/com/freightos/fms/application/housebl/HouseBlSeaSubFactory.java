package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.LinerCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

/**
 * SEA 확장 필드 매핑 담당.
 * HouseBlFactory 크기 분리를 위해 Sea 전용 apply 메서드를 이 클래스에 위임한다.
 */
@Component
class HouseBlSeaSubFactory {

    void applySeaCreate(HouseBl entity, CreateHouseBlCommand.SeaDetailCommand s) {
        if (s == null || !(entity instanceof HouseBlSea sea)) return;
        sea.updateSeaSchedule(LinerCode.of(s.linerCode()),
                VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo()), BlDate.of(s.onboardDate()));
        sea.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                PortCode.of(s.porCode()), PortCode.of(s.finalDestCode()),
                BlDate.of(s.issueDate()), NoOfBl.fromNumber(s.noOfBl()),
                PortCode.of(s.issuePlace()), BlDate.of(s.doDate()), PortCode.of(s.payableAt()),
                Boolean.TRUE.equals(s.triangle()), Nullables.mapOrNull(s.loadType(), LoadType::valueOf)));
        applySeaCargoTerms(sea, s.serviceTerm(), s.rton(), s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    void applySeaRemark(HouseBl entity, String remark) {
        if (!(entity instanceof HouseBlSea sea)) return;
        sea.updateRemark(remark);
    }

    void applySeaUpdate(HouseBl entity, UpdateHouseBlCommand.SeaDetailCommand s) {
        if (s == null || !(entity instanceof HouseBlSea sea)) return;
        sea.updateSeaSchedule(
                Nullables.mapOrElse(s.linerCode(),   LinerCode::of,                                             sea::getLinerCode),
                Nullables.mapOrElse(s.vesselName(),  v -> VesselVoyage.of(s.vesselCode(), v, s.voyageNo()),      sea::getVesselVoyage),
                Nullables.mapOrElse(s.onboardDate(), BlDate::of,                                                 sea::getOnboardDate));
        if (s.porCode() != null || s.finalDestCode() != null || s.issueDate() != null
                || s.noOfBl() != null || s.issuePlace() != null || s.doDate() != null
                || s.payableAt() != null || s.triangle() != null || s.loadType() != null) {
            sea.updateSeaRouteAndFlags(new HouseBlSea.SeaRouteAndFlags(
                    Nullables.mapOrElse(s.porCode(),       PortCode::of,          sea::getPorCode),
                    Nullables.mapOrElse(s.finalDestCode(), PortCode::of,          sea::getFinalDestCode),
                    Nullables.mapOrElse(s.issueDate(),     BlDate::of,            sea::getIssueDate),
                    Nullables.mapOrElse(s.noOfBl(),        NoOfBl::fromNumber,    sea::getNoOfBl),
                    Nullables.mapOrElse(s.issuePlace(),    PortCode::of,          sea::getIssuePlace),
                    Nullables.mapOrElse(s.doDate(),        BlDate::of,            sea::getDoDate),
                    Nullables.mapOrElse(s.payableAt(),     PortCode::of,          sea::getPayableAt),
                    Nullables.firstNonNull(s.triangle(),                           sea::isTriangle),
                    Nullables.mapOrElse(s.loadType(),      LoadType::valueOf,     sea::getLoadType)));
        }
        applySeaCargoTerms(sea, s.serviceTerm(), s.rton(), s.sayInformation(), s.noOfContainerOrPackages());
        if (s.blType() != null) sea.updateBlType(BlType.valueOf(s.blType()));
        if (s.vesselNationality() != null) sea.updateVesselNationality(s.vesselNationality());
    }

    private void applySeaCargoTerms(HouseBlSea sea, String serviceTerm,
                                    java.math.BigDecimal rton, String sayInfo, String noOfCtnr) {
        if (serviceTerm != null || rton != null || sayInfo != null || noOfCtnr != null) {
            sea.updateSeaCargoTerms(
                    Nullables.mapOrElse(serviceTerm, ServiceTerm::fromLabel, sea::getServiceTerm),
                    Nullables.mapOrElse(rton,        Rton::of,               sea::getRton),
                    Nullables.firstNonNull(sayInfo,  sea::getSayInformation),
                    Nullables.firstNonNull(noOfCtnr, sea::getNoOfContainerOrPackages));
        }
    }
}
