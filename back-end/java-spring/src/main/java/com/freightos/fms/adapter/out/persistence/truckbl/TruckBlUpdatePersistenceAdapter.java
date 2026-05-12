package com.freightos.fms.adapter.out.persistence.truckbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlTruckDescRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlTruckRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckOrderJpaEntity;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.truckbl.port.out.TruckBlPersistencePort;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.entity.HouseBlTruckOrder;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TruckBlUpdatePersistenceAdapter implements TruckBlPersistencePort {

    private final HouseBlRepository houseBlRepository;
    private final HouseBlTruckRepository houseBlTruckRepository;
    private final HouseBlTruckDescRepository houseBlTruckDescRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;
    private final HouseBlFactory houseBlFactory;

    @Override
    @Transactional
    public void update(Long id, UpdateHouseBlCommand command) {
        HouseBlJpaEntity parentJpa = houseBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND));
        if (parentJpa.getJobDiv() != JobDiv.TRUCK) {
            throw new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND);
        }
        HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.TRUCK_BL_NOT_FOUND));

        HouseBlTruckDescJpaEntity descJpa = houseBlTruckDescRepository.findByTruck_HouseBlTruckId(
                truckJpa.getHouseBlTruckId()).orElse(null);

        // 도메인 변환 — truckOrders/dims 컬렉션까지 로드
        HouseBlTruck domain = jpaToDomainMapper.toTruckDomain(parentJpa, truckJpa, descJpa);

        // 팩토리가 도메인 검증(etd≤eta 등) 포함 모든 필드를 도메인에 적용
        houseBlFactory.applyToEntity(command, domain);

        // 도메인 → JPA dirty-check 기반 필드 반영
        domainToJpaMapper.applyTruckCommonFields(domain, parentJpa);
        domainToJpaMapper.applyTruckBlFields(domain, truckJpa);

        // Desc 동기화 — 1:1 관계
        applyDescSync(domain, truckJpa, descJpa);

        // TruckOrder 그리드 merge-by-id — id 일치 시 UPDATE, 미일치 시 INSERT, orphanRemoval이 DELETE 처리
        List<HouseBlTruckOrderJpaEntity> incomingOrders = domain.getTruckOrders().stream()
                .map(this::toTruckOrderJpa)
                .toList();
        truckJpa.mergeTruckOrders(incomingOrders);

        // Dim merge-by-id
        List<com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckDimJpaEntity> incomingDims =
                domain.getDims().stream().map(houseBlCargoMapper::toTruckDimJpa).toList();
        truckJpa.mergeDims(incomingDims);
        // 트랜잭션 커밋 시 dirty-checking으로 parentJpa·truckJpa UPDATE 자동 발생
    }

    private void applyDescSync(HouseBlTruck domain, HouseBlTruckJpaEntity truckJpa,
                               HouseBlTruckDescJpaEntity existingDescJpa) {
        HouseBlDesc domainDesc = domain.getDesc();
        if (domainDesc == null) return;

        HouseBlTruckDescJpaEntity targetJpa = existingDescJpa != null
                ? existingDescJpa
                : new HouseBlTruckDescJpaEntity();
        houseBlDocMapper.applyTruckDescFields(domainDesc, targetJpa, truckJpa);
        if (existingDescJpa == null) {
            houseBlTruckDescRepository.save(targetJpa);
        }
    }

    private HouseBlTruckOrderJpaEntity toTruckOrderJpa(HouseBlTruckOrder order) {
        HouseBlTruckOrderJpaEntity jpa = new HouseBlTruckOrderJpaEntity();
        // mergeTruckOrders 매칭용 id set — 미매칭 시 IDENTITY 신규 INSERT
        if (order.getId() != null) jpa.setHouseBlTruckOrderId(order.getId());
        houseBlDocMapper.applyTruckOrderFields(order, jpa);
        return jpa;
    }
}
