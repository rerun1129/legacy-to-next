package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.SeaDetailProjection;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.LinerCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * SEA 확장 필드 매핑 담당.
 * MasterBlFactory 크기 분리를 위해 Sea 전용 apply 메서드를 이 클래스에 위임한다.
 */
@Component
class MasterBlSeaSubFactory {

    void applySeaCreate(MasterBl entity, CreateMasterBlCommand.SeaDetailCommand s) {
        if (!(entity instanceof MasterBlSea sea)) return;
        sea.updateSeaFields(
                Nullables.mapOrNull(s.loadType(), LoadType::valueOf),
                LinerCode.of(s.linerCode()),
                VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo()),
                BlDate.of(s.onboardDate()), BlNumber.of(s.lineBkgNo()), BlDate.of(s.issueDate())
        );
        applySeaCommon(sea, s.vesselNationality(), s.serviceTerm(), s.blType(), s.porCode(), s.finalDestCode(), s.rton());
    }

    void applySeaUpdate(MasterBl entity, UpdateMasterBlCommand.SeaDetailCommand s) {
        if (!(entity instanceof MasterBlSea sea)) return;
        sea.updateSeaFields(
                Nullables.mapOrElse(s.loadType(),    LoadType::valueOf,                                        sea::getLoadType),
                Nullables.mapOrElse(s.linerCode(),   LinerCode::of,                                            sea::getLinerCode),
                Nullables.mapOrElse(s.vesselName(),  v -> VesselVoyage.of(s.vesselCode(), v, s.voyageNo()),    sea::getVesselVoyage),
                Nullables.mapOrElse(s.onboardDate(), BlDate::of,                                               sea::getOnboardDate),
                Nullables.mapOrElse(s.lineBkgNo(),   BlNumber::of,                                             sea::getLineBkgNo),
                Nullables.mapOrElse(s.issueDate(),   BlDate::of,                                               sea::getIssueDate)
        );
        applySeaCommon(sea, s.vesselNationality(), s.serviceTerm(), s.blType(), s.porCode(), s.finalDestCode(), s.rton());
    }

    public SeaDetailProjection toSeaDetailProjection(MasterBlSea sea) {
        return new SeaDetailProjection(
                Nullables.mapOrNull(sea.getLoadType(), LoadType::name),
                VoMapper.mapOrNull(sea.getLinerCode(), LinerCode::value),
                sea.getVesselVoyage() != null ? sea.getVesselVoyage().vesselCode() : null,
                sea.getVesselVoyage() != null ? sea.getVesselVoyage().vesselName() : null,
                sea.getVesselVoyage() != null ? sea.getVesselVoyage().voyageNo() : null,
                VoMapper.mapOrNull(sea.getOnboardDate(), BlDate::asString),
                sea.getVesselNationality(),
                Nullables.mapOrNull(sea.getServiceTerm(), ServiceTerm::name),
                Nullables.mapOrNull(sea.getBlType(), BlType::name),
                VoMapper.mapOrNull(sea.getPorCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getFinalDestCode(), PortCode::value),
                VoMapper.mapOrNull(sea.getRton(), Rton::ton),
                VoMapper.mapOrNull(sea.getLineBkgNo(), BlNumber::value),
                VoMapper.mapOrNull(sea.getIssueDate(), BlDate::asString),
                sea.getRemark()
        );
    }

    private void applySeaCommon(MasterBlSea sea, String vesselNationality,
                                String serviceTerm, String blType, String porCode, String finalDestCode, BigDecimal rton) {
        if (vesselNationality != null) sea.updateVesselNationality(vesselNationality);
        if (serviceTerm != null)       sea.updateServiceTerm(ServiceTerm.valueOf(serviceTerm));
        if (blType != null)            sea.updateBlType(BlType.valueOf(blType));
        if (porCode != null || finalDestCode != null) sea.updateRoute(PortCode.of(porCode), PortCode.of(finalDestCode));
        if (rton != null)              sea.updateRton(Rton.of(rton));
    }
}
