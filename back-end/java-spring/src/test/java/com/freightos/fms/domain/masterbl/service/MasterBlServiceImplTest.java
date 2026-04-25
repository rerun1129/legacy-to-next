package com.freightos.fms.domain.masterbl.service;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.repository.MasterBlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MasterBlServiceImplTest {

    @Mock
    private MasterBlRepository masterBlRepository;

    @InjectMocks
    private MasterBlServiceImpl masterBlService;

    @Test
    @DisplayName("list - Bound.EXP 조회 시 repository.findAllByBound(EXP, pageable) 위임")
    void list_exp_delegatesToFindAllByBound() {
        // given
        Pageable pageable = PageRequest.of(0, 50);
        MasterBl mockEntity = mock(MasterBl.class);
        Page<MasterBl> expected = new PageImpl<>(List.of(mockEntity));
        given(masterBlRepository.findAllByBound(Bound.EXP, pageable)).willReturn(expected);

        // when
        Page<MasterBl> result = masterBlService.list(Bound.EXP, pageable);

        // then
        assertThat(result).isEqualTo(expected);
        then(masterBlRepository).should().findAllByBound(Bound.EXP, pageable);
    }

    @Test
    @DisplayName("list - Bound.IMP 조회 시 repository.findAllByBound(IMP, pageable) 위임")
    void list_imp_delegatesToFindAllByBound() {
        // given
        Pageable pageable = PageRequest.of(0, 50);
        MasterBl mockEntity = mock(MasterBl.class);
        Page<MasterBl> expected = new PageImpl<>(List.of(mockEntity));
        given(masterBlRepository.findAllByBound(Bound.IMP, pageable)).willReturn(expected);

        // when
        Page<MasterBl> result = masterBlService.list(Bound.IMP, pageable);

        // then
        assertThat(result).isEqualTo(expected);
        then(masterBlRepository).should().findAllByBound(Bound.IMP, pageable);
    }

    @Test
    @DisplayName("getById - 존재하는 ID로 조회 시 엔티티 반환")
    void getById_existingId_returnsEntity() {
        // given
        UUID id = UUID.randomUUID();
        MasterBl mockEntity = mock(MasterBl.class);
        given(masterBlRepository.findById(id)).willReturn(Optional.of(mockEntity));

        // when
        MasterBl result = masterBlService.getById(id);

        // then
        assertThat(result).isEqualTo(mockEntity);
        then(masterBlRepository).should().findById(id);
    }

    @Test
    @DisplayName("getById - 존재하지 않는 ID 조회 시 ResourceNotFoundException 발생")
    void getById_notFound_throwsResourceNotFoundException() {
        // given
        UUID id = UUID.randomUUID();
        given(masterBlRepository.findById(id)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> masterBlService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
        then(masterBlRepository).should().findById(id);
    }

    @Test
    @DisplayName("delete - 존재하는 ID 삭제 시 repository.delete 호출")
    void delete_existingId_callsRepositoryDelete() {
        // given
        UUID id = UUID.randomUUID();
        MasterBl mockEntity = mock(MasterBl.class);
        given(masterBlRepository.findById(id)).willReturn(Optional.of(mockEntity));

        // when
        masterBlService.delete(id);

        // then
        then(masterBlRepository).should().findById(id);
        then(masterBlRepository).should().delete(mockEntity);
    }
}
