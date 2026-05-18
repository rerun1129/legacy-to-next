package com.freightos.admin.adapter.out.persistence.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserJpaToDomainMapper {

    private final ObjectMapper objectMapper;

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
        domain.assignAttributes(parseAttributes(e.getAttributes()));
        return domain;
    }

    public UserSummary toSummary(UserJpaEntity e) {
        return new UserSummary(e.getId(), e.getUsername(), e.getEmail(), e.getRole(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt());
    }

    private Map<String, List<String>> parseAttributes(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            // 역직렬화 실패 시 빈 Map 반환 — 권한 평가에 영향을 주지 않도록 조용히 처리
            log.warn("attributes JSON 파싱 실패: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }
}
