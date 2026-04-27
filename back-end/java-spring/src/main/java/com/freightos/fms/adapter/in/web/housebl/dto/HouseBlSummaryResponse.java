package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;

import java.time.LocalDateTime;

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
                entity.getHblNo(),
                entity.getJobDiv(),
                entity.getBound(),
                entity.getPolCode(),
                entity.getPodCode(),
                entity.getEtd(),
                entity.getEta(),
                entity.getShipperCode(),
                entity.getConsigneeCode(),
                entity.getPkgQty(),
                entity.getPkgUnit(),
                entity.getCreatedAt()
        );
    }
}
