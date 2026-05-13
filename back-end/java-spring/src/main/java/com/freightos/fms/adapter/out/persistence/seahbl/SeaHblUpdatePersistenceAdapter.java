package com.freightos.fms.adapter.out.persistence.seahbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaDescRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.seahbl.port.out.SeaHblPersistencePort;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sea House B/L update 전용 어댑터 (§6.35).
 * parent fetch → jobDiv 검증 → ext fetch → 도메인 변환 → factory 적용 →
 * attached JPA 직접 매핑 → mergeContainers → dirty-checking으로 UPDATE 자동 발사.
 * saveHouseBl 미호출 — SELECT 6→4 절감.
 */
@Component
@RequiredArgsConstructor
public class SeaHblUpdatePersistenceAdapter implements SeaHblPersistencePort {

    private final HouseBlRepository houseBlRepository;
    private final HouseBlSeaRepository houseBlSeaRepository;
    private final HouseBlSeaDescRepository houseBlSeaDescRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;
    private final HouseBlFactory houseBlFactory;

    @Override
    @Transactional
    public void update(Long id, UpdateHouseBlCommand command) {
        HouseBlJpaEntity parentJpa = houseBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.SEA_HBL_NOT_FOUND));
        if (parentJpa.getJobDiv() != JobDiv.SEA) {
            throw new ResourceNotFoundException(MessageCode.SEA_HBL_NOT_FOUND);
        }
        HouseBlSeaJpaEntity seaJpa = houseBlSeaRepository.findByHouseBlHouseBlId(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.SEA_HBL_NOT_FOUND));

        HouseBlSeaDescJpaEntity descJpa = houseBlSeaDescRepository
                .findBySea_HouseBlSeaId(seaJpa.getHouseBlSeaId()).orElse(null);

        // 도메인 변환 — containers 컬렉션까지 LAZY 트리거
        HouseBlSea domain = jpaToDomainMapper.toSeaDomain(parentJpa, seaJpa, descJpa);

        // 팩토리가 도메인 검증(etd≤eta 등) 포함 모든 필드를 도메인에 적용
        houseBlFactory.applyToEntity(command, domain);

        // 도메인 → JPA dirty-check 기반 필드 반영 (§6.37 sub-set 매퍼)
        domainToJpaMapper.applySeaCommonFields(domain, parentJpa);
        domainToJpaMapper.applySeaBlFields(domain, seaJpa);

        // Desc 동기화 — 1:1 관계
        applyDescSync(domain, seaJpa, descJpa);

        // Container merge-by-id — id 일치 시 UPDATE, 미일치 시 INSERT, orphanRemoval이 DELETE 처리 (§6.35)
        List<HouseBlSeaContainerJpaEntity> incomingContainers = domain.getContainers().stream()
                .map(houseBlCargoMapper::toSeaContainerJpa)
                .toList();
        seaJpa.mergeContainers(incomingContainers);
        // 트랜잭션 커밋 시 dirty-checking으로 parentJpa·seaJpa UPDATE 자동 발생
    }

    private void applyDescSync(HouseBlSea domain, HouseBlSeaJpaEntity seaJpa,
                               HouseBlSeaDescJpaEntity existingDescJpa) {
        HouseBlDesc domainDesc = domain.getDesc();
        if (domainDesc == null) return;

        HouseBlSeaDescJpaEntity targetJpa = existingDescJpa != null
                ? existingDescJpa
                : new HouseBlSeaDescJpaEntity();
        houseBlDocMapper.applySeaDescFields(domainDesc, targetJpa, seaJpa);
        if (existingDescJpa == null) {
            houseBlSeaDescRepository.save(targetJpa);
        }
    }
}
