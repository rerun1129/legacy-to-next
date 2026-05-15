package com.freightos.fms.adapter.in.web.masterbl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FindMasterBlByMblNoRequest(
        @NotBlank @Size(max = 50) String mblNo
) {}
