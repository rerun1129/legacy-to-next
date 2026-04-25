package com.freightos.fms.domain.housebl.service;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.housebl.api.dto.HouseBlSummaryResponse;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.repository.HouseBlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseBlServiceImpl implements HouseBlService {

    private final HouseBlRepository houseBlRepository;

    @Override
    public Page<HouseBlSummaryResponse> list(JobDiv jobDiv, Bound bound, Pageable pageable) {
        return houseBlRepository
                .findAllByJobDivAndBoundOrderByCreatedAtDesc(jobDiv, bound, pageable)
                .map(HouseBlSummaryResponse::from);
    }

    @Override
    public HouseBl getById(UUID id) {
        return houseBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HouseBl", id));
    }

    @Override
    @Transactional
    public HouseBl save(HouseBl houseBl) {
        log.debug("Saving HouseBl: {}", houseBl.getHblNo());
        return houseBlRepository.save(houseBl);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        HouseBl entity = getById(id);
        houseBlRepository.delete(entity);
        log.info("Deleted HouseBl id={}", id);
    }
}
