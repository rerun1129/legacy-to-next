package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.domain.user.entity.AdminUser;
import org.springframework.stereotype.Component;

@Component
public class UserJpaToDomainMapper {

    public AdminUser toDomain(UserJpaEntity e) {
        AdminUser domain = AdminUser.create(e.getUsername(), e.getEmail(), e.getPasswordHash(), e.getRole(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }

    public UserSummary toSummary(UserJpaEntity e) {
        return new UserSummary(e.getId(), e.getUsername(), e.getEmail(), e.getRole(), e.getActive(), e.getUpdatedAt());
    }
}
