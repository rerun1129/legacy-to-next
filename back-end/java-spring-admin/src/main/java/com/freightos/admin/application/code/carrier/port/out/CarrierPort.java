package com.freightos.admin.application.code.carrier.port.out;

import com.freightos.admin.application.code.carrier.command.SearchCarrierCommand;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.carrier.entity.Carrier;

import java.util.List;
import java.util.Optional;

public interface CarrierPort {
    PagedResult<CarrierSummary> searchSummaries(SearchCarrierCommand command);
    Optional<Carrier> findById(Long id);
    Long save(Carrier carrier);
    void update(Long id, Carrier patchData);
    void softDelete(Long id);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
