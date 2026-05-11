package com.freightos.fms.application.nonbl.projection;

import com.freightos.common.util.Nullables;
import com.freightos.common.util.VoMapper;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.TeamCode;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlContainer;
import com.freightos.fms.domain.housebl.enums.ContainerType;
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
        String bound,
        String workDivision,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
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
        String volumeDivisor,
        String originalBlRef,
        BigDecimal rton,
        BigDecimal volumeWtKg,
        Integer pkgQty,
        String pkgUnit,
        String weightUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        String operatorCode,
        String salesManCode,
        String teamCode,
        String salesClass,
        String mainItemName,
        String hsCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String remark,
        List<NonBlContainerView> containers,
        List<NonBlDimView> dims
) {

    public static NonBlDetailResult from(HouseBlNonBl nonBl) {
        return new NonBlDetailResult(
                nonBl.getId(),
                VoMapper.mapOrNull(nonBl.getHblNo(), BlNumber::value),
                Nullables.mapOrNull(nonBl.getBound(), Bound::name),
                Nullables.mapOrNull(nonBl.getWorkDivision(), HouseBlNonBl.WorkDivision::name),
                VoMapper.mapOrNull(nonBl.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(nonBl.getNotifyCode(), CustomerCode::value),
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
                Nullables.mapOrNull(nonBl.getVolumeDivisor(), Enum::name),
                VoMapper.mapOrNull(nonBl.getOriginalBlRef(), BlNumber::value),
                VoMapper.mapOrNull(nonBl.getRton(), Rton::ton),
                VoMapper.mapOrNull(nonBl.getVolumeWtKg(), Weight::kg),
                VoMapper.mapOrNull(nonBl.getPkgQty(), Quantity::count),
                nonBl.getPkgUnit(),
                Nullables.mapOrNull(nonBl.getWeightUnit(), Enum::name),
                VoMapper.mapOrNull(nonBl.getGrossWeightKg(), Weight::kg),
                VoMapper.mapOrNull(nonBl.getCbm(), Volume::cbm),
                VoMapper.mapOrNull(nonBl.getOperatorCode(), EmployeeCode::value),
                VoMapper.mapOrNull(nonBl.getSalesManCode(), EmployeeCode::value),
                VoMapper.mapOrNull(nonBl.getTeamCode(), TeamCode::value),
                Nullables.mapOrNull(nonBl.getSalesClass(), Enum::name),
                nonBl.getMainItemName(),
                nonBl.getHsCode(),
                nonBl.getCreatedAt(),
                nonBl.getUpdatedAt(),
                nonBl.getRemark(),
                nonBl.getContainers() == null ? List.of() : nonBl.getContainers().stream().map(NonBlContainerView::from).toList(),
                nonBl.getDims() == null ? List.of() : nonBl.getDims().stream().map(NonBlDimView::from).toList()
        );
    }

    public record NonBlContainerView(
            Long id,
            String containerNo,
            String containerType,
            String sealNo1,
            String sealNo2,
            String sealNo3,
            Integer pkgQty,
            String pkgUnit,
            BigDecimal grossWeightKg,
            BigDecimal cbm
    ) {
        public static NonBlContainerView from(HouseBlContainer c) {
            return new NonBlContainerView(
                    c.getId(),
                    VoMapper.mapOrNull(c.getContainerNo(), ContainerNumber::value),
                    Nullables.mapOrNull(c.getContainerType(), ContainerType::getCode),
                    VoMapper.mapOrNull(c.getSealNo1(), SealNumber::value),
                    VoMapper.mapOrNull(c.getSealNo2(), SealNumber::value),
                    VoMapper.mapOrNull(c.getSealNo3(), SealNumber::value),
                    VoMapper.mapOrNull(c.getPkgQty(), Quantity::count),
                    c.getPkgUnit(),
                    VoMapper.mapOrNull(c.getGrossWeightKg(), Weight::kg),
                    VoMapper.mapOrNull(c.getCbm(), Volume::cbm)
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

}
