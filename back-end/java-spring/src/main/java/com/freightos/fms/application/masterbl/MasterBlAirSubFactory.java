package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.domain.common.enums.FlightType;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.SecurityStatus;
import com.freightos.fms.domain.common.vo.AirlineCode;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.HandlingInformation;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.enums.HandlingInfoCode;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

/**
 * AIR 확장 필드 매핑 담당.
 * MasterBlFactory 크기 분리를 위해 Air 전용 apply 메서드를 이 클래스에 위임한다.
 */
@Component
class MasterBlAirSubFactory {

    void applyAirCreate(MasterBl entity, CreateMasterBlCommand.AirDetailCommand a) {
        if (!(entity instanceof MasterBlAir air)) return;
        air.updateAirFields(new MasterBlAir.AirFields(
                AirlineCode.of(a.airlineCode()),
                Weight.of(a.chargeWeightKg()),
                Weight.of(a.volumeWeightKg()),
                Nullables.mapOrNull(a.rateClass(), RateClass::fromCode),
                CurrencyCode.of(a.currencyCode()),
                a.declaredValueCarriage(),
                a.declaredValueCustoms(),
                a.insurance(),
                a.accountInformation(),
                Nullables.mapOrNull(a.securityStatus(), SecurityStatus::fromCode),
                Nullables.mapOrNull(a.flightType(), FlightType::fromCode),
                BlDate.of(a.issueDate()),
                PortCode.of(a.issuePlace()),
                a.signature(),
                Nullables.mapOrNull(a.otherTerm(), FreightTerm::valueOf),
                HandlingInformation.of(HandlingInfoCode.fromCode(a.handlingInfoCode()), a.handlingInfoText())
        ));
        // remark는 MasterBlFactory.applyRemark에서 공통 처리 — SEA와 동일 패턴
    }

    void applyAirUpdate(MasterBl entity, UpdateMasterBlCommand.AirDetailCommand a) {
        if (!(entity instanceof MasterBlAir air)) return;
        air.updateAirFields(new MasterBlAir.AirFields(
                Nullables.mapOrElse(a.airlineCode(), AirlineCode::of, air::getAirlineCode),
                Nullables.mapOrElse(a.chargeWeightKg(), Weight::of, air::getChargeWeightKg),
                Nullables.mapOrElse(a.volumeWeightKg(), Weight::of, air::getVolumeWeightKg),
                Nullables.mapOrElse(a.rateClass(), RateClass::fromCode, air::getRateClass),
                Nullables.mapOrElse(a.currencyCode(), CurrencyCode::of, air::getCurrencyCode),
                Nullables.firstNonNull(a.declaredValueCarriage(), air::getDeclaredValueCarriage),
                Nullables.firstNonNull(a.declaredValueCustoms(), air::getDeclaredValueCustoms),
                Nullables.firstNonNull(a.insurance(), air::getInsurance),
                Nullables.firstNonNull(a.accountInformation(), air::getAccountInformation),
                Nullables.mapOrElse(a.securityStatus(), SecurityStatus::fromCode, air::getSecurityStatus),
                Nullables.mapOrElse(a.flightType(), FlightType::fromCode, air::getFlightType),
                Nullables.mapOrElse(a.issueDate(), BlDate::of, air::getIssueDate),
                Nullables.mapOrElse(a.issuePlace(), PortCode::of, air::getIssuePlace),
                Nullables.firstNonNull(a.signature(), air::getSignature),
                Nullables.mapOrElse(a.otherTerm(), FreightTerm::valueOf, air::getOtherTerm),
                resolveHandlingInformation(a, air)
        ));
        // remark는 MasterBlFactory.applyRemark에서 공통 처리 — SEA와 동일 패턴
    }

    private HandlingInformation resolveHandlingInformation(UpdateMasterBlCommand.AirDetailCommand a, MasterBlAir air) {
        if (a.handlingInfoCode() == null && a.handlingInfoText() == null) return air.getHandlingInformation();
        HandlingInfoCode code = a.handlingInfoCode() != null
                ? HandlingInfoCode.fromCode(a.handlingInfoCode())
                : (air.getHandlingInformation() != null ? air.getHandlingInformation().code() : null);
        String text = a.handlingInfoText() != null
                ? a.handlingInfoText()
                : (air.getHandlingInformation() != null ? air.getHandlingInformation().description() : null);
        return HandlingInformation.of(code, text);
    }
}
