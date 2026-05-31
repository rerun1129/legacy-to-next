package com.freightos.admin.common.security;

import com.freightos.admin.adapter.out.persistence.subscriber.SubscriberRepository;
import com.freightos.admin.adapter.out.persistence.team.TeamRepository;
import com.freightos.admin.adapter.out.persistence.user.UserJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * admin 모듈 부트 시 'admin'/'fms' 계정이 없으면 BCrypt hash로 시드 INSERT.
 * Flyway(V52)가 ApplicationRunner보다 먼저 실행되므로 admin.team이 이미 존재한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_RAW_PASSWORD = "admin1234";
    private static final String FMS_USERNAME = "fms";
    private static final String FMS_RAW_PASSWORD = "fms12345";

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SubscriberRepository subscriberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // SELF 고객사 ID는 두 시드 모두 공통으로 사용하므로 한 번만 조회
        Long selfSubscriberId = subscriberRepository.findBySubscriberCode("SELF")
                .map(s -> s.getSubscriberId())
                .orElse(null);

        if (userRepository.findByUsernameAndDeletedAtIsNull(ADMIN_USERNAME).isEmpty()) {
            Long strategyTeamId = teamRepository.findByTeamCode("STRATEGY")
                    .map(t -> t.getId())
                    .orElse(null);
            UserJpaEntity entity = new UserJpaEntity();
            entity.setUsername(ADMIN_USERNAME);
            entity.setEmail(null);
            entity.setPasswordHash(passwordEncoder.encode(ADMIN_RAW_PASSWORD));
            entity.setActive(true);
            entity.setAttributes("{\"role\":[\"ADMIN\"],\"module\":[\"ADMIN\"],\"admin_scope\":[\"CODE\",\"USER\",\"CUSTOMER\",\"CMS_NOTICE\",\"ACCESS\",\"SUBSCRIBER\"]}");
            entity.setTeamId(strategyTeamId);
            entity.setSubscriberId(selfSubscriberId);
            userRepository.save(entity);
            log.info("AdminUserSeeder: seeded '{}' user", ADMIN_USERNAME);
        }
        if (userRepository.findByUsernameAndDeletedAtIsNull(FMS_USERNAME).isEmpty()) {
            Long salesTeamId = teamRepository.findByTeamCode("SALES")
                    .map(t -> t.getId())
                    .orElse(null);
            UserJpaEntity fmsEntity = new UserJpaEntity();
            fmsEntity.setUsername(FMS_USERNAME);
            fmsEntity.setEmail(null);
            fmsEntity.setPasswordHash(passwordEncoder.encode(FMS_RAW_PASSWORD));
            fmsEntity.setActive(true);
            fmsEntity.setAttributes("{\"role\":[\"USER\"],\"module\":[\"FMS\"],\"fms_scope\":[\"SEA\",\"AIR\",\"TRUCK\",\"NON_BL\"]}");
            fmsEntity.setTeamId(salesTeamId);
            fmsEntity.setSubscriberId(selfSubscriberId);
            userRepository.save(fmsEntity);
            log.info("AdminUserSeeder: seeded '{}' user", FMS_USERNAME);
        }
    }
}
