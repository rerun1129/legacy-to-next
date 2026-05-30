package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.application.housebl.projection.SeaContainerProjection;
import com.freightos.fms.application.housebl.projection.SeaDescProjection;
import com.freightos.fms.application.housebl.projection.SeaDetailProjection;

import java.math.BigDecimal;
import java.util.List;

/** SEA 본체 상세 응답 DTO. SeaDetailProjection 코드 필드 + view에서 주입된 name 필드를 포함한다. */
public record SeaDetailResponse(
        String linerCode,
        String linerName,
        String vesselCode,
        String vesselName,
        String voyageNo,
        String onboardDate,
        String porCode,
        String finalDestCode,
        String issueDate,
        String noOfBl,
        String issuePlace,
        String issuePlaceName,
        String doDate,
        String payableAt,
        String payableAtName,
        boolean triangle,
        String serviceTerm,
        String vesselNationality,
        BigDecimal rton,
        String sayInformation,
        String noOfContainerOrPackages,
        String deliveryName,
        List<SeaContainerView> containers,
        SeaDescView desc
) {
    /**
     * SeaDetailProjection(코드) + view에서 별도 전달받은 name 4종으로 빌드.
     * SeaDetailProjection은 변경하지 않는다.
     */
    public static SeaDetailResponse from(
            SeaDetailProjection p,
            String issuePlaceName,
            String payableAtName,
            String deliveryName,
            String linerName
    ) {
        return new SeaDetailResponse(
                p.linerCode(),
                linerName,
                p.vesselCode(),
                p.vesselName(),
                p.voyageNo(),
                p.onboardDate(),
                p.porCode(),
                p.finalDestCode(),
                p.issueDate(),
                p.noOfBl(),
                p.issuePlace(),
                issuePlaceName,
                p.doDate(),
                p.payableAt(),
                payableAtName,
                p.triangle(),
                p.serviceTerm(),
                p.vesselNationality(),
                p.rton(),
                p.sayInformation(),
                p.noOfContainerOrPackages(),
                deliveryName,
                p.containers() == null ? List.of() : p.containers().stream().map(SeaContainerView::from).toList(),
                p.desc() != null ? SeaDescView.from(p.desc()) : null
        );
    }

    public record SeaContainerView(
            Long id,
            String containerNo,
            String containerType,
            Integer lengthFeet,
            String sealNo1,
            String sealNo2,
            String sealNo3,
            String sealNo4,
            String sealNo5,
            String sealNo6,
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            BigDecimal netWeightKg,
            BigDecimal cbm,
            BigDecimal vgmKg,
            boolean soc,
            int seq
    ) {
        public static SeaContainerView from(SeaContainerProjection p) {
            return new SeaContainerView(
                    p.id(),
                    p.containerNo(),
                    p.containerType(),
                    p.lengthFeet(),
                    p.sealNo1(),
                    p.sealNo2(),
                    p.sealNo3(),
                    p.sealNo4(),
                    p.sealNo5(),
                    p.sealNo6(),
                    p.pkgQty(),
                    p.pkgUnit(),
                    p.grossWeightKg(),
                    p.netWeightKg(),
                    p.cbm(),
                    p.vgmKg(),
                    p.soc(),
                    p.seq()
            );
        }
    }

    public record SeaDescView(
            String marks,
            String description,
            String descClause1,
            String descClause2
    ) {
        public static SeaDescView from(SeaDescProjection p) {
            return new SeaDescView(p.marks(), p.description(), p.descClause1(), p.descClause2());
        }
    }
}
