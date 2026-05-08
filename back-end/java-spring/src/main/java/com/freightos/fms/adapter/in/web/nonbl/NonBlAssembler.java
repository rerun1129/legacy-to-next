package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.model.PagedResult;
import com.freightos.fms.adapter.in.web.housebl.HouseBlAssembler;
import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlDetailResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlSummaryResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.SearchNonBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import org.springframework.stereotype.Component;

/**
 * Request DTO → Command 변환 및 Projection → Response DTO 변환 담당.
 * Create/Update 변환은 HouseBlAssembler에 위임하고 jobDiv=NON_BL을 강제 오버라이드한다.
 */
@Component
public class NonBlAssembler {

    private final HouseBlAssembler houseBlAssembler;

    public NonBlAssembler(HouseBlAssembler houseBlAssembler) {
        this.houseBlAssembler = houseBlAssembler;
    }

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

    /**
     * HouseBlAssembler에 위임 후 jobDiv=NON_BL로 강제한다.
     * Non B/L 엔드포인트는 항상 jobDiv가 NON_BL이어야 하므로 클라이언트 입력값을 무시한다.
     */
    public CreateHouseBlCommand toCreateCommand(CreateHouseBlRequest req) {
        CreateHouseBlCommand base = houseBlAssembler.toCreateCommand(req);
        return new CreateHouseBlCommand(
                "NON_BL",
                base.bound(),
                base.hblNo(),
                base.shipmentType(),
                base.freightTerm(),
                base.shipperCode(),
                base.shipperAddress(),
                base.consigneeCode(),
                base.consigneeAddress(),
                base.notifyCode(),
                base.notifyAddress(),
                base.docPartnerCode(),
                base.docPartnerAddress(),
                base.settlePartnerCode(),
                base.polCode(),
                base.podCode(),
                base.etd(),
                base.eta(),
                base.pkgQty(),
                base.pkgUnit(),
                base.grossWeightKg(),
                base.cbm(),
                base.actualCustomerCode(),
                base.operatorCode(),
                base.teamCode(),
                base.salesManCode(),
                base.masterBlId(),
                base.incoterms(),
                base.salesClass(),
                base.mainItemName(),
                base.hsCode(),
                base.mblNo(),
                base.masterRefNo(),
                base.workDivision(),
                base.originalBlRef(),
                base.linerCode(),
                base.linerName(),
                base.vesselName(),
                base.voyageNo(),
                base.finalDestCode(),
                base.finalDestName(),
                base.finalEta(),
                base.volumeWeightKg(),
                base.rton(),
                base.seaDetail(),
                base.desc(),
                base.dims(),
                base.containers(),
                base.scheduleLegs(),
                base.truckOrders(),
                base.airCharges()
        );
    }

    /**
     * HouseBlAssembler에 위임 후 jobDiv=NON_BL로 강제한다.
     */
    public UpdateHouseBlCommand toUpdateCommand(UpdateHouseBlRequest req) {
        UpdateHouseBlCommand base = houseBlAssembler.toUpdateCommand(req);
        return new UpdateHouseBlCommand(
                "NON_BL",
                base.bound(),
                base.hblNo(),
                base.shipmentType(),
                base.freightTerm(),
                base.shipperCode(),
                base.shipperAddress(),
                base.consigneeCode(),
                base.consigneeAddress(),
                base.notifyCode(),
                base.notifyAddress(),
                base.docPartnerCode(),
                base.docPartnerAddress(),
                base.settlePartnerCode(),
                base.polCode(),
                base.podCode(),
                base.etd(),
                base.eta(),
                base.pkgQty(),
                base.pkgUnit(),
                base.grossWeightKg(),
                base.cbm(),
                base.actualCustomerCode(),
                base.operatorCode(),
                base.teamCode(),
                base.salesManCode(),
                base.masterBlId(),
                base.incoterms(),
                base.salesClass(),
                base.mainItemName(),
                base.hsCode(),
                base.mblNo(),
                base.masterRefNo(),
                base.workDivision(),
                base.originalBlRef(),
                base.linerCode(),
                base.linerName(),
                base.vesselName(),
                base.voyageNo(),
                base.finalDestCode(),
                base.finalDestName(),
                base.finalEta(),
                base.volumeWeightKg(),
                base.rton(),
                base.seaDetail(),
                base.desc(),
                base.dims(),
                base.containers(),
                base.scheduleLegs(),
                base.truckOrders(),
                base.airCharges()
        );
    }
}
