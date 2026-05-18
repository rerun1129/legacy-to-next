package com.freightos.admin.adapter.out.persistence.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDomainToJpaMapper {

    private final ObjectMapper objectMapper;

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public UserJpaEntity toNewJpa(AdminUser domain) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setAttributes(serializeAttributes(domain.getAttributes()));
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. username은 불변이므로 건드리지 않는다.
     * passwordHash가 null이면 기존 값을 유지한다.
     */
    public void applyUpdateFields(UserJpaEntity entity, AdminUser patch) {
        entity.setEmail(patch.getEmail());
        entity.setActive(patch.isActive());
        entity.setAttributes(serializeAttributes(patch.getAttributes()));
        if (patch.getPasswordHash() != null) {
            entity.setPasswordHash(patch.getPasswordHash());
        }
    }

    private String serializeAttributes(Map<String, List<String>> attrs) {
        try {
            return objectMapper.writeValueAsString(attrs);
        } catch (Exception ex) {
            log.warn("attributes JSON 직렬화 실패: {}", ex.getMessage());
            return "{}";
        }
    }
}
