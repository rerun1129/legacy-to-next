package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.projection.SeaContainerProjection;
import com.freightos.fms.application.housebl.projection.SeaDescProjection;
import com.freightos.fms.application.housebl.projection.SeaDetailProjection;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.LinerCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SEA 자식 projection 변환 담당.
 * HouseBlFactory 크기 분리를 위해 Sea projection 헬퍼 메서드를 이 클래스에 위임한다.
 */
@Component
class HouseBlSeaDetailSubFactory {

    public SeaDetailProjection toSeaDetailProjection(HouseBlSea sea) {
        return new SeaDetailProjection(
                VoMapper.mapOrNull(sea.getLinerCode(), LinerCode::value),
                Nullables.mapOrNull(sea.getVesselVoyage(), VesselVoyage::vesselCode),
                Nullables.mapOrNull(sea.getVesselVoyage(), VesselVoyage::vesselName),
                Nullables.mapOrNull(sea.getVesselVoyage(), VesselVoyage::voyageNo),
                VoMapper.mapOrNull(sea.getOnboardDate(), BlDate::asString),
                VoMapper.mapOrNull(sea.getPorCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getFinalDestCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getIssueDate(), BlDate::asString),
                Nullables.mapOrNull(sea.getNoOfBl(), NoOfBl::name),
                VoMapper.mapOrNull(sea.getIssuePlace(), PortCode::value),
                VoMapper.mapOrNull(sea.getDoDate(), BlDate::asString),
                VoMapper.mapOrNull(sea.getPayableAt(), PortCode::value),
                sea.isTriangle(),
                Nullables.mapOrNull(sea.getServiceTerm(), ServiceTerm::name),
                sea.getVesselNationality(),
                VoMapper.mapOrNull(sea.getRton(), Rton::ton),
                sea.getSayInformation(),
                sea.getNoOfContainerOrPackages(),
                toSeaContainerProjections(sea.getContainers()),
                toSeaDescProjection(sea.getDesc())
        );
    }

    public List<SeaContainerProjection> toSeaContainerProjections(List<HouseBlContainer> containers) {
        if (containers == null) return List.of();
        return containers.stream().map(c -> new SeaContainerProjection(
                c.getId(),
                VoMapper.mapOrNull(c.getContainerNo(), ContainerNumber::value),
                Nullables.mapOrNull(c.getContainerType(), ContainerType::name),
                c.getLengthFeet(),
                VoMapper.mapOrNull(c.getSealNo1(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo2(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo3(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo4(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo5(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo6(), SealNumber::value),
                VoMapper.mapOrNull(c.getPkgQty(), Quantity::count),
                c.getPkgUnit(),
                VoMapper.mapOrNull(c.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(c.getNetWeightKg(), Weight::kg),
                VoMapper.mapOrNull(c.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(c.getVgmKg(), Weight::kg),
                c.isSoc(),
                c.getSeq()
        )).toList();
    }

    public SeaDescProjection toSeaDescProjection(HouseBlDesc desc) {
        if (desc == null) return null;
        return new SeaDescProjection(
                desc.getMarks(),
                desc.getDescription(),
                Nullables.mapOrNull(desc.getDescClause1(), Enum::name),
                Nullables.mapOrNull(desc.getDescClause2(), Enum::name)
        );
    }
}
