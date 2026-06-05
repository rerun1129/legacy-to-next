package com.freightos.bms.adapter.out.persistence.financialdocument;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * GroupNoSeqJdbcRepository 단위 테스트.
 * JdbcTemplate Mock — DB 불필요. 순차 증가만 결정적 검증.
 */
@ExtendWith(MockitoExtension.class)
class GroupNoSeqJdbcRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private GroupNoSeqJdbcRepository repository;

    @Test
    @DisplayName("upsertNextSeq — JdbcTemplate 반환값을 그대로 반환한다")
    void upsertNextSeq_returnsJdbcResult() {
        given(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq("INVOICE"), eq("2606")))
            .willReturn(1);

        int result = repository.upsertNextSeq("INVOICE", "2606");

        assertThat(result).isEqualTo(1);
    }

    @Test
    @DisplayName("upsertNextSeq — 두 번 호출 시 순차 증가값 반환")
    void upsertNextSeq_sequentialCalls_incrementing() {
        given(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq("INVOICE"), eq("2606")))
            .willReturn(1)
            .willReturn(2);

        int first = repository.upsertNextSeq("INVOICE", "2606");
        int second = repository.upsertNextSeq("INVOICE", "2606");

        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(2);
        assertThat(second).isGreaterThan(first);
    }

    @Test
    @DisplayName("upsertNextSeq — JdbcTemplate이 null 반환 시 IllegalStateException")
    void upsertNextSeq_nullResult_throwsIllegalState() {
        given(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), eq("INVOICE"), eq("2606")))
            .willReturn(null);

        assertThatThrownBy(() -> repository.upsertNextSeq("INVOICE", "2606"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("그룹 채번 실패");
    }
}
