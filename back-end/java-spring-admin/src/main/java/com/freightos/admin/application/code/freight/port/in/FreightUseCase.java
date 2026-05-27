package com.freightos.admin.application.code.freight.port.in;

import com.freightos.admin.application.code.freight.command.CreateFreightCommand;
import com.freightos.admin.application.code.freight.command.SaveFreightChangesCommand;
import com.freightos.admin.application.code.freight.command.SearchFreightCommand;
import com.freightos.admin.application.code.freight.command.UpdateFreightCommand;
import com.freightos.admin.application.code.freight.projection.FreightSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.freight.entity.Freight;

import java.util.List;

public interface FreightUseCase {
    PagedResult<FreightSummary> searchFreights(SearchFreightCommand command);
    Freight getFreightById(Long id);
    Long createFreight(CreateFreightCommand command);
    void updateFreight(Long id, UpdateFreightCommand command);
    void deleteFreight(Long id);
    void deleteFreights(List<Long> ids);
    SaveChangesResult saveFreightChanges(SaveFreightChangesCommand command);
    List<AutocompleteItem> autocompleteFreights(String query, int limit);
}
