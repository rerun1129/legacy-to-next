package com.freightos.fms.application.housebl.command;

/**
 * House B/L 번호 단일 필드 변경 커맨드.
 * 일반 update 경로(UpdateHouseBlCommand)에는 hblNo 필드가 없어 차단되므로,
 * 별도 전용 커맨드로 의도적으로 분리한다.
 */
public record ChangeHouseBlNoCommand(String hblNo) {}
