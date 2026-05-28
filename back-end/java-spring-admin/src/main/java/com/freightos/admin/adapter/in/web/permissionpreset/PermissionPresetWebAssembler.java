package com.freightos.admin.adapter.in.web.permissionpreset;

import com.freightos.admin.adapter.in.web.permissionpreset.dto.AssignAttributeValuesRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.AttributeValueRef;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.CreatePermissionPresetRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.PermissionPresetResponse;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.PermissionPresetSummaryResponse;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.SavePermissionPresetChangesRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.SearchPermissionPresetRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.UpdatePermissionPresetRequest;
import com.freightos.admin.application.permissionpreset.command.AssignAttributeValuesCommand;
import com.freightos.admin.application.permissionpreset.command.CreatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.ListPermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.command.SavePermissionPresetChangesCommand;
import com.freightos.admin.application.permissionpreset.command.UpdatePermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetDetail;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetSummary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionPresetWebAssembler {

    public CreatePermissionPresetCommand toCreateCommand(CreatePermissionPresetRequest req) {
        boolean active = req.active() != null ? req.active() : true;
        return new CreatePermissionPresetCommand(req.code(), req.name(), req.description(), active);
    }

    public UpdatePermissionPresetCommand toUpdateCommand(UpdatePermissionPresetRequest req) {
        return new UpdatePermissionPresetCommand(req.name(), req.description(), req.active());
    }

    public ListPermissionPresetCommand toListCommand(SearchPermissionPresetRequest req) {
        boolean activeOnly = req.activeOnly() != null && req.activeOnly();
        return new ListPermissionPresetCommand(activeOnly);
    }

    public AssignAttributeValuesCommand toAssignCommand(AssignAttributeValuesRequest req) {
        return new AssignAttributeValuesCommand(req.addIds(), req.removeIds());
    }

    public PermissionPresetSummaryResponse toSummaryResponse(PermissionPresetSummary summary) {
        return new PermissionPresetSummaryResponse(
                summary.id(), summary.code(), summary.name(),
                summary.description(), summary.active(), summary.attributeValueIds()
        );
    }

    public PermissionPresetResponse toDetailResponse(PermissionPresetDetail detail) {
        List<AttributeValueRef> refs = detail.attributeValues().stream()
                .map(item -> new AttributeValueRef(item.id(), item.attributeKey(), item.value(), item.label()))
                .toList();
        return new PermissionPresetResponse(
                detail.id(), detail.code(), detail.name(), detail.description(),
                detail.active(), detail.attributeValueIds(), refs
        );
    }

    public SavePermissionPresetChangesCommand toSaveChangesCommand(SavePermissionPresetChangesRequest req) {
        List<CreatePermissionPresetCommand> creates = req.creates() == null ? List.of()
                : req.creates().stream().map(this::toCreateCommand).toList();
        List<SavePermissionPresetChangesCommand.UpdatePermissionPresetItem> updates = req.updates() == null ? List.of()
                : req.updates().stream()
                        .map(u -> new SavePermissionPresetChangesCommand.UpdatePermissionPresetItem(u.id(), u.name(), u.description(), u.active()))
                        .toList();
        List<Long> deleteIds = req.deleteIds() == null ? List.of() : req.deleteIds();
        return new SavePermissionPresetChangesCommand(creates, updates, deleteIds);
    }
}
