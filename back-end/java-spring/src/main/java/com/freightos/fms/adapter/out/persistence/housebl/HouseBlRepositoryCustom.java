package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;

import java.util.List;

public interface HouseBlRepositoryCustom {
    PagedResult<HouseBlSummary> searchSummaries(HouseBlFilter filter, PageRequest pageRequest);
    List<ConsoledHouseBlSeaSummary> findConsoledSeaSummariesByMasterBlId(Long masterBlId);
    List<ConsoledHouseBlAirSummary> findConsoledAirSummariesByMasterBlId(Long masterBlId);
    long updateHblNoById(Long id, String newHblNo, JobDiv expectedJobDiv);
    List<Long> findHouseBlKeysByHblNoExact(String hblNo, JobDiv jobDiv);
}
