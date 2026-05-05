package com.freightos.fms.application.housebl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseBlService implements HouseBlUseCase {

    private final HouseBlPort houseBlPort;

    @Override
    public PagedResult<HouseBlSummary> searchHouseBls(HouseBlFilter filter, PageRequest pageRequest) {
        return houseBlPort.searchHouseBls(filter, pageRequest);
    }

    @Override
    public HouseBl findHouseBlById(Long id) {
        return houseBlPort.findHouseBlById(id).orElseThrow(() -> new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));
    }

    @Override
    @Transactional
    public Long createHouseBl(HouseBl houseBl) {
        log.debug("Creating HouseBl: {}", houseBl.getHblNo());
        return houseBlPort.saveHouseBl(houseBl).getId();
    }

    @Override
    @Transactional
    public HouseBl updateHouseBl(Long id, Consumer<HouseBl> patcher) {
        HouseBl existing = findHouseBlById(id);
        patcher.accept(existing);
        return houseBlPort.saveHouseBl(existing);
    }

    @Override
    @Transactional
    public void deleteHouseBlById(Long id) {
        houseBlPort.deleteHouseBl(findHouseBlById(id));
        log.info("Deleted HouseBl id={}", id);
    }
}
