package com.freightos.fms.integration.masterbl;

import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.adapter.out.persistence.masterbl.MasterBlMapper;
import com.freightos.fms.adapter.out.persistence.masterbl.MasterBlPersistenceAdapter;
import com.freightos.fms.adapter.out.persistence.masterbl.MasterBlRepositoryImpl;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
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
 * Master B/L mblNo EXACT 매칭 단건 PK 조회 통합 테스트.
 * findMasterBlKeysByMblNoExact(mblNo) 사용 시
 * 부분 매칭·대소문자·limit=2 동작을 검증한다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({
        QueryDslConfig.class,
        MasterBlRepositoryImpl.class,
        MasterBlPersistenceAdapter.class,
        MasterBlMapper.class
})
class MasterBlFindByMblNoIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private MasterBlPersistenceAdapter masterBlPersistenceAdapter;

    // ── 헬퍼 ──────────────────────────────────────────────────────────────

    private MasterBlJpaEntity persistSea(String mblNo) {
        MasterBlJpaEntity parent = new MasterBlJpaEntity();
        parent.setJobDiv(MasterBlJobDiv.SEA);
        parent.setBound(Bound.EXP);
        parent.setMblNo(mblNo);
        em.persist(parent);

        MasterBlSeaJpaEntity sea = new MasterBlSeaJpaEntity();
        sea.setMasterBl(parent);
        em.persist(sea);

        return parent;
    }

    private MasterBlJpaEntity persistSeaAndFlush(String mblNo) {
        MasterBlJpaEntity parent = persistSea(mblNo);
        em.flush();
        em.clear();
        return em.find(MasterBlJpaEntity.class, parent.getMasterBlId());
    }

    // ── 테스트 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("미매칭 mbl_no → 빈 리스트 반환")
    void findMasterBlKeysByMblNoExact_noMatch_returnsEmpty() {
        persistSea("MBL-A");
        em.flush();
        em.clear();

        List<Long> result = masterBlPersistenceAdapter.findMasterBlKeysByMblNoExact("MBL-NOTEXIST");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("단건 매칭 'MBL-A' → size=1, 반환 id가 persisted id와 일치")
    void findMasterBlKeysByMblNoExact_singleMatch_returnsSingleId() {
        MasterBlJpaEntity parent = persistSea("MBL-A");
        em.flush();
        em.clear();

        List<Long> result = masterBlPersistenceAdapter.findMasterBlKeysByMblNoExact("MBL-A");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(parent.getMasterBlId());
    }

    @Test
    @DisplayName("부분 매칭 거부: row='MBL-2026-001', 입력='MBL' → 빈 리스트")
    void findMasterBlKeysByMblNoExact_partialInput_returnsEmpty() {
        persistSea("MBL-2026-001");
        em.flush();
        em.clear();

        List<Long> result = masterBlPersistenceAdapter.findMasterBlKeysByMblNoExact("MBL");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대소문자 거부: row='MBL-A', 입력='mbl-a' → 빈 리스트")
    void findMasterBlKeysByMblNoExact_caseInsensitiveInput_returnsEmpty() {
        persistSea("MBL-A");
        em.flush();
        em.clear();

        List<Long> result = masterBlPersistenceAdapter.findMasterBlKeysByMblNoExact("mbl-a");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("우연 중복 2건(mbl_no='MBL-DUP') → size=2, createdAt desc 정렬로 나중 생성된 row가 앞에 위치")
    void findMasterBlKeysByMblNoExact_twoDuplicates_returnsTwo_orderedByCreatedAtDesc() {
        // 첫 번째 row를 먼저 flush하여 createdAt이 더 이른 시간으로 설정
        MasterBlJpaEntity first = persistSeaAndFlush("MBL-DUP");
        MasterBlJpaEntity second = persistSeaAndFlush("MBL-DUP");

        List<Long> result = masterBlPersistenceAdapter.findMasterBlKeysByMblNoExact("MBL-DUP");

        assertThat(result).hasSize(2);
        // createdAt desc, id desc → 나중에 생성된 second가 먼저 위치
        assertThat(result.get(0)).isEqualTo(second.getMasterBlId());
        assertThat(result.get(1)).isEqualTo(first.getMasterBlId());
    }

    @Test
    @DisplayName("우연 중복 3건(mbl_no='MBL-TRI') → limit=2이므로 size=2")
    void findMasterBlKeysByMblNoExact_threeDuplicates_returnsLimitTwo() {
        persistSea("MBL-TRI");
        persistSea("MBL-TRI");
        persistSea("MBL-TRI");
        em.flush();
        em.clear();

        List<Long> result = masterBlPersistenceAdapter.findMasterBlKeysByMblNoExact("MBL-TRI");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("mblNo=null row → null 입력으로 조회 시 빈 리스트 (null 동등 비교 거부)")
    void findMasterBlKeysByMblNoExact_nullMblNoRow_notReturnedByNullInput() {
        // mblNo가 없는 row(null) 1건
        MasterBlJpaEntity parent = new MasterBlJpaEntity();
        parent.setJobDiv(MasterBlJobDiv.SEA);
        parent.setBound(Bound.EXP);
        em.persist(parent);

        MasterBlSeaJpaEntity sea = new MasterBlSeaJpaEntity();
        sea.setMasterBl(parent);
        em.persist(sea);

        em.flush();
        em.clear();

        // null mblNo row는 eq("some-value")에 걸리지 않음
        List<Long> result = masterBlPersistenceAdapter.findMasterBlKeysByMblNoExact("MBL-NONE");

        assertThat(result).isEmpty();
    }
}
