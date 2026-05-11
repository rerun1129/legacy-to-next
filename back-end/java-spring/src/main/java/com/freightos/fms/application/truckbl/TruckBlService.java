package com.freightos.fms.application.truckbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.truckbl.port.in.TruckBlUseCase;
import com.freightos.fms.application.truckbl.port.out.TruckBlPersistencePort;
import com.freightos.fms.application.truckbl.port.out.TruckBlSearchPort;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TruckBlService implements TruckBlUseCase {

    private final HouseBlPort houseBlPort;
    private final TruckBlFactory truckBlFactory;
    private final TruckBlPersistencePort truckBlPersistencePort;
    private final TruckBlSearchPort truckBlSearchPort;

    @Override
    public TruckBlDetailResult findTruckBlById(Long id) {
        return truckBlFactory.toDetailResult(findTruckDomainById(id));
    }

    @Override
    @Transactional
    public Long createTruckBl(CreateHouseBlCommand command) {
        HouseBl saved = houseBlPort.saveHouseBl(truckBlFactory.toEntity(command));
        return saved.getId();
    }

    @Override
    @Transactional
    public void updateTruckBl(Long id, UpdateHouseBlCommand command) {
        truckBlPersistencePort.update(id, command);
    }

    @Override
    @Transactional
    public void deleteTruckBlById(Long id) {
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
