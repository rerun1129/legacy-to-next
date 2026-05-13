package com.freightos.fms.integration.housebl;

import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlPersistenceAdapter;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlRepositoryImpl;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlAirPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlNonBlPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlSeaPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlTruckPersistenceStrategy;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
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
 * House B/L hblNo EXACT 매칭 단건 PK 조회 통합 테스트.
 * findHouseBlKeysByHblNoExact(hblNo, jobDiv) 사용 시
 * 부분 매칭·대소문자·jobDiv 격리, 중복 한도(limit=2) 동작을 검증한다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({
        QueryDslConfig.class,
        HouseBlRepositoryImpl.class,
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
class HouseBlFindByHblNoIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private HouseBlPersistenceAdapter houseBlPersistenceAdapter;

    // ── 헬퍼 ──────────────────────────────────────────────────────────────

    private HouseBlJpaEntity persistSea(String hblNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.SEA);
        house.setBound(Bound.EXP);
        house.setHblNo(hblNo);
        em.persist(house);

        HouseBlSeaJpaEntity sea = new HouseBlSeaJpaEntity();
        sea.setHouseBl(house);
        em.persist(sea);

        return house;
    }

    private HouseBlJpaEntity persistSeaAndFlush(String hblNo) {
        HouseBlJpaEntity house = persistSea(hblNo);
        em.flush();
        em.clear();
        return em.find(HouseBlJpaEntity.class, house.getHouseBlId());
    }

    private HouseBlJpaEntity persistNonBl(String hblNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.NON_BL);
        house.setBound(Bound.EXP);
        house.setHblNo(hblNo);
        em.persist(house);
        return house;
    }

    // ── 테스트 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("미매칭 hbl_no → 빈 리스트 반환")
    void findHouseBlKeysByHblNoExact_noMatch_returnsEmpty() {
        persistSea("SEA-A");
        em.flush();
        em.clear();

        List<Long> result = houseBlPersistenceAdapter.findHouseBlKeysByHblNoExact("SEA-NOTEXIST", JobDiv.SEA);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단건 매칭 'SEA-A' → size=1, 반환 id가 persisted id와 일치")
    void findHouseBlKeysByHblNoExact_singleMatch_returnsSingleId() {
        HouseBlJpaEntity house = persistSea("SEA-A");
        em.flush();
        em.clear();

        List<Long> result = houseBlPersistenceAdapter.findHouseBlKeysByHblNoExact("SEA-A", JobDiv.SEA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(house.getHouseBlId());
    }

    @Test
    @DisplayName("부분 매칭 거부: row='SEA-2026-001', 입력='SEA' → 빈 리스트")
    void findHouseBlKeysByHblNoExact_partialInput_returnsEmpty() {
        persistSea("SEA-2026-001");
        em.flush();
        em.clear();

        List<Long> result = houseBlPersistenceAdapter.findHouseBlKeysByHblNoExact("SEA", JobDiv.SEA);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대소문자 거부: row='SEA-A', 입력='sea-a' → 빈 리스트")
    void findHouseBlKeysByHblNoExact_caseInsensitiveInput_returnsEmpty() {
        persistSea("SEA-A");
        em.flush();
        em.clear();

        List<Long> result = houseBlPersistenceAdapter.findHouseBlKeysByHblNoExact("sea-a", JobDiv.SEA);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("우연 중복 2건(hbl_no='SEA-DUP') → size=2, createdAt desc 정렬로 나중 생성된 row가 앞에 위치")
    void findHouseBlKeysByHblNoExact_twoDuplicates_returnsTwo_orderedByCreatedAtDesc() {
        // 첫 번째 row를 먼저 flush하여 createdAt이 더 이른 시간으로 설정
        HouseBlJpaEntity first = persistSeaAndFlush("SEA-DUP");
        HouseBlJpaEntity second = persistSeaAndFlush("SEA-DUP");

        List<Long> result = houseBlPersistenceAdapter.findHouseBlKeysByHblNoExact("SEA-DUP", JobDiv.SEA);

        assertThat(result).hasSize(2);
        // createdAt desc, id desc → 나중에 생성된 second가 먼저 위치
        assertThat(result.get(0)).isEqualTo(second.getHouseBlId());
        assertThat(result.get(1)).isEqualTo(first.getHouseBlId());
    }

    @Test
    @DisplayName("우연 중복 3건(hbl_no='SEA-TRI') → limit=2이므로 size=2")
    void findHouseBlKeysByHblNoExact_threeDuplicates_returnsLimitTwo() {
        persistSea("SEA-TRI");
        persistSea("SEA-TRI");
        persistSea("SEA-TRI");
        em.flush();
        em.clear();

        List<Long> result = houseBlPersistenceAdapter.findHouseBlKeysByHblNoExact("SEA-TRI", JobDiv.SEA);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("NON_BL jobDiv row('SEA-X') + SEA row('SEA-Y'), NON_BL jobDiv로 'SEA-X' 검색 → SEA 필터로 빈 리스트")
    void findHouseBlKeysByHblNoExact_nonBlJobDivRow_excludedByJobDivFilter() {
        persistNonBl("SEA-X");
        persistSea("SEA-Y");
        em.flush();
        em.clear();

        // SEA jobDiv로 조회 시 NON_BL row 'SEA-X'는 제외됨
        List<Long> result = houseBlPersistenceAdapter.findHouseBlKeysByHblNoExact("SEA-X", JobDiv.SEA);

        assertThat(result).isEmpty();
    }
}
