package com.freightos.fms.application.truckbl;

import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.TeamCode;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlDim;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.entity.HouseBlTruckOrder;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Truck B/L 도메인 변환 팩토리.
 * CREATE: HouseBlFactory.toEntity()에 위임 (jobDiv="TRUCK" 고정).
 * READ:   HouseBlTruck → TruckBlDetailResult 변환.
 */
@Component
public class TruckBlFactory {

    private final HouseBlFactory houseBlFactory;

    public TruckBlFactory(HouseBlFactory houseBlFactory) {
        this.houseBlFactory = houseBlFactory;
    }

    /**
     * CreateHouseBlCommand → HouseBl(HouseBlTruck) 생성.
     * jobDiv는 컨트롤러/어셈블러에서 "TRUCK"으로 고정하여 전달한다.
     */
    public HouseBl toEntity(CreateHouseBlCommand command) {
        return houseBlFactory.toEntity(command);
    }

    public TruckBlDetailResult toDetailResult(HouseBlTruck truck) {
        return new TruckBlDetailResult(
                truck.getId(),
                VoMapper.mapOrNull(truck.getHblNo(), BlNumber::value),
                Nullables.mapOrNull(truck.getJobDiv(), Enum::name),
                Nullables.mapOrNull(truck.getBound(), Enum::name),
                Nullables.mapOrNull(truck.getShipmentType(), Enum::name),
                Nullables.mapOrNull(truck.getFreightTerm(), Enum::name),
                VoMapper.mapOrNull(truck.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getShipperCode(), CustomerCode::address),
                VoMapper.mapOrNull(truck.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getConsigneeCode(), CustomerCode::address),
                VoMapper.mapOrNull(truck.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getNotifyCode(), CustomerCode::address),
                VoMapper.mapOrNull(truck.getSettlePartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getDocPartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(truck.getDocPartnerCode(), CustomerCode::address),
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
                VoMapper.mapOrNull(truck.getVesselVoyage(), VesselVoyage::vesselName),
                VoMapper.mapOrNull(truck.getVesselVoyage(), VesselVoyage::voyageNo),
                truck.getRemark(),
                toTruckOrderViews(truck.getTruckOrders()),
                toDescView(truck.getDesc()),
                Nullables.mapOrNull(truck.getVolumeDivisor(), Enum::name),
                toDimViews(truck.getDims())
        );
    }

    private List<TruckBlDetailResult.TruckOrderView> toTruckOrderViews(List<HouseBlTruckOrder> orders) {
        if (orders == null) return null;
        return orders.stream().map(this::toTruckOrderView).toList();
    }

    private TruckBlDetailResult.TruckOrderView toTruckOrderView(HouseBlTruckOrder order) {
        return new TruckBlDetailResult.TruckOrderView(
                order.getId(),
                order.getTruckOrderNo(),
                order.getPkgQty(),
                order.getPkgUnit(),
                VoMapper.mapOrNull(order.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(order.getCbm(), Volume::cbm),
                order.getTruckNo(),
                Nullables.mapOrNull(order.getTruckType(), Enum::name),
                order.getDriver(),
                order.getMobileNo(),
                VoMapper.mapOrNull(order.getContainerNo(), ContainerNumber::value),
                Nullables.mapOrNull(order.getContainerType(), ContainerType::getCode),  // FE 옵션 value와 정합 (§6.45)
                VoMapper.mapOrNull(order.getSealNo1(), SealNumber::value),
                VoMapper.mapOrNull(order.getSealNo2(), SealNumber::value),
                VoMapper.mapOrNull(order.getSealNo3(), SealNumber::value)
        );
    }

    private List<TruckBlDetailResult.DimView> toDimViews(List<HouseBlDim> dims) {
        if (dims == null) return null;
        return dims.stream().map(d -> new TruckBlDetailResult.DimView(
                d.getId(), d.getLengthCm(), d.getWidthCm(), d.getHeightCm(),
                d.getQuantity(), d.getCbm(), d.getVolumeWeightKg())).toList();
    }

    private TruckBlDetailResult.DescView toDescView(HouseBlDesc desc) {
        if (desc == null) return null;
        return new TruckBlDetailResult.DescView(
                desc.getMarks(),
                desc.getDescription(),
                Nullables.mapOrNull(desc.getDescClause1(), Enum::name),
                Nullables.mapOrNull(desc.getDescClause2(), Enum::name)
        );
    }
}
