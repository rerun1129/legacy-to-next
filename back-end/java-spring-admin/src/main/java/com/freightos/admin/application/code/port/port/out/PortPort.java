package com.freightos.admin.application.code.port.port.out;

import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.code.port.entity.Port;

import java.util.List;
import java.util.Optional;

public interface PortPort {
    PagedResult<PortSummary> searchSummaries(SearchPortCommand command);
    Optional<Port> findById(Long id);
    Long save(Port port);
    void update(Long id, Port patchData);
    void softDelete(Long id);
    List<AutocompleteItem> autocomplete(String query, String type, int limit);
}
