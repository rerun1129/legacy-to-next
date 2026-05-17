package com.freightos.admin.adapter.out.persistence.terms;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import com.freightos.admin.domain.terms.entity.TermsType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        schema = "admin",
        name = "terms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_admin_terms_type_version", columnNames = {"type", "version"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TermsJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30, updatable = false)
    private TermsType type;

    @Column(name = "version", nullable = false, updatable = false)
    private Integer version;

    @Column(name = "effective_at", nullable = false)
    private LocalDateTime effectiveAt;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
