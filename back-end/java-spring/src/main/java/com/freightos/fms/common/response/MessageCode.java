package com.freightos.fms.common.response;

/**
 * 사용자 노출 메시지 코드. 추후 MessageSource 기반 i18n으로 확장한다.
 */
public enum MessageCode {

    HOUSE_BL_CREATED("House B/L이 생성되었습니다."),
    HOUSE_BL_UPDATED("수정되었습니다."),
    HOUSE_BL_DELETED("삭제되었습니다."),
    HOUSE_BL_NOT_FOUND("House B/L을 찾을 수 없습니다."),
    MASTER_BL_CREATED("등록되었습니다."),
    MASTER_BL_UPDATED("수정되었습니다."),
    MASTER_BL_DELETED("삭제되었습니다."),
    MASTER_BL_NOT_FOUND("Master B/L을 찾을 수 없습니다."),
    SWITCH_BL_CREATED("Switch B/L이 생성되었습니다."),
    SWITCH_BL_UPDATED("수정되었습니다."),
    SWITCH_BL_DELETED("삭제되었습니다."),
    SWITCH_BL_NOT_FOUND("Switch B/L을 찾을 수 없습니다."),
    NON_BL_CREATED("Non B/L이 생성되었습니다."),
    NON_BL_UPDATED("수정되었습니다."),
    NON_BL_DELETED("삭제되었습니다."),
    NON_BL_NOT_FOUND("Non B/L을 찾을 수 없습니다."),
    TRUCK_BL_CREATED("Truck B/L이 생성되었습니다."),
    TRUCK_BL_UPDATED("수정되었습니다."),
    TRUCK_BL_DELETED("삭제되었습니다."),
    TRUCK_BL_NOT_FOUND("Truck B/L을 찾을 수 없습니다."),
    SEA_HBL_UPDATED("수정되었습니다."),
    SEA_HBL_NOT_FOUND("Sea House B/L을 찾을 수 없습니다."),
    AIR_HBL_UPDATED("수정되었습니다."),
    AIR_HBL_NOT_FOUND("Air House B/L을 찾을 수 없습니다."),
    FREIGHT_LINE_REQUIRED("운임 라인에 필수 항목이 누락되었습니다."),
    FREIGHT_LINE_QTY_INVALID("운임 라인의 수량(qty)은 0보다 커야 합니다."),
    FREIGHT_LINE_PRICE_INVALID("운임 라인의 단가(price)는 0보다 커야 합니다."),
    FREIGHT_DELETE_BLOCKED("운임 라인이 존재하는 B/L은 삭제할 수 없습니다. 먼저 운임 라인을 삭제하십시오.");

    private final String message;

    MessageCode(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
