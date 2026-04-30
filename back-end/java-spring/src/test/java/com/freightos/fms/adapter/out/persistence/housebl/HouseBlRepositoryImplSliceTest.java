package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.application.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QueryDSL HouseBlSummary projection의 N+1 회피와 페이징·정렬 동작을 검증한다.
 * H2 in-memory + Hibernate Statistics로 실제 PreparedStatement 횟수를 측정.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class HouseBlRepositoryImplSliceTest {

    @Autowired
    private HouseBlRepository houseBlRepository;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManagerFactory emf;

    private Statistics stats;

    @BeforeEach
    void enableStatistics() {
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: PreparedStatement 정확히 2회 (SELECT content + COUNT)")
    void findSummariesByJobDivAndBound_emitsExactlyTwoStatements() {
        persist(JobDiv.SEA, Bound.EXP, 2);
        em.flush();
        em.clear();
        stats.clear();

        houseBlRepository.findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        assertThat(stats.getPrepareStatementCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: jobDiv+bound 필터가 정확히 동작한다")
    void findSummariesByJobDivAndBound_filtersByJobDivAndBound() {
        persist(JobDiv.SEA, Bound.EXP, 2);
        persist(JobDiv.SEA, Bound.IMP, 1);
        persist(JobDiv.AIR, Bound.EXP, 1);
        em.flush();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        result.getContent().forEach(s -> {
            assertThat(s.jobDiv()).isEqualTo(JobDiv.SEA);
            assertThat(s.bound()).isEqualTo(Bound.EXP);
        });
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: HouseBlSummary projection의 핵심 필드가 올바르게 채워진다")
    void findSummariesByJobDivAndBound_returnsCorrectProjection() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.SEA);
        jpa.setBound(Bound.EXP);
        jpa.setHblNo("HBL-TEST-001");
        jpa.setPolCode("KRPUS");
        jpa.setPodCode("USNYC");
        jpa.setShipperCode("SHIP01");
        jpa.setConsigneeCode("CONS01");
        jpa.setPkgQty(5);
        jpa.setPkgUnit("CTN");
        em.persist(jpa);
        em.flush();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        HouseBlSummary s = result.getContent().get(0);
        assertThat(s.houseBlId()).isNotNull();
        assertThat(s.hblNo()).isEqualTo("HBL-TEST-001");
        assertThat(s.jobDiv()).isEqualTo(JobDiv.SEA);
        assertThat(s.bound()).isEqualTo(Bound.EXP);
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.pkgQty()).isEqualTo(5);
        assertThat(s.pkgUnit()).isEqualTo("CTN");
        assertThat(s.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: 결과가 createdAt 내림차순으로 정렬된다")
    void findSummariesByJobDivAndBound_sortedByCreatedAtDesc() {
        HouseBlJpaEntity first = newJpa(JobDiv.SEA, Bound.EXP);
        em.persist(first);
        em.flush();

        HouseBlJpaEntity second = newJpa(JobDiv.SEA, Bound.EXP);
        em.persist(second);
        em.flush();
        em.clear();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        List<HouseBlSummary> content = result.getContent();
        assertThat(content).hasSize(2);
        assertThat(content.get(0).createdAt())
                .isAfterOrEqualTo(content.get(1).createdAt());
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: 페이지네이션이 동작한다 (size=2, page=1 → 2건 반환, total=5)")
    void findSummariesByJobDivAndBound_appliesPagination() {
        persist(JobDiv.SEA, Bound.EXP, 5);
        em.flush();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    private void persist(JobDiv jobDiv, Bound bound, int count) {
        for (int i = 0; i < count; i++) {
            em.persist(newJpa(jobDiv, bound));
        }
    }

    private HouseBlJpaEntity newJpa(JobDiv jobDiv, Bound bound) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(jobDiv);
        jpa.setBound(bound);
        return jpa;
    }
}
