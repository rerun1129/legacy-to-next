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
 * jobDiv 격리(NON_BL 필터) 동작 및 adapter 라우팅을 검증한다.
 * EXACT 매칭 규칙(부분/대소문자/limit) SSOT는 HouseBlFindByHblNoIntegrationTest.
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
