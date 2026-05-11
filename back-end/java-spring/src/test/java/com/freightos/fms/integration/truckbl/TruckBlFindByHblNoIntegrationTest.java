package com.freightos.fms.integration.truckbl;

import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlPersistenceAdapter;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlAirPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlNonBlPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlSeaPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlTruckPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.truckbl.TruckBlPersistenceAdapter;
import com.freightos.fms.adapter.out.persistence.truckbl.TruckBlRepositoryImpl;
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
 * Truck B/L hblNo EXACT 매칭 단건 PK 조회 통합 테스트.
 * findTruckBlKeysByHblNoExact 사용 시 부분 매칭·대소문자·jobDiv 차단, 중복 한도(limit=2) 동작을 검증한다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({
        QueryDslConfig.class,
        TruckBlRepositoryImpl.class,
        TruckBlPersistenceAdapter.class,
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
class TruckBlFindByHblNoIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TruckBlPersistenceAdapter truckBlPersistenceAdapter;

    // ── 헬퍼 ──────────────────────────────────────────────────────────────

    private HouseBlJpaEntity persistTruck(String hblNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.TRUCK);
        house.setBound(Bound.EXP);
        house.setHblNo(hblNo);
        em.persist(house);

        HouseBlTruckJpaEntity truck = new HouseBlTruckJpaEntity();
        truck.setHouseBl(house);
        truck.setVesselName("TRUCK");
        em.persist(truck);

        return house;
    }

    private HouseBlJpaEntity persistTruckAndFlush(String hblNo) {
        HouseBlJpaEntity house = persistTruck(hblNo);
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
    void findTruckBlKeysByHblNoExact_noMatch_returnsEmpty() {
        persistTruck("TRK-A");
        em.flush();
        em.clear();

        List<Long> result = truckBlPersistenceAdapter.findTruckBlKeysByHblNoExact("TRK-NOTEXIST");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단건 매칭 'TRK-A' → size=1, 반환 id가 persisted id와 일치")
    void findTruckBlKeysByHblNoExact_singleMatch_returnsSingleId() {
        HouseBlJpaEntity house = persistTruck("TRK-A");
        em.flush();
        em.clear();

        List<Long> result = truckBlPersistenceAdapter.findTruckBlKeysByHblNoExact("TRK-A");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(house.getHouseBlId());
    }

    @Test
    @DisplayName("부분 매칭 거부: row='TRK-2026-001', 입력='TRK' → 빈 리스트")
    void findTruckBlKeysByHblNoExact_partialInput_returnsEmpty() {
        persistTruck("TRK-2026-001");
        em.flush();
        em.clear();

        List<Long> result = truckBlPersistenceAdapter.findTruckBlKeysByHblNoExact("TRK");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("우연 중복 2건(hbl_no='TRK-DUP') → size=2, createdAt desc 정렬로 나중 생성된 row가 앞에 위치")
    void findTruckBlKeysByHblNoExact_twoDuplicates_returnsTwo_orderedByCreatedAtDesc() {
        HouseBlJpaEntity first = persistTruckAndFlush("TRK-DUP");
        HouseBlJpaEntity second = persistTruckAndFlush("TRK-DUP");

        List<Long> result = truckBlPersistenceAdapter.findTruckBlKeysByHblNoExact("TRK-DUP");

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(second.getHouseBlId());
        assertThat(result.get(1)).isEqualTo(first.getHouseBlId());
    }

    @Test
    @DisplayName("우연 중복 3건(hbl_no='TRK-TRI') → limit=2이므로 size=2")
    void findTruckBlKeysByHblNoExact_threeDuplicates_returnsLimitTwo() {
        persistTruck("TRK-TRI");
        persistTruck("TRK-TRI");
        persistTruck("TRK-TRI");
        em.flush();
        em.clear();

        List<Long> result = truckBlPersistenceAdapter.findTruckBlKeysByHblNoExact("TRK-TRI");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("SEA jobDiv row('SEA-X') + TRUCK row('TRK-Y'), 'SEA-X' 검색 → TRUCK 필터로 빈 리스트")
    void findTruckBlKeysByHblNoExact_seaJobDivRow_excludedByJobDivFilter() {
        persistSea("SEA-X");
        persistTruck("TRK-Y");
        em.flush();
        em.clear();

        List<Long> result = truckBlPersistenceAdapter.findTruckBlKeysByHblNoExact("SEA-X");

        assertThat(result).isEmpty();
    }
}
