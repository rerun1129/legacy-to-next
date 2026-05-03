package com.freightos.fms.application.housebl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
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

    @Override
    public PagedResult<HouseBlSummary> getHouseBlsByJobDivAndBound(JobDiv jobDiv, Bound bound, PageRequest pageRequest) {
        return houseBlPort.findHouseBlsByJobDivAndBound(jobDiv, bound, PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), "createdAt", SortDirection.DESC));
    }

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
    public HouseBl save(HouseBl houseBl) {
        log.debug("Saving HouseBl: {}", houseBl.getHblNo());
        return houseBlPort.saveHouseBl(houseBl);
    }

    @Override
    @Transactional
    public void deleteHouseBlById(Long id) {
        houseBlPort.deleteHouseBl(findHouseBlById(id));
        log.info("Deleted HouseBl id={}", id);
    }
}
