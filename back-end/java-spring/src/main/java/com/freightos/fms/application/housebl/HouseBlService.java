package com.freightos.fms.application.housebl;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
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
    public PagedResult<HouseBl> getHouseBlsByJobDivAndBound(JobDiv jobDiv, Bound bound, PageRequest pageRequest) {
        return houseBlPort.findAllByJobDivAndBoundOrderByCreatedAtDesc(jobDiv, bound,
                PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), "createdAt", SortDirection.DESC));
    }

    @Override
    public HouseBl findHouseBlById(Long id) {
        return houseBlPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));
    }

    @Override
    @Transactional
    public HouseBl save(HouseBl houseBl) {
        log.debug("Saving HouseBl: {}", houseBl.getHblNo());
        return houseBlPort.save(houseBl);
    }

    @Override
    @Transactional
    public void deleteHouseBlById(Long id) {
        HouseBl entity = findHouseBlById(id);
        houseBlPort.delete(entity);
        log.info("Deleted HouseBl id={}", id);
    }
}
