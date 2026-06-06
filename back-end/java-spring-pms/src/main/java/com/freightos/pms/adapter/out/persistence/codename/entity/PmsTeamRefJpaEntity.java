package com.freightos.pms.adapter.out.persistence.codename.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * admin.team 읽기 전용 참조 엔티티.
 * PMS 조회 시 team_code → name 변환에만 사용.
 */
@Entity
@Immutable
@Table(schema = "admin", name = "team")
@Getter
@NoArgsConstructor
public class PmsTeamRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "team_code", nullable = false, length = 20)
    private String teamCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
