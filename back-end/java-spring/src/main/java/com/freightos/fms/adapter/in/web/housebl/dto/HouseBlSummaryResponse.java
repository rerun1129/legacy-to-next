package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;

import java.time.LocalDateTime;

import static com.freightos.fms.common.util.VoMapper.mapOrNull;

/** 리스트 화면용 요약 DTO */
public record HouseBlSummaryResponse(
        Long          id,
        String        hblNo,
        JobDiv        jobDiv,
        Bound         bound,
        String        polCode,
        String        podCode,
        String        etd,
        String        eta,
        String        shipperCode,
        String        consigneeCode,
        Integer       pkgQty,
        String        pkgUnit,
        LocalDateTime createdAt
) {
    public static HouseBlSummaryResponse from(HouseBl entity) {
        return new HouseBlSummaryResponse(
                entity.getId(),
                mapOrNull(entity.getHblNo(), BlNumber::value),
                entity.getJobDiv(),
                entity.getBound(),
                mapOrNull(entity.getPolCode(), PortCode::value),
                mapOrNull(entity.getPodCode(), PortCode::value),
                mapOrNull(entity.getEtd(), BlDate::asString),
                mapOrNull(entity.getEta(), BlDate::asString),
                mapOrNull(entity.getShipperCode(), CustomerCode::value),
                mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                mapOrNull(entity.getPkgQty(), Quantity::count),
                mapOrNull(entity.getPkgUnit(), WeightUnit::name),
                entity.getCreatedAt()
        );
    }

    /** QueryDSL projection 결과로부터 응답 DTO 생성. HouseBlSummary 필드는 이미 raw 타입. */
    public static HouseBlSummaryResponse from(HouseBlSummary summary) {
        return new HouseBlSummaryResponse(
                summary.houseBlId(),
                summary.hblNo(),
                summary.jobDiv(),
                summary.bound(),
                summary.polCode(),
                summary.podCode(),
                summary.etd(),
                summary.eta(),
                summary.shipperCode(),
                summary.consigneeCode(),
                summary.pkgQty(),
                summary.pkgUnit(),
                summary.createdAt()
        );
    }
}
