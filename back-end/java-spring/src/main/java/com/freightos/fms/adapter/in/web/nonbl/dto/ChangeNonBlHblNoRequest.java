package com.freightos.fms.adapter.in.web.nonbl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeNonBlHblNoRequest(
        @NotBlank(message = "hblNo는 비어 있을 수 없습니다.")
        @Size(max = 50, message = "hblNo는 50자를 초과할 수 없습니다.")
        String hblNo
) {}
