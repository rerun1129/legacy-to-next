package com.freightos.admin.domain.button.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class Button extends BaseEntity {

    private final String buttonCode;
    private Long menuId;
    private String label;
    private String labelEn;
    private ActionType actionType;
    private String apiMethod;
    private String apiPath;
    private Integer sortOrder;
    private Boolean active;

    private Button(String buttonCode, Long menuId, String label, String labelEn, ActionType actionType,
                   String apiMethod, String apiPath, Integer sortOrder, Boolean active) {
        this.buttonCode  = buttonCode;
        this.menuId      = menuId;
        this.label       = label;
        this.labelEn     = labelEn;
        this.actionType  = actionType;
        this.apiMethod   = apiMethod;
        this.apiPath     = apiPath;
        this.sortOrder   = sortOrder;
        this.active      = active;
    }

    public static Button create(String buttonCode, Long menuId, String label, String labelEn, ActionType actionType,
                                String apiMethod, String apiPath, Integer sortOrder, Boolean active) {
        return new Button(buttonCode, menuId, label, labelEn, actionType, apiMethod, apiPath, sortOrder, active);
    }

    /** 표시 필드만 갱신. 식별 필드(buttonCode)는 변경 불가. */
    public void applyUpdate(Long menuId, String label, ActionType actionType,
                            String apiMethod, String apiPath, Integer sortOrder, Boolean active) {
        this.menuId     = menuId;
        this.label      = label;
        this.actionType = actionType;
        this.apiMethod  = apiMethod;
        this.apiPath    = apiPath;
        this.sortOrder  = sortOrder;
        this.active     = active;
    }
}
