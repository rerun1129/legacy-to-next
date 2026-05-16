package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.application.masterbl.MasterBlFactory;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.port.out.SeaMasterPersistencePort;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SEA Master B/L update 전용 어댑터 (§6.35).
 * parent fetch → jobDiv 검증 → sea ext fetch → 도메인 변환 → factory 적용 →
 * attached JPA 직접 매핑 → applyDescSync → dirty-checking으로 UPDATE 자동 발사.
 * saveMasterBl 미호출 — dirty-checking 위임.
 *
 * House {@code SeaHblUpdatePersistenceAdapter} 패턴 정합.
 */
@Component
@RequiredArgsConstructor
public class SeaMasterUpdatePersistenceAdapter implements SeaMasterPersistencePort {

    private final MasterBlRepository masterBlRepository;
    private final MasterBlSeaRepository masterBlSeaRepository;
    private final MasterBlSeaDescRepository masterBlSeaDescRepository;
    private final MasterBlMapper masterBlMapper;
    private final MasterBlSeaSubMapper masterBlSeaSubMapper;
    private final MasterBlFactory masterBlFactory;

    @Override
    @Transactional
    public void update(Long id, UpdateMasterBlCommand command) {
        MasterBlJpaEntity parentJpa = masterBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));
        if (parentJpa.getJobDiv() != MasterBlJobDiv.SEA) {
            throw new IllegalStateException(
                    "Expected jobDiv=SEA but was: " + parentJpa.getJobDiv() + " for masterBlId=" + id);
        }
        MasterBlSeaJpaEntity seaJpa = masterBlSeaRepository.findByMasterBlMasterBlId(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));

        MasterBlSeaDescJpaEntity descJpa = masterBlSeaDescRepository
                .findBySea_MasterBlSeaId(seaJpa.getMasterBlSeaId()).orElse(null);

        // 도메인 변환 — factory가 도메인 검증 포함 모든 필드를 도메인에 적용
        MasterBlSea domain = (MasterBlSea) masterBlMapper.toSeaDomain(parentJpa, seaJpa, descJpa);
        masterBlFactory.applyToEntity(command, domain);

        // 도메인 → JPA dirty-check 기반 필드 반영 (§6.37 sub-set 매퍼)
        masterBlSeaSubMapper.applyMasterSeaCommonFields(domain, parentJpa);
        masterBlSeaSubMapper.applyMasterSeaFields(domain, seaJpa);

        // Desc 동기화 — 1:1 관계
        applyDescSync(domain, seaJpa, descJpa);
        // 트랜잭션 커밋 시 dirty-checking으로 parentJpa·seaJpa UPDATE 자동 발생
    }

    /**
     * Desc 1:1 동기화 (§6.35).
     *
     * <ul>
     *   <li>domain desc null + repo desc null → no-op
     *   <li>domain desc null + repo desc 존재 → 현행 유지 (§6.37 PATCH: null은 기존 값 보호)
     *   <li>domain desc 존재 + repo desc null → 신규 insert
     *   <li>domain desc 존재 + repo desc 존재 → 필드 덮어쓰기 (dirty-checking UPDATE)
     * </ul>
     *
     * House {@code SeaHblUpdatePersistenceAdapter.applyDescSync} 시그니처 정합.
     */
    private void applyDescSync(MasterBlSea domain, MasterBlSeaJpaEntity seaJpa,
                               MasterBlSeaDescJpaEntity existingDescJpa) {
        MasterBlDesc domainDesc = domain.getDesc();
        if (domainDesc == null) return;

        MasterBlSeaDescJpaEntity targetJpa = existingDescJpa != null
                ? existingDescJpa
                : new MasterBlSeaDescJpaEntity();
        masterBlSeaSubMapper.applySeaDescFields(domainDesc, targetJpa, seaJpa);
        if (existingDescJpa == null) {
            masterBlSeaDescRepository.save(targetJpa);
        }
    }
}
