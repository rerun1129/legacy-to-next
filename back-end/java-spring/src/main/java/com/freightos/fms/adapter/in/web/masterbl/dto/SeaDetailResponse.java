package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.masterbl.projection.SeaDescProjection;
import com.freightos.fms.application.masterbl.projection.SeaDetailProjection;

import java.math.BigDecimal;

/** Master SEA 본체 상세 응답 DTO. SeaDetailProjection을 1:1 매핑한다. */
public record SeaDetailResponse(
        String loadType,
        String linerCode,
        String vesselCode,
        String vesselName,
        String voyageNo,
        String onboardDate,
        String vesselNationality,
        String serviceTerm,
        String blType,
        String porCode,
        String finalDestCode,
        BigDecimal rton,
        String lineBkgNo,
        String issueDate,
        SeaDescView desc,
        String remark
) {
    public static SeaDetailResponse from(SeaDetailProjection p) {
        return new SeaDetailResponse(
                p.loadType(),
                p.linerCode(),
                p.vesselCode(),
                p.vesselName(),
                p.voyageNo(),
                p.onboardDate(),
                p.vesselNationality(),
                p.serviceTerm(),
                p.blType(),
                p.porCode(),
                p.finalDestCode(),
                p.rton(),
                p.lineBkgNo(),
                p.issueDate(),
                p.desc() != null ? SeaDescView.from(p.desc()) : null,
                p.remark()
        );
    }

    public record SeaDescView(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {
        public static SeaDescView empty() {
            return new SeaDescView(null, null, null, null);
        }

        public static SeaDescView from(SeaDescProjection p) {
            return new SeaDescView(p.marks(), p.description(), p.descClause1(), p.descClause2());
        }
    }
}
