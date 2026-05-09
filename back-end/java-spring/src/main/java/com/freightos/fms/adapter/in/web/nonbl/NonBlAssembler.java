package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.CreateNonBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlDetailResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlSummaryResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.SearchNonBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.UpdateNonBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Request DTO → Command 변환 및 Projection → Response DTO 변환 담당.
 * Non B/L 엔드포인트는 shipmentType·freightTerm 필드가 없으므로 null로 고정하고,
 * jobDiv는 항상 NON_BL로 강제한다.
 */
@Component
public class NonBlAssembler {

    public PagedResult<NonBlSummaryResponse> toSummaryPage(PagedResult<NonBlSummary> source) {
        return source.map(NonBlSummaryResponse::from);
    }

    public NonBlDetailResponse toDetail(NonBlDetailResult result) {
        return NonBlDetailResponse.from(result);
    }

    public SearchNonBlCommand toSearchCommand(SearchNonBlRequest req) {
        return new SearchNonBlCommand(
                req.bound(),
                req.hblNo(),
                req.etdFrom(), req.etdTo(),
                req.linerCode(),
                req.partyCode(), req.portCode(),
                req.vessel(), req.voyage(),
                req.operatorCode(), req.teamCode(),
                req.dateKind(),
                req.partyKind(),
                req.portKind()
        );
    }

    public CreateHouseBlCommand toCreateCommand(CreateNonBlRequest req) {
        return new CreateHouseBlCommand(
                "NON_BL",
                req.bound(),
                req.hblNo(),
                null,
                null,
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.docPartnerCode(),
                req.docPartnerAddress(),
                req.settlePartnerCode(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.actualCustomerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.salesManCode(),
                req.masterBlId(),
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                req.mblNo(),
                req.masterRefNo(),
                req.workDivision(),
                req.originalBlRef(),
                req.volumeDivisor(),
                req.linerCode(),
                req.linerName(),
                req.vesselName(),
                req.voyageNo(),
                req.finalDestCode(),
                req.finalDestName(),
                req.finalEta(),
                req.volumeWeightKg(),
                req.rton(),
                toSeaDetailCommand(req.seaDetail()),
                toDescCommand(req.desc()),
                toDimCommands(req.dims()),
                toContainerCommands(req.containers()),
                toScheduleLegCommands(req.scheduleLegs()),
                toTruckOrderCommands(req.truckOrders()),
                toAirChargeCommands(req.airCharges())
        );
    }

    public UpdateHouseBlCommand toUpdateCommand(UpdateNonBlRequest req) {
        return new UpdateHouseBlCommand(
                "NON_BL",
                req.bound(),
                req.hblNo(),
                null,
                null,
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.docPartnerCode(),
                req.docPartnerAddress(),
                req.settlePartnerCode(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.actualCustomerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.salesManCode(),
                req.masterBlId(),
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                req.mblNo(),
                req.masterRefNo(),
                req.workDivision(),
                req.originalBlRef(),
                req.volumeDivisor(),
                req.linerCode(),
                req.linerName(),
                req.vesselName(),
                req.voyageNo(),
                req.finalDestCode(),
                req.finalDestName(),
                req.finalEta(),
                req.volumeWeightKg(),
                req.rton(),
                toSeaDetailCommandU(req.seaDetail()),
                toDescCommandU(req.desc()),
                toDimCommandsU(req.dims()),
                toContainerCommandsU(req.containers()),
                toScheduleLegCommandsU(req.scheduleLegs()),
                toTruckOrderCommandsU(req.truckOrders()),
                toAirChargeCommandsU(req.airCharges())
        );
    }

    // ── CREATE sub ────────────────────────────────────────────────────

    private CreateHouseBlCommand.SeaDetailCommand toSeaDetailCommand(CreateHouseBlRequest.SeaDetailRequest s) {
        if (s == null) return null;
        return new CreateHouseBlCommand.SeaDetailCommand(
                s.loadType(), s.linerCode(), s.vesselCode(), s.vesselName(), s.voyageNo(),
                s.onboardDate(), s.porCode(), s.finalDestCode(), s.issueDate(), s.noOfBl(),
                s.issuePlace(), s.doDate(), s.payableAt(), s.triangle(), s.serviceTerm(),
                s.vesselCode2(), s.vesselNationality(), s.weightUnit(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages(), s.blType(), s.deliveryCode());
    }

    private CreateHouseBlCommand.DescCommand toDescCommand(CreateHouseBlRequest.DescRequest r) {
        if (r == null) return null;
        return new CreateHouseBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark());
    }

    private List<CreateHouseBlCommand.DimCommand> toDimCommands(List<CreateHouseBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.DimCommand(
                r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<CreateHouseBlCommand.ContainerCommand> toContainerCommands(List<CreateHouseBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.ContainerCommand(
                r.containerNo(), r.containerType(), r.lengthFeet(),
                r.sealNo1(), r.sealNo2(), r.sealNo3(), r.sealNo4(), r.sealNo5(), r.sealNo6(),
                r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.netWeightKg(), r.cbm(),
                r.vgmKg(), r.soc(), r.seq())).toList();
    }

    private List<CreateHouseBlCommand.ScheduleLegCommand> toScheduleLegCommands(List<CreateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.ScheduleLegCommand(
                r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<CreateHouseBlCommand.TruckOrderCommand> toTruckOrderCommands(List<CreateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.TruckOrderCommand(
                r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private List<CreateHouseBlCommand.AirChargeCommand> toAirChargeCommands(List<CreateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.AirChargeCommand(
                r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(),
                r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── UPDATE sub ────────────────────────────────────────────────────
    // UpdateNonBlRequest reuses CreateHouseBlRequest nested records, so conversion
    // targets UpdateHouseBlCommand nested types using the same source fields.

    private UpdateHouseBlCommand.SeaDetailCommand toSeaDetailCommandU(CreateHouseBlRequest.SeaDetailRequest s) {
        if (s == null) return null;
        return new UpdateHouseBlCommand.SeaDetailCommand(
                s.loadType(), s.linerCode(), s.vesselCode(), s.vesselName(), s.voyageNo(),
                s.onboardDate(), s.porCode(), s.finalDestCode(), s.issueDate(), s.noOfBl(),
                s.issuePlace(), s.doDate(), s.payableAt(), s.triangle(), s.serviceTerm(),
                s.vesselNationality(), s.weightUnit(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages(), s.blType(), s.deliveryCode());
    }

    private UpdateHouseBlCommand.DescCommand toDescCommandU(UpdateNonBlRequest.DescRequest r) {
        if (r == null) return null;
        return new UpdateHouseBlCommand.DescCommand(r.id(), r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark());
    }

    private List<UpdateHouseBlCommand.DimCommand> toDimCommandsU(List<UpdateNonBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.DimCommand(
                r.id(), r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<UpdateHouseBlCommand.ContainerCommand> toContainerCommandsU(List<UpdateNonBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ContainerCommand(
                r.id(), r.containerNo(), r.containerType(), r.lengthFeet(),
                r.sealNo1(), r.sealNo2(), r.sealNo3(), r.sealNo4(), r.sealNo5(), r.sealNo6(),
                r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.netWeightKg(), r.cbm(),
                r.vgmKg(), r.soc(), r.seq())).toList();
    }

    private List<UpdateHouseBlCommand.ScheduleLegCommand> toScheduleLegCommandsU(List<CreateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ScheduleLegCommand(
                r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<UpdateHouseBlCommand.TruckOrderCommand> toTruckOrderCommandsU(List<CreateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.TruckOrderCommand(
                r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private List<UpdateHouseBlCommand.AirChargeCommand> toAirChargeCommandsU(List<CreateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.AirChargeCommand(
                r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(),
                r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }
}
