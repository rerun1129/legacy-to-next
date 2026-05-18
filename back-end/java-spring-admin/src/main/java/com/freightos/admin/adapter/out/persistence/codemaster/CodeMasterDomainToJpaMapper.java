package com.freightos.admin.adapter.out.persistence.codemaster;

import com.freightos.admin.domain.codemaster.entity.CodeMaster;
import org.springframework.stereotype.Component;

@Component
public class CodeMasterDomainToJpaMapper {

    public CodeMasterJpaEntity toNewJpa(CodeMaster domain) {
        CodeMasterJpaEntity entity = new CodeMasterJpaEntity();
        entity.setMasterCode(domain.getMasterCode());
        entity.setMasterName(domain.getMasterName());
        entity.setDescription(domain.getDescription());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        return entity;
    }

    /** 표시 필드만 반영. masterCode는 식별 키이므로 변경하지 않는다. */
    public void applyUpdateFields(CodeMasterJpaEntity entity, CodeMaster patch) {
        entity.setMasterName(patch.getMasterName());
        entity.setDescription(patch.getDescription());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.getActive());
    }
}
