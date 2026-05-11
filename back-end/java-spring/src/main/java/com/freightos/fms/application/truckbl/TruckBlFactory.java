package com.freightos.fms.application.truckbl;

import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.TeamCode;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

/**
 * HouseBlTruck → TruckBlDetailResult 변환 팩토리.
 * VO 접근자 패턴은 HouseBlFactory.toDetailResult와 동일하게 유지한다.
 */
@Component
public class TruckBlFactory {

    public TruckBlDetailResult toDetailResult(HouseBlTruck truck) {
        return new TruckBlDetailResult(
                truck.getId(),
                VoMapper.mapOrNull(truck.getHblNo(), BlNumber::value),
                Nullables.mapOrNull(truck.getJobDiv(), Enum::name),
                Nullables.mapOrNull(truck.getBound(), Enum::name),
                Nullables.mapOrNull(truck.getShipmentType(), Enum::name),
                Nullables.mapOrNull(truck.getFreightTerm(), Enum::name),
                VoMapper.mapOrNull(truck.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getSettlePartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getPolCode(), PortCode::value),
                VoMapper.mapOrNull(truck.getPodCode(), PortCode::value),
                VoMapper.mapOrNull(truck.getDeliveryCode(), PortCode::value),
                VoMapper.mapOrNull(truck.getEtd(), BlDate::asString),
                VoMapper.mapOrNull(truck.getEta(), BlDate::asString),
                VoMapper.mapOrNull(truck.getPkgQty(), Quantity::count),
                truck.getPkgUnit(),
                Nullables.mapOrNull(truck.getWeightUnit(), WeightUnit::name),
                VoMapper.mapOrNull(truck.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(truck.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(truck.getActualCustomerCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(truck.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(truck.getSalesManCode(), EmployeeCode::value),
                Nullables.mapOrNull(truck.getIncoterms(), Incoterms::name),
                truck.getCreatedAt(),
                truck.getUpdatedAt(),
                VoMapper.mapOrNull(truck.getTruckerCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getTruckerPic(), EmployeeCode::value),
                VoMapper.mapOrNull(truck.getChargeWeightKg(), Weight::kg),
                VoMapper.mapOrNull(truck.getPickupDate(), BlDate::asString),
                truck.getPickupTm(),
                truck.getEtdTm(),
                truck.getEtaTm(),
                Nullables.mapOrNull(truck.getLoadType(), LoadType::name),
                Nullables.mapOrNull(truck.getServiceTerm(), ServiceTerm::name),
                VoMapper.mapOrNull(truck.getVesselVoyage(), VesselVoyage::voyageNo)
        );
    }
}
