package com.freightos.admin.adapter.in.web.commoncode;

import com.freightos.admin.adapter.in.web.commoncode.dto.CommonCodeGroupResponse;
import com.freightos.admin.adapter.in.web.commoncode.dto.CommonCodeResponse;
import com.freightos.admin.adapter.in.web.commoncode.dto.SaveCommonCodeChangesRequest;
import com.freightos.admin.application.commoncode.command.SaveCommonCodeChangesCommand;
import com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommonCodeAssembler {

    public CommonCodeGroupResponse toGroupResponse(CommonCodeGroupSummary summary) {
        return new CommonCodeGroupResponse(
                summary.id(), summary.groupCode(), summary.sourceModule(),
                summary.description(), summary.active());
    }

    public List<CommonCodeGroupResponse> toGroupResponseList(List<CommonCodeGroupSummary> summaries) {
        return summaries.stream().map(this::toGroupResponse).toList();
    }

    public CommonCodeResponse toCodeResponse(CommonCodeSummary summary) {
        return new CommonCodeResponse(
                summary.id(), summary.groupCode(), summary.code(),
                summary.label(), summary.labelKo(), summary.sortOrder(), summary.active());
    }

    public List<CommonCodeResponse> toCodeResponseList(List<CommonCodeSummary> summaries) {
        return summaries.stream().map(this::toCodeResponse).toList();
    }

    public SaveCommonCodeChangesCommand toSaveChangesCommand(SaveCommonCodeChangesRequest req) {
        List<SaveCommonCodeChangesCommand.CreateCommonCodeItem> creates =
                req.creates() == null ? List.of()
                        : req.creates().stream()
                                .map(c -> new SaveCommonCodeChangesCommand.CreateCommonCodeItem(
                                        c.code(), c.label(), c.labelKo(), c.sortOrder(), c.active()))
                                .toList();
        List<SaveCommonCodeChangesCommand.UpdateCommonCodeItem> updates =
                req.updates() == null ? List.of()
                        : req.updates().stream()
                                .map(u -> new SaveCommonCodeChangesCommand.UpdateCommonCodeItem(
                                        u.id(), u.label(), u.labelKo(), u.sortOrder(), u.active()))
                                .toList();
        return new SaveCommonCodeChangesCommand(req.groupCode(), creates, updates);
    }
}
