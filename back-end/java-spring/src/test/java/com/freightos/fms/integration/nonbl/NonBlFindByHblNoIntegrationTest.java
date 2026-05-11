package com.freightos.fms.integration.nonbl;

import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlPersistenceAdapter;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlAirPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlNonBlPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlSeaPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlTruckPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.nonbl.NonBlPersistenceAdapter;
import com.freightos.fms.adapter.out.persistence.nonbl.NonBlRepositoryImpl;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
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
 * Non B/L hblNo EXACT 매칭 단건 PK 조회 통합 테스트.
 * searchNonBlSummaries(LIKE+JOIN) 대신 findNonBlKeysByHblNoExact 사용 시
 * 부분 매칭·대소문자·jobDiv 차단, 중복 한도(limit=2) 동작을 검증한다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({
        QueryDslConfig.class,
        NonBlRepositoryImpl.class,
        NonBlPersistenceAdapter.class,
        HouseBlPersistenceAdapter.class,
        HouseBlJpaToDomainMapper.class,
        HouseBlDomainToJpaMapper.class,
        HouseBlCargoMapper.class,
        HouseBlDocMapper.class,
        HouseBlSeaPersistenceStrategy.class,
        HouseBlAirPersistenceStrategy.class,
        HouseBlTruckPersistenceStrategy.class,
        HouseBlNonBlPersistenceStrategy.class
})
class NonBlFindByHblNoIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private NonBlPersistenceAdapter nonBlPersistenceAdapter;

    // ── 헬퍼 ──────────────────────────────────────────────────────────────

    private HouseBlJpaEntity persistNonBl(String hblNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.NON_BL);
        house.setBound(Bound.EXP);
        house.setHblNo(hblNo);
        em.persist(house);

        HouseBlNonBlJpaEntity ext = new HouseBlNonBlJpaEntity();
        ext.setHouseBl(house);
        ext.setWorkDivision(HouseBlNonBl.WorkDivision.SEA);
        em.persist(ext);

        return house;
    }

    private HouseBlJpaEntity persistNonBlAndFlush(String hblNo) {
        HouseBlJpaEntity house = persistNonBl(hblNo);
        em.flush();
        em.clear();
        return em.find(HouseBlJpaEntity.class, house.getHouseBlId());
    }

    private HouseBlJpaEntity persistSea(String hblNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.SEA);
        house.setBound(Bound.EXP);
        house.setHblNo(hblNo);
        em.persist(house);
        return house;
    }

    // ── 테스트 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("미매칭 hbl_no → 빈 리스트 반환")
    void findNonBlKeysByHblNoExact_noMatch_returnsEmpty() {
        persistNonBl("HBL-A");
        em.flush();
        em.clear();

        List<Long> result = nonBlPersistenceAdapter.findNonBlKeysByHblNoExact("HBL-NOTEXIST");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단건 매칭 'HBL-A' → size=1, 반환 id가 persisted id와 일치")
    void findNonBlKeysByHblNoExact_singleMatch_returnsSingleId() {
        HouseBlJpaEntity house = persistNonBl("HBL-A");
        em.flush();
        em.clear();

        List<Long> result = nonBlPersistenceAdapter.findNonBlKeysByHblNoExact("HBL-A");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(house.getHouseBlId());
    }

    @Test
    @DisplayName("부분 매칭 거부: row='HBL-2026-001', 입력='HBL' → 빈 리스트")
    void findNonBlKeysByHblNoExact_partialInput_returnsEmpty() {
        persistNonBl("HBL-2026-001");
        em.flush();
        em.clear();

        List<Long> result = nonBlPersistenceAdapter.findNonBlKeysByHblNoExact("HBL");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대소문자 거부: row='HBL-A', 입력='hbl-a' → 빈 리스트")
    void findNonBlKeysByHblNoExact_caseInsensitiveInput_returnsEmpty() {
        persistNonBl("HBL-A");
        em.flush();
        em.clear();

        List<Long> result = nonBlPersistenceAdapter.findNonBlKeysByHblNoExact("hbl-a");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("우연 중복 2건(hbl_no='HBL-DUP') → size=2, createdAt desc 정렬로 나중 생성된 row가 앞에 위치")
    void findNonBlKeysByHblNoExact_twoDuplicates_returnsTwo_orderedByCreatedAtDesc() {
        // 첫 번째 row를 먼저 flush하여 createdAt이 더 이른 시간으로 설정
        HouseBlJpaEntity first = persistNonBlAndFlush("HBL-DUP");
        HouseBlJpaEntity second = persistNonBlAndFlush("HBL-DUP");

        List<Long> result = nonBlPersistenceAdapter.findNonBlKeysByHblNoExact("HBL-DUP");

        assertThat(result).hasSize(2);
        // createdAt desc → 나중에 생성된 second가 먼저 위치
        assertThat(result.get(0)).isEqualTo(second.getHouseBlId());
        assertThat(result.get(1)).isEqualTo(first.getHouseBlId());
    }

    @Test
    @DisplayName("우연 중복 3건(hbl_no='HBL-TRI') → limit=2이므로 size=2")
    void findNonBlKeysByHblNoExact_threeDuplicates_returnsLimitTwo() {
        persistNonBl("HBL-TRI");
        persistNonBl("HBL-TRI");
        persistNonBl("HBL-TRI");
        em.flush();
        em.clear();

        List<Long> result = nonBlPersistenceAdapter.findNonBlKeysByHblNoExact("HBL-TRI");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("SEA jobDiv row('SEA-X') + NON_BL row('NON-Y'), 'SEA-X' 검색 → NON_BL 필터로 빈 리스트")
    void findNonBlKeysByHblNoExact_seaJobDivRow_excludedByJobDivFilter() {
        persistSea("SEA-X");
        persistNonBl("NON-Y");
        em.flush();
        em.clear();

        List<Long> result = nonBlPersistenceAdapter.findNonBlKeysByHblNoExact("SEA-X");

        assertThat(result).isEmpty();
    }
}
