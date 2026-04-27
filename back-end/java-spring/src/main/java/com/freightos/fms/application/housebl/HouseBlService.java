package com.freightos.fms.application.housebl;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
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
    public PagedResult<HouseBl> list(JobDiv jobDiv, Bound bound, PageRequest pageRequest) {
        return houseBlPort.findAllByJobDivAndBoundOrderByCreatedAtDesc(jobDiv, bound, pageRequest);
    }

    @Override
    public HouseBl getById(Long id) {
        return houseBlPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HouseBl", id));
    }

    @Override
    @Transactional
    public HouseBl save(HouseBl houseBl) {
        log.debug("Saving HouseBl: {}", houseBl.getHblNo());
        return houseBlPort.save(houseBl);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        HouseBl entity = getById(id);
        houseBlPort.delete(entity);
        log.info("Deleted HouseBl id={}", id);
    }
}
