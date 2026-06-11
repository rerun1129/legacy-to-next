package com.freightos.admin.application.commoncode.port.in;

import com.freightos.admin.application.commoncode.command.SaveCommonCodeChangesCommand;
import com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import com.freightos.admin.common.response.SaveChangesResult;

import java.util.List;

public interface CommonCodeUseCase {
    List<CommonCodeGroupSummary> getCommonCodeGroups();
    List<CommonCodeSummary> getCommonCodesByGroup(String groupCode);
    SaveChangesResult saveCommonCodeChanges(SaveCommonCodeChangesCommand command);
}
