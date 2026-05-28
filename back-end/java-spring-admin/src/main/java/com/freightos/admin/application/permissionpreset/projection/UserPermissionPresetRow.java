package com.freightos.admin.application.permissionpreset.projection;

/**
 * user_permission_preset 한 행의 읽기 전용 프로젝션.
 * 컨트롤러 → assembler 가 응답 DTO 로 변환한다.
 */
public record UserPermissionPresetRow(
        Long id,
        Long userId,
        Long presetId,
        String presetCode,
        String presetName,
        boolean presetActive
) {}
