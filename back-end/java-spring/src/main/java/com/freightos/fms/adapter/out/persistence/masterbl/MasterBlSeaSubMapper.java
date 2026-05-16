package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.LinerCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.TeamCode;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.freightos.common.util.VoMapper.mapOrNull;

/**
 * SEA Master B/L UPDATE 전용 sub-set 매퍼 (§6.37 + §6.49 ㉓).
 *
 * 3분기 분류:
 *  1) form 미보유 + DB 보호 필드 — setter 라인 제거 (예: masterBlId 자기 PK는 attached entity PK 변경 불가)
 *  2) form 편집 가능 — conditional setter {@code if (domain.getXxx() != null) jpa.setXxx(...)}
 *     → 무수정 조회→저장 시 dirty 미발생 (§6.37 PATCH 의미론)
 *  3) 별도 흐름 — desc 동기화는 {@link SeaMasterUpdatePersistenceAdapter#applyDescSync} 전담
 *
 * House {@code HouseBlDomainToJpaMapper.applySeaCommonFields} / {@code applySeaBlFields} 동등 패턴.
 */
@Component
public class MasterBlSeaSubMapper {

    /**
     * SEA Master 공통 본체 (master_bl) 필드 적용 — UPDATE 경로 전용.
     *
     * <p>분기:
     * <ul>
     *   <li>form 미보유(DB 보호): masterBlId(자기 PK) — setter 없음 (attached entity PK 변경 불가)
     *   <li>form 편집 가능: mblNo·masterRefNo·bound·jobDiv 외 모든 toolbar+panel 필드
     *       — {@code if (domain.getXxx() != null) jpa.setXxx(...)} conditional setter
     *   <li>shipperCode·consigneeCode·notifyCode: null이면 skip (§6.37 PATCH — assignParties null 가드와 연동)
     * </ul>
     */
    public void applyMasterSeaCommonFields(MasterBl domain, MasterBlJpaEntity jpa) {
        // jobDiv·bound: 본체 식별자, UPDATE 경로에서 변경 불가 → setter 제거
        if (domain.getMblNo() != null) jpa.setMblNo(mapOrNull(domain.getMblNo(), BlNumber::value));
        if (domain.getMasterRefNo() != null) jpa.setMasterRefNo(mapOrNull(domain.getMasterRefNo(), BlNumber::value));
        // shipper/consignee/notify: null이면 DB 기존 값 보호 (§6.37 PATCH)
        // address는 trim-aware compare로 DB의 trailing whitespace로 인한 가짜 dirty 차단 (§6.63)
        if (domain.getShipperCode() != null) {
            setStringIfChanged(jpa::getShipperCode,    jpa::setShipperCode,    mapOrNull(domain.getShipperCode(), CustomerCode::value));
            setAddressIfChangedTrim(jpa::getShipperAddress, jpa::setShipperAddress, mapOrNull(domain.getShipperCode(), CustomerCode::address));
        }
        if (domain.getConsigneeCode() != null) {
            setStringIfChanged(jpa::getConsigneeCode,    jpa::setConsigneeCode,    mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
            setAddressIfChangedTrim(jpa::getConsigneeAddress, jpa::setConsigneeAddress, mapOrNull(domain.getConsigneeCode(), CustomerCode::address));
        }
        if (domain.getNotifyCode() != null) {
            setStringIfChanged(jpa::getNotifyCode,    jpa::setNotifyCode,    mapOrNull(domain.getNotifyCode(), CustomerCode::value));
            setAddressIfChangedTrim(jpa::getNotifyAddress, jpa::setNotifyAddress, mapOrNull(domain.getNotifyCode(), CustomerCode::address));
        }
        if (domain.getPolCode() != null) jpa.setPolCode(mapOrNull(domain.getPolCode(), PortCode::value));
        if (domain.getPodCode() != null) jpa.setPodCode(mapOrNull(domain.getPodCode(), PortCode::value));
        if (domain.getEtd() != null) jpa.setEtd(mapOrNull(domain.getEtd(), BlDate::asString));
        if (domain.getEta() != null) jpa.setEta(mapOrNull(domain.getEta(), BlDate::asString));
        if (domain.getFreightTerm() != null) jpa.setFreightTerm(domain.getFreightTerm());
        if (domain.getOperatorCode() != null) jpa.setOperatorCode(mapOrNull(domain.getOperatorCode(), EmployeeCode::value));
        if (domain.getTeamCode() != null) jpa.setTeamCode(mapOrNull(domain.getTeamCode(), TeamCode::value));
        if (domain.getPkgQty() != null) jpa.setPkgQty(mapOrNull(domain.getPkgQty(), Quantity::count));
        if (domain.getPkgUnit() != null) jpa.setPkgUnit(domain.getPkgUnit());
        if (domain.getWeightUnit() != null) jpa.setWeightUnit(domain.getWeightUnit());
        if (domain.getGrossWeightKg() != null) jpa.setGrossWeightKg(mapOrNull(domain.getGrossWeightKg(), Weight::kg));
        if (domain.getCbm() != null) jpa.setCbm(mapOrNull(domain.getCbm(), Volume::cbm));
        if (domain.getSettlePartnerCode() != null) jpa.setSettlePartnerCode(mapOrNull(domain.getSettlePartnerCode(), CustomerCode::value));
        if (domain.getMainItemName() != null) jpa.setMainItemName(domain.getMainItemName());
        if (domain.getHsCode() != null) jpa.setHsCode(domain.getHsCode());
        if (domain.getShipmentType() != null) jpa.setShipmentType(domain.getShipmentType());
    }

    /**
     * SEA 확장 테이블 (master_bl_sea) 필드 적용 — UPDATE 경로 전용.
     *
     * <p>분기:
     * <ul>
     *   <li>form 편집 가능: loadType·linerCode·vesselVoyage·onboardDate·lineBkgNo·issueDate
     *       ·vesselNationality·serviceTerm·blType·porCode·finalDestCode·rton·remark
     *       — conditional setter {@code if (domain.getXxx() != null)}
     * </ul>
     */
    public void applyMasterSeaFields(MasterBlSea domain, MasterBlSeaJpaEntity jpa) {
        if (domain.getLoadType() != null) jpa.setLoadType(domain.getLoadType());
        if (domain.getLinerCode() != null) jpa.setLinerCode(mapOrNull(domain.getLinerCode(), LinerCode::value));
        if (domain.getVesselVoyage() != null) {
            jpa.setVesselCode(domain.getVesselVoyage().vesselCode());
            jpa.setVesselName(domain.getVesselVoyage().vesselName());
            jpa.setVoyageNo(domain.getVesselVoyage().voyageNo());
        }
        if (domain.getOnboardDate() != null) jpa.setOnboardDate(mapOrNull(domain.getOnboardDate(), BlDate::asString));
        if (domain.getLineBkgNo() != null) jpa.setLineBkgNo(mapOrNull(domain.getLineBkgNo(), BlNumber::value));
        if (domain.getIssueDate() != null) jpa.setIssueDate(mapOrNull(domain.getIssueDate(), BlDate::asString));
        if (domain.getVesselNationality() != null) jpa.setVesselNationality(domain.getVesselNationality());
        if (domain.getServiceTerm() != null) jpa.setServiceTerm(domain.getServiceTerm());
        if (domain.getBlType() != null) jpa.setBlType(domain.getBlType());
        if (domain.getPorCode() != null) jpa.setPorCode(mapOrNull(domain.getPorCode(), PortCode::value));
        if (domain.getFinalDestCode() != null) jpa.setFinalDestCode(mapOrNull(domain.getFinalDestCode(), PortCode::value));
        if (domain.getRton() != null) jpa.setRton(mapOrNull(domain.getRton(), Rton::ton));
        if (domain.getRemark() != null) jpa.setRemark(domain.getRemark());
    }

    /**
     * SEA desc (master_bl_sea_desc) 필드 적용.
     * FK(sea) 설정 포함. House {@code HouseBlDocMapper.applySeaDescFields} 동등 패턴.
     */
    public void applySeaDescFields(MasterBlDesc domain, MasterBlSeaDescJpaEntity jpa, MasterBlSeaJpaEntity seaJpa) {
        jpa.setSea(seaJpa);
        jpa.setMarks(domain.getMarks());
        jpa.setDescription(domain.getDescription());
        jpa.setDescClause1(domain.getDescClause1());
        jpa.setDescClause2(domain.getDescClause2());
    }

    /**
     * 값이 다를 때만 setter 호출 — Hibernate dirty-checking이 같은 값에 대해 dirty 표기하지 않도록
     * 명시적으로 비교. 일반 String 비교 (trim 없음).
     */
    private static void setStringIfChanged(Supplier<String> getter, Consumer<String> setter, String newValue) {
        if (!Objects.equals(getter.get(), newValue)) setter.accept(newValue);
    }

    /**
     * Address 류 (free-text) 컬럼 전용 — trim-aware compare. DB의 trailing/leading whitespace가
     * 남아 있을 때 FE submit의 trim과 round-trip 차이로 발생하던 가짜 dirty를 차단 (§6.63).
     * blank/whitespace-only는 null과 동등 처리.
     */
    private static void setAddressIfChangedTrim(Supplier<String> getter, Consumer<String> setter, String newValue) {
        String current = normalizeTrim(getter.get());
        String next    = normalizeTrim(newValue);
        if (!Objects.equals(current, next)) setter.accept(newValue);
    }

    private static String normalizeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
