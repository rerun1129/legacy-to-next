package com.freightos.admin.adapter.out.persistence.code;

import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.domain.code.entity.Code;
import org.springframework.stereotype.Component;

@Component
public class CodeJpaToDomainMapper {

    public Code toDomain(CodeJpaEntity e) {
        Code domain = Code.create(e.getCodeGroup(), e.getCodeValue(), e.getCodeLabel(), e.getSortOrder(), e.getActive(), e.getRemark());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }

    public CodeSummary toSummary(CodeJpaEntity e) {
        return new CodeSummary(e.getId(), e.getCodeGroup(), e.getCodeValue(), e.getCodeLabel(), e.getSortOrder(), e.getActive(), e.getUpdatedAt());
    }
}
