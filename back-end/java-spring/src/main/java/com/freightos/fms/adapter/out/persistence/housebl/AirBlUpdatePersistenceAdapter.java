package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.out.AirBlPersistencePort;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Air House B/L update 전용 어댑터 (§6.35).
 * parent fetch → jobDiv 검증 → ext fetch → 도메인 변환 → factory 적용 →
 * attached JPA 직접 매핑 → mergeDims/mergeScheduleLegs/mergeAirCharges →
 * dirty-checking으로 UPDATE 자동 발사. saveHouseBl 미호출.
 */
@Component
@RequiredArgsConstructor
public class AirBlUpdatePersistenceAdapter implements AirBlPersistencePort {

    private final HouseBlRepository houseBlRepository;
    private final HouseBlAirRepository houseBlAirRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;
    private final HouseBlFactory houseBlFactory;

    @Override
    @Transactional
    public void update(Long id, UpdateHouseBlCommand command) {
        HouseBlJpaEntity parentJpa = houseBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.AIR_HBL_NOT_FOUND));
        if (parentJpa.getJobDiv() != JobDiv.AIR) {
            throw new ResourceNotFoundException(MessageCode.AIR_HBL_NOT_FOUND);
        }
        HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.AIR_HBL_NOT_FOUND));

        // 도메인 변환 — dims/scheduleLegs/airCharges 컬렉션까지 LAZY 트리거
        HouseBlAir domain = jpaToDomainMapper.toAirDomain(parentJpa, airJpa, null);

        // 팩토리가 도메인 검증(etd≤eta 등) 포함 모든 필드를 도메인에 적용
        houseBlFactory.applyToEntity(command, domain);

        // 도메인 → JPA dirty-check 기반 필드 반영 (§6.37 sub-set 매퍼)
        domainToJpaMapper.applyAirCommonFields(domain, parentJpa);
        domainToJpaMapper.applyAirBlFields(domain, airJpa);

        // 자식 컬렉션 merge — orphanRemoval이 DELETE, 신규 진입이 INSERT를 처리 (§6.28)
        List<HouseBlAirDimJpaEntity> incomingDims = domain.getDims().stream()
                .map(houseBlCargoMapper::toAirDimJpa)
                .toList();
        airJpa.mergeDims(incomingDims);

        List<HouseBlScheduleLegJpaEntity> incomingLegs = domain.getScheduleLegs().stream()
                .map(houseBlDocMapper::toScheduleLegJpa)
                .toList();
        airJpa.mergeScheduleLegs(incomingLegs);

        List<HouseBlAirChargeJpaEntity> incomingCharges = domain.getAirCharges().stream()
                .map(houseBlDocMapper::toAirChargeJpa)
                .toList();
        airJpa.mergeAirCharges(incomingCharges);
        // 트랜잭션 커밋 시 dirty-checking으로 parentJpa·airJpa UPDATE 자동 발생
    }
}
