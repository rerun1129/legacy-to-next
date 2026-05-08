package com.freightos.fms.application.nonbl.projection;

import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.MblNo;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.TeamCode;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlDim;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Non B/L 단건 조회·수정 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * application 경계에서 enum 필드를 String으로 통일한다.
 */
public record NonBlDetailResult(
        Long id,
        String hblNo,
        String jobDiv,
        String bound,
        String workDivision,
        String shipmentType,
        String freightTerm,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String docPartnerCode,
        String settlePartnerCode,
        String actualCustomerCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String linerCode,
        String linerName,
        String vesselName,
        String voyageNo,
        String finalDestCode,
        String finalDestName,
        String finalEta,
        String originalBlRef,
        BigDecimal rton,
        BigDecimal volumeWtKg,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String operatorCode,
        String salesManCode,
        String teamCode,
        String mblNo,
        String masterRefNo,
        Long masterBlId,
        String mainItemName,
        String hsCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<NonBlContainerView> containers,
        List<NonBlDimView> dims,
        NonBlDescView desc
) {

    public static NonBlDetailResult from(HouseBlNonBl nonBl) {
        return new NonBlDetailResult(
                nonBl.getId(),
                VoMapper.mapOrNull(nonBl.getHblNo(), BlNumber::value),
                Nullables.mapOrNull(nonBl.getJobDiv(), Enum::name),
                Nullables.mapOrNull(nonBl.getBound(), Bound::name),
                Nullables.mapOrNull(nonBl.getWorkDivision(), HouseBlNonBl.WorkDivision::name),
                Nullables.mapOrNull(nonBl.getShipmentType(), Enum::name),
                Nullables.mapOrNull(nonBl.getFreightTerm(), Enum::name),
                VoMapper.mapOrNull(nonBl.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getDocPartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getSettlePartnerCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getActualCustomerCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getPolCode(), PortCode::value),
                VoMapper.mapOrNull(nonBl.getPodCode(), PortCode::value),
                VoMapper.mapOrNull(nonBl.getEtd(), BlDate::asString),
                VoMapper.mapOrNull(nonBl.getEta(), BlDate::asString),
                nonBl.getLinerCode(),
                nonBl.getLinerName(),
                nonBl.getVesselName(),
                nonBl.getVoyageNo(),
                nonBl.getFinalDestCode(),
                nonBl.getFinalDestName(),
                nonBl.getFinalEta(),
                VoMapper.mapOrNull(nonBl.getOriginalBlRef(), BlNumber::value),
                VoMapper.mapOrNull(nonBl.getRton(), Rton::ton),
                VoMapper.mapOrNull(nonBl.getVolumeWtKg(), Weight::kg),
                VoMapper.mapOrNull(nonBl.getPkgQty(), Quantity::count),
                Nullables.mapOrNull(nonBl.getPkgUnit(), Enum::name),
                VoMapper.mapOrNull(nonBl.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(nonBl.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(nonBl.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(nonBl.getSalesManCode(), EmployeeCode::value),
                VoMapper.mapOrNull(nonBl.getTeamCode(), TeamCode::value),
                VoMapper.mapOrNull(nonBl.getMblNo(), MblNo::value),
                nonBl.getMasterRefNo(),
                nonBl.getMasterBlId(),
                nonBl.getMainItemName(),
                nonBl.getHsCode(),
                nonBl.getCreatedAt(),
                nonBl.getUpdatedAt(),
                nonBl.getContainers() == null ? List.of() : nonBl.getContainers().stream().map(NonBlContainerView::from).toList(),
                nonBl.getDims() == null ? List.of() : nonBl.getDims().stream().map(NonBlDimView::from).toList(),
                nonBl.getDesc() == null ? null : NonBlDescView.from(nonBl.getDesc())
        );
    }

    public record NonBlContainerView(
            Long id,
            int seq,
            String containerNo,
            String containerType,
            Integer lengthFeet,
            String sealNo1,
            String sealNo2,
            String sealNo3,
            String sealNo4,
            String sealNo5,
            String sealNo6,
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            BigDecimal netWeightKg,
            BigDecimal vgmKg,
            BigDecimal cbm,
            boolean isSoc
    ) {
        public static NonBlContainerView from(HouseBlContainer c) {
            return new NonBlContainerView(
                    c.getId(),
                    c.getSeq(),
                    VoMapper.mapOrNull(c.getContainerNo(), ContainerNumber::value),
                    Nullables.mapOrNull(c.getContainerType(), Enum::name),
                    c.getLengthFeet(),
                    VoMapper.mapOrNull(c.getSealNo1(), SealNumber::value),
                    VoMapper.mapOrNull(c.getSealNo2(), SealNumber::value),
                    VoMapper.mapOrNull(c.getSealNo3(), SealNumber::value),
                    VoMapper.mapOrNull(c.getSealNo4(), SealNumber::value),
                    VoMapper.mapOrNull(c.getSealNo5(), SealNumber::value),
                    VoMapper.mapOrNull(c.getSealNo6(), SealNumber::value),
                    VoMapper.mapOrNull(c.getPkgQty(), Quantity::count),
                    c.getPkgUnit(),
                    VoMapper.mapOrNull(c.getGrossWeightKg(), Weight::kg),
                    VoMapper.mapOrNull(c.getNetWeightKg(), Weight::kg),
                    VoMapper.mapOrNull(c.getVgmKg(), Weight::kg),
                    VoMapper.mapOrNull(c.getCbm(), Volume::cbm),
                    c.isSoc()
            );
        }
    }

    public record NonBlDimView(
            Long id,
            BigDecimal lengthCm,
            BigDecimal widthCm,
            BigDecimal heightCm,
            Integer quantity,
            BigDecimal cbm,
            BigDecimal volumeWeightKg
    ) {
        public static NonBlDimView from(HouseBlDim d) {
            return new NonBlDimView(
                    d.getId(),
                    d.getLengthCm(),
                    d.getWidthCm(),
                    d.getHeightCm(),
                    d.getQuantity(),
                    d.getCbm(),
                    d.getVolumeWeightKg()
            );
        }
    }

    public record NonBlDescView(
            String marks,
            String description,
            String remark
    ) {
        public static NonBlDescView from(HouseBlDesc desc) {
            return new NonBlDescView(
                    desc.getMarks(),
                    desc.getDescription(),
                    desc.getRemark()
            );
        }
    }
}
