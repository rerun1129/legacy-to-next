package com.freightos.admin.adapter.out.persistence.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserJpaToDomainMapper {

    private final ObjectMapper objectMapper;

    public AdminUser toDomain(UserJpaEntity e) {
        AdminUser domain = AdminUser.create(
                e.getUsername(), e.getEmail(), e.getPasswordHash(), e.getActive(), parseAttributes(e.getAttributes()));
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }

    public UserSummary toSummary(UserJpaEntity e) {
        return new UserSummary(e.getId(), e.getUsername(), e.getEmail(), e.getActive(), e.getDeletedAt(), e.getUpdatedAt(), parseAttributesForSummary(e.getAttributes()));
    }

    /** UserRepositoryImpl에서 UserSummary 생성 시 attributes 파싱에 공유 사용 */
    public Map<String, List<String>> parseAttributesForSummary(String json) {
        return parseAttributes(json);
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
