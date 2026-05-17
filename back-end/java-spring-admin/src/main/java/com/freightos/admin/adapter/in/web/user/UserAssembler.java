package com.freightos.admin.adapter.in.web.user;

import com.freightos.admin.adapter.in.web.user.dto.CreateUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.SearchUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.UpdateUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.UserDetailResponse;
import com.freightos.admin.adapter.in.web.user.dto.UserSummaryResponse;
import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.command.UpdateUserCommand;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import org.springframework.stereotype.Component;

@Component
public class UserAssembler {

    public SearchUserCommand toSearchCommand(SearchUserRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchUserCommand(req.username(), req.role(), req.active(), req.page(), size);
    }

    public CreateUserCommand toCreateCommand(CreateUserRequest req) {
        return new CreateUserCommand(req.username(), req.email(), req.password(), req.role(), req.active(), req.permissions());
    }

    public UpdateUserCommand toUpdateCommand(UpdateUserRequest req) {
        return new UpdateUserCommand(req.email(), req.password(), req.role(), req.active(), req.permissions());
    }

    public UserSummaryResponse toSummaryResponse(UserSummary p) {
        return new UserSummaryResponse(p.id(), p.username(), p.email(), p.role(), p.active(), p.updatedAt());
    }

    public UserDetailResponse toDetail(AdminUser domain) {
        return new UserDetailResponse(
                domain.getId(), domain.getUsername(), domain.getEmail(),
                domain.getRole(), domain.isActive(), domain.getPermissions(),
                domain.getCreatedAt(), domain.getUpdatedAt(),
                domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<UserSummaryResponse> toSummaryPage(PagedResult<UserSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
