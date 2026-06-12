package com.freightos.fms.application.housebl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.freight.FreightView;
import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.port.out.FreightInputPort;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.SearchHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.application.housebl.projection.HouseBlDetailView;
import com.freightos.fms.application.housebl.projection.SeaDetailProjection;
import com.freightos.fms.application.housebl.port.out.AirBlPersistencePort;
import com.freightos.fms.application.seahbl.port.out.SeaHblPersistencePort;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.freight.enums.FreightBlType;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.application.attachment.port.in.BlAttachmentUseCase;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseBlService implements HouseBlUseCase {

    private final HouseBlPort houseBlPort;
    private final HouseBlFactory houseBlFactory;
    private final SeaHblPersistencePort seaHblPersistencePort;
    private final AirBlPersistencePort airBlPersistencePort;
    private final CodeNameResolver codeNameResolver;
    private final FreightInputPort freightInputPort;
    private final HouseBlFreightCommandBuilder houseBlFreightCommandBuilder;
    private final BlAttachmentUseCase blAttachmentUseCase;

    @Override
    public PagedResult<HouseBlSummary> searchHouseBls(SearchHouseBlCommand cmd, PageRequest pageRequest) {
        return houseBlPort.searchHouseBls(houseBlFactory.toFilter(cmd), pageRequest);
    }

    @Override
    public HouseBlDetailView findHouseBlById(Long id) {
        HouseBlDetailResult base = houseBlFactory.toDetailResult(findEntityById(id));
        return enrichDetail(base);
    }

    private HouseBlDetailView enrichDetail(HouseBlDetailResult base) {
        Map<String, String> customerNames = resolveCustomerNames(base);
        Map<String, String> portNames = resolvePortNames(base);
        Map<String, String> userNames = resolveUserNames(base);
        Map<String, String> carrierNames = resolveCarrierNames(base);
        Map<String, String> hsCodeNames = resolveHsCodeNames(base);
        Map<String, String> teamNames = resolveTeamNames(base);
        Optional<FreightView> freightView = freightInputPort.findFreightByBl(
                FreightBlType.HOUSE, base.id());
        return new HouseBlDetailView(
                base,
                nameOrEmpty(customerNames, base.shipperCode()),
                nameOrEmpty(customerNames, base.consigneeCode()),
                nameOrEmpty(customerNames, base.notifyCode()),
                nameOrEmpty(customerNames, base.docPartnerCode()),
                nameOrEmpty(customerNames, base.settlePartnerCode()),
                nameOrEmpty(customerNames, base.actualCustomerCode()),
                nameOrEmpty(portNames, base.polCode()),
                nameOrEmpty(portNames, base.podCode()),
                nameOrEmpty(userNames, base.salesManCode()),
                nameOrEmpty(userNames, base.operatorCode()),
                nameOrEmpty(teamNames, base.teamCode()),
                seaPortName(portNames, base, SeaPortField.ISSUE_PLACE),
                seaPortName(portNames, base, SeaPortField.PAYABLE_AT),
                seaPortName(portNames, base, SeaPortField.DELIVERY),
                seaLinerName(carrierNames, base),
                nameOrEmpty(hsCodeNames, base.hsCode()),
                freightView.orElse(null)
        );
    }

    /** top-level customer 코드 6종을 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveCustomerNames(HouseBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.shipperCode());
        addIfHasText(codes, base.consigneeCode());
        addIfHasText(codes, base.notifyCode());
        addIfHasText(codes, base.docPartnerCode());
        addIfHasText(codes, base.settlePartnerCode());
        addIfHasText(codes, base.actualCustomerCode());
        return codeNameResolver.findCustomerNames(codes);
    }

    /**
     * port 코드 일괄 조회: top-level(pol/pod) + seaDetail(issuePlace/payableAt/delivery) 합쳐 1회.
     */
    private Map<String, String> resolvePortNames(HouseBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.polCode());
        addIfHasText(codes, base.podCode());
        SeaDetailProjection sea = base.seaDetail();
        if (sea != null) {
            addIfHasText(codes, sea.issuePlace());
            addIfHasText(codes, sea.payableAt());
        }
        addIfHasText(codes, base.deliveryCode());
        return codeNameResolver.findPortNames(codes);
    }

    /** salesMan/operator username 2종 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveUserNames(HouseBlDetailResult base) {
        Set<String> usernames = new HashSet<>();
        addIfHasText(usernames, base.salesManCode());
        addIfHasText(usernames, base.operatorCode());
        return codeNameResolver.findUserNames(usernames);
    }

    /** base.hsCode 1종 조회. */
    private Map<String, String> resolveHsCodeNames(HouseBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.hsCode());
        return codeNameResolver.findHsCodeNames(codes);
    }

    /** base.teamCode 1종 조회. */
    private Map<String, String> resolveTeamNames(HouseBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.teamCode());
        return codeNameResolver.findTeamNames(codes);
    }

    /** seaDetail.linerCode 1종 조회. seaDetail null이면 빈 맵. */
    private Map<String, String> resolveCarrierNames(HouseBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        SeaDetailProjection sea = base.seaDetail();
        if (sea != null) {
            addIfHasText(codes, sea.linerCode());
        }
        return codeNameResolver.findCarrierNames(codes);
    }

    private enum SeaPortField { ISSUE_PLACE, PAYABLE_AT, DELIVERY }

    private static String seaPortName(Map<String, String> portNames, HouseBlDetailResult base, SeaPortField field) {
        SeaDetailProjection sea = base.seaDetail();
        if (sea == null) {
            return "";
        }
        String code = switch (field) {
            case ISSUE_PLACE -> sea.issuePlace();
            case PAYABLE_AT  -> sea.payableAt();
            case DELIVERY    -> base.deliveryCode();
        };
        return nameOrEmpty(portNames, code);
    }

    private static String seaLinerName(Map<String, String> carrierNames, HouseBlDetailResult base) {
        SeaDetailProjection sea = base.seaDetail();
        if (sea == null) {
            return "";
        }
        return nameOrEmpty(carrierNames, sea.linerCode());
    }

    private static void addIfHasText(Set<String> target, String code) {
        if (code != null && !code.isBlank()) {
            target.add(code);
        }
    }

    private static String nameOrEmpty(Map<String, String> nameMap, String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        return nameMap.getOrDefault(code, "");
    }

    @Override
    @Transactional
    public Long createHouseBl(CreateHouseBlCommand command) {
        HouseBl entity = houseBlFactory.toEntity(command);
        log.debug("Creating HouseBl: {}", entity.getHblNo());
        Long houseBlId = houseBlPort.saveHouseBl(entity).getId();
        if (command.freight() != null) {
            FreightInputCommand freightCmd = houseBlFreightCommandBuilder.buildFromCreate(command, command.freight());
            freightInputPort.saveFreight(FreightBlType.HOUSE, houseBlId, freightCmd);
        }
        return houseBlId;
    }

    @Override
    @Transactional
    public void updateHouseBl(Long id, UpdateHouseBlCommand command) {
        HouseBl existing = findEntityById(id);
        houseBlFactory.applyToEntity(command, existing);
        houseBlPort.saveHouseBl(existing);
        if (command.freight() != null) {
            FreightInputCommand freightCmd = houseBlFreightCommandBuilder.buildFromUpdate(command, command.freight());
            freightInputPort.saveFreight(FreightBlType.HOUSE, id, freightCmd);
        }
    }

    @Override
    @Transactional
    public void updateSeaHbl(Long id, UpdateHouseBlCommand command) {
        seaHblPersistencePort.update(id, command);
        if (command.freight() != null) {
            FreightInputCommand freightCmd = houseBlFreightCommandBuilder.buildFromUpdate(command, command.freight());
            freightInputPort.saveFreight(FreightBlType.HOUSE, id, freightCmd);
        }
    }

    @Override
    @Transactional
    public void updateAirHbl(Long id, UpdateHouseBlCommand command) {
        airBlPersistencePort.update(id, command);
        if (command.freight() != null) {
            FreightInputCommand freightCmd = houseBlFreightCommandBuilder.buildFromUpdate(command, command.freight());
            freightInputPort.saveFreight(FreightBlType.HOUSE, id, freightCmd);
        }
    }

    @Override
    @Transactional
    public void deleteHouseBlById(Long id) {
        JobDiv jobDiv = houseBlPort.findJobDivById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));
        if (freightInputPort.existsFreightLines(FreightBlType.HOUSE, id)) {
            // 운임 라인 존재 시 삭제 차단 — 데이터 정합성 보호
            throw FmsException.conflict("FREIGHT_DELETE_BLOCKED", MessageCode.FREIGHT_DELETE_BLOCKED.message());
        }
        freightInputPort.deleteFreight(FreightBlType.HOUSE, id);
        blAttachmentUseCase.deleteAttachmentsByBl(AttachmentBlKind.HOUSE, id);
        houseBlPort.deleteByIdAndJobDiv(id, jobDiv);
        log.info("Deleted HouseBl id={}", id);
    }

    @Override
    @Transactional
    public void changeHblNo(Long id, ChangeHouseBlNoCommand command) {
        BlNumber newHblNo = BlNumber.of(command.hblNo());
        if (newHblNo == null) throw new IllegalArgumentException("hblNo must not be null or blank");
        long affected = houseBlPort.updateHblNoById(id, newHblNo, null);
        if (affected == 0) throw new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND);
        log.info("Changed HouseBl hblNo: id={}", id);
    }

    @Override
    public List<Long> findHouseBlKeysByHblNoExact(String hblNo, JobDiv jobDiv) {
        return houseBlPort.findHouseBlKeysByHblNoExact(hblNo, jobDiv);
    }

    private HouseBl findEntityById(Long id) {
        return houseBlPort.findHouseBlById(id).orElseThrow(() -> new ResourceNotFoundException(MessageCode.HOUSE_BL_NOT_FOUND));
    }
}
