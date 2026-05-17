package com.freightos.admin.domain.faq.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Faq extends BaseEntity {

    private Long faqCategoryId;
    private String question;
    private String answer;
    private int sortOrder;
    private boolean active;
    private LocalDateTime deletedAt;

    private Faq(Long faqCategoryId, String question, String answer, int sortOrder, boolean active) {
        this.faqCategoryId = faqCategoryId;
        this.question      = question;
        this.answer        = answer;
        this.sortOrder     = sortOrder;
        this.active        = active;
        this.deletedAt     = null;
    }

    public static Faq create(Long faqCategoryId, String question, String answer, int sortOrder, boolean active) {
        return new Faq(faqCategoryId, question, answer, sortOrder, active);
    }

    /** 수정 가능한 필드 갱신. faqCategoryId 변경 허용(카테고리 이동). */
    public void applyUpdate(Long faqCategoryId, String question, String answer, int sortOrder, boolean active) {
        this.faqCategoryId = faqCategoryId;
        this.question      = question;
        this.answer        = answer;
        this.sortOrder     = sortOrder;
        this.active        = active;
    }

    /** soft delete: 삭제 시각 기록. */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.active    = false;
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
}
