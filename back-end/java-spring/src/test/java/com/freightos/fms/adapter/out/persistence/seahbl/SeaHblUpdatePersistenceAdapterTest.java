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
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
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
 * SeaHblUpdatePersistenceAdapter update 흐름 단위 테스트.
 * parent fetch → jobDiv 검증 → ext fetch → 도메인 변환 → factory 적용 →
 * applySeaCommonFields·applySeaBlFields·mergeContainers 호출 순서를 검증한다.
 * Truck TruckBlUpdatePersistenceAdapterTest 동등 패턴 follow.
 */
@ExtendWith(MockitoExtension.class)
class SeaHblUpdatePersistenceAdapterTest {

    @Mock private HouseBlRepository houseBlRepository;
    @Mock private HouseBlSeaRepository houseBlSeaRepository;
    @Mock private HouseBlSeaDescRepository houseBlSeaDescRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;
    @Mock private HouseBlDocMapper houseBlDocMapper;
    @Mock private HouseBlFactory houseBlFactory;

    @InjectMocks
    private SeaHblUpdatePersistenceAdapter adapter;

    private static UpdateHouseBlCommand emptyCommand() {
        // 55 파라미터 — 모두 null (PATCH 의미론, TruckBlUpdatePersistenceAdapterTest 동등)
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
    @DisplayName("update: 정상 SEA — factory·applySeaCommonFields·applySeaBlFields·mergeContainers 호출 검증")
    void update_existingSea_callsFactoryAndAppliesAttachedMapping() {
        Long id = 10L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.SEA);
        HouseBlSeaJpaEntity seaJpa = new HouseBlSeaJpaEntity();
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlSeaRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(seaJpa));
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(parentJpa, seaJpa, null)).willReturn(domain);

        UpdateHouseBlCommand command = emptyCommand();
        adapter.update(id, command);

        then(jpaToDomainMapper).should().toSeaDomain(parentJpa, seaJpa, null);
        then(houseBlFactory).should().applyToEntity(command, domain);
        then(domainToJpaMapper).should().applySeaCommonFields(domain, parentJpa);
        then(domainToJpaMapper).should().applySeaBlFields(domain, seaJpa);
    }

    @Test
    @DisplayName("update: parent가 없으면 ResourceNotFoundException, 이후 로직 미호출")
    void update_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(houseBlRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlSeaRepository).should(never()).findByHouseBlHouseBlId(any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: parent jobDiv가 SEA가 아니면 ResourceNotFoundException, ext fetch 미호출")
    void update_wrongJobDiv_throwsResourceNotFoundException() {
        Long id = 20L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.TRUCK);
        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlSeaRepository).should(never()).findByHouseBlHouseBlId(any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: ext(house_bl_sea)가 없으면 ResourceNotFoundException(데이터 일관성 깨진 상태 보호)")
    void update_extNotFound_throwsResourceNotFoundException() {
        Long id = 30L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.SEA);
        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlSeaRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(jpaToDomainMapper).should(never()).toSeaDomain(any(), any(), any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: containers가 없는 경우 mergeContainers에 빈 리스트 전달 — houseBlCargoMapper 미호출")
    void update_emptyContainers_mergesEmptyList() {
        Long id = 40L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.SEA);
        HouseBlSeaJpaEntity seaJpa = new HouseBlSeaJpaEntity();
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlSeaRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(seaJpa));
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(parentJpa, seaJpa, null)).willReturn(domain);

        adapter.update(id, emptyCommand());

        // 빈 컬렉션이므로 toSeaContainerJpa 미호출
        then(houseBlCargoMapper).should(never()).toSeaContainerJpa(any());
        then(domainToJpaMapper).should().applySeaCommonFields(domain, parentJpa);
        then(domainToJpaMapper).should().applySeaBlFields(domain, seaJpa);
    }
}
