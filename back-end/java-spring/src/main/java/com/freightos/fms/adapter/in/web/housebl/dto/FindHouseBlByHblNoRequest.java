package com.freightos.fms.adapter.in.web.housebl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FindHouseBlByHblNoRequest(
        @NotBlank @Size(max = 50) String hblNo,
        @NotBlank String jobDiv
) {}
