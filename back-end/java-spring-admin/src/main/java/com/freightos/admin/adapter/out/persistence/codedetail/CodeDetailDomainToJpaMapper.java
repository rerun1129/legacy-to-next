package com.freightos.admin.adapter.out.persistence.codedetail;

import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import org.springframework.stereotype.Component;

@Component
public class CodeDetailDomainToJpaMapper {

    public CodeDetailJpaEntity toNewJpa(CodeDetail domain) {
        CodeDetailJpaEntity entity = new CodeDetailJpaEntity();
        entity.setMasterId(domain.getMasterId());
        entity.setCodeValue(domain.getCodeValue());
        entity.setCodeLabel(domain.getCodeLabel());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        entity.setRemark(domain.getRemark());
        return entity;
    }

    /** 표시 필드만 반영. masterId·codeValue는 식별 키이므로 변경하지 않는다. */
    public void applyUpdateFields(CodeDetailJpaEntity entity, CodeDetail patch) {
        entity.setCodeLabel(patch.getCodeLabel());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.getActive());
        entity.setRemark(patch.getRemark());
    }
}
