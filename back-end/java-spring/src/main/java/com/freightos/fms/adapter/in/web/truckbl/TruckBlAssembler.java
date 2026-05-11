package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.truckbl.dto.CreateTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.SearchTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlDetailResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlSummaryResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.UpdateTruckBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.truckbl.command.SearchTruckBlCommand;
import com.freightos.fms.application.truckbl.projection.TruckBlDetailResult;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Request DTO → Command 변환 및 Projection → Response DTO 변환 담당.
 * jobDiv는 항상 TRUCK으로 강제한다. hblNo는 toUpdateCommand에서 매핑하지 않는다.
 */
@Component
public class TruckBlAssembler {

    public PagedResult<TruckBlSummaryResponse> toSummaryPage(PagedResult<TruckBlSummary> source) {
        return source.map(TruckBlSummaryResponse::from);
    }

    public SearchTruckBlCommand toSearchCommand(SearchTruckBlRequest req) {
        return new SearchTruckBlCommand(
                req.bound(),
                req.truckBlNo(),
                req.etdFrom(), req.etdTo(),
                req.truckerCode(), req.docPartnerCode(),
                req.partyCode(), req.portCode(),
                req.operatorCode(), req.teamCode(),
                req.dateKind(),
                req.partyKind(),
                req.portKind()
        );
    }

    public TruckBlDetailResponse toDetail(TruckBlDetailResult result) {
        return TruckBlDetailResponse.from(result);
    }

    public CreateHouseBlCommand toCreateCommand(CreateTruckBlRequest req) {
        return new CreateHouseBlCommand(
                "TRUCK",
                req.bound(),
                req.hblNo(),
                null, // shipmentType
                null, // freightTerm
                req.shipperCode(),
                null, // shipperAddress
                req.consigneeCode(),
                null, // consigneeAddress
                req.notifyCode(),
                null, // notifyAddress
                null, // docPartnerCode
                null, // docPartnerAddress
                req.settlePartnerCode(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.actualCustomerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.salesManCode(),
                null, // masterBlId
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                null, // mblNo
                null, // masterRefNo
                null, // workDivision
                null, // originalBlRef
                null, // volumeDivisor
                null, // linerCode
                null, // linerName
                null, // vesselName (TRUCK: "TRUCK" 고정, HouseBlTruck.create에서 처리)
                null, // voyageNo (Non B/L 전용 일반 필드, Truck은 truckDetail.voyageNo로 전달)
                null, // finalDestCode
                null, // finalDestName
                null, // finalEta
                null, // volumeWeightKg
                null, // rton
                null, // remark
                null, // seaDetail — TRUCK 미사용
                toDescCommand(req.desc()),
                null, // dims
                null, // containers
                null, // scheduleLegs
                toTruckOrderCommands(req.truckOrders()),
                null, // airCharges
                toTruckDetailCreate(req)
        );
    }

    public UpdateHouseBlCommand toUpdateCommand(UpdateTruckBlRequest req) {
        // hblNo는 UpdateHouseBlCommand에 포함하지 않는다 (hblNo 매핑 금지 — §10 규칙)
        return new UpdateHouseBlCommand(
                "TRUCK",
                req.bound(),
                null, // shipmentType
                null, // freightTerm
                req.shipperCode(),
                null, // shipperAddress
                req.consigneeCode(),
                null, // consigneeAddress
                req.notifyCode(),
                null, // notifyAddress
                null, // docPartnerCode
                null, // docPartnerAddress
                req.settlePartnerCode(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.actualCustomerCode(),
                req.operatorCode(),
                req.teamCode(),
                req.salesManCode(),
                null, // masterBlId
                req.incoterms(),
                req.salesClass(),
                req.mainItemName(),
                req.hsCode(),
                null, // mblNo
                null, // masterRefNo
                null, // workDivision
                null, // originalBlRef
                null, // volumeDivisor
                null, // linerCode
                null, // linerName
                null, // vesselName
                null, // voyageNo (Non B/L 전용 일반 필드, Truck은 truckDetail.voyageNo로 전달)
                null, // finalDestCode
                null, // finalDestName
                null, // finalEta
                null, // volumeWeightKg
                null, // rton
                null, // remark
                null, // seaDetail
                toDescCommandU(req.desc()),
                null, // dims
                null, // containers
                null, // scheduleLegs
                toTruckOrderCommandsU(req.truckOrders()),
                null, // airCharges
                toTruckDetailUpdate(req)
        );
    }

    // ── CREATE 서브 변환 ──────────────────────────────────────────────

    private CreateHouseBlCommand.DescCommand toDescCommand(CreateTruckBlRequest.DescRequest r) {
        if (r == null) return null;
        return new CreateHouseBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark());
    }

    private List<CreateHouseBlCommand.TruckOrderCommand> toTruckOrderCommands(
            List<CreateTruckBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateHouseBlCommand.TruckOrderCommand(
                r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    // ── UPDATE 서브 변환 ──────────────────────────────────────────────

    private UpdateHouseBlCommand.DescCommand toDescCommandU(UpdateTruckBlRequest.DescRequest r) {
        if (r == null) return null;
        return new UpdateHouseBlCommand.DescCommand(r.id(), r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark());
    }

    private List<UpdateHouseBlCommand.TruckOrderCommand> toTruckOrderCommandsU(
            List<UpdateTruckBlRequest.TruckOrderRequest> reqs) {
        if (reqs == null) return null;
        // UpdateHouseBlCommand.TruckOrderCommand에 id 필드가 없으므로 id는 adapter update 시 사용 불가.
        // Truck update adapter는 syncTruckOrders(clear+addAll) 패턴으로 처리한다.
        return reqs.stream().map(r -> new UpdateHouseBlCommand.TruckOrderCommand(
                r.truckOrderNo(), r.pkgQty(), r.pkgUnit(), r.grossWeightKg(), r.cbm(),
                r.truckNo(), r.truckType(), r.driver(), r.mobileNo(),
                r.containerNo(), r.containerType(), r.sealNo1(), r.sealNo2(), r.sealNo3())).toList();
    }

    private CreateHouseBlCommand.TruckDetailCommand toTruckDetailCreate(CreateTruckBlRequest req) {
        return new CreateHouseBlCommand.TruckDetailCommand(
                req.truckerCode(), req.truckerPic(), req.chargeWeightKg(),
                req.pickupDate(), req.pickupTm(), req.etdTm(), req.etaTm(),
                req.loadType(), req.serviceTerm(), req.voyageNo());
    }

    private UpdateHouseBlCommand.TruckDetailCommand toTruckDetailUpdate(UpdateTruckBlRequest req) {
        return new UpdateHouseBlCommand.TruckDetailCommand(
                req.truckerCode(), req.truckerPic(), req.chargeWeightKg(),
                req.pickupDate(), req.pickupTm(), req.etdTm(), req.etaTm(),
                req.loadType(), req.serviceTerm(), req.voyageNo());
    }
}
