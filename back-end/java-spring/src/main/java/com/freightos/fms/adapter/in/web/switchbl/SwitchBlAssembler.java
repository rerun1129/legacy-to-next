package com.freightos.fms.adapter.in.web.switchbl;

import com.freightos.fms.adapter.in.web.switchbl.dto.CreateSwitchBlRequest;
import com.freightos.fms.adapter.in.web.switchbl.dto.SwitchBlDescriptionDto;
import com.freightos.fms.adapter.in.web.switchbl.dto.SwitchBlResponse;
import com.freightos.fms.adapter.in.web.switchbl.dto.UpdateSwitchBlRequest;
import com.freightos.fms.application.switchbl.command.CreateSwitchBlCommand;
import com.freightos.fms.application.switchbl.command.UpdateSwitchBlCommand;
import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

/**
 * Switch B/L 요청 DTO를 Application Command로 변환하고,
 * Application Projection을 응답 DTO로 변환한다.
 * 컨트롤러는 매핑을 직접 호출하지 않고 본 어셈블러에 위임한다.
 */
@Component
public class SwitchBlAssembler {

    public CreateSwitchBlCommand toCreateCommand(CreateSwitchBlRequest req) {
        return new CreateSwitchBlCommand(
                req.houseBlId(),
                req.switchBlNo(),
                req.blType(),
                req.incoterms(),
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                Nullables.mapOrNull(req.description(), d -> new CreateSwitchBlCommand.DescriptionCommand(d.marks(), d.natureQuantity()))
        );
    }

    public UpdateSwitchBlCommand toUpdateCommand(UpdateSwitchBlRequest req) {
        return new UpdateSwitchBlCommand(
                req.switchBlNo(),
                req.blType(),
                req.incoterms(),
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                Nullables.mapOrNull(req.description(), d -> new UpdateSwitchBlCommand.DescriptionCommand(d.marks(), d.natureQuantity()))
        );
    }

    public SwitchBlResponse toResponse(SwitchBlDetailResult result) {
        return SwitchBlResponse.from(result);
    }
}
