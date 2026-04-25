package com.freightos.fms.domain.masterbl.service;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.repository.MasterBlRepository;
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
public class MasterBlServiceImpl implements MasterBlService {

    private final MasterBlRepository masterBlRepository;

    @Override
    public Page<MasterBl> list(Bound bound, Pageable pageable) {
        return masterBlRepository.findAllByBound(bound, pageable);
    }

    @Override
    public MasterBl getById(UUID id) {
        return masterBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterBl", id));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MasterBl entity = getById(id);
        masterBlRepository.delete(entity);
        log.info("Deleted MasterBl id={}", id);
    }
}
