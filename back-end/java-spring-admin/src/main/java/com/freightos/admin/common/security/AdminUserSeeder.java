package com.freightos.admin.common.security;

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
 * admin 모듈 부트 시 'admin' 계정이 없으면 BCrypt hash로 시드 INSERT.
 * Flyway 활성화(G7) + DB 사용자 관리 UI를 통한 시드 절차 도입 전까지의 임시.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

    private static final String SEED_USERNAME = "admin";
    private static final String SEED_RAW_PASSWORD = "admin1234";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsernameAndDeletedAtIsNull(SEED_USERNAME).isPresent()) {
            return;
        }
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUsername(SEED_USERNAME);
        entity.setEmail(null);
        entity.setPasswordHash(passwordEncoder.encode(SEED_RAW_PASSWORD));
        entity.setActive(true);
        entity.setAttributes("{\"role\":[\"ADMIN\"]}");
        userRepository.save(entity);
        log.info("AdminUserSeeder: seeded '{}' user", SEED_USERNAME);
    }
}
