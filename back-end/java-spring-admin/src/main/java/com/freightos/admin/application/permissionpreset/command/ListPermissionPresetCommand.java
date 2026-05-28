package com.freightos.admin.application.permissionpreset.command;

/**
 * 프리셋 목록 조회 시 active 필터를 전달한다.
 * activeOnly=true 이면 active=true 인 프리셋만 반환한다.
 */
public record ListPermissionPresetCommand(boolean activeOnly) {}
