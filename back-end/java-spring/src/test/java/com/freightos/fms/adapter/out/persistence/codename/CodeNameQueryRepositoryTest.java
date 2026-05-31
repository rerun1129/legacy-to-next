package com.freightos.fms.adapter.out.persistence.codename;

import com.freightos.common.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CodeNameQueryRepository — admin 스키마 code→name batch resolve 검증.
 * H2 ddl-auto:create-drop + create_namespaces:true 로 admin 스키마 자동 생성.
 * @Immutable 엔티티라 EntityManager.persist 불가 → @Sql native insert 시드 사용.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, CodeNameQueryRepository.class})
@Sql("/codename-seed.sql")
class CodeNameQueryRepositoryTest {

    @Autowired
    private CodeNameQueryRepository queryRepository;

    // ── customer ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findCustomerNames - 활성 코드에 대해 code→name 정확 매핑")
    void fetchCustomerNames_activeCode_returnsCorrectName() {
        Map<String, String> result = queryRepository.fetchCustomerNames(List.of("CUST-A"));

        assertThat(result).containsEntry("CUST-A", "고객 A");
    }

    @Test
    @DisplayName("findCustomerNames - deleted_at IS NOT NULL 행은 결과에 포함되지 않음")
    void fetchCustomerNames_deletedCode_excluded() {
        Map<String, String> result = queryRepository.fetchCustomerNames(List.of("CUST-B"));

        assertThat(result).doesNotContainKey("CUST-B");
    }

    @Test
    @DisplayName("findCustomerNames - 미존재 코드는 맵에 없음(예외 아님)")
    void fetchCustomerNames_nonexistentCode_absentWithoutException() {
        Map<String, String> result = queryRepository.fetchCustomerNames(List.of("NO-SUCH"));

        assertThat(result).doesNotContainKey("NO-SUCH");
    }

    @Test
    @DisplayName("findCustomerNames - IN 배치 동작: 활성 코드만 반환, 삭제 코드 제외")
    void fetchCustomerNames_batch_returnsOnlyActiveCodes() {
        Map<String, String> result = queryRepository.fetchCustomerNames(List.of("CUST-A", "CUST-B", "NO-SUCH"));

        assertThat(result).containsOnlyKeys("CUST-A");
        assertThat(result.get("CUST-A")).isEqualTo("고객 A");
    }

    @Test
    @DisplayName("findCustomerNames - 빈 입력은 빈 맵 반환")
    void fetchCustomerNames_emptyCodes_returnsEmptyMap() {
        Map<String, String> result = queryRepository.fetchCustomerNames(List.of());

        assertThat(result).isEmpty();
    }

    // ── port ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findPortNames - 활성 포트 코드에 대해 code→name 정확 매핑")
    void fetchPortNames_activeCode_returnsCorrectName() {
        Map<String, String> result = queryRepository.fetchPortNames(List.of("KRPUS"));

        assertThat(result).containsEntry("KRPUS", "부산항");
    }

    @Test
    @DisplayName("findPortNames - deleted_at IS NOT NULL 행은 결과에 포함되지 않음")
    void fetchPortNames_deletedCode_excluded() {
        Map<String, String> result = queryRepository.fetchPortNames(List.of("KRSEL"));

        assertThat(result).doesNotContainKey("KRSEL");
    }

    @Test
    @DisplayName("findPortNames - IN 배치 동작: 활성 코드만 반환")
    void fetchPortNames_batch_returnsOnlyActiveCodes() {
        Map<String, String> result = queryRepository.fetchPortNames(List.of("KRPUS", "KRSEL", "UNKNOWN"));

        assertThat(result).containsOnlyKeys("KRPUS");
    }

    @Test
    @DisplayName("findPortNames - 빈 입력은 빈 맵 반환")
    void fetchPortNames_emptyCodes_returnsEmptyMap() {
        Map<String, String> result = queryRepository.fetchPortNames(List.of());

        assertThat(result).isEmpty();
    }

    // ── carrier ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findCarrierNames - 활성 선사 코드에 대해 code→name 정확 매핑")
    void fetchCarrierNames_activeCode_returnsCorrectName() {
        Map<String, String> result = queryRepository.fetchCarrierNames(List.of("HMMD"));

        assertThat(result).containsEntry("HMMD", "HMM");
    }

    @Test
    @DisplayName("findCarrierNames - deleted_at IS NOT NULL 행은 결과에 포함되지 않음")
    void fetchCarrierNames_deletedCode_excluded() {
        Map<String, String> result = queryRepository.fetchCarrierNames(List.of("GONE"));

        assertThat(result).doesNotContainKey("GONE");
    }

    @Test
    @DisplayName("findCarrierNames - IN 배치 동작: 활성 코드만 반환")
    void fetchCarrierNames_batch_returnsOnlyActiveCodes() {
        Map<String, String> result = queryRepository.fetchCarrierNames(List.of("HMMD", "GONE", "UNKNOWN"));

        assertThat(result).containsOnlyKeys("HMMD");
    }

    @Test
    @DisplayName("findCarrierNames - 빈 입력은 빈 맵 반환")
    void fetchCarrierNames_emptyCodes_returnsEmptyMap() {
        Map<String, String> result = queryRepository.fetchCarrierNames(List.of());

        assertThat(result).isEmpty();
    }

    // ── admin_user ────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchUserNames - user_eng_name 있는 사용자: username → user_eng_name 반환")
    void fetchUserNames_withEngName_returnsEngName() {
        Map<String, String> result = queryRepository.fetchUserNames(List.of("john.doe"));

        assertThat(result).containsEntry("john.doe", "John Doe");
    }

    @Test
    @DisplayName("fetchUserNames - user_eng_name NULL → email fallback 반환")
    void fetchUserNames_engNameNull_returnsEmailFallback() {
        Map<String, String> result = queryRepository.fetchUserNames(List.of("jane.smith"));

        assertThat(result).containsEntry("jane.smith", "jane.smith@example.com");
    }

    @Test
    @DisplayName("fetchUserNames - deleted_at IS NOT NULL 행은 결과에 포함되지 않음")
    void fetchUserNames_deletedUser_excluded() {
        Map<String, String> result = queryRepository.fetchUserNames(List.of("ghost.user"));

        assertThat(result).doesNotContainKey("ghost.user");
    }

    @Test
    @DisplayName("fetchUserNames - 빈 입력은 빈 맵 반환")
    void fetchUserNames_emptyUsernames_returnsEmptyMap() {
        Map<String, String> result = queryRepository.fetchUserNames(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fetchUserNames - IN 배치: 활성 사용자만, 삭제 사용자 제외")
    void fetchUserNames_batch_returnsOnlyActiveUsers() {
        Map<String, String> result = queryRepository.fetchUserNames(List.of("john.doe", "jane.smith", "ghost.user", "unknown"));

        assertThat(result).containsOnlyKeys("john.doe", "jane.smith");
        assertThat(result.get("john.doe")).isEqualTo("John Doe");
        assertThat(result.get("jane.smith")).isEqualTo("jane.smith@example.com");
    }

    // ── hs_code ───────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchHsCodeNames - 활성 HS 코드에 대해 code→name 정확 매핑")
    void fetchHsCodeNames_activeCode_returnsCorrectName() {
        Map<String, String> result = queryRepository.fetchHsCodeNames(List.of("8471.30"));

        assertThat(result).containsEntry("8471.30", "휴대용 자동자료처리기계");
    }

    @Test
    @DisplayName("fetchHsCodeNames - deleted_at IS NOT NULL 행은 결과에 포함되지 않음")
    void fetchHsCodeNames_deletedCode_excluded() {
        Map<String, String> result = queryRepository.fetchHsCodeNames(List.of("9999.99"));

        assertThat(result).doesNotContainKey("9999.99");
    }

    @Test
    @DisplayName("fetchHsCodeNames - IN 배치 동작: 활성 코드만 반환, 삭제 코드 제외")
    void fetchHsCodeNames_batch_returnsOnlyActiveCodes() {
        Map<String, String> result = queryRepository.fetchHsCodeNames(List.of("8471.30", "9999.99", "UNKNOWN"));

        assertThat(result).containsOnlyKeys("8471.30");
        assertThat(result.get("8471.30")).isEqualTo("휴대용 자동자료처리기계");
    }

    @Test
    @DisplayName("fetchHsCodeNames - 빈 입력은 빈 맵 반환")
    void fetchHsCodeNames_emptyCodes_returnsEmptyMap() {
        Map<String, String> result = queryRepository.fetchHsCodeNames(List.of());

        assertThat(result).isEmpty();
    }

    // ── team ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("fetchTeamNames - active=true 팀에 대해 teamCode→name 정확 매핑")
    void fetchTeamNames_activeTeam_returnsCorrectName() {
        Map<String, String> result = queryRepository.fetchTeamNames(List.of("TEAM-A"));

        assertThat(result).containsEntry("TEAM-A", "영업팀");
    }

    @Test
    @DisplayName("fetchTeamNames - active=false 팀은 결과에 포함되지 않음")
    void fetchTeamNames_inactiveTeam_excluded() {
        Map<String, String> result = queryRepository.fetchTeamNames(List.of("TEAM-Z"));

        assertThat(result).doesNotContainKey("TEAM-Z");
    }

    @Test
    @DisplayName("fetchTeamNames - 미존재 코드는 맵에 없음(예외 아님)")
    void fetchTeamNames_nonexistentCode_absentWithoutException() {
        Map<String, String> result = queryRepository.fetchTeamNames(List.of("NO-SUCH-TEAM"));

        assertThat(result).doesNotContainKey("NO-SUCH-TEAM");
    }

    @Test
    @DisplayName("fetchTeamNames - 빈 입력은 빈 맵 반환")
    void fetchTeamNames_emptyCodes_returnsEmptyMap() {
        Map<String, String> result = queryRepository.fetchTeamNames(List.of());

        assertThat(result).isEmpty();
    }
}
