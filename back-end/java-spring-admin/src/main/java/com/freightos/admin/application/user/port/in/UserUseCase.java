package com.freightos.admin.application.user.port.in;

import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.command.UpdateUserCommand;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;

import java.util.List;

public interface UserUseCase {
    PagedResult<UserSummary> searchUsers(SearchUserCommand command);
    AdminUser findUserById(Long id);
    AdminUser findUserByUsername(String username);
    Long createUser(CreateUserCommand command);
    void updateUser(Long id, UpdateUserCommand command);
    void deleteUser(Long id);
    void deleteUsers(List<Long> ids);
}
