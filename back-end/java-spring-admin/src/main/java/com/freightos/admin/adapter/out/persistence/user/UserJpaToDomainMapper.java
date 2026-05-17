package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class UserJpaToDomainMapper {

    /** permissions 없이 변환. findById/findByUsername에서 permissions는 어댑터가 별도 주입한다. */
    public AdminUser toDomain(UserJpaEntity e) {
        return toDomain(e, Collections.emptySet());
    }

    /** permissions를 함께 받아 변환. */
    public AdminUser toDomain(UserJpaEntity e, Set<Permission> permissions) {
        AdminUser domain = AdminUser.create(
                e.getUsername(), e.getEmail(), e.getPasswordHash(), e.getRole(), e.getActive(), permissions);
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }

    public UserSummary toSummary(UserJpaEntity e) {
        return new UserSummary(e.getId(), e.getUsername(), e.getEmail(), e.getRole(), e.getActive(), e.getUpdatedAt());
    }
}
