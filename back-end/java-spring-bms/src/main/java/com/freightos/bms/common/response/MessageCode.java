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
    FINANCIAL_DOCUMENT_MIXED_TYPE("선택된 라인의 서류 종류가 서로 다릅니다. 동일 종류 라인만 한 서류로 발행할 수 있습니다."),
    FINANCIAL_DOCUMENT_LINE_OTHER_DOCUMENT("다른 서류에 이미 연결된 라인은 편집 대상에 포함할 수 없습니다."),
    FINANCIAL_DOCUMENT_GROUPED("그룹 번호가 부여되었습니다."),
    FINANCIAL_DOCUMENT_GROUP_MIXED_CATEGORY("INVOICE·PAYMENT·D/C Note는 서로 다른 그룹 카테고리이므로 함께 그룹화할 수 없습니다."),
    FINANCIAL_DOCUMENT_GROUP_MULTIPLE_EXISTING("선택된 서류들이 이미 서로 다른 그룹에 속해 있습니다. 기존 그룹 중 하나를 선택해 합류하거나 개별 해제 후 다시 시도하세요."),
    FINANCIAL_DOCUMENT_DELETE_NOT_CREATED("그룹·세금·전표 단계의 서류는 삭제할 수 없습니다. 먼저 그룹을 해제하여 생성(CREATED) 상태로 되돌린 뒤 삭제하세요."),

    // 단계 E 발급 전용 MessageCode (S3 신규 3종 + 발급 성공)
    FREIGHT_LINE_NOT_ISSUED_YET("서류가 발행되지 않은 운임 행이 포함되어 있습니다. 서류 발행 후 발급하세요."),
    FREIGHT_LINE_TAX_ALREADY_ISSUED("이미 세금계산서가 발급된 운임 행이 포함되어 있습니다."),
    FREIGHT_LINE_SLIP_ALREADY_ISSUED("이미 전표가 발급된 운임 행이 포함되어 있습니다."),
    FREIGHT_LINE_TAX_ISSUED("세금계산서가 발급되었습니다."),
    FREIGHT_LINE_SLIP_ISSUED("전표가 발급되었습니다.");

    private final String message;

    MessageCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
