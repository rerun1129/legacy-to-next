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
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Truck B/L hblNo 변경 격리 통합 테스트.
 * 동일 hbl_no 를 공유하는 두 TRUCK 행에서 한 행만 변경되고 나머지는 영향 없음을 검증한다.
 * SEA jobDiv 행에 대한 jobDiv 불일치 updateHblNoById → affected=0 동작도 검증한다.
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
class TruckBlChangeHblNoIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private HouseBlPersistenceAdapter houseBlPersistenceAdapter;

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
    @DisplayName("동일 hbl_no='TRK-001' 인 TRUCK A·B 중 A만 변경 → A.hblNo='CHANGED', B.hblNo 불변")
    void changeHblNo_onlyTargetRowUpdated_otherUnaffected() {
        HouseBlJpaEntity rowA = persistTruck("TRK-001");
        HouseBlJpaEntity rowB = persistTruck("TRK-001");
        em.flush();
        em.clear();

        long affected = houseBlPersistenceAdapter.updateHblNoById(rowA.getHouseBlId(), BlNumber.of("CHANGED"), JobDiv.TRUCK);
        em.flush();
        em.clear();

        assertThat(affected).isEqualTo(1L);
        HouseBlTruck reloadedA = (HouseBlTruck) houseBlPersistenceAdapter.findHouseBlById(rowA.getHouseBlId()).orElseThrow();
        assertThat(reloadedA.getHblNo().value()).isEqualTo("CHANGED");

        HouseBlTruck reloadedB = (HouseBlTruck) houseBlPersistenceAdapter.findHouseBlById(rowB.getHouseBlId()).orElseThrow();
        assertThat(reloadedB.getHblNo().value()).isEqualTo("TRK-001");
    }

    @Test
    @DisplayName("jobDiv 불일치: SEA 행 id에 TRUCK 조건으로 updateHblNoById → affected=0, SEA 행 hblNo 불변")
    void updateHblNoById_jobDivMismatch_returnsZeroAndSeaRowUnchanged() {
        HouseBlJpaEntity seaRow = persistSea("SEA-ORIGINAL");
        em.flush();
        em.clear();

        long affected = houseBlPersistenceAdapter.updateHblNoById(seaRow.getHouseBlId(), BlNumber.of("CHANGED"), JobDiv.TRUCK);
        em.flush();
        em.clear();

        assertThat(affected).isZero();
        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, seaRow.getHouseBlId());
        assertThat(reloaded.getHblNo()).isEqualTo("SEA-ORIGINAL");
    }

    @Test
    @DisplayName("TRUCK 미존재: id=99999 으로 updateHblNoById → affected=0")
    void updateHblNoById_nonExistentId_returnsZero() {
        long affected = houseBlPersistenceAdapter.updateHblNoById(99999L, BlNumber.of("ANY"), JobDiv.TRUCK);

        assertThat(affected).isZero();
    }
}
