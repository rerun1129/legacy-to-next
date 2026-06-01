package com.freightos.admin.adapter.out.persistence.userlayout;

import com.freightos.admin.domain.userlayout.entity.UserUiLayout;
import org.springframework.stereotype.Component;

@Component
public class UserUiLayoutJpaToDomainMapper {

    public UserUiLayout toDomain(UserUiLayoutJpaEntity e) {
        UserUiLayout domain = UserUiLayout.create(e.getUserId(), e.getStorageKey(), e.getPayload());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt());
        return domain;
    }
}
