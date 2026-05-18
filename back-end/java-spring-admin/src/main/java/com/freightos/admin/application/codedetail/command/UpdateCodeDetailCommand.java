package com.freightos.admin.application.codedetail.command;

public record UpdateCodeDetailCommand(
        String codeLabel,
        Integer sortOrder,
        Boolean active,
        String remark
) {}
