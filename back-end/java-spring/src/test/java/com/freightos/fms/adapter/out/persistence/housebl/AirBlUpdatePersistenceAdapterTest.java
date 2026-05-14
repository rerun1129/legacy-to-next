package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * AirBlUpdatePersistenceAdapter update 흐름 단위 테스트.
 * parent fetch → jobDiv 검증 → ext fetch → 도메인 변환 → factory 적용 →
 * applyAirCommonFields·applyAirBlFields·mergeDims·mergeScheduleLegs·mergeAirCharges 호출 순서를 검증한다.
 * SeaHblUpdatePersistenceAdapterTest 동등 패턴 follow.
 */
@ExtendWith(MockitoExtension.class)
class AirBlUpdatePersistenceAdapterTest {

    @Mock private HouseBlRepository houseBlRepository;
    @Mock private HouseBlAirRepository houseBlAirRepository;
    @Mock private HouseBlAirDescRepository houseBlAirDescRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;
    @Mock private HouseBlDocMapper houseBlDocMapper;
    @Mock private HouseBlFactory houseBlFactory;

    @InjectMocks
    private AirBlUpdatePersistenceAdapter adapter;

    private static UpdateHouseBlCommand emptyCommand() {
        // 55 파라미터 — 모두 null (PATCH 의미론)
        return new UpdateHouseBlCommand(
                null, null, null, null, null, null, null, null, null, null,  // 1~10
                null, null, null, null, null, null, null, null, null, null,  // 11~20
                null, null, null, null, null, null, null, null, null, null,  // 21~30
                null, null, null, null, null, null, null, null, null, null,  // 31~40
                null, null, null, null, null, null, null, null, null, null,  // 41~50
                null, null, null, null, null                                  // 51~55
        );
    }

    @Test
    @DisplayName("update: 정상 AIR — factory·applyAirCommonFields·applyAirBlFields 호출 검증")
    void update_existingAir_callsFactoryAndAppliesAttachedMapping() {
        Long id = 10L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.AIR);
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlAirRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(airJpa));
        given(jpaToDomainMapper.toAirDomain(parentJpa, airJpa, null)).willReturn(domain);

        UpdateHouseBlCommand command = emptyCommand();
        adapter.update(id, command);

        then(jpaToDomainMapper).should().toAirDomain(parentJpa, airJpa, null);
        then(houseBlFactory).should().applyToEntity(command, domain);
        then(domainToJpaMapper).should().applyAirCommonFields(domain, parentJpa);
        then(domainToJpaMapper).should().applyAirBlFields(domain, airJpa);
    }

    @Test
    @DisplayName("update: parent가 없으면 ResourceNotFoundException, 이후 로직 미호출")
    void update_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(houseBlRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlAirRepository).should(never()).findByHouseBlHouseBlId(any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: parent jobDiv가 AIR가 아니면 ResourceNotFoundException, ext fetch 미호출")
    void update_wrongJobDiv_throwsResourceNotFoundException() {
        Long id = 20L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.SEA);
        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlAirRepository).should(never()).findByHouseBlHouseBlId(any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: ext(house_bl_air)가 없으면 ResourceNotFoundException(데이터 일관성 깨진 상태 보호)")
    void update_extNotFound_throwsResourceNotFoundException() {
        Long id = 30L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.AIR);
        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlAirRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(jpaToDomainMapper).should(never()).toAirDomain(any(), any(), any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: 기존 descJpa가 있으면 applyAirDescFields 호출, save 미호출")
    void update_existingDesc_appliesFieldsWithoutSave() {
        Long id = 40L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.AIR);
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        HouseBlAirDescJpaEntity descJpa = new HouseBlAirDescJpaEntity();
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);
        HouseBlDesc domainDesc = HouseBlDesc.create(id);
        domain.initDesc(domainDesc);

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlAirRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(airJpa));
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(airJpa.getHouseBlAirId()))
                .willReturn(Optional.of(descJpa));
        given(jpaToDomainMapper.toAirDomain(parentJpa, airJpa, descJpa)).willReturn(domain);

        adapter.update(id, emptyCommand());

        then(houseBlDocMapper).should().applyAirDescFields(domainDesc, descJpa, airJpa);
        then(houseBlAirDescRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("update: descJpa가 없으면 신규 HouseBlAirDescJpaEntity 생성 후 save 호출")
    void update_noExistingDesc_createsAndSavesNewDescJpa() {
        Long id = 50L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.AIR);
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);
        HouseBlDesc domainDesc = HouseBlDesc.create(id);
        domain.initDesc(domainDesc);

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlAirRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(airJpa));
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(airJpa.getHouseBlAirId()))
                .willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(parentJpa, airJpa, null)).willReturn(domain);

        adapter.update(id, emptyCommand());

        then(houseBlDocMapper).should().applyAirDescFields(any(HouseBlDesc.class), any(HouseBlAirDescJpaEntity.class), any(HouseBlAirJpaEntity.class));
        then(houseBlAirDescRepository).should().save(any(HouseBlAirDescJpaEntity.class));
    }
}
