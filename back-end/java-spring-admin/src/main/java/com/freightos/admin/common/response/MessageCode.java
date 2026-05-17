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
    USER_LAST_ADMIN("마지막 활성 관리자는 삭제·비활성화할 수 없습니다."),
    AUTH_ME_OK("사용자 정보를 조회했습니다."),
    AUTH_LOGIN_OK("로그인되었습니다."),
    AUTH_REFRESH_OK("토큰이 갱신되었습니다."),
    AUTH_LOGOUT_OK("로그아웃되었습니다.");

    private final String message;

    MessageCode(String message) { this.message = message; }

    public String getMessage() { return message; }
}
