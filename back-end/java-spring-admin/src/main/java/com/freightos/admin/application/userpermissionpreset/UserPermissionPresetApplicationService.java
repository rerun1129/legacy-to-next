package com.freightos.admin.application.userpermissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;
import com.freightos.admin.application.userpermissionpreset.port.in.AssignUserPermissionPresetUseCase;
import com.freightos.admin.application.userpermissionpreset.port.in.ListUserPermissionPresetUseCase;
import com.freightos.admin.application.userpermissionpreset.port.in.RevokeUserPermissionPresetUseCase;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPermissionPresetApplicationService
        implements AssignUserPermissionPresetUseCase,
                   RevokeUserPermissionPresetUseCase,
                   ListUserPermissionPresetUseCase {

    private final UserPermissionPresetRepository userPresetRepository;
    private final PermissionPresetRepository presetRepository;

    @Override
    @Transactional
    public Long assignUserPermissionPreset(Long userId, Long presetId) {
        if (!presetRepository.existsPermissionPresetById(presetId)) {
            throw ApplicationException.notFound("PERMISSION_PRESET_NOT_FOUND", MessageCode.PERMISSION_PRESET_NOT_FOUND.getMessage());
        }
        if (userPresetRepository.existsByUserIdAndPresetId(userId, presetId)) {
            throw ApplicationException.conflict("USER_PERMISSION_PRESET_ALREADY_ASSIGNED", MessageCode.USER_PERMISSION_PRESET_ALREADY_ASSIGNED.getMessage());
        }
        return userPresetRepository.saveUserPermissionPreset(userId, presetId);
    }

    @Override
    @Transactional
    public void revokeUserPermissionPreset(Long id) {
        if (!userPresetRepository.existsUserPermissionPresetById(id)) {
            throw ApplicationException.notFound("USER_PERMISSION_PRESET_NOT_FOUND", MessageCode.USER_PERMISSION_PRESET_NOT_FOUND.getMessage());
        }
        userPresetRepository.deleteUserPermissionPresetById(id);
    }

    @Override
    public List<UserPermissionPresetRow> listUserPermissionPresets(Long userId) {
        return userPresetRepository.findRowsByUserId(userId);
    }
}
