package com.freightos.admin.application.code.freight.port.out;

import com.freightos.admin.application.code.freight.command.SearchFreightCommand;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.freight.entity.Freight;

import java.util.Optional;

public interface FreightPort {
    PagedResult<FreightSummary> searchSummaries(SearchFreightCommand command);
    Optional<Freight> findById(Long id);
    Long save(Freight freight);
    void update(Long id, Freight patchData);
    void softDelete(Long id);
}
