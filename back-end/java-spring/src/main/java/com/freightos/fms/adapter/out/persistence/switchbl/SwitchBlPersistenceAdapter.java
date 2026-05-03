package com.freightos.fms.adapter.out.persistence.switchbl;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlRepository;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlDescriptionJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlJpaEntity;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.entity.SwitchBlDescription;
import com.freightos.fms.domain.switchbl.port.out.SwitchBlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SwitchBlPersistenceAdapter implements SwitchBlPort {

    private final SwitchBlJpaRepository switchBlJpaRepository;
    private final SwitchBlDescriptionJpaRepository switchBlDescriptionJpaRepository;
    private final HouseBlRepository houseBlRepository;
    private final SwitchBlMapper switchBlMapper;

    @Override
    public Optional<SwitchBl> findSwitchBlByHouseBlId(Long houseBlId) {
        return switchBlJpaRepository.findByHouseBlHouseBlId(houseBlId)
                .map(switchBlMapper::toDomain);
    }

    @Override
    public Optional<SwitchBl> findSwitchBlById(Long switchBlId) {
        return switchBlJpaRepository.findById(switchBlId)
                .map(switchBlMapper::toDomain);
    }

    @Override
    @Transactional
    public SwitchBl saveSwitchBl(SwitchBl domain) {
        SwitchBlJpaEntity jpa = switchBlJpaRepository
                .findByHouseBlHouseBlId(domain.getHouseBlId())
                .orElseGet(SwitchBlJpaEntity::new);

        // 신규 엔티티라면 HouseBl JPA 프록시를 FK 참조로 설정
        if (jpa.getHouseBl() == null) {
            jpa.setHouseBl(houseBlRepository.getReferenceById(domain.getHouseBlId()));
        }

        switchBlMapper.applyFields(domain, jpa);
        SwitchBlJpaEntity savedJpa = switchBlJpaRepository.save(jpa);

        // description upsert
        SwitchBlDescription descDomain = domain.getDescription();
        if (descDomain != null) {
            SwitchBlDescriptionJpaEntity descJpa = switchBlDescriptionJpaRepository
                    .findBySwitchBlSwitchBlId(savedJpa.getSwitchBlId())
                    .orElseGet(SwitchBlDescriptionJpaEntity::new);
            if (descJpa.getSwitchBl() == null) {
                descJpa.setSwitchBl(savedJpa);
            }
            descJpa.setMarks(descDomain.getMarks());
            descJpa.setNatureQuantity(descDomain.getNatureQuantity());
            switchBlDescriptionJpaRepository.save(descJpa);
        }

        SwitchBlJpaEntity reloaded = switchBlJpaRepository.findById(savedJpa.getSwitchBlId())
                .orElseThrow(() -> new ResourceNotFoundException("SwitchBl", savedJpa.getSwitchBlId()));
        return switchBlMapper.toDomain(reloaded);
    }

    @Override
    @Transactional
    public void deleteSwitchBl(SwitchBl switchBl) {
        Long switchBlId = switchBl.getSwitchBlId();
        // description 먼저 삭제 후 본체 삭제 (FK 제약 준수)
        switchBlDescriptionJpaRepository.findBySwitchBlSwitchBlId(switchBlId)
                .ifPresent(switchBlDescriptionJpaRepository::delete);
        switchBlJpaRepository.deleteById(switchBlId);
    }
}
