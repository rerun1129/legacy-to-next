package com.freightos.admin.adapter.out.persistence.code;

import com.freightos.admin.domain.code.entity.Code;
import org.springframework.stereotype.Component;

@Component
public class CodeDomainToJpaMapper {

    public CodeJpaEntity toNewJpa(Code domain) {
        CodeJpaEntity entity = new CodeJpaEntity();
        entity.setCodeGroup(domain.getCodeGroup());
        entity.setCodeValue(domain.getCodeValue());
        entity.setCodeLabel(domain.getCodeLabel());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        entity.setRemark(domain.getRemark());
        return entity;
    }

    /** 표시 필드만 반영. codeGroup·codeValue는 식별 키이므로 변경하지 않는다. */
    public void applyUpdateFields(CodeJpaEntity entity, Code patch) {
        entity.setCodeLabel(patch.getCodeLabel());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.getActive());
        entity.setRemark(patch.getRemark());
    }
}
