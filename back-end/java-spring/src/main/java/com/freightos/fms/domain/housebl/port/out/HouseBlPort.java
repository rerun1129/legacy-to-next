package com.freightos.fms.domain.housebl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HouseBlPort {
    Optional<HouseBl> findById(UUID id);
    PagedResult<HouseBl> findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv jobDiv, Bound bound, PageRequest pageRequest);
    PagedResult<HouseBl> findBySchedule(JobDiv jobDiv, Bound bound, LocalDate from, LocalDate to, PageRequest pageRequest);
    long countByMasterBlId(UUID masterBlId);
    HouseBl save(HouseBl houseBl);
    void delete(HouseBl houseBl);
}
