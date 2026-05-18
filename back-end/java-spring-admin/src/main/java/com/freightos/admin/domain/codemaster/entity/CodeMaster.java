package com.freightos.admin.domain.codemaster.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class CodeMaster extends BaseEntity {

    private final String masterCode;
    private String masterName;
    private String description;
    private Integer sortOrder;
    private Boolean active;

    private CodeMaster(String masterCode, String masterName, String description,
                       Integer sortOrder, Boolean active) {
        this.masterCode  = masterCode;
        this.masterName  = masterName;
        this.description = description;
        this.sortOrder   = sortOrder;
        this.active      = active;
    }

    public static CodeMaster create(String masterCode, String masterName, String description,
                                    Integer sortOrder, Boolean active) {
        return new CodeMaster(masterCode, masterName, description, sortOrder, active);
    }

    /**
     * 표시 필드만 갱신. 식별 필드(masterCode)는 변경 불가.
     */
    public void applyUpdate(String masterName, String description, Integer sortOrder, Boolean active) {
        this.masterName  = masterName;
        this.description = description;
        this.sortOrder   = sortOrder;
        this.active      = active;
    }
}
