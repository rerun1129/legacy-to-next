package com.freightos.admin.adapter.out.persistence.commoncode;

import com.freightos.admin.domain.commoncode.entity.CommonCode;
import org.springframework.stereotype.Component;

@Component
public class CommonCodeDomainToJpaMapper {

    public CommonCodeJpaEntity toNewJpa(CommonCode domain) {
        CommonCodeJpaEntity entity = new CommonCodeJpaEntity();
        entity.setGroupCode(domain.getGroupCode());
        entity.setCode(domain.getCode());
        entity.setLabel(domain.getLabel());
        entity.setLabelKo(domain.getLabelKo());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        return entity;
    }

    public void applyUpdateFields(CommonCodeJpaEntity entity, CommonCode patchData) {
        entity.setLabel(patchData.getLabel());
        entity.setLabelKo(patchData.getLabelKo());
        entity.setSortOrder(patchData.getSortOrder());
        entity.setActive(patchData.getActive());
    }
}
