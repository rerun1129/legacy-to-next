package com.freightos.fms.adapter.in.web.switchbl;

import com.freightos.fms.adapter.in.web.switchbl.dto.CreateSwitchBlRequest;
import com.freightos.fms.adapter.in.web.switchbl.dto.SwitchBlDescriptionDto;
import com.freightos.fms.adapter.in.web.switchbl.dto.SwitchBlResponse;
import com.freightos.fms.adapter.in.web.switchbl.dto.UpdateSwitchBlRequest;
import com.freightos.fms.application.switchbl.command.CreateSwitchBlCommand;
import com.freightos.fms.application.switchbl.command.UpdateSwitchBlCommand;
import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;
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
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.description() != null
                        ? new CreateSwitchBlCommand.DescriptionCommand(req.description().marks(), req.description().natureQuantity())
                        : null
        );
    }

    public UpdateSwitchBlCommand toUpdateCommand(UpdateSwitchBlRequest req) {
        return new UpdateSwitchBlCommand(
                req.switchBlNo(),
                req.shipperCode(),
                req.shipperAddress(),
                req.consigneeCode(),
                req.consigneeAddress(),
                req.notifyCode(),
                req.notifyAddress(),
                req.description() != null
                        ? new UpdateSwitchBlCommand.DescriptionCommand(req.description().marks(), req.description().natureQuantity())
                        : null
        );
    }

    public SwitchBlResponse toResponse(SwitchBlDetailResult result) {
        return SwitchBlResponse.from(result);
    }
}
