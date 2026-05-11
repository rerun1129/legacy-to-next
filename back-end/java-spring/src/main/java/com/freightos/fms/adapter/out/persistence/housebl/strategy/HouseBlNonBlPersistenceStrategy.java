package com.freightos.fms.adapter.out.persistence.housebl.strategy;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlContainerRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlDimRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.housebl.entity.HouseBlDim;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NON_BL jobDiv 전용 영속성 전략.
 * house_bl_non_bl + house_bl_nonbl_container + house_bl_nonbl_dim 처리 담당.
 *
 * NON_BL은 loadWithExt 제거로 응답용 SELECT를 줄이기 위해 역방향 sync 패치 흐름을 사용한다.
 * saveExt 호출 후 DB 재조회 없이 in-memory 도메인 객체에 생성된 PK를 역방향 sync하여 반환한다.
 */
@Component
@RequiredArgsConstructor
public class HouseBlNonBlPersistenceStrategy implements HouseBlPersistenceStrategy<HouseBlNonBl> {

    private final HouseBlNonBlRepository houseBlNonBlRepository;
    private final HouseBlNonBlContainerRepository houseBlNonBlContainerRepository;
    private final HouseBlNonBlDimRepository houseBlNonBlDimRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;

    @Override
    public JobDiv jobDiv() {
        return JobDiv.NON_BL;
    }

    @Override
    public HouseBlNonBl saveExt(HouseBlNonBl nonBl, HouseBlJpaEntity savedParent) {
        HouseBlNonBlJpaEntity nonBlJpa = houseBlNonBlRepository.findByHouseBlHouseBlId(savedParent.getHouseBlId())
                .orElseGet(HouseBlNonBlJpaEntity::new);
        nonBlJpa.setHouseBl(savedParent);
        domainToJpaMapper.applyNonBlFields(nonBl, nonBlJpa);
        // NON_BL 컨테이너 merge — nonBlJpa 소유 (house_bl_nonbl_container)
        List<HouseBlNonBlContainerJpaEntity> nonBlContainers = nonBl.getContainers().stream()
                .map(houseBlCargoMapper::toNonBlContainerJpa)
                .toList();
        nonBlJpa.mergeContainers(nonBlContainers);
        // NON_BL dim merge — nonBlJpa 소유 (house_bl_nonbl_dim)
        List<HouseBlNonBlDimJpaEntity> nonBlDims = nonBl.getDims().stream()
                .map(houseBlCargoMapper::toNonBlDimJpa)
                .toList();
        nonBlJpa.mergeDims(nonBlDims);
        // NON_BL은 desc를 사용하지 않음 — remark는 house_bl_non_bl 컬럼으로 저장됨
        houseBlNonBlRepository.save(nonBlJpa);
        // in-memory 도메인에 JPA save 결과(parent id, 감사 필드, 자식 PK)를 역방향 sync.
        // loadWithExt 제거로 응답용 SELECT 제거 — 직렬 저장 순서와 동일하므로 인덱스 매핑 안전.
        nonBl.assignIdentity(savedParent.getHouseBlId(), savedParent.getCreatedAt(), savedParent.getUpdatedAt(),
                savedParent.getCreatedBy(), savedParent.getUpdatedBy());
        syncChildIds(nonBl.getContainers(), nonBlJpa.getContainers());
        syncDimIds(nonBl.getDims(), nonBlJpa.getDims());
        return nonBl;
    }

    @Override
    public HouseBl loadWithExt(HouseBlJpaEntity parent) {
        return jpaToDomainMapper.toNonBlDomain(parent, houseBlNonBlRepository.findByHouseBlHouseBlId(parent.getHouseBlId()).orElse(null));
    }

    @Override
    public void deleteExt(Long parentId) {
        houseBlNonBlContainerRepository.deleteByParentHouseBlId(parentId);
        houseBlNonBlDimRepository.deleteByParentHouseBlId(parentId);
        houseBlNonBlRepository.deleteByHouseBl_HouseBlId(parentId);
    }

    /**
     * NON_BL 컨테이너 도메인 자식에 JPA save 후 생성된 PK를 역방향 sync.
     * mergeContainers의 결과 순서(incoming 순) = 도메인 자식 순서이므로 인덱스 1:1 매핑이 안전하다.
     * 기존에 id가 있던 자식은 동일 id가 유지되므로 중복 할당 무해.
     */
    private void syncChildIds(List<HouseBlContainer> domainContainers, List<HouseBlNonBlContainerJpaEntity> jpaContainers) {
        int size = Math.min(domainContainers.size(), jpaContainers.size());
        for (int i = 0; i < size; i++) {
            HouseBlNonBlContainerJpaEntity jpa = jpaContainers.get(i);
            HouseBlContainer domainContainer = domainContainers.get(i);
            if (domainContainer.getId() == null && jpa.getHouseBlNonBlContainerId() != null) {
                domainContainer.assignIdentity(jpa.getHouseBlNonBlContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                        jpa.getCreatedBy(), jpa.getUpdatedBy());
            }
        }
    }

    /**
     * NON_BL DIM 도메인 자식에 JPA save 후 생성된 PK를 역방향 sync.
     * mergeDims의 결과 순서(incoming 순) = 도메인 자식 순서이므로 인덱스 1:1 매핑이 안전하다.
     */
    private void syncDimIds(List<HouseBlDim> domainDims, List<HouseBlNonBlDimJpaEntity> jpaDims) {
        int size = Math.min(domainDims.size(), jpaDims.size());
        for (int i = 0; i < size; i++) {
            HouseBlNonBlDimJpaEntity jpa = jpaDims.get(i);
            HouseBlDim domainDim = domainDims.get(i);
            if (domainDim.getId() == null && jpa.getHouseBlNonBlDimId() != null) {
                domainDim.assignIdentity(jpa.getHouseBlNonBlDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                        jpa.getCreatedBy(), jpa.getUpdatedBy());
            }
        }
    }
}
