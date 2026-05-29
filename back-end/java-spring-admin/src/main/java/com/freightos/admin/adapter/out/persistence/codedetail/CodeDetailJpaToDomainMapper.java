package com.freightos.admin.adapter.out.persistence.codedetail;

import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import org.springframework.stereotype.Component;

@Component
public class CodeDetailJpaToDomainMapper {

    public CodeDetail toDomain(CodeDetailJpaEntity e) {
        CodeDetail domain = CodeDetail.create(e.getMasterId(), e.getCodeValue(), e.getCodeLabel(), e.getSortOrder(), e.getActive(), e.getRemark());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }

    public CodeDetailSummary toSummary(CodeDetailJpaEntity e) {
        return new CodeDetailSummary(e.getId(), e.getMasterId(), e.getCodeValue(), e.getCodeLabel(), e.getSortOrder(), e.getActive(), e.getRemark(), e.getUpdatedAt());
    }
}
