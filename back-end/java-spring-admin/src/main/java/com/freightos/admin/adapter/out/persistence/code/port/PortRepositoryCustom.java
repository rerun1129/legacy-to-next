package com.freightos.admin.adapter.out.persistence.code.port;

import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.response.PagedResult;

public interface PortRepositoryCustom {
    PagedResult<PortSummary> searchSummaries(SearchPortCommand command);
}
