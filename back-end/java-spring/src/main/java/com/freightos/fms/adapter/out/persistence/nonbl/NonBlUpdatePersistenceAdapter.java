package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.nonbl.port.out.NonBlPersistencePort;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NonBlUpdatePersistenceAdapter implements NonBlPersistencePort {

    private final HouseBlRepository houseBlRepository;
    private final HouseBlNonBlRepository houseBlNonBlRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlFactory houseBlFactory;

    @Override
    @Transactional
    public void update(Long id, UpdateHouseBlCommand command) {
        HouseBlJpaEntity parentJpa = houseBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));
        if (parentJpa.getJobDiv() != JobDiv.NON_BL) {
            throw new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND);
        }
        HouseBlNonBlJpaEntity nonBlJpa = houseBlNonBlRepository.findByHouseBlHouseBlId(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));

        // 도메인 객체로 변환 — toNonBlDomain이 containers/dims 컬렉션까지 로드하므로 LAZY 재트리거 없음
        HouseBlNonBl domain = jpaToDomainMapper.toNonBlDomain(parentJpa, nonBlJpa);

        // 팩토리가 도메인 검증(etd≤eta 등) 포함 모든 필드를 도메인에 적용
        houseBlFactory.applyToEntity(command, domain);

        // 도메인 → JPA dirty-check 기반 필드 반영 (saveHouseBl 미호출)
        domainToJpaMapper.applyCommonFields(domain, parentJpa);
        domainToJpaMapper.applyNonBlFields(domain, nonBlJpa);

        // 자식 컬렉션 merge — orphanRemoval이 DELETE, 신규 진입이 INSERT를 처리
        List<HouseBlNonBlContainerJpaEntity> incomingContainers = domain.getContainers().stream()
                .map(houseBlCargoMapper::toNonBlContainerJpa)
                .toList();
        nonBlJpa.mergeContainers(incomingContainers);

        List<HouseBlNonBlDimJpaEntity> incomingDims = domain.getDims().stream()
                .map(houseBlCargoMapper::toNonBlDimJpa)
                .toList();
        nonBlJpa.mergeDims(incomingDims);
        // 트랜잭션 커밋 시 dirty-checking으로 parentJpa·nonBlJpa UPDATE 자동 발생
    }
}
