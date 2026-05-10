package com.freightos.fms.application.housebl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseBlService implements HouseBlUseCase {

    private final HouseBlPort houseBlPort;
    private final HouseBlFactory houseBlFactory;

    @Override
    public PagedResult<HouseBlSummary> searchHouseBls(SearchHouseBlCommand cmd, PageRequest pageRequest) {
        return houseBlPort.searchHouseBls(houseBlFactory.toFilter(cmd), pageRequest);
    }

    @Override
    public HouseBlDetailResult findHouseBlById(Long id) {
        return houseBlFactory.toDetailResult(findEntityById(id));
    }

    @Override
    @Transactional
    public Long createHouseBl(CreateHouseBlCommand command) {
        HouseBl entity = houseBlFactory.toEntity(command);
        log.debug("Creating HouseBl: {}", entity.getHblNo());
        return houseBlPort.saveHouseBl(entity).getId();
    }

    @Override
    @Transactional
    public HouseBlDetailResult updateHouseBl(Long id, UpdateHouseBlCommand command) {
        HouseBl existing = findEntityById(id);
        houseBlFactory.applyToEntity(command, existing);
        return houseBlFactory.toDetailResult(houseBlPort.saveHouseBl(existing));
    }

    @Override
    @Transactional
    public void deleteHouseBlById(Long id) {
        JobDiv jobDiv = houseBlPort.findJobDivById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));
        houseBlPort.deleteByIdAndJobDiv(id, jobDiv);
        log.info("Deleted HouseBl id={}", id);
    }

    @Override
    @Transactional
    public void changeHblNo(Long id, ChangeHouseBlNoCommand command) {
        BlNumber newHblNo = BlNumber.of(command.hblNo());
        if (newHblNo == null) throw new IllegalArgumentException("hblNo must not be null or blank");
        long affected = houseBlPort.updateHblNoById(id, newHblNo, null);
        if (affected == 0) throw new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND);
        log.info("Changed HouseBl hblNo: id={}", id);
    }

    private HouseBl findEntityById(Long id) {
        return houseBlPort.findHouseBlById(id).orElseThrow(() -> new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));
    }
}
