package com.freightos.admin.application.user;

import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.command.UpdateUserCommand;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.application.user.port.out.UserPort;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserUseCase {

    private final UserPort userPort;
    private final UserFactory userFactory;

    @Override
    public PagedResult<UserSummary> searchUsers(SearchUserCommand command) {
        return userPort.searchSummaries(command);
    }

    @Override
    public AdminUser findUserById(Long id) {
        return userPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("USER_NOT_FOUND", MessageCode.USER_NOT_FOUND.getMessage()));
    }

    @Override
    public AdminUser findUserByUsername(String username) {
        return userPort.findByUsername(username)
                .orElseThrow(() -> ApplicationException.notFound("USER_NOT_FOUND", MessageCode.USER_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createUser(CreateUserCommand command) {
        try {
            Long id = userPort.save(userFactory.from(command));
            userPort.savePermissions(id, command.permissions());
            return id;
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("USER_DUPLICATE_USERNAME", MessageCode.USER_DUPLICATE_USERNAME.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateUser(Long id, UpdateUserCommand command) {
        AdminUser existing = findUserById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("USER_DELETED", MessageCode.USER_ALREADY_DELETED.getMessage());
        }
        // 마지막 활성 ADMIN 비활성화·역할 강등 방어
        if (existing.getRole() == UserRole.ADMIN && existing.isActive()) {
            boolean demotingOrDeactivating = command.role() != UserRole.ADMIN || !command.active();
            if (demotingOrDeactivating && userPort.countActiveByRole(UserRole.ADMIN) <= 1) {
                throw ApplicationException.conflict("LAST_ADMIN", MessageCode.USER_LAST_ADMIN.getMessage());
            }
        }
        existing.applyUpdate(command.email(), userFactory.hashIfPresent(command.rawPasswordOrNull()), command.role(), command.active(), command.permissions());
        userPort.update(id, existing);
        userPort.savePermissions(id, command.permissions());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        AdminUser existing = findUserById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("USER_DELETED", MessageCode.USER_ALREADY_DELETED.getMessage());
        }
        // 마지막 활성 ADMIN 삭제 방어
        if (existing.getRole() == UserRole.ADMIN && existing.isActive()
                && userPort.countActiveByRole(UserRole.ADMIN) <= 1) {
            throw ApplicationException.conflict("LAST_ADMIN", MessageCode.USER_LAST_ADMIN.getMessage());
        }
        userPort.softDelete(id);
    }
}
