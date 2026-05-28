package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.port.out.UserPort;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPort {

    private final UserRepository userRepository;
    private final UserDomainToJpaMapper domainToJpaMapper;
    private final UserJpaToDomainMapper jpaToDomainMapper;

    @Override
    public PagedResult<UserSummary> searchSummaries(SearchUserCommand command) {
        return userRepository.searchSummaries(command);
    }

    @Override
    public Optional<AdminUser> findById(Long id) {
        return userRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public Optional<AdminUser> findByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username).map(jpaToDomainMapper::toDomain);
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
    public long countActiveAdmins() {
        // attributes.role에 "ADMIN"을 가진 활성 사용자를 메모리 필터로 집계.
        // 관리자 수가 소량이므로 전체 조회 후 필터가 적합하다.
        return userRepository.findAllByActiveTrueAndDeletedAtIsNull().stream()
                .filter(e -> jpaToDomainMapper.toDomain(e).hasRole("ADMIN"))
                .count();
    }

    @Override
    public List<AutocompleteItem> autocomplete(String query, int limit) {
        return userRepository.autocomplete(query, limit);
    }
}
