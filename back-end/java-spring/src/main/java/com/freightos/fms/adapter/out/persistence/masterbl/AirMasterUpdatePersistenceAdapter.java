package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.application.masterbl.MasterBlFactory;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.port.out.AirMasterPersistencePort;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * AIR Master B/L update 전용 어댑터.
 * parent fetch → jobDiv 검증 → air ext fetch → 도메인 변환 → factory 적용 →
 * attached JPA 직접 매핑 → applyDescSync + 1:N 자식 syncXxx → dirty-checking으로 UPDATE 자동 발사.
 *
 * saveMasterBl 미호출 — dirty-checking 위임.
 * House {@code SeaMasterUpdatePersistenceAdapter} 패턴 정합.
 */
@Component
@RequiredArgsConstructor
public class AirMasterUpdatePersistenceAdapter implements AirMasterPersistencePort {

    private final MasterBlRepository masterBlRepository;
    private final MasterBlAirRepository masterBlAirRepository;
    private final MasterBlAirDescRepository masterBlAirDescRepository;
    private final MasterBlMapper masterBlMapper;
    private final MasterBlAirSubMapper masterBlAirSubMapper;
    private final MasterBlFactory masterBlFactory;

    @Override
    @Transactional
    public void update(Long id, UpdateMasterBlCommand command) {
        MasterBlJpaEntity parentJpa = masterBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));
        if (parentJpa.getJobDiv() != MasterBlJobDiv.AIR) {
            throw new IllegalStateException(
                    "Expected jobDiv=AIR but was: " + parentJpa.getJobDiv() + " for masterBlId=" + id);
        }
        MasterBlAirJpaEntity airJpa = masterBlAirRepository.findByMasterBlMasterBlId(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));

        MasterBlAirDescJpaEntity descJpa = masterBlAirDescRepository
                .findByAir_MasterBlAirId(airJpa.getMasterBlAirId()).orElse(null);

        // 도메인 변환 — factory가 도메인 검증 포함 모든 필드를 도메인에 적용
        MasterBlAir domain = (MasterBlAir) masterBlMapper.toAirDomain(parentJpa, airJpa, airJpa.getAirCharges(), descJpa);
        masterBlFactory.applyToEntity(command, domain);

        // 도메인 → JPA dirty-check 기반 필드 반영 (§6.37 sub-set 매퍼)
        masterBlAirSubMapper.applyMasterAirCommonFields(domain, parentJpa);
        masterBlAirSubMapper.applyMasterAirFields(domain, airJpa);

        // 1:N 자식 merge-by-id — orphanRemoval 활용, 기존 row id 매칭으로 무수정 저장 시 INSERT-DELETE 회피
        airJpa.mergeDims(domain.getDims().stream().map(masterBlMapper::toDimJpa).toList());
        airJpa.mergeScheduleLegs(domain.getScheduleLegs().stream().map(masterBlMapper::toScheduleLegJpa).toList());
        airJpa.mergeAirCharges(domain.getAirCharges().stream().map(masterBlMapper::toAirChargeJpa).toList());

        // Desc 동기화 — 1:1 관계
        applyDescSync(domain, airJpa, descJpa);
        // 트랜잭션 커밋 시 dirty-checking으로 parentJpa·airJpa UPDATE 자동 발생
    }

    /**
     * Desc 1:1 동기화 (§6.62 SRP 분리).
     *
     * <ul>
     *   <li>domain desc null + repo desc null → no-op
     *   <li>domain desc null + repo desc 존재 → 현행 유지 (§6.37 PATCH: null은 기존 값 보호)
     *   <li>domain desc 존재 + repo desc null → 신규 insert ({@code createAirDescIfPresent} 의미)
     *   <li>domain desc 존재 + repo desc 존재 → 필드 덮어쓰기 ({@code updateOrDeleteAirDesc} 의미, dirty-checking UPDATE)
     * </ul>
     *
     * House {@code SeaMasterUpdatePersistenceAdapter.applyDescSync} 시그니처 정합.
     */
    void applyDescSync(MasterBlAir domain, MasterBlAirJpaEntity airJpa,
                       MasterBlAirDescJpaEntity existingDescJpa) {
        MasterBlDesc domainDesc = domain.getDesc();
        if (domainDesc == null) return;

        MasterBlAirDescJpaEntity targetJpa = existingDescJpa != null
                ? existingDescJpa
                : new MasterBlAirDescJpaEntity();
        masterBlAirSubMapper.applyAirDescFields(domainDesc, targetJpa, airJpa);
        if (existingDescJpa == null) {
            masterBlAirDescRepository.save(targetJpa);
        }
    }
}
