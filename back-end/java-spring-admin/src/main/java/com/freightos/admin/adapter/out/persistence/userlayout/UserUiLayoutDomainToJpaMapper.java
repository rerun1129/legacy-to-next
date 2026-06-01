package com.freightos.admin.adapter.out.persistence.userlayout;

import com.freightos.admin.domain.userlayout.entity.UserUiLayout;
import org.springframework.stereotype.Component;

@Component
public class UserUiLayoutDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public UserUiLayoutJpaEntity toNewJpa(UserUiLayout domain) {
        UserUiLayoutJpaEntity entity = new UserUiLayoutJpaEntity();
        entity.setUserId(domain.getUserId());
        entity.setStorageKey(domain.getStorageKey());
        entity.setPayload(domain.getPayload());
        return entity;
    }

    /** payload 필드만 갱신. */
    public void applyPayloadUpdate(UserUiLayoutJpaEntity entity, UserUiLayout domain) {
        entity.setPayload(domain.getPayload());
    }
}
