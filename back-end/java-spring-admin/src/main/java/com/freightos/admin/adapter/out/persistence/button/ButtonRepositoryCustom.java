package com.freightos.admin.adapter.out.persistence.button;

import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.response.PagedResult;

public interface ButtonRepositoryCustom {
    PagedResult<ButtonSummary> searchSummaries(SearchButtonCommand command);
}
