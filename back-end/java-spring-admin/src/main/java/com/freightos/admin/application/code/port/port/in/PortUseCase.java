package com.freightos.admin.application.code.port.port.in;

import com.freightos.admin.application.code.port.command.CreatePortCommand;
import com.freightos.admin.application.code.port.command.SavePortChangesCommand;
import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.command.UpdatePortCommand;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.port.entity.Port;

import java.util.List;

public interface PortUseCase {
    PagedResult<PortSummary> searchPorts(SearchPortCommand command);
    Port getPortById(Long id);
    Long createPort(CreatePortCommand command);
    void updatePort(Long id, UpdatePortCommand command);
    void deletePort(Long id);
    void deletePorts(List<Long> ids);
    SaveChangesResult savePortChanges(SavePortChangesCommand command);
    List<AutocompleteItem> autocompletePorts(String query, String type, int limit);
}
