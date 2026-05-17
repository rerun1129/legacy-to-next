package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicUpdate;

/**
 * JPA ORM 엔티티 — House B/L 해상 확장.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "house_bl_sea")
@Getter
@NoArgsConstructor
@DynamicUpdate
public class HouseBlSeaJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_sea_id", updatable = false, nullable = false)
    private Long houseBlSeaId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true, updatable = false)
    private HouseBlJpaEntity houseBl;

    @Column(name = "load_type", length = 10)
    @Enumerated(EnumType.STRING)
    private LoadType loadType;

    @Column(name = "liner_code", length = 20)
    private String linerCode;

    @Column(name = "vessel_name", length = 100)
    private String vesselName;

    @Column(name = "voyage_no", length = 20)
    private String voyageNo;

    @Column(name = "onboard_date", length = 8)
    private String onboardDate;

    @Column(name = "por_code", length = 10)
    private String porCode;

    @Column(name = "final_dest_code", length = 10)
    private String finalDestCode;

    @Column(name = "issue_date", length = 8)
    private String issueDate;

    @Column(name = "no_of_bl", length = 10)
    @Enumerated(EnumType.STRING)
    private NoOfBl noOfBl;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    @Column(name = "do_date", length = 8)
    private String doDate;

    @Column(name = "payable_at", length = 50)
    private String payableAt;

    @Column(name = "triangle", nullable = false)
    private boolean isTriangle = false;

    @Column(name = "service_term", length = 20)
    @Enumerated(EnumType.STRING)
    private ServiceTerm serviceTerm;

    @Column(name = "vessel_code", length = 20)
    private String vesselCode;

    @Column(name = "vessel_nationality", length = 50)
    private String vesselNationality;

    @Column(name = "rton", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal rton;

    @Column(name = "say_information", length = 500)
    private String sayInformation;

    @Column(name = "no_of_container_or_packages", length = 100)
    private String noOfContainerOrPackages;

    @Column(name = "bl_type", length = 15)
    @Enumerated(EnumType.STRING)
    private BlType blType;

    @Column(name = "delivery_code", length = 10)
    private String deliveryCode;

    @Column(name = "remark", length = 1000)
    private String remark;

    // SEA 전용 컨테이너 컬렉션 — house_bl_sea_container.house_bl_sea_id FK 소유
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "house_bl_sea_id", nullable = false, updatable = false)
    private List<HouseBlSeaContainerJpaEntity> containers = new ArrayList<>();

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setLoadType(LoadType v) { this.loadType = v; }
    public void setLinerCode(String v) { this.linerCode = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setVoyageNo(String v) { this.voyageNo = v; }
    public void setOnboardDate(String v) { this.onboardDate = v; }
    public void setPorCode(String v) { this.porCode = v; }
    public void setFinalDestCode(String v) { this.finalDestCode = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
    public void setNoOfBl(NoOfBl v) { this.noOfBl = v; }
    public void setIssuePlace(String v) { this.issuePlace = v; }
    public void setDoDate(String v) { this.doDate = v; }
    public void setPayableAt(String v) { this.payableAt = v; }
    public void setIsTriangle(boolean v) { this.isTriangle = v; }
    public void setServiceTerm(ServiceTerm v) { this.serviceTerm = v; }
    public void setVesselCode(String v) { this.vesselCode = v; }
    public void setVesselNationality(String v) { this.vesselNationality = v; }
    public void setRton(BigDecimal v) { this.rton = v; }
    public void setSayInformation(String v) { this.sayInformation = v; }
    public void setNoOfContainerOrPackages(String v) { this.noOfContainerOrPackages = v; }
    public void setBlType(BlType v) { this.blType = v; }
    public void setDeliveryCode(String v) { this.deliveryCode = v; }
    public void setRemark(String v) { this.remark = v; }

    /** SEA 컨테이너 동기화: 기존 컬렉션 참조를 유지하면서 내용 교체 (CREATE 경로 전용, orphanRemoval 지원). */
    public void syncContainers(List<HouseBlSeaContainerJpaEntity> newContainers) {
        this.containers.clear();
        this.containers.addAll(newContainers);
    }

    /**
     * SEA Container merge-by-id (UPDATE 경로 전용).
     * incoming id가 기존 영속 엔티티와 일치하면 필드 mutate(UPDATE), 없으면 신규 추가(INSERT).
     * orphanRemoval이 컬렉션에서 제거된 엔티티를 자동 DELETE한다.
     */
    public void mergeContainers(List<HouseBlSeaContainerJpaEntity> incoming) {
        Map<Long, HouseBlSeaContainerJpaEntity> existingById = new HashMap<>();
        for (HouseBlSeaContainerJpaEntity e : this.containers) {
            if (e.getHouseBlSeaContainerId() != null) existingById.put(e.getHouseBlSeaContainerId(), e);
        }
        List<HouseBlSeaContainerJpaEntity> merged = new ArrayList<>();
        for (HouseBlSeaContainerJpaEntity inc : incoming) {
            if (inc.getHouseBlSeaContainerId() != null && existingById.containsKey(inc.getHouseBlSeaContainerId())) {
                HouseBlSeaContainerJpaEntity existing = existingById.get(inc.getHouseBlSeaContainerId());
                copyContainerFields(inc, existing);
                merged.add(existing);
            } else {
                merged.add(inc);
            }
        }
        this.containers.clear();
        this.containers.addAll(merged);
    }

    private void copyContainerFields(HouseBlSeaContainerJpaEntity src, HouseBlSeaContainerJpaEntity dst) {
        dst.setContainerNo(src.getContainerNo());
        dst.setContainerType(src.getContainerType());
        dst.setLengthFeet(src.getLengthFeet());
        dst.setSealNo1(src.getSealNo1());
        dst.setSealNo2(src.getSealNo2());
        dst.setSealNo3(src.getSealNo3());
        dst.setSealNo4(src.getSealNo4());
        dst.setSealNo5(src.getSealNo5());
        dst.setSealNo6(src.getSealNo6());
        dst.setPkgQty(src.getPkgQty());
        dst.setPkgUnit(src.getPkgUnit());
        dst.setGrossWeightKg(src.getGrossWeightKg());
        dst.setNetWeightKg(src.getNetWeightKg());
        dst.setCbm(src.getCbm());
        dst.setVgmKg(src.getVgmKg());
        dst.setIsSoc(src.isSoc());
        dst.setSeq(src.getSeq());
    }

    public List<HouseBlSeaContainerJpaEntity> getContainers() { return containers; }
}
