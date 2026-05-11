package com.freightos.fms.adapter.out.persistence.nonbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA ORM 엔티티 — House B/L Non-B/L 확장.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "house_bl_non_bl")
@Getter
@NoArgsConstructor
public class HouseBlNonBlJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_non_bl_id", updatable = false, nullable = false)
    private Long houseBlNonBlId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true)
    private HouseBlJpaEntity houseBl;

    @Column(name = "work_division", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private HouseBlNonBl.WorkDivision workDivision;

    @Column(name = "original_bl_ref", length = 50)
    private String originalBlRef;

    @Column(name = "rton", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal rton;

    @Column(name = "volume_wt_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWtKg;

    @Column(name = "liner_code", length = 10)
    private String linerCode;

    @Column(name = "liner_name", length = 100)
    private String linerName;

    @Column(name = "vessel_name", length = 100)
    private String vesselName;

    @Column(name = "voyage_no", length = 20)
    private String voyageNo;

    @Column(name = "final_dest_code", length = 5)
    private String finalDestCode;

    @Column(name = "final_dest_name", length = 100)
    private String finalDestName;

    @Column(name = "final_eta", length = 8)
    private String finalEta;

    @Column(name = "volume_divisor", length = 10)
    @Enumerated(EnumType.STRING)
    private VolumeDivisor volumeDivisor;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    // NON_BL 전용 컨테이너 컬렉션 — house_bl_nonbl_container.house_bl_non_bl_id FK 소유
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "house_bl_non_bl_id", nullable = false, updatable = false)
    private List<HouseBlNonBlContainerJpaEntity> containers = new ArrayList<>();

    // NON_BL 전용 치수 명세 — house_bl_nonbl_dim.house_bl_non_bl_id FK 소유
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "house_bl_non_bl_id", nullable = false, updatable = false)
    private List<HouseBlNonBlDimJpaEntity> dims = new ArrayList<>();

    public void setHouseBl(HouseBlJpaEntity v)                   { this.houseBl = v; }
    public void setWorkDivision(HouseBlNonBl.WorkDivision v)     { this.workDivision = v; }
    public void setOriginalBlRef(String v)                        { this.originalBlRef = v; }
    public void setRton(BigDecimal v)                             { this.rton = v; }
    public void setVolumeWtKg(BigDecimal v)                      { this.volumeWtKg = v; }
    public void setLinerCode(String v)                            { this.linerCode = v; }
    public void setLinerName(String v)                            { this.linerName = v; }
    public void setVesselName(String v)                           { this.vesselName = v; }
    public void setVoyageNo(String v)                             { this.voyageNo = v; }
    public void setFinalDestCode(String v)                        { this.finalDestCode = v; }
    public void setFinalDestName(String v)                        { this.finalDestName = v; }
    public void setFinalEta(String v)                             { this.finalEta = v; }
    public void setVolumeDivisor(VolumeDivisor v)                  { this.volumeDivisor = v; }
    public void setRemark(String v)                               { this.remark = v; }

    /**
     * NON_BL 컨테이너 merge-by-id.
     * incoming id가 기존 영속 엔티티와 일치하면 필드 mutate(UPDATE), 없으면 신규 추가(INSERT).
     * orphanRemoval이 제거된 엔티티를 자동 DELETE한다.
     */
    public void mergeContainers(List<HouseBlNonBlContainerJpaEntity> incoming) {
        Map<Long, HouseBlNonBlContainerJpaEntity> existingById = new HashMap<>();
        for (HouseBlNonBlContainerJpaEntity e : this.containers) {
            if (e.getHouseBlNonBlContainerId() != null) existingById.put(e.getHouseBlNonBlContainerId(), e);
        }
        List<HouseBlNonBlContainerJpaEntity> merged = new ArrayList<>();
        for (HouseBlNonBlContainerJpaEntity inc : incoming) {
            if (inc.getHouseBlNonBlContainerId() != null && existingById.containsKey(inc.getHouseBlNonBlContainerId())) {
                HouseBlNonBlContainerJpaEntity existing = existingById.get(inc.getHouseBlNonBlContainerId());
                copyContainerFields(inc, existing);
                merged.add(existing);
            } else {
                merged.add(inc);
            }
        }
        this.containers.clear();
        this.containers.addAll(merged);
    }

    public List<HouseBlNonBlContainerJpaEntity> getContainers() { return containers; }

    public List<HouseBlNonBlDimJpaEntity> getDims() { return dims; }

    /**
     * NON_BL DIM merge-by-id.
     * incoming id가 기존 영속 엔티티와 일치하면 필드 mutate(UPDATE), 없으면 신규 추가(INSERT).
     * orphanRemoval이 제거된 엔티티를 자동 DELETE한다.
     */
    public void mergeDims(List<HouseBlNonBlDimJpaEntity> incoming) {
        Map<Long, HouseBlNonBlDimJpaEntity> existingById = new HashMap<>();
        for (HouseBlNonBlDimJpaEntity e : this.dims) {
            if (e.getHouseBlNonBlDimId() != null) existingById.put(e.getHouseBlNonBlDimId(), e);
        }
        List<HouseBlNonBlDimJpaEntity> merged = new ArrayList<>();
        for (HouseBlNonBlDimJpaEntity inc : incoming) {
            if (inc.getHouseBlNonBlDimId() != null && existingById.containsKey(inc.getHouseBlNonBlDimId())) {
                HouseBlNonBlDimJpaEntity existing = existingById.get(inc.getHouseBlNonBlDimId());
                copyDimFields(inc, existing);
                merged.add(existing);
            } else {
                merged.add(inc);
            }
        }
        this.dims.clear();
        this.dims.addAll(merged);
    }

    private void copyDimFields(HouseBlNonBlDimJpaEntity src, HouseBlNonBlDimJpaEntity dst) {
        dst.setLengthCm(src.getLengthCm());
        dst.setWidthCm(src.getWidthCm());
        dst.setHeightCm(src.getHeightCm());
        dst.setQuantity(src.getQuantity());
        dst.setCbm(src.getCbm());
        dst.setVolumeWeightKg(src.getVolumeWeightKg());
    }

    private void copyContainerFields(HouseBlNonBlContainerJpaEntity src, HouseBlNonBlContainerJpaEntity dst) {
        dst.setContainerNo(src.getContainerNo());
        dst.setContainerType(src.getContainerType());
        dst.setSealNo1(src.getSealNo1());
        dst.setSealNo2(src.getSealNo2());
        dst.setSealNo3(src.getSealNo3());
        dst.setPkgQty(src.getPkgQty());
        dst.setPkgUnit(src.getPkgUnit());
        dst.setGrossWeightKg(src.getGrossWeightKg());
        dst.setCbm(src.getCbm());
        // lengthFeet, sealNo4-6, netWeightKg, vgmKg, isSoc, seq는 NonBl form 미사용 필드.
        // 기존 영속 엔티티 값을 그대로 유지하여 DB 덮어쓰기(데이터 손실) 방지.
    }
}
