package com.freightos.admin.application.codedetail.command;

public record SearchCodeDetailCommand(
        Long masterId,
        String codeValue,
        String codeLabel,
        Boolean active,
        int page,
        int size
) {}
