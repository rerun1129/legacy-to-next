package com.freightos.fms.domain.housebl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.util.Optional;

public interface HouseBlPort {
    Optional<HouseBl> findById(Long id);
    PagedResult<HouseBl> findAllByJobDivAndBoundOrderByCreatedAtDesc(JobDiv jobDiv, Bound bound, PageRequest pageRequest);
    PagedResult<HouseBl> findBySchedule(JobDiv jobDiv, Bound bound, String from, String to, PageRequest pageRequest);
    long countByMasterBlId(Long masterBlId);
    HouseBl save(HouseBl houseBl);
    void delete(HouseBl houseBl);
}
