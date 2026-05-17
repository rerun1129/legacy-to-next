package com.freightos.admin.application.user.port.out;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;

import java.util.Optional;
import java.util.Set;

public interface UserPort {
    PagedResult<UserSummary> searchSummaries(SearchUserCommand command);
    Optional<AdminUser> findById(Long id);
    Optional<AdminUser> findByUsername(String username);
    Long save(AdminUser user);
    void update(Long id, AdminUser patchData);
    void softDelete(Long id);
    long countActiveByRole(UserRole role);
    void savePermissions(Long userId, Set<Permission> permissions);
    Set<Permission> findPermissionsByUserId(Long userId);
}
