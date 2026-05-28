package com.freightos.admin.application.user;

import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.application.user.command.SaveUserChangesCommand;
import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.command.UpdateUserCommand;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.application.user.port.out.UserPort;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            return userPort.save(userFactory.from(command));
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
        if (existing.hasRole("ADMIN") && existing.isActive()) {
            boolean deactivating = !command.active();
            boolean demoting = !command.attributes().getOrDefault("role", List.of()).contains("ADMIN");
            if ((deactivating || demoting) && userPort.countActiveAdmins() <= 1) {
                throw ApplicationException.conflict("LAST_ADMIN", MessageCode.USER_LAST_ADMIN.getMessage());
            }
        }
        existing.applyUpdate(command.email(), userFactory.hashIfPresent(command.rawPasswordOrNull()), command.active(), command.attributes());
        userPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        AdminUser existing = findUserById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("USER_DELETED", MessageCode.USER_ALREADY_DELETED.getMessage());
        }
        // 마지막 활성 ADMIN 삭제 방어
        if (existing.hasRole("ADMIN") && existing.isActive() && userPort.countActiveAdmins() <= 1) {
            throw ApplicationException.conflict("LAST_ADMIN", MessageCode.USER_LAST_ADMIN.getMessage());
        }
        userPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteUsers(List<Long> ids) {
        for (Long id : ids) {
            deleteUser(id);
        }
    }

    @Override
    @Transactional
    public SaveChangesResult saveUserChanges(SaveUserChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deleteUser(id);
        }
        for (SaveUserChangesCommand.UpdateEntry entry : command.updates()) {
            // grid에서 보내는 attributes를 기존 attributes에 merge하여 덮어쓴다
            AdminUser existing = findUserById(entry.id());
            Map<String, List<String>> merged = new HashMap<>(existing.getAttributes());
            merged.putAll(entry.command().attributes());
            UpdateUserCommand mergedCmd = new UpdateUserCommand(entry.command().email(), entry.command().rawPasswordOrNull(), entry.command().active(), merged);
            updateUser(entry.id(), mergedCmd);
        }
        for (CreateUserCommand create : command.creates()) {
            createUser(create);
        }
        return new SaveChangesResult(command.creates().size(), command.updates().size(), command.deleteIds().size());
    }

    @Override
    public List<AutocompleteItem> autocompleteUsers(String query, int limit) {
        return userPort.autocomplete(query, limit);
    }
}
