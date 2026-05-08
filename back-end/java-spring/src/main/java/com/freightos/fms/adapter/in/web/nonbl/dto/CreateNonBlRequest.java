package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record CreateNonBlRequest(
        @NotNull String jobDiv,
        @NotNull String bound,
        @Size(max = 35) String hblNo,
        @Size(max = 20) String workDivision,
        String originalBlRef,
        String salesClass,
        @Size(max = 35) String mblNo,
        @Size(max = 35) String masterRefNo,
        Long masterBlId,
        @Size(max = 20) String shipperCode,
        String shipperAddress,
        @Size(max = 20) String consigneeCode,
        String consigneeAddress,
        @Size(max = 20) String notifyCode,
        String notifyAddress,
        @Size(max = 20) String docPartnerCode,
        String docPartnerAddress,
        @Size(max = 20) String settlePartnerCode,
        @Size(max = 5) String polCode,
        @Size(max = 5) String podCode,
        @Pattern(regexp = "\\d{8}") String etd,
        @Pattern(regexp = "\\d{8}") String eta,
        @Min(0) Integer pkgQty,
        String pkgUnit,
        @DecimalMin("0") BigDecimal grossWeightKg,
        @DecimalMin("0") BigDecimal cbm,
        @Size(max = 20) String actualCustomerCode,
        String operatorCode,
        String teamCode,
        String salesManCode,
        String incoterms,
        String mainItemName,
        String hsCode,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        @Pattern(regexp = "\\d{8}") String finalEta,
        @DecimalMin("0") BigDecimal volumeWeightKg,
        @DecimalMin("0") BigDecimal rton,

        // SEA 확장 필드
        CreateHouseBlRequest.SeaDetailRequest seaDetail,

        // Sub 엔티티
        CreateHouseBlRequest.DescRequest desc,
        List<CreateHouseBlRequest.DimRequest> dims,
        List<CreateHouseBlRequest.ContainerRequest> containers,
        List<CreateHouseBlRequest.ScheduleLegRequest> scheduleLegs,
        List<CreateHouseBlRequest.TruckOrderRequest> truckOrders,
        List<CreateHouseBlRequest.AirChargeRequest> airCharges
) {}
