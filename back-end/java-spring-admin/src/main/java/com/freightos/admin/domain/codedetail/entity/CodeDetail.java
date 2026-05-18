package com.freightos.admin.domain.codedetail.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class CodeDetail extends BaseEntity {

    private final Long masterId;
    private final String codeValue;
    private String codeLabel;
    private Integer sortOrder;
    private Boolean active;
    private String remark;

    private CodeDetail(Long masterId, String codeValue, String codeLabel,
                       Integer sortOrder, Boolean active, String remark) {
        this.masterId   = masterId;
        this.codeValue  = codeValue;
        this.codeLabel  = codeLabel;
        this.sortOrder  = sortOrder;
        this.active     = active;
        this.remark     = remark;
    }

    public static CodeDetail create(Long masterId, String codeValue, String codeLabel,
                                    Integer sortOrder, Boolean active, String remark) {
        return new CodeDetail(masterId, codeValue, codeLabel, sortOrder, active, remark);
    }

    /**
     * 표시 필드만 갱신. 식별 필드(masterId·codeValue)는 변경 불가.
     */
    public void applyUpdate(String codeLabel, Integer sortOrder, Boolean active, String remark) {
        this.codeLabel = codeLabel;
        this.sortOrder = sortOrder;
        this.active    = active;
        this.remark    = remark;
    }
}
