package com.freightos.admin.application.codedetail.command;

public record CreateCodeDetailCommand(
        Long masterId,
        String codeValue,
        String codeLabel,
        Integer sortOrder,
        Boolean active,
        String remark
) {}
