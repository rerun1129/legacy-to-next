package com.freightos.fms.adapter.in.web.masterbl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeMasterBlNoRequest(
        @NotBlank(message = "mblNo는 비어 있을 수 없습니다.")
        @Size(max = 50, message = "mblNo는 50자를 초과할 수 없습니다.")
        String mblNo,
        @NotBlank(message = "masterRefNo는 비어 있을 수 없습니다.")
        @Size(max = 50, message = "masterRefNo는 50자를 초과할 수 없습니다.")
        String masterRefNo
) {}
