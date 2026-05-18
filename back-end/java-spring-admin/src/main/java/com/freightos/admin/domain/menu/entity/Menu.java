package com.freightos.admin.domain.menu.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class Menu extends BaseEntity {

    private final String menuCode;
    private Long parentId;
    private String path;
    private String label;
    private String labelEn;
    private String icon;
    private Integer sortOrder;
    private Boolean active;
    private String moduleCode;

    private Menu(String menuCode, Long parentId, String path, String label, String labelEn,
                 String icon, Integer sortOrder, Boolean active, String moduleCode) {
        this.menuCode   = menuCode;
        this.parentId   = parentId;
        this.path       = path;
        this.label      = label;
        this.labelEn    = labelEn;
        this.icon       = icon;
        this.sortOrder  = sortOrder;
        this.active     = active;
        this.moduleCode = moduleCode;
    }

    public static Menu create(String menuCode, Long parentId, String path, String label, String labelEn,
                              String icon, Integer sortOrder, Boolean active, String moduleCode) {
        return new Menu(menuCode, parentId, path, label, labelEn, icon, sortOrder, active, moduleCode);
    }

    /** 표시 필드만 갱신. 식별 필드(menuCode)는 변경 불가. */
    public void applyUpdate(Long parentId, String path, String label, String labelEn,
                            String icon, Integer sortOrder, Boolean active, String moduleCode) {
        this.parentId   = parentId;
        this.path       = path;
        this.label      = label;
        this.labelEn    = labelEn;
        this.icon       = icon;
        this.sortOrder  = sortOrder;
        this.active     = active;
        this.moduleCode = moduleCode;
    }
}
