package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import jakarta.validation.constraints.NotNull;

/**
 * POST /api/house-bl 요청 본문.
 * jobDiv 와 bound 는 House B/L 생성의 필수 식별자이므로 @NotNull.
 */
public record CreateHouseBlRequest(

        @NotNull(message = "jobDiv는 필수입니다.")
        JobDiv jobDiv,

        @NotNull(message = "bound는 필수입니다.")
        Bound bound
) {
}
