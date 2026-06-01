package com.freightos.admin.adapter.out.persistence.userlayout;

import com.freightos.admin.application.userlayout.port.out.UserUiLayoutPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.domain.userlayout.entity.UserUiLayout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserUiLayoutPersistenceAdapter implements UserUiLayoutPort {

    private final UserUiLayoutRepository userUiLayoutRepository;
    private final UserUiLayoutDomainToJpaMapper domainToJpaMapper;
    private final UserUiLayoutJpaToDomainMapper jpaToDomainMapper;

    @Override
    public Optional<UserUiLayout> findByUserIdAndStorageKey(Long userId, String storageKey) {
        return userUiLayoutRepository.findByUserIdAndStorageKey(userId, storageKey)
                .map(jpaToDomainMapper::toDomain);
    }

    @Override
    public UserUiLayout save(UserUiLayout layout) {
        if (layout.getId() == null) {
            // 신규 삽입
            UserUiLayoutJpaEntity entity = domainToJpaMapper.toNewJpa(layout);
            UserUiLayoutJpaEntity saved = userUiLayoutRepository.save(entity);
            return jpaToDomainMapper.toDomain(saved);
        }
        // 기존 row 갱신 — dirty checking으로 flush 시 자동 UPDATE
        UserUiLayoutJpaEntity entity = userUiLayoutRepository.findById(layout.getId())
                .orElseThrow(() -> ApplicationException.notFound("UI_LAYOUT_NOT_FOUND", MessageCode.UI_LAYOUT_DELETED.getMessage()));
        domainToJpaMapper.applyPayloadUpdate(entity, layout);
        return jpaToDomainMapper.toDomain(entity);
    }

    @Override
    public void deleteByUserIdAndStorageKey(Long userId, String storageKey) {
        userUiLayoutRepository.deleteByUserIdAndStorageKey(userId, storageKey);
    }
}
