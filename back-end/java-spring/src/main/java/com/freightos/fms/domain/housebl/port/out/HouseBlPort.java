package com.freightos.fms.domain.housebl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.util.Optional;

public interface HouseBlPort {
    Optional<HouseBl> findHouseBlById(Long id);
    PagedResult<HouseBl> findHouseBlsByJobDivAndBound(JobDiv jobDiv, Bound bound, PageRequest pageRequest);
    PagedResult<HouseBl> findHouseBlsBySchedule(JobDiv jobDiv, Bound bound, String from, String to, PageRequest pageRequest);
    long countHouseBlsByMasterBlId(Long masterBlId);
    HouseBl saveHouseBl(HouseBl houseBl);
    void deleteHouseBl(HouseBl houseBl);
}
