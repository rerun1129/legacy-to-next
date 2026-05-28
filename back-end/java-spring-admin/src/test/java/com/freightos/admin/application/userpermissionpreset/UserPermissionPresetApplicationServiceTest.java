package com.freightos.admin.application.userpermissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;
import com.freightos.admin.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class UserPermissionPresetApplicationServiceTest {

    @Mock
    private UserPermissionPresetRepository userPresetRepository;

    @Mock
    private PermissionPresetRepository presetRepository;

    @InjectMocks
    private UserPermissionPresetApplicationService service;

    // ── assign: 정상 → id 반환, save 호출 ────────────────────────────────────

    @Test
    void assignUserPermissionPreset_validInput_returnsId() {
        given(presetRepository.existsPermissionPresetById(2L)).willReturn(true);
        given(userPresetRepository.existsByUserIdAndPresetId(1L, 2L)).willReturn(false);
        given(userPresetRepository.saveUserPermissionPreset(1L, 2L)).willReturn(10L);

        Long id = service.assignUserPermissionPreset(1L, 2L);

        assertThat(id).isEqualTo(10L);
        then(userPresetRepository).should().saveUserPermissionPreset(1L, 2L);
    }

    // ── assign: preset 미존재 → NOT_FOUND ────────────────────────────────────

    @Test
    void assignUserPermissionPreset_presetNotFound_throwsNotFound() {
        given(presetRepository.existsPermissionPresetById(99L)).willReturn(false);

        assertThatThrownBy(() -> service.assignUserPermissionPreset(1L, 99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getStatus()).isEqualTo(NOT_FOUND));
    }

    // ── assign: 이미 부여된 조합 → CONFLICT ──────────────────────────────────

    @Test
    void assignUserPermissionPreset_alreadyAssigned_throwsConflict() {
        given(presetRepository.existsPermissionPresetById(2L)).willReturn(true);
        given(userPresetRepository.existsByUserIdAndPresetId(1L, 2L)).willReturn(true);

        assertThatThrownBy(() -> service.assignUserPermissionPreset(1L, 2L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getStatus()).isEqualTo(CONFLICT));
    }

    // ── revoke: 정상 → deleteById 호출 ───────────────────────────────────────

    @Test
    void revokeUserPermissionPreset_existingId_callsDelete() {
        given(userPresetRepository.existsUserPermissionPresetById(10L)).willReturn(true);

        service.revokeUserPermissionPreset(10L);

        then(userPresetRepository).should().deleteUserPermissionPresetById(10L);
    }

    // ── revoke: 미존재 id → NOT_FOUND ────────────────────────────────────────

    @Test
    void revokeUserPermissionPreset_notFound_throwsNotFound() {
        given(userPresetRepository.existsUserPermissionPresetById(99L)).willReturn(false);

        assertThatThrownBy(() -> service.revokeUserPermissionPreset(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(e -> assertThat(((ApplicationException) e).getStatus()).isEqualTo(NOT_FOUND));
    }

    // ── listUserPermissionPresets: userId 기준 row 목록 반환 ──────────────────

    @Test
    void listUserPermissionPresets_returnsRowsFromRepository() {
        UserPermissionPresetRow row = new UserPermissionPresetRow(10L, 1L, 2L, "PRESET_A", "Preset A", true);
        given(userPresetRepository.findRowsByUserId(1L)).willReturn(List.of(row));

        List<UserPermissionPresetRow> result = service.listUserPermissionPresets(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).presetCode()).isEqualTo("PRESET_A");
    }

    // ── listUserPermissionPresets: 부여 없음 → 빈 목록 ───────────────────────

    @Test
    void listUserPermissionPresets_noAssignments_returnsEmptyList() {
        given(userPresetRepository.findRowsByUserId(1L)).willReturn(List.of());

        List<UserPermissionPresetRow> result = service.listUserPermissionPresets(1L);

        assertThat(result).isEmpty();
    }
}
