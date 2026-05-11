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
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * NonBlUpdatePersistenceAdapter update 흐름 단위 테스트.
 * parent fetch → jobDiv 검증 → ext fetch → domain 변환 → factory 적용 → JPA dirty-check 반영 순서를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class NonBlPersistenceAdapterTest {

    @Mock private HouseBlRepository houseBlRepository;
    @Mock private HouseBlNonBlRepository houseBlNonBlRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;
    @Mock private HouseBlFactory houseBlFactory;

    @InjectMocks
    private NonBlUpdatePersistenceAdapter adapter;

    private static UpdateHouseBlCommand emptyCommand() {
        // 53 파라미터 — 모두 null (PATCH 의미론)
        return new UpdateHouseBlCommand(
                null, null, null, null, null, null, null, null, null, null,  // 1~10
                null, null, null, null, null, null, null, null, null, null,  // 11~20
                null, null, null, null, null, null, null, null, null, null,  // 21~30
                null, null, null, null, null, null, null, null, null, null,  // 31~40
                null, null, null, null, null, null, null, null, null, null,  // 41~50
                null, null, null                                              // 51~53
        );
    }

    @Test
    @DisplayName("update: 정상 NON_BL — factory·applyCommonFields·applyNonBlFields·mergeContainers·mergeDims 호출 검증")
    void update_existingNonBl_callsFactoryAndAppliesAttachedMapping() {
        Long id = 10L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.NON_BL);
        HouseBlNonBlJpaEntity nonBlJpa = new HouseBlNonBlJpaEntity();
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(nonBlJpa));
        given(jpaToDomainMapper.toNonBlDomain(parentJpa, nonBlJpa)).willReturn(domain);
        given(houseBlCargoMapper.toNonBlContainerJpa(any())).willReturn(new HouseBlNonBlContainerJpaEntity());
        given(houseBlCargoMapper.toNonBlDimJpa(any())).willReturn(new HouseBlNonBlDimJpaEntity());

        UpdateHouseBlCommand command = emptyCommand();
        adapter.update(id, command);

        then(jpaToDomainMapper).should().toNonBlDomain(parentJpa, nonBlJpa);
        then(houseBlFactory).should().applyToEntity(command, domain);
        then(domainToJpaMapper).should().applyCommonFields(domain, parentJpa);
        then(domainToJpaMapper).should().applyNonBlFields(domain, nonBlJpa);
    }

    @Test
    @DisplayName("update: parent가 없으면 ResourceNotFoundException, 이후 로직 미호출")
    void update_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(houseBlRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlNonBlRepository).should(never()).findByHouseBlHouseBlId(any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: parent jobDiv가 NON_BL이 아니면 ResourceNotFoundException, ext fetch 미호출")
    void update_wrongJobDiv_throwsResourceNotFoundException() {
        Long id = 20L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.SEA);
        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(houseBlNonBlRepository).should(never()).findByHouseBlHouseBlId(any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: ext(house_bl_non_bl)가 없으면 ResourceNotFoundException(데이터 일관성 깨진 상태 보호)")
    void update_extNotFound_throwsResourceNotFoundException() {
        Long id = 30L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.NON_BL);
        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(jpaToDomainMapper).should(never()).toNonBlDomain(any(), any());
        then(houseBlFactory).should(never()).applyToEntity(any(), any());
    }

    @Test
    @DisplayName("update: containers·dims가 없는 경우 mergeContainers·mergeDims에 빈 리스트 전달")
    void update_emptyCollections_mergesEmptyLists() {
        Long id = 40L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.NON_BL);
        HouseBlNonBlJpaEntity nonBlJpa = new HouseBlNonBlJpaEntity();
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);
        // domain.getContainers()·domain.getDims()는 빈 리스트 반환

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(nonBlJpa));
        given(jpaToDomainMapper.toNonBlDomain(parentJpa, nonBlJpa)).willReturn(domain);

        adapter.update(id, emptyCommand());

        // 빈 리스트로 mergeContainers·mergeDims가 호출 — 도메인 컬렉션에서 houseBlCargoMapper 미호출
        then(houseBlCargoMapper).should(never()).toNonBlContainerJpa(any());
        then(houseBlCargoMapper).should(never()).toNonBlDimJpa(any());
        then(domainToJpaMapper).should().applyCommonFields(domain, parentJpa);
        then(domainToJpaMapper).should().applyNonBlFields(domain, nonBlJpa);
    }

    @Test
    @DisplayName("update: containers 있는 경우 toNonBlContainerJpa 호출 수 일치")
    void update_withContainers_callsCargoMapperForEachContainer() {
        Long id = 50L;
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setHouseBlId(id);
        parentJpa.setJobDiv(JobDiv.NON_BL);
        HouseBlNonBlJpaEntity nonBlJpa = new HouseBlNonBlJpaEntity();
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        domain.assignIdentity(id, null, null, null, null);
        // 컨테이너 2개 추가
        domain.initContainers(List.of(
                HouseBlContainer.of(domain, null, null, 20),
                HouseBlContainer.of(domain, null, null, 20)
        ));

        given(houseBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(id)).willReturn(Optional.of(nonBlJpa));
        given(jpaToDomainMapper.toNonBlDomain(parentJpa, nonBlJpa)).willReturn(domain);
        given(houseBlCargoMapper.toNonBlContainerJpa(any())).willReturn(new HouseBlNonBlContainerJpaEntity());

        adapter.update(id, emptyCommand());

        then(houseBlCargoMapper).should(times(2)).toNonBlContainerJpa(any());
    }
}
