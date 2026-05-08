package com.freightos.fms.application.nonbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.MblNo;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.TeamCode;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlDim;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.housebl.enums.PortKind;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonBlService implements NonBlUseCase {

    private final HouseBlUseCase houseBlUseCase;
    private final HouseBlPort houseBlPort;
    private final NonBlSearchPort nonBlSearchPort;

    @Override
    public PagedResult<NonBlSummary> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest) {
        Bound bound = Nullables.mapOrNull(cmd.bound(), Bound::valueOf);
        DateKind dateKind = Nullables.mapOrNull(cmd.dateKind(), DateKind::valueOf);
        PartyKind partyKind = Nullables.mapOrNull(cmd.partyKind(), PartyKind::valueOf);
        PortKind portKind = Nullables.mapOrNull(cmd.portKind(), PortKind::valueOf);
        NonBlFilter filter = NonBlFilter.of(
                bound, cmd.hblNo(), cmd.etdFrom(), cmd.etdTo(),
                cmd.linerCode(), cmd.partyCode(), cmd.portCode(),
                cmd.vessel(), cmd.voyage(), cmd.operatorCode(), cmd.teamCode()
        ).withKinds(dateKind, partyKind, portKind);
        return nonBlSearchPort.searchNonBlSummaries(filter, pageRequest);
    }

    @Override
    public NonBlDetailResult findNonBlById(Long id) {
        return toNonBlDetailResult(findNonBlDomainById(id));
    }

    @Override
    @Transactional
    public Long createNonBl(CreateHouseBlCommand command) {
        return houseBlUseCase.createHouseBl(command);
    }

    @Override
    @Transactional
    public NonBlDetailResult updateNonBl(Long id, UpdateHouseBlCommand command) {
        houseBlUseCase.updateHouseBl(id, command);
        return toNonBlDetailResult(findNonBlDomainById(id));
    }

    @Override
    @Transactional
    public void deleteNonBlById(Long id) {
        houseBlUseCase.deleteHouseBlById(id);
        log.info("Deleted NonBl id={}", id);
    }

    private HouseBlNonBl findNonBlDomainById(Long id) {
        return houseBlPort.findHouseBlById(id)
                .filter(e -> e instanceof HouseBlNonBl)
                .map(e -> (HouseBlNonBl) e)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));
    }

    private NonBlDetailResult toNonBlDetailResult(HouseBlNonBl nonBl) {
        return new NonBlDetailResult(
                nonBl.getId(),
                VoMapper.mapOrNull(nonBl.getHblNo(), BlNumber::value),
                Nullables.mapOrNull(nonBl.getJobDiv(), Enum::name),
                Nullables.mapOrNull(nonBl.getBound(), Bound::name),
                Nullables.mapOrNull(nonBl.getWorkDivision(), HouseBlNonBl.WorkDivision::name),
                Nullables.mapOrNull(nonBl.getShipmentType(), Enum::name),
                Nullables.mapOrNull(nonBl.getFreightTerm(), Enum::name),
                VoMapper.mapOrNull(nonBl.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getDocPartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getSettlePartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getActualCustomerCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getPolCode(), PortCode::value),
                VoMapper.mapOrNull(nonBl.getPodCode(), PortCode::value),
                VoMapper.mapOrNull(nonBl.getEtd(), BlDate::asString),
                VoMapper.mapOrNull(nonBl.getEta(), BlDate::asString),
                nonBl.getLinerCode(),
                nonBl.getLinerName(),
                nonBl.getVesselName(),
                nonBl.getVoyageNo(),
                nonBl.getFinalDestCode(),
                nonBl.getFinalDestName(),
                nonBl.getFinalEta(),
                VoMapper.mapOrNull(nonBl.getOriginalBlRef(), BlNumber::value),
                VoMapper.mapOrNull(nonBl.getRton(), Rton::ton),
                VoMapper.mapOrNull(nonBl.getVolumeWtKg(), Weight::kg),
                VoMapper.mapOrNull(nonBl.getPkgQty(), Quantity::count),
                Nullables.mapOrNull(nonBl.getPkgUnit(), Enum::name),
                VoMapper.mapOrNull(nonBl.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(nonBl.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(nonBl.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(nonBl.getSalesManCode(), EmployeeCode::value),
                VoMapper.mapOrNull(nonBl.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(nonBl.getMblNo(), MblNo::value),
                nonBl.getMasterRefNo(),
                nonBl.getMasterBlId(),
                nonBl.getMainItemName(),
                nonBl.getHsCode(),
                nonBl.getCreatedAt(),
                nonBl.getUpdatedAt(),
                toContainerViews(nonBl.getContainers()),
                toDimViews(nonBl.getDims()),
                toDescView(nonBl.getDesc())
        );
    }

    private List<NonBlDetailResult.NonBlContainerView> toContainerViews(List<HouseBlContainer> containers) {
        if (containers == null) return List.of();
        return containers.stream().map(c -> new NonBlDetailResult.NonBlContainerView(
                c.getId(),
                c.getSeq(),
                VoMapper.mapOrNull(c.getContainerNo(), ContainerNumber::value),
                Nullables.mapOrNull(c.getContainerType(), Enum::name),
                c.getLengthFeet(),
                VoMapper.mapOrNull(c.getSealNo1(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo2(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo3(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo4(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo5(), SealNumber::value),
                VoMapper.mapOrNull(c.getSealNo6(), SealNumber::value),
                VoMapper.mapOrNull(c.getPkgQty(), Quantity::count),
                Nullables.mapOrNull(c.getPkgUnit(), Enum::name),
                VoMapper.mapOrNull(c.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(c.getNetWeightKg(), Weight::kg),
                VoMapper.mapOrNull(c.getVgmKg(), Weight::kg),
                VoMapper.mapOrNull(c.getCbm(), Volume::cbm),
                c.isSoc()
        )).toList();
    }

    private List<NonBlDetailResult.NonBlDimView> toDimViews(List<HouseBlDim> dims) {
        if (dims == null) return List.of();
        return dims.stream().map(d -> new NonBlDetailResult.NonBlDimView(
                d.getId(),
                d.getLengthCm(),
                d.getWidthCm(),
                d.getHeightCm(),
                d.getQuantity(),
                d.getCbm(),
                d.getVolumeWeightKg()
        )).toList();
    }

    private NonBlDetailResult.NonBlDescView toDescView(HouseBlDesc desc) {
        if (desc == null) return null;
        return new NonBlDetailResult.NonBlDescView(
                desc.getMarks(),
                desc.getDescription(),
                desc.getRemark()
        );
    }
}
