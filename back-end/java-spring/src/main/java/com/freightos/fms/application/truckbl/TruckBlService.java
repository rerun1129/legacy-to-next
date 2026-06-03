package com.freightos.fms.application.truckbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.freight.FreightView;
import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.port.out.FreightInputPort;
import com.freightos.fms.application.housebl.HouseBlFreightCommandBuilder;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.truckbl.port.in.TruckBlUseCase;
import com.freightos.fms.application.truckbl.port.out.TruckBlPersistencePort;
import com.freightos.fms.application.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailView;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.freight.enums.FreightBlType;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TruckBlService implements TruckBlUseCase {

    private final HouseBlPort houseBlPort;
    private final TruckBlFactory truckBlFactory;
    private final TruckBlPersistencePort truckBlPersistencePort;
    private final TruckBlSearchPort truckBlSearchPort;
    private final CodeNameResolver codeNameResolver;
    private final FreightInputPort freightInputPort;
    private final HouseBlFreightCommandBuilder houseBlFreightCommandBuilder;

    @Override
    public TruckBlDetailView findTruckBlById(Long id) {
        TruckBlDetailResult base = truckBlFactory.toDetailResult(findTruckDomainById(id));
        return enrichDetail(base);
    }

    private TruckBlDetailView enrichDetail(TruckBlDetailResult base) {
        Map<String, String> hsCodeNames = resolveHsCodeNames(base);
        Map<String, String> teamNames = resolveTeamNames(base);
        Optional<FreightView> freightView = freightInputPort.findFreightByBl(
                FreightBlType.HOUSE, String.valueOf(base.id()));
        return new TruckBlDetailView(
                base,
                nameOrEmpty(hsCodeNames, base.hsCode()),
                nameOrEmpty(teamNames, base.teamCode()),
                freightView.orElse(null)
        );
    }

    /** base.hsCode 1종 조회. */
    private Map<String, String> resolveHsCodeNames(TruckBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.hsCode());
        return codeNameResolver.findHsCodeNames(codes);
    }

    /** base.teamCode 1종 조회. */
    private Map<String, String> resolveTeamNames(TruckBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.teamCode());
        return codeNameResolver.findTeamNames(codes);
    }

    private static void addIfHasText(Set<String> target, String code) {
        if (code != null && !code.isBlank()) {
            target.add(code);
        }
    }

    private static String nameOrEmpty(Map<String, String> nameMap, String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return nameMap.getOrDefault(code, "");
    }

    @Override
    @Transactional
    public Long createTruckBl(CreateHouseBlCommand command) {
        HouseBl saved = houseBlPort.saveHouseBl(truckBlFactory.toEntity(command));
        Long truckBlId = saved.getId();
        if (command.freight() != null) {
            FreightInputCommand freightCmd = houseBlFreightCommandBuilder.buildFromCreate(command, command.freight());
            freightInputPort.saveFreight(FreightBlType.HOUSE, String.valueOf(truckBlId), freightCmd);
        }
        return truckBlId;
    }

    @Override
    @Transactional
    public void updateTruckBl(Long id, UpdateHouseBlCommand command) {
        truckBlPersistencePort.update(id, command);
        if (command.freight() != null) {
            FreightInputCommand freightCmd = houseBlFreightCommandBuilder.buildFromUpdate(command, command.freight());
            freightInputPort.saveFreight(FreightBlType.HOUSE, String.valueOf(id), freightCmd);
        }
    }

    @Override
    @Transactional
    public void deleteTruckBlById(Long id) {
        String blIdStr = String.valueOf(id);
        if (freightInputPort.existsFreightLines(FreightBlType.HOUSE, blIdStr)) {
            // 운임 라인 존재 시 삭제 차단 — 데이터 정합성 보호
            throw FmsException.conflict("FREIGHT_DELETE_BLOCKED", MessageCode.FREIGHT_DELETE_BLOCKED.message());
        }
        freightInputPort.deleteFreight(FreightBlType.HOUSE, blIdStr);
        // TRUCK은 jobDiv가 고정이므로 SELECT 없이 직접 호출
        houseBlPort.deleteByIdAndJobDiv(id, JobDiv.TRUCK);
        log.info("Deleted TruckBl id={}", id);
    }

    @Override
    public List<Long> findTruckBlKeysByHblNoExact(String hblNo) {
        return truckBlSearchPort.findTruckBlKeysByHblNoExact(hblNo);
    }

    @Override
    @Transactional
    public void changeTruckBlHblNo(Long id, ChangeHouseBlNoCommand command) {
        BlNumber newHblNo = BlNumber.of(command.hblNo());
        if (newHblNo == null) throw new IllegalArgumentException("hblNo must not be null or blank");
        long affected = houseBlPort.updateHblNoById(id, newHblNo, JobDiv.TRUCK);
        if (affected == 0) throw new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND);
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────

    private HouseBlTruck findTruckDomainById(Long id) {
        HouseBl entity = houseBlPort.findHouseBlById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND));
        if (!(entity instanceof HouseBlTruck truck)) {
            throw new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND);
        }
        return truck;
    }
}
