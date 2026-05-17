package com.freightos.admin.common.response;

public enum MessageCode {
    SUCCESS("성공"),
    CODE_CREATED("코드가 등록되었습니다."),
    CODE_UPDATED("코드가 수정되었습니다."),
    CODE_DELETED("코드가 삭제되었습니다."),
    CODE_NOT_FOUND("코드를 찾을 수 없습니다."),
    CODE_DUPLICATE_GROUP_VALUE("동일한 codeGroup·codeValue 코드가 이미 존재합니다."),
    USER_CREATED("사용자가 등록되었습니다."),
    USER_UPDATED("사용자가 수정되었습니다."),
    USER_DELETED("사용자가 삭제되었습니다."),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    USER_DUPLICATE_USERNAME("동일한 username 사용자가 이미 존재합니다."),
    USER_ALREADY_DELETED("이미 삭제된 사용자입니다."),
    USER_LAST_ADMIN("마지막 활성 관리자는 삭제·비활성화할 수 없습니다."),
    PARTNER_CREATED("협력사가 등록되었습니다."),
    PARTNER_UPDATED("협력사가 수정되었습니다."),
    PARTNER_DELETED("협력사가 삭제되었습니다."),
    PARTNER_NOT_FOUND("협력사를 찾을 수 없습니다."),
    PARTNER_DUPLICATE_CODE("동일한 partner_code 협력사가 이미 존재합니다."),
    PARTNER_ALREADY_DELETED("이미 삭제된 협력사입니다."),
    NOTICE_CREATED("공지사항이 등록되었습니다."),
    NOTICE_UPDATED("공지사항이 수정되었습니다."),
    NOTICE_DELETED("공지사항이 삭제되었습니다."),
    NOTICE_NOT_FOUND("공지사항을 찾을 수 없습니다."),
    NOTICE_ALREADY_DELETED("이미 삭제된 공지사항입니다."),
    TERMS_CREATED("약관이 등록되었습니다."),
    TERMS_UPDATED("약관이 수정되었습니다."),
    TERMS_DELETED("약관이 삭제되었습니다."),
    TERMS_NOT_FOUND("약관을 찾을 수 없습니다."),
    TERMS_ALREADY_DELETED("이미 삭제된 약관입니다."),
    AUTH_ME_OK("사용자 정보를 조회했습니다."),
    AUTH_LOGIN_OK("로그인되었습니다."),
    AUTH_REFRESH_OK("토큰이 갱신되었습니다."),
    AUTH_LOGOUT_OK("로그아웃되었습니다.");

    private final String message;

    MessageCode(String message) { this.message = message; }

    public String getMessage() { return message; }
}
