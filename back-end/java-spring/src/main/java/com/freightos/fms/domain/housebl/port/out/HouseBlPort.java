package com.freightos.fms.domain.housebl.port.out;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;

import java.util.List;
import java.util.Optional;

public interface HouseBlPort {
    Optional<HouseBl> findHouseBlById(Long id);
    PagedResult<HouseBlSummary> findHouseBlsByJobDivAndBound(JobDiv jobDiv, Bound bound, PageRequest pageRequest);
    PagedResult<HouseBlSummary> searchHouseBls(HouseBlFilter filter, PageRequest pageRequest);
    PagedResult<HouseBl> findHouseBlsBySchedule(JobDiv jobDiv, Bound bound, String from, String to, PageRequest pageRequest);
    long countHouseBlsByMasterBlId(Long masterBlId);
    HouseBl saveHouseBl(HouseBl houseBl);
    void deleteHouseBl(HouseBl houseBl);
    List<ConsoledHouseBlSeaSummary> findConsoledSeaSummariesByMasterBlId(Long masterBlId);
    List<ConsoledHouseBlAirSummary> findConsoledAirSummariesByMasterBlId(Long masterBlId);
}
