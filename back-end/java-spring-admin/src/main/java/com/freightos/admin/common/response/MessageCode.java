package com.freightos.admin.common.response;

public enum MessageCode {
    SUCCESS("성공"),
    CODE_CREATED("코드가 등록되었습니다."),
    CODE_UPDATED("코드가 수정되었습니다."),
    CODE_DELETED("코드가 삭제되었습니다."),
    CODE_NOT_FOUND("코드를 찾을 수 없습니다."),
    CODE_DUPLICATE_GROUP_VALUE("동일한 codeGroup·codeValue 코드가 이미 존재합니다.");

    private final String message;

    MessageCode(String message) { this.message = message; }

    public String getMessage() { return message; }
}
