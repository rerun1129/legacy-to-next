package com.freightos.fms.adapter.in.web.nonbl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FindNonBlByHblNoRequest(
        @NotBlank @Size(max = 50) String hblNo
) {}
