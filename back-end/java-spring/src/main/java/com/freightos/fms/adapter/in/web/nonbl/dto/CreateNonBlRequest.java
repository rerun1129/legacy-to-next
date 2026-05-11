package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateNonBlRequest(
        String jobDiv,
        String bound,
        @NotBlank @Size(max = 35) String hblNo,
        @NotBlank String workDivision,
        String originalBlRef,
        String volumeDivisor,
        String salesClass,
        String mblNo,
        String masterRefNo,
        Long masterBlId,
        String shipperCode,
        String shipperAddress,
        String consigneeCode,
        String consigneeAddress,
        String notifyCode,
        String notifyAddress,
        String docPartnerCode,
        String docPartnerAddress,
        String settlePartnerCode,
        @NotBlank @Size(max = 5) String polCode,
        @NotBlank @Size(max = 5) String podCode,
        @NotBlank @Pattern(regexp = "\\d{8}") String etd,
        @NotBlank @Pattern(regexp = "\\d{8}") String eta,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        @NotBlank @Size(max = 20) String actualCustomerCode,
        @NotBlank @Size(max = 20) String operatorCode,
        @NotBlank @Size(max = 20) String teamCode,
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
        String finalEta,
        BigDecimal volumeWeightKg,
        BigDecimal rton,

        String remark,

        // SEA 확장 필드
        CreateHouseBlRequest.SeaDetailRequest seaDetail,

        // Sub 엔티티
        List<CreateHouseBlRequest.DimRequest> dims,
        List<CreateHouseBlRequest.ContainerRequest> containers,
        List<CreateHouseBlRequest.ScheduleLegRequest> scheduleLegs,
        List<CreateHouseBlRequest.TruckOrderRequest> truckOrders,
        List<CreateHouseBlRequest.AirChargeRequest> airCharges
) {}
