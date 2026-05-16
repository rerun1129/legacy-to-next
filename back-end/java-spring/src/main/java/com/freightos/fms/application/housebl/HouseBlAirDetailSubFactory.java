package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.projection.AirChargeProjection;
import com.freightos.fms.application.housebl.projection.AirDescProjection;
import com.freightos.fms.application.housebl.projection.AirDetailProjection;
import com.freightos.fms.application.housebl.projection.AirDimProjection;
import com.freightos.fms.application.housebl.projection.AirScheduleLegProjection;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.AirlineCode;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.HandlingInformation;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlAirCharge;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlDim;
import com.freightos.fms.domain.housebl.entity.HouseBlScheduleLeg;
import com.freightos.fms.domain.housebl.enums.CargoType;
import com.freightos.fms.domain.housebl.enums.Fhd;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AIR 자식 projection 변환 담당.
 * HouseBlFactory 크기 분리를 위해 Air projection 헬퍼 메서드를 이 클래스에 위임한다.
 */
@Component
class HouseBlAirDetailSubFactory {

    public AirDetailProjection toAirDetailProjection(HouseBlAir air) {
        return new AirDetailProjection(
                VoMapper.mapOrNull(air.getAirlineCode(), AirlineCode::value),
                VoMapper.mapOrNull(air.getChargeWeightKg(), Weight::kg),
                VoMapper.mapOrNull(air.getVolumeWeightKg(), Weight::kg),
                Nullables.mapOrNull(air.getRateClass(), RateClass::name),
                VoMapper.mapOrNull(air.getCurrencyCode(), CurrencyCode::value),
                air.getDeclaredValueCarriage(),
                air.getDeclaredValueCustoms(),
                air.getInsurance(),
                air.getAccountInformation(),
                Nullables.mapOrNull(air.getOtherTerm(), FreightTerm::name),
                VoMapper.mapOrNull(air.getIssueDate(), BlDate::asString),
                VoMapper.mapOrNull(air.getIssuePlace(), PortCode::value),
                air.getSignature(),
                Nullables.mapOrNull(air.getFhd(), Fhd::name),
                Nullables.mapOrNull(air.getHandlingInformation(), hi -> Nullables.mapOrNull(hi.code(), Enum::name)),
                Nullables.mapOrNull(air.getHandlingInformation(), HandlingInformation::description),
                air.getOriginOfGoods(),
                Nullables.mapOrNull(air.getCargoType(), CargoType::getCode),
                toAirScheduleLegProjections(air.getScheduleLegs()),
                toAirChargeProjections(air.getAirCharges()),
                toAirDimProjections(air.getDims()),
                toAirDescProjection(air.getDesc())
        );
    }

    public List<AirScheduleLegProjection> toAirScheduleLegProjections(List<HouseBlScheduleLeg> legs) {
        if (legs == null) return List.of();
        return legs.stream().map(s -> new AirScheduleLegProjection(
                s.getId(),
                s.getToCode(),
                s.getByCarrier(),
                s.getFlightNo(),
                s.getOnBoardDt(),
                s.getOnBoardTm(),
                s.getArrivalDt(),
                s.getArrivalTm()
        )).toList();
    }

    public List<AirChargeProjection> toAirChargeProjections(List<HouseBlAirCharge> charges) {
        if (charges == null) return List.of();
        return charges.stream().map(c -> new AirChargeProjection(
                c.getId(),
                c.getFreightCode(),
                VoMapper.mapOrNull(c.getCurrencyCode(), CurrencyCode::value),
                Nullables.mapOrNull(c.getPer(), Per::getCode),
                Nullables.mapOrNull(c.getFreightTerm(), FreightTerm::name),
                VoMapper.mapOrNull(c.getGrossWeightKg(), Weight::kg),
                Nullables.mapOrNull(c.getRateClass(), RateClass::name),
                VoMapper.mapOrNull(c.getChargeWeightKg(), Weight::kg),
                c.getRate()
        )).toList();
    }

    public List<AirDimProjection> toAirDimProjections(List<HouseBlDim> dims) {
        if (dims == null) return List.of();
        return dims.stream().map(d -> new AirDimProjection(
                d.getId(),
                d.getLengthCm(),
                d.getWidthCm(),
                d.getHeightCm(),
                d.getQuantity(),
                d.getCbm(),
                d.getVolumeWeightKg()
        )).toList();
    }

    public AirDescProjection toAirDescProjection(HouseBlDesc desc) {
        if (desc == null) return AirDescProjection.empty();
        return new AirDescProjection(
                desc.getMarks(),
                desc.getDescription(),
                Nullables.mapOrNull(desc.getDescClause1(), Enum::name),
                Nullables.mapOrNull(desc.getDescClause2(), Enum::name)
        );
    }
}
