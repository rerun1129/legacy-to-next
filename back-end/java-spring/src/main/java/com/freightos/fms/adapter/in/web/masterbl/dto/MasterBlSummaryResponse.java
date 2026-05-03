package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;

import java.time.LocalDateTime;

import static com.freightos.common.util.VoMapper.mapOrNull;

/** Master B/L 리스트 화면용 요약 DTO. */
public record MasterBlSummaryResponse(
        Long id,
        String mblNo,
        String masterRefNo,
        MasterBlJobDiv jobDiv,
        Bound bound,
        String shipperCode,
        String consigneeCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String operatorCode,
        LocalDateTime createdAt
) {
    public static MasterBlSummaryResponse from(MasterBl entity) {
        return new MasterBlSummaryResponse(
                entity.getId(),
                mapOrNull(entity.getMblNo(), BlNumber::value),
                mapOrNull(entity.getMasterRefNo(), BlNumber::value),
                entity.getJobDiv(),
                entity.getBound(),
                mapOrNull(entity.getShipperCode(), CustomerCode::value),
                mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                mapOrNull(entity.getPolCode(), PortCode::value),
                mapOrNull(entity.getPodCode(), PortCode::value),
                mapOrNull(entity.getEtd(), BlDate::asString),
                mapOrNull(entity.getEta(), BlDate::asString),
                mapOrNull(entity.getOperatorCode(), EmployeeCode::value),
                entity.getCreatedAt()
        );
    }
}
