package com.freightos.admin.adapter.out.persistence.commoncode;

import com.freightos.admin.domain.commoncode.entity.CommonCode;
import org.springframework.stereotype.Component;

@Component
public class CommonCodeJpaToDomainMapper {

    public CommonCode toDomain(CommonCodeJpaEntity entity) {
        CommonCode domain = CommonCode.create(
                entity.getGroupCode(),
                entity.getCode(),
                entity.getLabel(),
                entity.getLabelKo(),
                entity.getSortOrder(),
                entity.getActive()
        );
        domain.assignIdentity(
                entity.getCommonCodeId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
        return domain;
    }
}
