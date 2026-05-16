package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
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
 * nullifyMasterRefByMasterBlId: master 삭제 전 자식 참조 3컬럼 NULL화 동작 검증.
 * FK 위반 없이 Master B/L을 삭제할 수 있도록 하는 사전 조치 쿼리.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class HouseBlNullifyMasterRefRepositoryTest {

    @Autowired
    private HouseBlRepository houseBlRepository;

    @Autowired
    private TestEntityManager em;

    private MasterBlJpaEntity persistMasterBl() {
        MasterBlJpaEntity master = new MasterBlJpaEntity();
        master.setJobDiv(MasterBlJobDiv.SEA);
        master.setBound(Bound.EXP);
        em.persist(master);
        em.flush();
        return master;
    }

    private HouseBlJpaEntity persistHouseBlLinkedTo(Long masterBlId, String mblNo, String masterRefNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.SEA);
        house.setBound(Bound.EXP);
        house.setMasterBlId(masterBlId);
        house.setMblNo(mblNo);
        house.setMasterRefNo(masterRefNo);
        em.persist(house);
        em.flush();
        return house;
    }

    private HouseBlJpaEntity persistUnlinkedHouseBl() {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.AIR);
        house.setBound(Bound.IMP);
        // masterBlId/mblNo/masterRefNo 모두 null — 독립 HBL
        em.persist(house);
        em.flush();
        return house;
    }

    @Test
    @DisplayName("nullifyMasterRefByMasterBlId - 참조 행의 3컬럼이 모두 NULL이 된다")
    void nullifyMasterRef_linkedRows_allThreeColumnsBecomNull() {
        MasterBlJpaEntity master = persistMasterBl();
        HouseBlJpaEntity linked = persistHouseBlLinkedTo(master.getMasterBlId(), "MBL-001", "REF-001");

        int affected = houseBlRepository.nullifyMasterRefByMasterBlId(master.getMasterBlId());
        em.clear();

        HouseBlJpaEntity refreshed = em.find(HouseBlJpaEntity.class, linked.getHouseBlId());
        assertThat(refreshed.getMasterBlId()).isNull();
        assertThat(refreshed.getMblNo()).isNull();
        assertThat(refreshed.getMasterRefNo()).isNull();
        assertThat(affected).isEqualTo(1);
    }

    @Test
    @DisplayName("nullifyMasterRefByMasterBlId - 미참조 HBL 행은 영향받지 않는다")
    void nullifyMasterRef_unlinkedRows_notAffected() {
        MasterBlJpaEntity master = persistMasterBl();
        HouseBlJpaEntity unlinked = persistUnlinkedHouseBl();

        houseBlRepository.nullifyMasterRefByMasterBlId(master.getMasterBlId());
        em.clear();

        HouseBlJpaEntity refreshed = em.find(HouseBlJpaEntity.class, unlinked.getHouseBlId());
        // 원래 null이었으므로 변경 없음, 행 자체도 삭제되지 않음
        assertThat(refreshed).isNotNull();
        assertThat(refreshed.getMasterBlId()).isNull();
    }

    @Test
    @DisplayName("nullifyMasterRefByMasterBlId - 영향 row 수를 정확히 반환한다(다수 연결)")
    void nullifyMasterRef_multipleLinkedRows_returnsCorrectCount() {
        MasterBlJpaEntity master = persistMasterBl();
        persistHouseBlLinkedTo(master.getMasterBlId(), "MBL-A", "REF-A");
        persistHouseBlLinkedTo(master.getMasterBlId(), "MBL-B", "REF-B");

        // 다른 마스터에 연결된 HBL은 영향받지 않아야 한다
        MasterBlJpaEntity otherMaster = persistMasterBl();
        persistHouseBlLinkedTo(otherMaster.getMasterBlId(), "MBL-C", "REF-C");

        int affected = houseBlRepository.nullifyMasterRefByMasterBlId(master.getMasterBlId());

        assertThat(affected).isEqualTo(2);

        // 다른 마스터의 HBL은 그대로
        List<HouseBlJpaEntity> others = houseBlRepository.findAll().stream()
                .filter(h -> otherMaster.getMasterBlId().equals(h.getMasterBlId()))
                .toList();
        assertThat(others).hasSize(1);
    }
}
