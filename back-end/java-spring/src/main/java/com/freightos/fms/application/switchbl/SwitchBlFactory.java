package com.freightos.fms.application.switchbl;

import com.freightos.fms.application.switchbl.command.CreateSwitchBlCommand;
import com.freightos.fms.application.switchbl.command.UpdateSwitchBlCommand;
import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.entity.SwitchBlDescription;
import com.freightos.common.util.VoMapper;
import org.springframework.stereotype.Component;

/**
 * Command → 도메인 Entity 변환 팩토리.
 * Adapter(in)에서 분리된 VO/Entity 생성 로직을 이 계층에 집중한다.
 */
@Component
public class SwitchBlFactory {

    public SwitchBl toEntity(CreateSwitchBlCommand cmd) {
        SwitchBl entity = SwitchBl.create(cmd.houseBlId(), CustomerCode.of(cmd.shipperCode(), cmd.shipperAddress()));
        entity.updateDetails(
                cmd.switchBlNo(),
                CustomerCode.of(cmd.shipperCode(), cmd.shipperAddress()),
                CustomerCode.of(cmd.consigneeCode(), cmd.consigneeAddress()),
                CustomerCode.of(cmd.notifyCode(), cmd.notifyAddress())
        );
        if (cmd.description() != null) {
            entity.attachDescription(toDescriptionEntity(cmd.description()));
        }
        return entity;
    }

    public void applyToEntity(UpdateSwitchBlCommand cmd, SwitchBl entity) {
        entity.updateDetails(
                cmd.switchBlNo()    != null ? cmd.switchBlNo()    : entity.getSwitchBlNo(),
                cmd.shipperCode()   != null
                        ? CustomerCode.of(cmd.shipperCode(), cmd.shipperAddress())
                        : entity.getShipperCode(),
                cmd.consigneeCode() != null
                        ? CustomerCode.of(cmd.consigneeCode(), cmd.consigneeAddress())
                        : entity.getConsigneeCode(),
                cmd.notifyCode()    != null
                        ? CustomerCode.of(cmd.notifyCode(), cmd.notifyAddress())
                        : entity.getNotifyCode()
        );
        if (cmd.description() != null) {
            entity.attachDescription(toDescriptionEntity(cmd.description()));
        }
    }

    public SwitchBlDetailResult toDetailResult(SwitchBl entity) {
        SwitchBlDescription desc = entity.getDescription();
        return new SwitchBlDetailResult(
                entity.getId(),
                entity.getSwitchBlId(),
                entity.getHouseBlId(),
                entity.getSwitchBlNo(),
                VoMapper.mapOrNull(entity.getShipperCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getShipperCode(), CustomerCode::address),
                VoMapper.mapOrNull(entity.getConsigneeCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getConsigneeCode(), CustomerCode::address),
                VoMapper.mapOrNull(entity.getNotifyCode(), CustomerCode::value),
                VoMapper.mapOrNull(entity.getNotifyCode(), CustomerCode::address),
                desc != null ? desc.getMarks() : null,
                desc != null ? desc.getNatureQuantity() : null
        );
    }

    private SwitchBlDescription toDescriptionEntity(CreateSwitchBlCommand.DescriptionCommand dto) {
        // switchBlId는 저장 이후 PersistenceAdapter가 연결하므로 여기서는 null 허용
        SwitchBlDescription desc = SwitchBlDescription.create(null);
        desc.updateContent(dto.marks(), dto.natureQuantity());
        return desc;
    }

    private SwitchBlDescription toDescriptionEntity(UpdateSwitchBlCommand.DescriptionCommand dto) {
        SwitchBlDescription desc = SwitchBlDescription.create(null);
        desc.updateContent(dto.marks(), dto.natureQuantity());
        return desc;
    }
}
