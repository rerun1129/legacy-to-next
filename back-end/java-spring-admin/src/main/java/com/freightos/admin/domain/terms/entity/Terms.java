package com.freightos.admin.domain.terms.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Terms extends BaseEntity {

    private final TermsType type;
    private final int version;
    private LocalDateTime effectiveAt;
    private String content;
    private String summary;
    private LocalDateTime deletedAt;

    private Terms(TermsType type, int version, LocalDateTime effectiveAt, String content, String summary) {
        this.type        = type;
        this.version     = version;
        this.effectiveAt = effectiveAt;
        this.content     = content;
        this.summary     = summary;
        this.deletedAt   = null;
    }

    public static Terms create(TermsType type, int version, LocalDateTime effectiveAt, String content, String summary) {
        return new Terms(type, version, effectiveAt, content, summary);
    }

    /** content·summary·effectiveAt 갱신. type·version은 불변이므로 파라미터에 포함하지 않는다. */
    public void applyUpdate(String content, String summary, LocalDateTime effectiveAt) {
        this.content     = content;
        this.summary     = summary;
        this.effectiveAt = effectiveAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 deletedAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 effectiveAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignEffectiveAt(LocalDateTime effectiveAt) {
        this.effectiveAt = effectiveAt;
    }
}
