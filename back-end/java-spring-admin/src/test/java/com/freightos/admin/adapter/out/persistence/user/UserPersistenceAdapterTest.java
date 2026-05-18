package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.projection.UserScope;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.config.JpaAuditingConfig;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.user.entity.AdminUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({UserPersistenceAdapter.class, UserDomainToJpaMapper.class, UserJpaToDomainMapper.class, UserRepositoryImpl.class, JpaAuditingConfig.class, JacksonAutoConfiguration.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:userpa;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.flyway.enabled=false"
})
class UserPersistenceAdapterTest {

    @Autowired
    private UserPersistenceAdapter adapter;

    @Autowired
    private UserRepository userRepository;

    private static final Map<String, List<String>> ATTRS_USER = Map.of("role", List.of("USER"));
    private static final Map<String, List<String>> ATTRS_ADMIN = Map.of("role", List.of("ADMIN"));

    // ── save → id 반환 + findById 재조회 + 필드 일치 ─────────────────────────

    @Test
    void save_thenFindById_fieldsMatch() {
        AdminUser user = AdminUser.create("alice", "alice@example.com", "hashed_pw", true, ATTRS_USER);

        Long id = adapter.save(user);

        Optional<AdminUser> found = adapter.findById(id);
        assertThat(found).isPresent();
        AdminUser result = found.get();
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.isActive()).isTrue();
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getAttributes()).isEqualTo(ATTRS_USER);
        // id는 동적 참조 (T1: 시퀀스 절대값 비교 금지)
        assertThat(result.getId()).isEqualTo(id);
    }

    // ── findByUsername: deletedAt IS NULL 필터 검증 ───────────────────────────

    @Test
    void findByUsername_deletedUserExcluded() {
        AdminUser user = AdminUser.create("bob", "bob@example.com", "hashed_pw", true, ATTRS_USER);
        Long id = adapter.save(user);

        // soft delete 수행
        adapter.softDelete(id);

        // deletedAt IS NULL 조건으로 조회 시 empty 반환
        Optional<AdminUser> found = adapter.findByUsername("bob");
        assertThat(found).isEmpty();
    }

    // ── softDelete 후 findByUsername empty (deletedAt IS NULL 필터) ─────────────

    @Test
    void softDelete_thenFindByUsername_returnsEmpty() {
        AdminUser user = AdminUser.create("carol", null, "hashed_pw", true, ATTRS_ADMIN);
        Long id = adapter.save(user);

        adapter.softDelete(id);

        // findByUsername은 deletedAt IS NULL 필터 적용 — 삭제 후 empty
        assertThat(adapter.findByUsername("carol")).isEmpty();
    }

    // ── softDelete 후 findById는 여전히 도메인 반환 ───────────────────────────

    @Test
    void findById_softDeletedUser_returnsDomain() {
        AdminUser user = AdminUser.create("carol2", null, "hashed_pw", true, ATTRS_ADMIN);
        Long id = adapter.save(user);

        adapter.softDelete(id);

        // findById는 deletedAt filter 없음 — 삭제된 사용자도 반환
        Optional<AdminUser> found = adapter.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().isDeleted()).isTrue();
        assertThat(found.get().isActive()).isFalse();
    }

    // ── searchSummaries: 여러 row + filter + page ─────────────────────────────

    @Test
    void searchSummaries_filterAndPage() {
        adapter.save(AdminUser.create("alice", "alice@example.com", "h1", true, ATTRS_USER));
        adapter.save(AdminUser.create("adam", "adam@example.com", "h2", true, ATTRS_ADMIN));
        adapter.save(AdminUser.create("bob", "bob@example.com", "h3", false, ATTRS_USER));

        // username 앞일치 "a" 필터
        SearchUserCommand command = new SearchUserCommand("a", UserScope.ALL, 0, 20);
        PagedResult<UserSummary> result = adapter.searchSummaries(command);

        assertThat(result.getTotalElements()).isEqualTo(2L);
        // username asc 정렬 — adam, alice 순
        assertThat(result.getContent().get(0).username()).isEqualTo("adam");
        assertThat(result.getContent().get(1).username()).isEqualTo("alice");
    }

    // ── countActiveAdmins: ADMIN attributes 2건 → 2, 1건 비활성 → 1 ──────────

    @Test
    void countActiveAdmins_returnsCorrectCount() {
        adapter.save(AdminUser.create("admin1", null, "h1", true, ATTRS_ADMIN));
        adapter.save(AdminUser.create("admin2", null, "h2", true, ATTRS_ADMIN));
        adapter.save(AdminUser.create("admin3", null, "h3", false, ATTRS_ADMIN));

        long activeCount = adapter.countActiveAdmins();

        // active=true이고 deletedAt IS NULL인 ADMIN만 카운트
        assertThat(activeCount).isEqualTo(2L);
    }

    // ── searchSummaries scope=DELETED → 삭제된 사용자만 반환 ─────────────────

    @Test
    void searchSummaries_scopeDeleted_returnsOnlySoftDeleted() {
        adapter.save(AdminUser.create("scopeAdmin", "sa@example.com", "h1", true, ATTRS_ADMIN));
        Long testerId = adapter.save(AdminUser.create("scopeTester", "st@example.com", "h2", true, ATTRS_USER));

        // tester만 soft delete
        adapter.softDelete(testerId);

        // DELETED scope → 삭제된 1건만
        PagedResult<UserSummary> deletedResult = adapter.searchSummaries(new SearchUserCommand(null, UserScope.DELETED, 0, 20));
        assertThat(deletedResult.getContent()).hasSize(1);
        assertThat(deletedResult.getContent().get(0).username()).isEqualTo("scopeTester");
        assertThat(deletedResult.getContent().get(0).deletedAt()).isNotNull();

        // ALL scope → 미삭제 1건만 (deletedAt IS NULL)
        PagedResult<UserSummary> allResult = adapter.searchSummaries(new SearchUserCommand("scope", UserScope.ALL, 0, 20));
        assertThat(allResult.getContent()).hasSize(1);
        assertThat(allResult.getContent().get(0).username()).isEqualTo("scopeAdmin");
        assertThat(allResult.getContent().get(0).deletedAt()).isNull();

        // ACTIVE scope → active=true 미삭제만
        PagedResult<UserSummary> activeResult = adapter.searchSummaries(new SearchUserCommand("scope", UserScope.ACTIVE, 0, 20));
        assertThat(activeResult.getContent()).hasSize(1);
        assertThat(activeResult.getContent().get(0).username()).isEqualTo("scopeAdmin");
    }

    // ── attributes가 update 후 반영되는지 확인 ────────────────────────────────

    @Test
    void update_attributes_persisted() {
        AdminUser user = AdminUser.create("updatetest", null, "hashed", true, ATTRS_USER);
        Long id = adapter.save(user);

        AdminUser existing = adapter.findById(id).orElseThrow();
        existing.applyUpdate(null, null, true, ATTRS_ADMIN);
        adapter.update(id, existing);

        AdminUser updated = adapter.findById(id).orElseThrow();
        assertThat(updated.hasRole("ADMIN")).isTrue();
        assertThat(updated.hasRole("USER")).isFalse();
    }
}
