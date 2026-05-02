package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.UpdateMasterBlRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CargoSummary;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import org.springframework.stereotype.Component;

@Component
public class MasterBlAssembler {

    public PagedResult<MasterBlSummaryResponse> toSummaryPage(PagedResult<MasterBl> source) {
        return source.map(MasterBlSummaryResponse::from);
    }

    public MasterBlDetailResponse toDetail(MasterBlDetail source) {
        return MasterBlDetailResponse.from(source);
    }

    /** 신규 등록 요청 DTO → 도메인 엔티티 생성. jobDiv로 SEA/AIR 구체 타입 분기. */
    public MasterBl toEntity(CreateMasterBlRequest req) {
        MasterBl entity = req.jobDiv() == MasterBlJobDiv.SEA
                ? MasterBlSea.create(req.bound())
                : MasterBlAir.create(req.bound());

        entity.assignMblNo(
                BlNumber.of(req.mblNo()),
                BlNumber.of(req.masterRefNo())
        );
        entity.assignParties(
                CustomerCode.of(req.shipperCode()),
                CustomerCode.of(req.consigneeCode()),
                null
        );
        entity.updateSchedule(
                PortCode.of(req.polCode()),
                PortCode.of(req.podCode()),
                BlDate.of(req.etd()),
                BlDate.of(req.eta())
        );
        entity.updateFreightAndOperator(
                req.freightTerm(),
                EmployeeCode.of(req.operatorCode()),
                null
        );
        entity.updateCargoSummary(new CargoSummary(
                Quantity.of(req.pkgQty()),
                null,
                Weight.of(req.grossWeightKg()),
                Volume.of(req.cbm())
        ));
        return entity;
    }

    /**
     * 수정 요청 DTO를 기존 엔티티에 적용한다. null 필드는 기존 값을 그대로 유지한다.
     * null 여부로 수정 의도를 판별하므로, 명시된 필드만 덮어쓴다.
     */
    public void applyToEntity(UpdateMasterBlRequest req, MasterBl entity) {
        entity.assignMblNo(
                req.mblNo()       != null ? BlNumber.of(req.mblNo())       : entity.getMblNo(),
                req.masterRefNo() != null ? BlNumber.of(req.masterRefNo()) : entity.getMasterRefNo()
        );
        entity.assignParties(
                req.shipperCode()   != null ? CustomerCode.of(req.shipperCode())   : entity.getShipperCode(),
                req.consigneeCode() != null ? CustomerCode.of(req.consigneeCode()) : entity.getConsigneeCode(),
                entity.getNotifyCode()
        );
        entity.updateSchedule(
                req.polCode() != null ? PortCode.of(req.polCode()) : entity.getPolCode(),
                req.podCode() != null ? PortCode.of(req.podCode()) : entity.getPodCode(),
                req.etd()     != null ? BlDate.of(req.etd())       : entity.getEtd(),
                req.eta()     != null ? BlDate.of(req.eta())       : entity.getEta()
        );
        entity.updateFreightAndOperator(
                req.freightTerm()   != null ? req.freightTerm()                    : entity.getFreightTerm(),
                req.operatorCode()  != null ? EmployeeCode.of(req.operatorCode())  : entity.getOperatorCode(),
                entity.getTeamCode()
        );
        entity.updateCargoSummary(new CargoSummary(
                req.pkgQty()        != null ? Quantity.of(req.pkgQty())            : entity.getPkgQty(),
                entity.getPkgUnit(),
                req.grossWeightKg() != null ? Weight.of(req.grossWeightKg())       : entity.getGrossWeightKg(),
                req.cbm()           != null ? Volume.of(req.cbm())                 : entity.getCbm()
        ));
    }
}
