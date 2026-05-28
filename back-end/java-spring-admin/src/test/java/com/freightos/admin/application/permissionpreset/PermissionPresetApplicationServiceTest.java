package com.freightos.admin.application.permissionpreset;

import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.permissionpreset.command.AssignAttributeValuesCommand;
import com.freightos.admin.application.permissionpreset.command.CreatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.ListPermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.UpdatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PermissionPresetApplicationServiceTest {

    @Mock
    private PermissionPresetRepository presetRepository;
    @Mock
    private PermissionPresetAttributeValueRepository presetAttributeValueRepository;
    @Mock
    private UserPermissionPresetRepository userPresetRepository;
    @Mock
    private AttributeValuePort attributeValuePort;

    @InjectMocks
    private PermissionPresetApplicationService service;

    // --- fixture helpers ---

    private static AttributeValue moduleAv() {
        AttributeValue av = AttributeValue.create("module", "FMS", "FMS", 1, true);
        av.assignIdentity(100L, null, null, null, null);
        return av;
    }

    private static AttributeValue fmsScopeAv() {
        AttributeValue av = AttributeValue.create("fms_scope", "SEA", "해상", 1, true);
        av.assignIdentity(200L, null, null, null, null);
        return av;
    }

    private static AttributeValue adminScopeAv() {
        AttributeValue av = AttributeValue.create("admin_scope", "CODE", "Code Master", 1, true);
        av.assignIdentity(201L, null, null, null, null);
        return av;
    }

    // --- createPermissionPreset ---

    @Test
    void createPermissionPreset_validCode_savesAndReturnsId() {
        CreatePermissionPresetCommand command = new CreatePermissionPresetCommand("PRESET_ADMIN_ALL", "전체 관리자", null, true);
        given(presetRepository.existsPermissionPresetByCode("PRESET_ADMIN_ALL")).willReturn(false);
        given(presetRepository.savePermissionPreset(any())).willReturn(1L);

        Long id = service.createPermissionPreset(command);

        assertThat(id).isEqualTo(1L);
        then(presetRepository).should().savePermissionPreset(any());
    }

    @Test
    void createPermissionPreset_duplicateCode_throwsConflict() {
        CreatePermissionPresetCommand command = new CreatePermissionPresetCommand("PRESET_ADMIN_ALL", "전체 관리자", null, true);
        given(presetRepository.existsPermissionPresetByCode("PRESET_ADMIN_ALL")).willReturn(true);

        assertThatThrownBy(() -> service.createPermissionPreset(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        then(presetRepository).should(never()).savePermissionPreset(any());
    }

    @Test
    void createPermissionPreset_invalidCodePattern_throwsBadRequest() {
        // code 패턴 위반 — 도메인 검증기에서 BAD_REQUEST (중복 체크보다 도메인 검증이 나중 실행)
        CreatePermissionPresetCommand command = new CreatePermissionPresetCommand("INVALID_CODE", "잘못된 코드", null, true);
        given(presetRepository.existsPermissionPresetByCode("INVALID_CODE")).willReturn(false);

        assertThatThrownBy(() -> service.createPermissionPreset(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createPermissionPreset_missingPresetPrefix_throwsBadRequest() {
        CreatePermissionPresetCommand command = new CreatePermissionPresetCommand("ADMIN_ALL", "잘못된 코드", null, true);
        given(presetRepository.existsPermissionPresetByCode("ADMIN_ALL")).willReturn(false);

        assertThatThrownBy(() -> service.createPermissionPreset(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    // --- deletePermissionPreset ---

    @Test
    void deletePermissionPreset_notFound_throwsNotFound() {
        given(presetRepository.existsPermissionPresetById(99L)).willReturn(false);

        assertThatThrownBy(() -> service.deletePermissionPreset(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deletePermissionPreset_inUse_throwsConflict() {
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        given(userPresetRepository.countByPresetId(1L)).willReturn(2L);

        assertThatThrownBy(() -> service.deletePermissionPreset(1L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        then(presetRepository).should(never()).deletePermissionPresetById(any());
    }

    @Test
    void deletePermissionPreset_notInUse_deletesSuccessfully() {
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        given(userPresetRepository.countByPresetId(1L)).willReturn(0L);

        service.deletePermissionPreset(1L);

        then(presetRepository).should().deletePermissionPresetById(1L);
    }

    // --- updatePermissionPreset ---

    @Test
    void updatePermissionPreset_notFound_throwsNotFound() {
        given(presetRepository.findPermissionPresetById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePermissionPreset(99L, new UpdatePermissionPresetCommand("name", null, true)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updatePermissionPreset_valid_updatesFields() {
        PermissionPreset preset = PermissionPreset.create("PRESET_FMS_SEA", "원래 이름", null, true);
        given(presetRepository.findPermissionPresetById(1L)).willReturn(Optional.of(preset));

        service.updatePermissionPreset(1L, new UpdatePermissionPresetCommand("새 이름", "설명", false));

        then(presetRepository).should().updatePermissionPreset(eq(1L), any());
        assertThat(preset.getName()).isEqualTo("새 이름");
        assertThat(preset.isActive()).isFalse();
    }

    // --- listPermissionPresets ---

    @Test
    void listPermissionPresets_activeOnly_returnsOnlyActive() {
        PermissionPreset active = PermissionPreset.create("PRESET_A", "A", null, true);
        given(presetRepository.findAllPermissionPresets(true)).willReturn(List.of(active));

        List<PermissionPresetSummary> result = service.listPermissionPresets(new ListPermissionPresetCommand(true));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("PRESET_A");
    }

    @Test
    void listPermissionPresets_all_returnsAll() {
        PermissionPreset p1 = PermissionPreset.create("PRESET_A", "A", null, true);
        PermissionPreset p2 = PermissionPreset.create("PRESET_B", "B", null, false);
        given(presetRepository.findAllPermissionPresets(false)).willReturn(List.of(p1, p2));

        List<PermissionPresetSummary> result = service.listPermissionPresets(new ListPermissionPresetCommand(false));

        assertThat(result).hasSize(2);
    }

    // --- assignAttributeValuesToPreset ---

    @Test
    void assignAttributeValues_presetNotFound_throwsNotFound() {
        given(presetRepository.existsPermissionPresetById(99L)).willReturn(false);
        List<Long> addIds = List.of(1L);

        assertThatThrownBy(() -> service.assignAttributeValuesToPreset(99L, new AssignAttributeValuesCommand(addIds, List.of())))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void assignAttributeValues_addAndRemove_appliesBothInOrder() {
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        List<Long> addIds    = List.of(100L, 200L);
        List<Long> removeIds = List.of(30L);
        // DB 현재 상태: removeIds 만 존재 → remove 후 addIds 가 최종 set
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(1L)).willReturn(List.of(30L));
        given(attributeValuePort.findAttributeValuesByIds(any())).willReturn(List.of(moduleAv(), fmsScopeAv()));

        service.assignAttributeValuesToPreset(1L, new AssignAttributeValuesCommand(addIds, removeIds));

        // remove 먼저
        then(presetAttributeValueRepository).should().deleteByPresetIdAndAttributeValueIdsIn(1L, removeIds);
        then(presetAttributeValueRepository).should().saveAllByPresetId(1L, addIds);
    }

    @Test
    void assignAttributeValues_emptyAdd_skipsAdd() {
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        List<Long> removeIds = List.of(30L);
        // DB 현재 상태: removeIds 만 존재 → remove 후 최종 set 비어있음 → 검증 skip
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(1L)).willReturn(List.of(30L));

        service.assignAttributeValuesToPreset(1L, new AssignAttributeValuesCommand(List.of(), removeIds));

        then(presetAttributeValueRepository).should().deleteByPresetIdAndAttributeValueIdsIn(1L, removeIds);
        then(presetAttributeValueRepository).should(never()).saveAllByPresetId(any(), any());
    }

    // --- assignAttributeValuesToPreset 검증: module + scope 규칙 ---

    @Test
    void assignAttributeValues_resultSetHasModuleAndScope_passes() {
        // module + fms_scope 조합 → 검증 통과, 예외 없음
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        List<Long> addIds = List.of(100L, 200L);
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(1L)).willReturn(List.of());
        given(attributeValuePort.findAttributeValuesByIds(any())).willReturn(List.of(moduleAv(), fmsScopeAv()));

        service.assignAttributeValuesToPreset(1L, new AssignAttributeValuesCommand(addIds, List.of()));
        // 예외 없으면 통과
    }

    @Test
    void assignAttributeValues_resultSetEmpty_skipsValidation() {
        // 결과 set 이 비어있으면 검증 자체를 수행하지 않는다
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(1L)).willReturn(List.of());

        service.assignAttributeValuesToPreset(1L, new AssignAttributeValuesCommand(List.of(), List.of()));

        then(attributeValuePort).should(never()).findAttributeValuesByIds(any());
    }

    @Test
    void assignAttributeValues_missingScope_throwsBadRequest() {
        // module 만 있고 scope 없음 → 400
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        List<Long> addIds = List.of(100L);
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(1L)).willReturn(List.of());
        given(attributeValuePort.findAttributeValuesByIds(any())).willReturn(List.of(moduleAv()));

        assertThatThrownBy(() -> service.assignAttributeValuesToPreset(1L, new AssignAttributeValuesCommand(addIds, List.of())))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void assignAttributeValues_missingModule_throwsBadRequest() {
        // scope 만 있고 module 없음 → 400
        given(presetRepository.existsPermissionPresetById(1L)).willReturn(true);
        List<Long> addIds = List.of(200L);
        given(presetAttributeValueRepository.findAttributeValueIdsByPresetId(1L)).willReturn(List.of());
        given(attributeValuePort.findAttributeValuesByIds(any())).willReturn(List.of(fmsScopeAv()));

        assertThatThrownBy(() -> service.assignAttributeValuesToPreset(1L, new AssignAttributeValuesCommand(addIds, List.of())))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
