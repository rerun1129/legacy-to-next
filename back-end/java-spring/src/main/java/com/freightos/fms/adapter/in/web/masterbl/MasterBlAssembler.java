package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.SearchMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.UpdateMasterBlRequest;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.common.model.PagedResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MasterBlAssembler {

    public PagedResult<MasterBlSummaryResponse> toSummaryPage(PagedResult<MasterBlSummaryResult> source) {
        return source.map(MasterBlSummaryResponse::from);
    }

    public MasterBlDetailResponse toDetail(MasterBlDetailResult result) {
        return MasterBlDetailResponse.from(result);
    }

    public SearchMasterBlCommand toSearchCommand(SearchMasterBlRequest req) {
        return new SearchMasterBlCommand(
                req.bound(),
                req.mblNo(),
                req.shipperCode(),
                req.consigneeCode(),
                req.polCode(),
                req.podCode(),
                req.etdFrom(),
                req.etdTo()
        );
    }

    public CreateMasterBlCommand toCreateCommand(CreateMasterBlRequest req) {
        return new CreateMasterBlCommand(
                req.jobDiv(),
                req.bound(),
                req.mblNo(),
                req.masterRefNo(),
                req.freightTerm(),
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.hsCode(),
                req.mainItemName(),
                req.settlePartnerCode(),
                req.operatorCode(),
                req.remark(),
                toSeaDetailCommand(req.seaDetail()),
                toDescCommand(req.desc()),
                toDimCommands(req.dims()),
                toLegCommands(req.scheduleLegs()),
                toChargeCommands(req.airCharges())
        );
    }

    public UpdateMasterBlCommand toUpdateCommand(UpdateMasterBlRequest req) {
        return new UpdateMasterBlCommand(
                req.jobDiv(),
                req.bound(),
                req.mblNo(),
                req.masterRefNo(),
                req.freightTerm(),
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.polCode(),
                req.podCode(),
                req.etd(),
                req.eta(),
                req.pkgQty(),
                req.pkgUnit(),
                req.weightUnit(),
                req.grossWeightKg(),
                req.cbm(),
                req.hsCode(),
                req.mainItemName(),
                req.settlePartnerCode(),
                req.operatorCode(),
                req.remark(),
                toSeaDetailCommand(req.seaDetail()),
                toDescCommand(req.desc()),
                toDimCommandsU(req.dims()),
                toLegCommandsU(req.scheduleLegs()),
                toChargeCommandsU(req.airCharges())
        );
    }

    // ── 중첩 record 변환 ──────────────────────────────────────────────

    private CreateMasterBlCommand.SeaDetailCommand toSeaDetailCommand(CreateMasterBlRequest.SeaDetailRequest r) {
        if (r == null) return null;
        return new CreateMasterBlCommand.SeaDetailCommand(r.loadType(), r.linerCode(), r.vesselCode(), r.vesselName(), r.voyageNo(), r.onboardDate(), r.vesselNationality(), r.serviceTerm(), r.blType(), r.porCode(), r.finalDestCode(), r.rton(), r.lineBkgNo(), r.issueDate());
    }

    private UpdateMasterBlCommand.SeaDetailCommand toSeaDetailCommand(UpdateMasterBlRequest.SeaDetailRequest r) {
        if (r == null) return null;
        return new UpdateMasterBlCommand.SeaDetailCommand(r.loadType(), r.linerCode(), r.vesselCode(), r.vesselName(), r.voyageNo(), r.onboardDate(), r.vesselNationality(), r.serviceTerm(), r.blType(), r.porCode(), r.finalDestCode(), r.rton(), r.lineBkgNo(), r.issueDate());
    }

    private CreateMasterBlCommand.DescCommand toDescCommand(CreateMasterBlRequest.DescRequest r) {
        if (r == null) return null;
        return new CreateMasterBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2());
    }

    private UpdateMasterBlCommand.DescCommand toDescCommand(UpdateMasterBlRequest.DescRequest r) {
        if (r == null) return null;
        return new UpdateMasterBlCommand.DescCommand(r.marks(), r.description(), r.descClause1(), r.descClause2());
    }

    private List<CreateMasterBlCommand.DimCommand> toDimCommands(List<CreateMasterBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateMasterBlCommand.DimCommand(r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<UpdateMasterBlCommand.DimCommand> toDimCommandsU(List<UpdateMasterBlRequest.DimRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateMasterBlCommand.DimCommand(r.lengthCm(), r.widthCm(), r.heightCm(), r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<CreateMasterBlCommand.ScheduleLegCommand> toLegCommands(List<CreateMasterBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateMasterBlCommand.ScheduleLegCommand(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<UpdateMasterBlCommand.ScheduleLegCommand> toLegCommandsU(List<UpdateMasterBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateMasterBlCommand.ScheduleLegCommand(r.toCode(), r.byCarrier(), r.flightNo(), r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<CreateMasterBlCommand.AirChargeCommand> toChargeCommands(List<CreateMasterBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new CreateMasterBlCommand.AirChargeCommand(r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    private List<UpdateMasterBlCommand.AirChargeCommand> toChargeCommandsU(List<UpdateMasterBlRequest.AirChargeRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream().map(r -> new UpdateMasterBlCommand.AirChargeCommand(r.freightCode(), r.currencyCode(), r.per(), r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }
}
