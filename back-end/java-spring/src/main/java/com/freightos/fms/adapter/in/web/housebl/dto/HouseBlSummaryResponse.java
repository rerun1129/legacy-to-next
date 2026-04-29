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
                entity.getHblNo() != null ? entity.getHblNo().value() : null,
                entity.getJobDiv(),
                entity.getBound(),
                entity.getPolCode() != null ? entity.getPolCode().value() : null,
                entity.getPodCode() != null ? entity.getPodCode().value() : null,
                entity.getEtd() != null ? entity.getEtd().asString() : null,
                entity.getEta() != null ? entity.getEta().asString() : null,
                entity.getShipperCode() != null ? entity.getShipperCode().value() : null,
                entity.getConsigneeCode() != null ? entity.getConsigneeCode().value() : null,
                entity.getPkgQty() != null ? entity.getPkgQty().count() : null,
                entity.getPkgUnit(),
                entity.getCreatedAt()
        );
    }
}
