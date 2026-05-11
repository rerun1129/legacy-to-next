package com.freightos.fms.adapter.in.web.truckbl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FindTruckBlByHblNoRequest(
        @NotBlank @Size(max = 50) String hblNo
) {}
