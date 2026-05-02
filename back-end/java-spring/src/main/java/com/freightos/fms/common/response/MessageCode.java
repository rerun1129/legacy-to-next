package com.freightos.fms.common.response;

/**
 * 사용자 노출 메시지 코드. 추후 MessageSource 기반 i18n으로 확장한다.
 */
public enum MessageCode {

    HOUSE_BL_CREATED("House B/L이 생성되었습니다."),
    HOUSE_BL_DELETED("삭제되었습니다."),
    HOUSE_BL_NOT_FOUND("House B/L을 찾을 수 없습니다."),
    MASTER_BL_CREATED("등록되었습니다."),
    MASTER_BL_UPDATED("수정되었습니다."),
    MASTER_BL_DELETED("삭제되었습니다."),
    MASTER_BL_NOT_FOUND("Master B/L을 찾을 수 없습니다.");

    private final String message;

    MessageCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
