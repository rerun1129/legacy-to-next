package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface UserRepositoryCustom {
    PagedResult<UserSummary> searchSummaries(SearchUserCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
