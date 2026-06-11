package com.freightos.admin.adapter.out.persistence.commoncode;

import com.freightos.admin.application.commoncode.port.out.CommonCodePort;
import com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.domain.commoncode.entity.CommonCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommonCodePersistenceAdapter implements CommonCodePort {

    private final CommonCodeGroupRepository commonCodeGroupRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final CommonCodeDomainToJpaMapper domainToJpaMapper;
    private final CommonCodeJpaToDomainMapper jpaToDomainMapper;

    @Override
    public boolean existsGroupByGroupCode(String groupCode) {
        return commonCodeGroupRepository.existsByGroupCode(groupCode);
    }

    @Override
    public List<CommonCodeGroupSummary> findAllGroupSummaries() {
        return commonCodeGroupRepository.findAllGroupSummaries();
    }

    @Override
    public List<CommonCodeSummary> findCodeSummariesByGroupCode(String groupCode) {
        return commonCodeRepository.findCodeSummariesByGroupCode(groupCode);
    }

    @Override
    public Optional<CommonCode> findCommonCodeById(Long id) {
        return commonCodeRepository.findById(id).map(jpaToDomainMapper::toDomain);
    }

    @Override
    public boolean existsByGroupCodeAndCode(String groupCode, String code) {
        return commonCodeRepository.existsByGroupCodeAndCode(groupCode, code);
    }

    @Override
    public void saveCommonCode(CommonCode commonCode) {
        commonCodeRepository.save(domainToJpaMapper.toNewJpa(commonCode));
    }

    @Override
    public void updateCommonCodeById(Long id, CommonCode patchData) {
        CommonCodeJpaEntity entity = commonCodeRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("COMMON_CODE_NOT_FOUND",
                        MessageCode.COMMON_CODE_NOT_FOUND.getMessage()));
        domainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public List<CommonCodeSummary> findActiveCodeSummariesByGroupCodeOrdered(String groupCode) {
        return commonCodeRepository.findActiveCodeSummariesByGroupCodeOrdered(groupCode);
    }
}
