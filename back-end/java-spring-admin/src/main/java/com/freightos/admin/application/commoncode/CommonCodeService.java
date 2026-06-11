package com.freightos.admin.application.commoncode;

import com.freightos.admin.application.commoncode.command.SaveCommonCodeChangesCommand;
import com.freightos.admin.application.commoncode.port.in.CommonCodeUseCase;
import com.freightos.admin.application.commoncode.port.out.CommonCodeCachePort;
import com.freightos.admin.application.commoncode.port.out.CommonCodePort;
import com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.commoncode.entity.CommonCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService implements CommonCodeUseCase {

    private final CommonCodePort commonCodePort;
    private final CommonCodeCachePort commonCodeCachePort;

    @Override
    public List<CommonCodeGroupSummary> getCommonCodeGroups() {
        return commonCodePort.findAllGroupSummaries();
    }

    @Override
    public List<CommonCodeSummary> getCommonCodesByGroup(String groupCode) {
        if (!commonCodePort.existsGroupByGroupCode(groupCode)) {
            throw ApplicationException.notFound("COMMON_CODE_GROUP_NOT_FOUND",
                    MessageCode.COMMON_CODE_GROUP_NOT_FOUND.getMessage());
        }
        return commonCodePort.findCodeSummariesByGroupCode(groupCode);
    }

    @Override
    @Transactional
    public SaveChangesResult saveCommonCodeChanges(SaveCommonCodeChangesCommand command) {
        if (!commonCodePort.existsGroupByGroupCode(command.groupCode())) {
            throw ApplicationException.notFound("COMMON_CODE_GROUP_NOT_FOUND",
                    MessageCode.COMMON_CODE_GROUP_NOT_FOUND.getMessage());
        }

        int createCount = 0;
        for (SaveCommonCodeChangesCommand.CreateCommonCodeItem item : command.creates()) {
            if (commonCodePort.existsByGroupCodeAndCode(command.groupCode(), item.code())) {
                throw ApplicationException.conflict("COMMON_CODE_DUPLICATE",
                        MessageCode.COMMON_CODE_DUPLICATE.getMessage());
            }
            CommonCode newCode = CommonCode.create(command.groupCode(), item.code(),
                    item.label(), item.labelKo(), item.sortOrder(), item.active());
            commonCodePort.saveCommonCode(newCode);
            createCount++;
        }

        int updateCount = 0;
        for (SaveCommonCodeChangesCommand.UpdateCommonCodeItem item : command.updates()) {
            CommonCode existing = commonCodePort.findCommonCodeById(item.id())
                    .orElseThrow(() -> ApplicationException.notFound("COMMON_CODE_NOT_FOUND",
                            MessageCode.COMMON_CODE_NOT_FOUND.getMessage()));
            existing.applyUpdate(item.label(), item.labelKo(), item.sortOrder(), item.active());
            commonCodePort.updateCommonCodeById(item.id(), existing);
            updateCount++;
        }

        // Redis write-through — 실패해도 응답은 정상(소비자 DB 폴백)
        List<CommonCodeSummary> activeCodes =
                commonCodePort.findActiveCodeSummariesByGroupCodeOrdered(command.groupCode());
        commonCodeCachePort.putGroupCodes(command.groupCode(), activeCodes);

        return new SaveChangesResult(createCount, updateCount, 0);
    }
}
