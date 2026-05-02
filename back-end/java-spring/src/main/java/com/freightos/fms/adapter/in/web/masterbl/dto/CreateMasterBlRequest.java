package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import jakarta.validation.constraints.NotNull;

/**
 * POST /api/master-bl 요청 본문.
 * jobDiv 와 bound 는 Master B/L 생성의 필수 식별자이므로 @NotNull.
 */
public record CreateMasterBlRequest(

        @NotNull(message = "jobDiv는 필수입니다.")
        MasterBlJobDiv jobDiv,

        @NotNull(message = "bound는 필수입니다.")
        Bound bound
) {
}
