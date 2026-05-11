package com.freightos.fms.adapter.out.persistence.housebl.strategy;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;

/**
 * jobDiv별 영속성 확장 처리 전략 인터페이스.
 * 어댑터가 부모(house_bl) save 완료 후 호출 — Strategy는 @Transactional 미부착 (어댑터 트랜잭션 안에서 호출).
 * Application/도메인 계층은 이 인터페이스를 알지 않는다(Adapter 패키지 내부 전용).
 *
 * @param <T> jobDiv별 도메인 서브타입
 */
interface HouseBlPersistenceStrategy<T extends HouseBl> {

    /** 이 Strategy가 담당하는 jobDiv */
    JobDiv jobDiv();

    /**
     * 부모 save 완료 후 jobDiv 전용 확장 엔티티(ext + 자식)를 저장한다.
     * NON_BL은 역방향 sync 후 도메인 객체를 직접 반환(DB 재조회 없이 응답).
     * SEA/AIR/TRUCK은 저장 후 loadWithExt 호출을 위해 savedParent를 활용한다.
     *
     * @param domain     저장할 도메인 객체
     * @param savedParent 부모 JPA 엔티티(이미 save 완료, PK 보유)
     * @return 업데이트된 도메인 객체 (NON_BL: 역방향 sync된 domain, 나머지: loadWithExt 결과)
     */
    T saveExt(T domain, HouseBlJpaEntity savedParent);

    /**
     * 부모 JPA 엔티티를 기반으로 jobDiv 전용 확장 엔티티를 조회해 도메인 객체로 변환한다.
     *
     * @param parent 부모 JPA 엔티티
     * @return 확장 필드가 채워진 도메인 객체
     */
    HouseBl loadWithExt(HouseBlJpaEntity parent);

    /**
     * jobDiv 전용 자식 엔티티(ext + 자식들)를 모두 삭제한다.
     * 부모(house_bl) 삭제는 어댑터가 수행 — Strategy는 ext 계층만 담당.
     *
     * @param parentId house_bl.house_bl_id
     */
    void deleteExt(Long parentId);
}
