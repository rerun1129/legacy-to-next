package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.domain.user.entity.AdminUser;
import org.springframework.stereotype.Component;

@Component
public class UserDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public UserJpaEntity toNewJpa(AdminUser domain) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setRole(domain.getRole());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. username은 불변이므로 건드리지 않는다.
     * passwordHash가 null이면 기존 값을 유지한다.
     */
    public void applyUpdateFields(UserJpaEntity entity, AdminUser patch) {
        entity.setEmail(patch.getEmail());
        entity.setRole(patch.getRole());
        entity.setActive(patch.isActive());
        if (patch.getPasswordHash() != null) {
            entity.setPasswordHash(patch.getPasswordHash());
        }
    }
}
