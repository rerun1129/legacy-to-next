package com.freightos.fms.adapter.in.web.nonbl.dto;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import java.math.BigDecimal;
import java.util.List;

// null 필드는 기존 값 유지 정책 (PATCH 의미론)
public record UpdateNonBlRequest(
        String jobDiv,
        String bound,
        String hblNo,
        String workDivision,
        String originalBlRef,
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
        String polCode,
        String podCode,
        String etd,
        String eta,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String actualCustomerCode,
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
        String finalEta,
        BigDecimal volumeWeightKg,
        BigDecimal rton,

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
