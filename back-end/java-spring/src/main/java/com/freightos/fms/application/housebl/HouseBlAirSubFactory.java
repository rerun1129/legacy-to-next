package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.AirlineCode;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.HandlingInformation;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.enums.CargoType;
import com.freightos.fms.domain.housebl.enums.Fhd;
import com.freightos.fms.domain.housebl.enums.HandlingInfoCode;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

/**
 * AIR 확장 필드 매핑 담당.
 * HouseBlFactory 크기 분리를 위해 AIR 전용 apply 메서드를 이 클래스에 위임한다.
 */
@Component
class HouseBlAirSubFactory {

    void applyAirCreate(HouseBl entity, CreateHouseBlCommand.AirDetailCommand airDetail) {
        if (airDetail == null || !(entity instanceof HouseBlAir air)) return;
        air.updateAirFields(new HouseBlAir.AirFields(
                AirlineCode.of(airDetail.airlineCode()),
                Weight.of(airDetail.chargeWeightKg()),
                Weight.of(airDetail.volumeWeightKg()),
                Nullables.mapOrNull(airDetail.rateClass(), RateClass::valueOf),
                CurrencyCode.of(airDetail.currencyCode()),
                airDetail.declaredValueCarriage(),
                airDetail.declaredValueCustoms(),
                airDetail.insurance(),
                airDetail.accountInformation(),
                Nullables.mapOrNull(airDetail.otherTerm(), FreightTerm::valueOf),
                BlDate.of(airDetail.issueDate()),
                PortCode.of(airDetail.issuePlace()),
                airDetail.signature(),
                Nullables.mapOrNull(airDetail.fhd(), Fhd::valueOf),
                HandlingInformation.of(Nullables.mapOrNull(airDetail.handlingInformationCode(), HandlingInfoCode::fromCode), airDetail.handlingInformationDesc()),
                airDetail.originOfGoods(),
                Nullables.mapOrNull(airDetail.cargoType(), CargoType::fromCode)));
    }

    void applyAirUpdate(HouseBl entity, UpdateHouseBlCommand.AirDetailCommand airDetail) {
        if (airDetail == null || !(entity instanceof HouseBlAir air)) return;
        air.updateAirFields(new HouseBlAir.AirFields(
                Nullables.mapOrElse(airDetail.airlineCode(),    AirlineCode::of,                               air::getAirlineCode),
                Nullables.mapOrElse(airDetail.chargeWeightKg(), Weight::of,                                    air::getChargeWeightKg),
                Nullables.mapOrElse(airDetail.volumeWeightKg(), Weight::of,                                    air::getVolumeWeightKg),
                Nullables.mapOrElse(airDetail.rateClass(),      RateClass::valueOf,                            air::getRateClass),
                Nullables.mapOrElse(airDetail.currencyCode(),   CurrencyCode::of,                              air::getCurrencyCode),
                Nullables.firstNonNull(airDetail.declaredValueCarriage(),                                       air::getDeclaredValueCarriage),
                Nullables.firstNonNull(airDetail.declaredValueCustoms(),                                        air::getDeclaredValueCustoms),
                Nullables.firstNonNull(airDetail.insurance(),                                                   air::getInsurance),
                Nullables.firstNonNull(airDetail.accountInformation(),                                          air::getAccountInformation),
                Nullables.mapOrElse(airDetail.otherTerm(),      FreightTerm::valueOf,                          air::getOtherTerm),
                Nullables.mapOrElse(airDetail.issueDate(),      BlDate::of,                                    air::getIssueDate),
                Nullables.mapOrElse(airDetail.issuePlace(),     PortCode::of,                                  air::getIssuePlace),
                Nullables.firstNonNull(airDetail.signature(),                                                   air::getSignature),
                Nullables.mapOrElse(airDetail.fhd(),            Fhd::valueOf,                                  air::getFhd),
                buildHandlingInformation(air.getHandlingInformation(), airDetail.handlingInformationCode(), airDetail.handlingInformationDesc()),
                Nullables.firstNonNull(airDetail.originOfGoods(),                                               air::getOriginOfGoods),
                Nullables.mapOrElse(airDetail.cargoType(),      CargoType::fromCode,                           air::getCargoType)));
    }

    void applyAirRemark(HouseBl entity, String remark) {
        if (!(entity instanceof HouseBlAir air)) return;
        air.updateRemark(remark);
    }

    // PATCH 의미론: 두 필드 모두 null이면 기존 값 유지, 일부만 들어오면 들어온 값과 기존 값 결합.
    private HandlingInformation buildHandlingInformation(HandlingInformation existing, String codeStr, String descStr) {
        HandlingInfoCode code = codeStr != null
                ? HandlingInfoCode.fromCode(codeStr)
                : (existing != null ? existing.code() : null);
        String desc = descStr != null
                ? descStr
                : (existing != null ? existing.description() : null);
        return HandlingInformation.of(code, desc);
    }
}
