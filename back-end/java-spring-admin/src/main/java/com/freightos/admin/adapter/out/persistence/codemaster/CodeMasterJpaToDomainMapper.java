package com.freightos.admin.adapter.out.persistence.codemaster;

import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;
import org.springframework.stereotype.Component;

@Component
public class CodeMasterJpaToDomainMapper {

    public CodeMaster toDomain(CodeMasterJpaEntity e) {
        CodeMaster domain = CodeMaster.create(e.getMasterCode(), e.getMasterName(), e.getDescription(), e.getSortOrder(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }

    public CodeMasterSummary toSummary(CodeMasterJpaEntity e) {
        return new CodeMasterSummary(e.getId(), e.getMasterCode(), e.getMasterName(), e.getDescription(), e.getSortOrder(), e.getActive(), e.getUpdatedAt());
    }
}
