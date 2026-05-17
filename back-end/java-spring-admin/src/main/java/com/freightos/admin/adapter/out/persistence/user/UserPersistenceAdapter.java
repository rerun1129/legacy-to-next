package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.port.out.UserPort;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserDomainToJpaMapper domainToJpaMapper;
    private final UserJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<UserSummary> searchSummaries(SearchUserCommand command) {
        return userRepository.searchSummaries(command);
    }

    @Override
    public Optional<AdminUser> findById(Long id) {
        return userRepository.findById(id)
                .map(this::loadDomainWithPermissions);
    }

    @Override
    public Optional<AdminUser> findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(this::loadDomainWithPermissions);
    }

    @Override
    public Long save(AdminUser user) {
        UserJpaEntity entity = domainToJpaMapper.toNewJpa(user);
        userRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long id, AdminUser patchData) {
        UserJpaEntity entity = userRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("USER_NOT_FOUND", MessageCode.USER_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
        // 영속 컨텍스트 dirty checking으로 flush 시 자동 UPDATE
    }

    @Override
    public void softDelete(Long id) {
        UserJpaEntity entity = userRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("USER_NOT_FOUND", MessageCode.USER_NOT_FOUND.getMessage()));
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
    }

    @Override
    public long countActiveByRole(UserRole role) {
        return userRepository.countByRoleAndActiveTrueAndDeletedAtIsNull(role);
    }

    @Override
    public void savePermissions(Long userId, Set<Permission> permissions) {
        userPermissionRepository.deleteAllByUserId(userId);
        permissions.forEach(p -> userPermissionRepository.save(new UserPermissionJpaEntity(userId, p)));
    }

    @Override
    public Set<Permission> findPermissionsByUserId(Long userId) {
        return userPermissionRepository.findAllByUserId(userId).stream()
                .map(UserPermissionJpaEntity::getPermission)
                .collect(Collectors.toSet());
    }

    /** JPA 엔티티를 도메인으로 변환하면서 permissions를 함께 로드한다. */
    private AdminUser loadDomainWithPermissions(UserJpaEntity entity) {
        Set<Permission> permissions = findPermissionsByUserId(entity.getId());
        return jpaToDomainMapper.toDomain(entity, permissions);
    }
}
