package com.freightos.pms.common.response;

/**
 * PMS 사용자 노출 메시지 코드. 추후 MessageSource 기반 i18n으로 확장한다.
 */
public enum MessageCode {

    PMS_PERFORMANCE_QUERIED("실적 조회가 완료되었습니다.");

    private final String message;

    MessageCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
