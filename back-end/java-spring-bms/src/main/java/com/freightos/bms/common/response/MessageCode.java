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
    FINANCIAL_DOCUMENT_NOT_FOUND("금융 서류를 찾을 수 없습니다."),
    FINANCIAL_DOCUMENT_LINE_EMPTY("발행 대상 운임 라인이 없습니다."),
    FINANCIAL_DOCUMENT_LINE_ALREADY_ISSUED("이미 서류가 발행된 라인이 포함되어 있습니다."),
    FINANCIAL_DOCUMENT_MIXED_CUSTOMER("선택된 라인의 고객사가 서로 다릅니다. 동일 고객사 라인만 한 서류로 발행할 수 있습니다."),
    FINANCIAL_DOCUMENT_MIXED_TYPE("선택된 라인의 서류 종류가 서로 다릅니다. 동일 종류 라인만 한 서류로 발행할 수 있습니다.");

    private final String message;

    MessageCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
