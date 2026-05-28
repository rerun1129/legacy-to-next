package com.freightos.admin.application.permissionpreset.port.out;

import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;

import java.util.List;
import java.util.Optional;

public interface PermissionPresetRepository {
    Optional<PermissionPreset> findPermissionPresetById(Long presetId);
    Optional<PermissionPreset> findPermissionPresetByCode(String code);
    boolean existsPermissionPresetByCode(String code);
    boolean existsPermissionPresetById(Long presetId);
    Long savePermissionPreset(PermissionPreset preset);
    void updatePermissionPreset(Long presetId, PermissionPreset patch);
    void deletePermissionPresetById(Long presetId);
    List<PermissionPreset> findAllPermissionPresets(boolean activeOnly);
}
