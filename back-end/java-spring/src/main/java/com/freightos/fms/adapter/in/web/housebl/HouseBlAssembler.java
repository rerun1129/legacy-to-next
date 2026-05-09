package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.SearchHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Request DTO → Command 변환 및 도메인 Entity → Response DTO 변환 담당.
 * 도메인 VO/Entity 생성 책임은 Application 계층(HouseBlFactory)에 있으므로
 * 본 클래스는 1:1 필드 복사만 수행한다.
 */
@Component
public class HouseBlAssembler {

    public PagedResult<HouseBlSummaryResponse> toSummaryPage(PagedResult<HouseBlSummary> source) {
        return source.map(HouseBlSummaryResponse::from);
    }

    public HouseBlDetailResponse toDetail(HouseBlDetailResult result) {
        return HouseBlDetailResponse.from(result);
    }

    public SearchHouseBlCommand toSearchCommand(SearchHouseBlRequest req) {
        return new SearchHouseBlCommand(
                req.jobDiv(),
                req.bound(),
                req.hblNo(), req.mblNo(),
                req.shipperCode(), req.consigneeCode(),
                req.polCode(), req.podCode(),
                req.etdFrom(), req.etdTo(),
                req.vessel(), req.voyage(),
                req.linerCode(), req.operatorCode(),
                req.teamCode(), req.partyCode(), req.portCode(),
                req.dateKind(),
                req.partyKind(),
                req.portKind()
        );
    }

    /** CREATE 요청 DTO를 커맨드로 변환한다. VO 변환은 없으며 1:1 필드 복사만 수행한다. */
    public CreateHouseBlCommand toCreateCommand(CreateHouseBlRequest req) {
        return new CreateHouseBlCommand(
                req.jobDiv(),
                req.bound(),
                req.hblNo(),
                req.shipmentType(),
                req.freightTerm(),
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
                null,
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

    /** UPDATE 요청 DTO를 커맨드로 변환한다. VO 변환은 없으며 1:1 필드 복사만 수행한다. */
    public UpdateHouseBlCommand toUpdateCommand(UpdateHouseBlRequest req) {
        return new UpdateHouseBlCommand(
                req.jobDiv(),
                req.bound(),
                req.hblNo(),
                req.shipmentType(),
                req.freightTerm(),
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
                null,
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

    private UpdateHouseBlCommand.SeaDetailCommand toSeaDetailCommandU(UpdateHouseBlRequest.SeaDetailRequest s) {
        if (s == null) return null;
        return new UpdateHouseBlCommand.SeaDetailCommand(
                s.loadType(), s.linerCode(), s.vesselCode(), s.vesselName(), s.voyageNo(),
                s.onboardDate(), s.porCode(), s.finalDestCode(), s.issueDate(), s.noOfBl(),
                s.issuePlace(), s.doDate(), s.payableAt(), s.triangle(), s.serviceTerm(),
                s.vesselNationality(), s.weightUnit(), s.rton(),
                s.sayInformation(), s.noOfContainerOrPackages(), s.blType(), s.deliveryCode());
    }

    private UpdateHouseBlCommand.DescCommand toDescCommandU(UpdateHouseBlRequest.DescRequest r) {
        if (r == null) return null;
        // Sea/Air/Truck UPDATE는 sync 방식이므로 id null 허용
        return new UpdateHouseBlCommand.DescCommand(null, r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark());
    }

    private List<UpdateHouseBlCommand.DimCommand> toDimCommandsU(List<UpdateHouseBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        // Sea/Air/Truck UPDATE는 sync 방식이므로 id null 허용
        return reqs.stream().map(r -> new UpdateHouseBlCommand.DimCommand(
                null, r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<UpdateHouseBlCommand.ContainerCommand> toContainerCommandsU(List<UpdateHouseBlRequest.ContainerRequest> reqs) {
        if (reqs == null) return null;
        // Sea/Air/Truck UPDATE는 sync 방식이므로 id null 허용
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ContainerCommand(
                null, r.containerNo(), r.containerType(), r.lengthFeet(),
                r.sealNo1(), r.sealNo2(), r.sealNo3(), r.sealNo4(), r.sealNo5(), r.sealNo6(),
                r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.netWeightKg(), r.cbm(),
                r.vgmKg(), r.soc(), r.seq())).toList();
    }

    private List<UpdateHouseBlCommand.ScheduleLegCommand> toScheduleLegCommandsU(List<UpdateHouseBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.ScheduleLegCommand(
                r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<UpdateHouseBlCommand.TruckOrderCommand> toTruckOrderCommandsU(List<UpdateHouseBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.TruckOrderCommand(
                r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private List<UpdateHouseBlCommand.AirChargeCommand> toAirChargeCommandsU(List<UpdateHouseBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateHouseBlCommand.AirChargeCommand(
                r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(),
                r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }
}
