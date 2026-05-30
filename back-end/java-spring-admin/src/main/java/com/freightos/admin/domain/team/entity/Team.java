package com.freightos.admin.domain.team.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class Team extends BaseEntity {

    private final String teamCode;
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean active;

    private Team(String teamCode, String name, String description,
                 Integer sortOrder, Boolean active) {
        this.teamCode    = teamCode;
        this.name        = name;
        this.description = description;
        this.sortOrder   = sortOrder;
        this.active      = active;
    }

    public static Team create(String teamCode, String name, String description,
                              Integer sortOrder, Boolean active) {
        return new Team(teamCode, name, description, sortOrder, active);
    }

    /**
     * 표시 필드만 갱신. 식별 필드(teamCode)는 변경 불가.
     */
    public void applyUpdate(String name, String description, Integer sortOrder, Boolean active) {
        this.name        = name;
        this.description = description;
        this.sortOrder   = sortOrder;
        this.active      = active;
    }
}
