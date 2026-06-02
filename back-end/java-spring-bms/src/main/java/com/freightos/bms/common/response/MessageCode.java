package com.freightos.bms.common.response;

/**
 * BMS 사용자 노출 메시지 코드. 추후 MessageSource 기반 i18n으로 확장한다.
 */
public enum MessageCode {

    FREIGHT_HEADER_CREATED("운임 헤더가 생성되었습니다."),
    FREIGHT_HEADER_UPDATED("수정되었습니다."),
    FREIGHT_HEADER_DELETED("삭제되었습니다."),
    FREIGHT_HEADER_NOT_FOUND("운임 헤더를 찾을 수 없습니다."),
    FREIGHT_LINE_CREATED("운임 라인이 생성되었습니다."),
    FREIGHT_LINE_UPDATED("수정되었습니다."),
    FREIGHT_LINE_DELETED("삭제되었습니다."),
    FREIGHT_LINE_NOT_FOUND("운임 라인을 찾을 수 없습니다."),
    FINANCIAL_DOCUMENT_CREATED("금융 서류가 생성되었습니다."),
    FINANCIAL_DOCUMENT_UPDATED("수정되었습니다."),
    FINANCIAL_DOCUMENT_DELETED("삭제되었습니다."),
    FINANCIAL_DOCUMENT_NOT_FOUND("금융 서류를 찾을 수 없습니다.");

    private final String message;

    MessageCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
