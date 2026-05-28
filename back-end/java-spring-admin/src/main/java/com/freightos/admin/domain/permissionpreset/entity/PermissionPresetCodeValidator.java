package com.freightos.admin.domain.permissionpreset.entity;

import com.freightos.admin.common.exception.ApplicationException;

import java.util.regex.Pattern;

/**
 * Permission Preset code 명명 규칙 검증기.
 * 규칙: PRESET_ 접두사 + 대문자·숫자·언더스코어 조합 (예: PRESET_ADMIN_ALL)
 */
public final class PermissionPresetCodeValidator {

    private static final Pattern VALID_CODE = Pattern.compile("^PRESET_[A-Z0-9_]+$");

    private PermissionPresetCodeValidator() {}

    public static void validate(String code) {
        if (code == null || !VALID_CODE.matcher(code).matches()) {
            throw new ApplicationException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "PERMISSION_PRESET_CODE_INVALID",
                    "Permission Preset code 는 'PRESET_' 으로 시작하고 대문자·숫자·언더스코어만 허용됩니다: " + code
            );
        }
    }
}
