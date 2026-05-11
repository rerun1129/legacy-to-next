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
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
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
 * Non B/L hblNo 변경 격리 통합 테스트.
 * 동일 hbl_no 를 공유하는 두 NON_BL 행에서 한 행만 변경되고 나머지는 영향 없음을 검증한다.
 * SEA jobDiv 행에 대한 findNonBlById 가 빈 Optional 을 반환함(NON_BL 외 차단)도 검증한다.
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
class NonBlChangeHblNoIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private HouseBlPersistenceAdapter houseBlPersistenceAdapter;

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
    @DisplayName("동일 hbl_no='DUP-001' 인 NON_BL A·B 중 A만 변경 → A.hblNo='CHANGED', B.hblNo 불변")
    void changeHblNo_onlyTargetRowUpdated_otherUnaffected() {
        HouseBlJpaEntity rowA = persistNonBl("DUP-001");
        HouseBlJpaEntity rowB = persistNonBl("DUP-001");
        em.flush();
        em.clear();

        // A 조회 → 도메인 변경 → 저장
        HouseBlNonBl domainA = (HouseBlNonBl) houseBlPersistenceAdapter.findHouseBlById(rowA.getHouseBlId()).orElseThrow();
        domainA.changeHblNo(BlNumber.of("CHANGED"));
        houseBlPersistenceAdapter.saveHouseBl(domainA);
        em.flush();
        em.clear();

        // A 검증
        HouseBlNonBl reloadedA = (HouseBlNonBl) houseBlPersistenceAdapter.findHouseBlById(rowA.getHouseBlId()).orElseThrow();
        assertThat(reloadedA.getHblNo().value()).isEqualTo("CHANGED");

        // B 검증 — B는 영향 없음
        HouseBlNonBl reloadedB = (HouseBlNonBl) houseBlPersistenceAdapter.findHouseBlById(rowB.getHouseBlId()).orElseThrow();
        assertThat(reloadedB.getHblNo().value()).isEqualTo("DUP-001");
    }

    @Test
    @DisplayName("SEA jobDiv 행 id에 findNonBlById → 빈 Optional 반환 (NON_BL 외 차단)")
    void findNonBlById_seaJobDiv_returnsEmpty() {
        HouseBlJpaEntity seaRow = persistSea("SEA-001");
        em.flush();
        em.clear();

        Optional<HouseBlNonBl> result = nonBlPersistenceAdapter.findNonBlById(seaRow.getHouseBlId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("jobDiv 불일치: SEA 행 id에 NON_BL 조건으로 updateHblNoById → affected=0, SEA 행 hblNo 불변")
    void updateHblNoById_jobDivMismatch_returnsZeroAndSeaRowUnchanged() {
        HouseBlJpaEntity seaRow = persistSea("SEA-ORIGINAL");
        em.flush();
        em.clear();

        long affected = houseBlPersistenceAdapter.updateHblNoById(seaRow.getHouseBlId(), BlNumber.of("CHANGED"), JobDiv.NON_BL);
        em.flush();
        em.clear();

        assertThat(affected).isZero();
        HouseBlJpaEntity reloaded = em.find(HouseBlJpaEntity.class, seaRow.getHouseBlId());
        assertThat(reloaded.getHblNo()).isEqualTo("SEA-ORIGINAL");
    }

    @Test
    @DisplayName("NON_BL 미존재: id=99999 으로 updateHblNoById → affected=0")
    void updateHblNoById_nonExistentId_returnsZero() {
        long affected = houseBlPersistenceAdapter.updateHblNoById(99999L, BlNumber.of("ANY"), JobDiv.NON_BL);

        assertThat(affected).isZero();
    }
}
