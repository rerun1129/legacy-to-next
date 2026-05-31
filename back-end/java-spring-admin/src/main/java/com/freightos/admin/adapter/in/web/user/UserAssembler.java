package com.freightos.admin.adapter.in.web.user;

import com.freightos.admin.adapter.in.web.user.dto.CreateUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.SaveUserChangesRequest;
import com.freightos.admin.adapter.in.web.user.dto.SearchUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.UpdateUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.UserDetailResponse;
import com.freightos.admin.adapter.in.web.user.dto.UserSummaryResponse;
import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.application.user.command.SaveUserChangesCommand;
import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.command.UpdateUserCommand;
import com.freightos.admin.application.user.projection.UserScope;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserAssembler {

    public SearchUserCommand toSearchCommand(SearchUserRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        UserScope scope = req.scope() != null ? req.scope() : UserScope.ALL;
        return new SearchUserCommand(req.username(), scope, req.page(), size);
    }

    public CreateUserCommand toCreateCommand(CreateUserRequest req) {
        return new CreateUserCommand(req.username(), req.email(), req.password(), req.active(),
                req.attributes() != null ? req.attributes() : Collections.emptyMap(), req.teamId(), req.subscriberId());
    }

    public UpdateUserCommand toUpdateCommand(UpdateUserRequest req) {
        return new UpdateUserCommand(req.email(), req.password(), req.active(),
                req.attributes() != null ? req.attributes() : Collections.emptyMap(), req.teamId(), req.subscriberId());
    }

    public UserSummaryResponse toSummaryResponse(UserSummary p) {
        return new UserSummaryResponse(p.id(), p.username(), p.email(), p.active(), p.deletedAt(), p.updatedAt(), p.attributes(), p.teamId(), p.subscriberId());
    }

    public UserDetailResponse toDetail(AdminUser domain) {
        return new UserDetailResponse(
                domain.getId(), domain.getUsername(), domain.getEmail(),
                domain.isActive(), domain.getDeletedAt(), domain.getAttributes(),
                domain.getCreatedAt(), domain.getUpdatedAt(),
                domain.getCreatedBy(), domain.getUpdatedBy(), domain.getTeamId(), domain.getSubscriberId()
        );
    }

    public PagedResult<UserSummaryResponse> toSummaryPage(PagedResult<UserSummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public SaveUserChangesCommand toSaveChangesCommand(SaveUserChangesRequest req) {
        List<CreateUserCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SaveUserChangesCommand.UpdateEntry> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SaveUserChangesCommand.UpdateEntry(u.id(),
                            new UpdateUserCommand(u.email(), u.password(), u.active(), u.attributes(), u.teamId(), u.subscriberId())))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SaveUserChangesCommand(creates, updates, deleteIds);
    }
}
