package com.freightos.admin.adapter.in.web.code.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * page/size 기본값은 null 허용 후 Assembler에서 처리하지 않고,
 * Jackson이 int primitive를 0으로 역직렬화함에 따라 Assembler에서 기본값을 보정한다.
 * size 기본값 20은 toSearchCommand 에서 적용.
 */
public record SearchCodeRequest(
        String codeGroup,
        String codeValue,
        String codeLabel,
        Boolean active,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
