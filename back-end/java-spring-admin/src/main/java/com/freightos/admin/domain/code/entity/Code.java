package com.freightos.admin.domain.code.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class Code extends BaseEntity {

    private final String codeGroup;
    private final String codeValue;
    private String codeLabel;
    private Integer sortOrder;
    private Boolean active;
    private String remark;

    private Code(String codeGroup, String codeValue, String codeLabel,
                 Integer sortOrder, Boolean active, String remark) {
        this.codeGroup  = codeGroup;
        this.codeValue  = codeValue;
        this.codeLabel  = codeLabel;
        this.sortOrder  = sortOrder;
        this.active     = active;
        this.remark     = remark;
    }

    public static Code create(String codeGroup, String codeValue, String codeLabel,
                              Integer sortOrder, Boolean active, String remark) {
        return new Code(codeGroup, codeValue, codeLabel, sortOrder, active, remark);
    }

    /**
     * 표시 필드만 갱신. 식별 필드(codeGroup·codeValue)는 변경 불가.
     */
    public void applyUpdate(String codeLabel, Integer sortOrder, Boolean active, String remark) {
        this.codeLabel  = codeLabel;
        this.sortOrder  = sortOrder;
        this.active     = active;
        this.remark     = remark;
    }
}
