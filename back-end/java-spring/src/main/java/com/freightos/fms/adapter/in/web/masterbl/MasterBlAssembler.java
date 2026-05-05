package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.UpdateMasterBlRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlAirCharge;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlDim;
import com.freightos.fms.domain.masterbl.entity.MasterBlScheduleLeg;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CargoSummary;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.LinerCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

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

        entity.assignMblNo(BlNumber.of(req.mblNo()), BlNumber.of(req.masterRefNo()));
        entity.assignParties(
                CustomerCode.of(req.shipperCode()),
                CustomerCode.of(req.consigneeCode()),
                CustomerCode.of(req.notifyCode())
        );
        entity.updateSchedule(
                PortCode.of(req.polCode()), PortCode.of(req.podCode()),
                BlDate.of(req.etd()), BlDate.of(req.eta())
        );
        entity.updateFreightAndOperator(req.freightTerm(), EmployeeCode.of(req.operatorCode()), null);
        entity.updateCargoSummary(new CargoSummary(
                Quantity.of(req.pkgQty()), WeightUnit.fromCode(req.pkgUnit()),
                Weight.of(req.grossWeightKg()), Volume.of(req.cbm())
        ));
        if (req.mainItemName() != null || req.hsCode() != null) {
            entity.updateTradeInfo(req.mainItemName(), req.hsCode());
        }
        if (req.settlePartnerCode() != null) {
            entity.assignSettlePartner(CustomerCode.of(req.settlePartnerCode()));
        }

        if (req.seaDetail() != null) {
            applySeaCreate(entity, req.seaDetail());
        }
        applySubEntities(entity,
                toDescParams(req.desc()),
                toDimParamsList(req.dims()),
                toLegParamsList(req.scheduleLegs()),
                toChargeParamsList(req.airCharges())
        );
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
                req.notifyCode()    != null ? CustomerCode.of(req.notifyCode())    : entity.getNotifyCode()
        );
        entity.updateSchedule(
                req.polCode() != null ? PortCode.of(req.polCode()) : entity.getPolCode(),
                req.podCode() != null ? PortCode.of(req.podCode()) : entity.getPodCode(),
                req.etd()     != null ? BlDate.of(req.etd())       : entity.getEtd(),
                req.eta()     != null ? BlDate.of(req.eta())       : entity.getEta()
        );
        entity.updateFreightAndOperator(
                req.freightTerm()  != null ? req.freightTerm()                   : entity.getFreightTerm(),
                req.operatorCode() != null ? EmployeeCode.of(req.operatorCode()) : entity.getOperatorCode(),
                entity.getTeamCode()
        );
        entity.updateCargoSummary(new CargoSummary(
                req.pkgQty()        != null ? Quantity.of(req.pkgQty())          : entity.getPkgQty(),
                req.pkgUnit()       != null ? WeightUnit.fromCode(req.pkgUnit()) : entity.getPkgUnit(),
                req.grossWeightKg() != null ? Weight.of(req.grossWeightKg())     : entity.getGrossWeightKg(),
                req.cbm()           != null ? Volume.of(req.cbm())               : entity.getCbm()
        ));
        if (req.mainItemName() != null || req.hsCode() != null) {
            entity.updateTradeInfo(
                    req.mainItemName() != null ? req.mainItemName() : entity.getMainItemName(),
                    req.hsCode()       != null ? req.hsCode()       : entity.getHsCode()
            );
        }
        if (req.settlePartnerCode() != null) {
            entity.assignSettlePartner(CustomerCode.of(req.settlePartnerCode()));
        }

        if (req.seaDetail() != null) {
            applySeaUpdate(entity, req.seaDetail());
        }
        applySubEntities(entity,
                toDescParams(req.desc()),
                toDimParamsListU(req.dims()),
                toLegParamsListU(req.scheduleLegs()),
                toChargeParamsListU(req.airCharges())
        );
    }

    // ── SEA 확장 필드 매핑 ────────────────────────────────────────────

    private void applySeaCreate(MasterBl entity, CreateMasterBlRequest.SeaDetailRequest s) {
        if (!(entity instanceof MasterBlSea sea)) return;
        sea.updateSeaFields(
                s.loadType() != null ? LoadType.valueOf(s.loadType()) : null,
                LinerCode.of(s.linerCode()),
                VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo()),
                BlDate.of(s.onboardDate()), BlNumber.of(s.lineBkgNo()), BlDate.of(s.issueDate())
        );
        applySeaCommon(sea, s.vesselNationality(), s.weightUnit(), s.serviceTerm(),
                s.blType(), s.porCode(), s.finalDestCode(), s.rton());
    }

    private void applySeaUpdate(MasterBl entity, UpdateMasterBlRequest.SeaDetailRequest s) {
        if (!(entity instanceof MasterBlSea sea)) return;
        sea.updateSeaFields(
                s.loadType() != null ? LoadType.valueOf(s.loadType()) : sea.getLoadType(),
                s.linerCode() != null ? LinerCode.of(s.linerCode()) : sea.getLinerCode(),
                s.vesselName() != null
                        ? VesselVoyage.of(s.vesselCode(), s.vesselName(), s.voyageNo())
                        : sea.getVesselVoyage(),
                s.onboardDate() != null ? BlDate.of(s.onboardDate()) : sea.getOnboardDate(),
                s.lineBkgNo() != null ? BlNumber.of(s.lineBkgNo()) : sea.getLineBkgNo(),
                s.issueDate() != null ? BlDate.of(s.issueDate()) : sea.getIssueDate()
        );
        applySeaCommon(sea, s.vesselNationality(), s.weightUnit(), s.serviceTerm(),
                s.blType(), s.porCode(), s.finalDestCode(), s.rton());
    }

    private void applySeaCommon(MasterBlSea sea, String vesselNationality, String weightUnit,
                                String serviceTerm, String blType,
                                String porCode, String finalDestCode, BigDecimal rton) {
        if (vesselNationality != null) sea.updateVesselNationality(vesselNationality);
        if (weightUnit != null)        sea.updateWeightUnit(WeightUnit.fromCode(weightUnit));
        if (serviceTerm != null)       sea.updateServiceTerm(ServiceTerm.fromLabel(serviceTerm));
        if (blType != null)            sea.updateBlType(BlType.valueOf(blType));
        if (porCode != null || finalDestCode != null) {
            sea.updateRoute(PortCode.of(porCode), PortCode.of(finalDestCode));
        }
        if (rton != null)              sea.updateRton(Rton.of(rton));
    }

    // ── Sub 엔티티 공통 파라미터 변환 ─────────────────────────────────

    /** Desc 파라미터: [marks, description, descClause1, descClause2, remark] */
    private String[] toDescParams(CreateMasterBlRequest.DescRequest r) {
        if (r == null) return null;
        return new String[]{ r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark() };
    }

    private String[] toDescParams(UpdateMasterBlRequest.DescRequest r) {
        if (r == null) return null;
        return new String[]{ r.marks(), r.description(), r.descClause1(), r.descClause2(), r.remark() };
    }

    /** Dim 파라미터: [lengthCm, widthCm, heightCm, quantity, cbm, volumeWeightKg] */
    private record DimParams(BigDecimal l, BigDecimal w, BigDecimal h,
                              Integer qty, BigDecimal cbm, BigDecimal vwKg) {}

    private List<DimParams> toDimParamsList(List<CreateMasterBlRequest.DimRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();
        return reqs.stream().map(r -> new DimParams(r.lengthCm(), r.widthCm(), r.heightCm(),
                r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    private List<DimParams> toDimParamsListU(List<UpdateMasterBlRequest.DimRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();
        return reqs.stream().map(r -> new DimParams(r.lengthCm(), r.widthCm(), r.heightCm(),
                r.quantity(), r.cbm(), r.volumeWeightKg())).toList();
    }

    /** ScheduleLeg 파라미터. */
    private record LegParams(String toCode, String byCarrier, String flightNo,
                              String onBoardDt, String onBoardTm,
                              String arrivalDt, String arrivalTm) {}

    private List<LegParams> toLegParamsList(List<CreateMasterBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();
        return reqs.stream().map(r -> new LegParams(r.toCode(), r.byCarrier(), r.flightNo(),
                r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    private List<LegParams> toLegParamsListU(List<UpdateMasterBlRequest.ScheduleLegRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();
        return reqs.stream().map(r -> new LegParams(r.toCode(), r.byCarrier(), r.flightNo(),
                r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm())).toList();
    }

    /** AirCharge 파라미터. */
    private record ChargeParams(String freightCode, String currencyCode, String per,
                                 String freightTerm, BigDecimal grossWt, String rateClass,
                                 BigDecimal chargeWt, BigDecimal rate) {}

    private List<ChargeParams> toChargeParamsList(List<CreateMasterBlRequest.AirChargeRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();
        return reqs.stream().map(r -> new ChargeParams(r.freightCode(), r.currencyCode(), r.per(),
                r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    private List<ChargeParams> toChargeParamsListU(List<UpdateMasterBlRequest.AirChargeRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) return List.of();
        return reqs.stream().map(r -> new ChargeParams(r.freightCode(), r.currencyCode(), r.per(),
                r.freightTerm(), r.grossWeightKg(), r.rateClass(), r.chargeWeightKg(), r.rate())).toList();
    }

    // ── Sub 엔티티 실제 생성 ──────────────────────────────────────────

    private void applySubEntities(MasterBl entity, String[] descParams,
                                  List<DimParams> dimParams, List<LegParams> legParams,
                                  List<ChargeParams> chargeParams) {
        if (descParams != null) {
            MasterBlDesc desc = MasterBlDesc.create(null);
            desc.updateContent(descParams[0], descParams[1],
                    DescClause1.fromCode(descParams[2]), DescClause2.fromCode(descParams[3]),
                    descParams[4]);
            entity.initDesc(desc);
        }
        if (!dimParams.isEmpty()) {
            entity.initDims(dimParams.stream()
                    .map(r -> MasterBlDim.create(null, r.l(), r.w(), r.h(), r.qty(), r.cbm(), r.vwKg()))
                    .toList());
        }
        if (!legParams.isEmpty()) {
            entity.initScheduleLegs(legParams.stream().map(r -> {
                MasterBlScheduleLeg leg = MasterBlScheduleLeg.create(null, r.toCode(), r.onBoardDt(), r.arrivalDt());
                leg.updateDetails(r.toCode(), r.byCarrier(), r.flightNo(),
                        r.onBoardDt(), r.onBoardTm(), r.arrivalDt(), r.arrivalTm());
                return leg;
            }).toList());
        }
        if (!chargeParams.isEmpty()) {
            entity.initAirCharges(chargeParams.stream().map(r -> {
                MasterBlAirCharge charge = MasterBlAirCharge.create(null);
                charge.updateDetails(new MasterBlAirCharge.Details(
                        r.freightCode(), CurrencyCode.of(r.currencyCode()), Per.fromCode(r.per()),
                        r.freightTerm() != null ? FreightTerm.valueOf(r.freightTerm()) : null,
                        Weight.of(r.grossWt()), RateClass.fromCode(r.rateClass()),
                        Weight.of(r.chargeWt()), r.rate()
                ));
                return charge;
            }).toList());
        }
    }
}
