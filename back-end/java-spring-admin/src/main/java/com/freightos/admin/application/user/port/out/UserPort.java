package com.freightos.admin.application.user.port.out;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;

import java.util.Optional;

public interface UserPort {
    PagedResult<UserSummary> searchSummaries(SearchUserCommand command);
    Optional<AdminUser> findById(Long id);
    Optional<AdminUser> findByUsername(String username);
    Long save(AdminUser user);
    void update(Long id, AdminUser patchData);
    void softDelete(Long id);
    /** attributes.role에 "ADMIN"을 가진 활성 사용자 수를 반환한다. */
    long countActiveAdmins();
}
