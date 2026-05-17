package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.PagedResult;

public interface UserRepositoryCustom {
    PagedResult<UserSummary> searchSummaries(SearchUserCommand command);
}
