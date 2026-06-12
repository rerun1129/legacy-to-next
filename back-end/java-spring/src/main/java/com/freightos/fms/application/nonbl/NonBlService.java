package com.freightos.fms.application.nonbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.freight.FreightView;
import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.port.out.FreightInputPort;
import com.freightos.fms.application.housebl.HouseBlFreightCommandBuilder;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.attachment.port.in.BlAttachmentUseCase;
import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;
import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.fms.application.nonbl.port.out.NonBlPersistencePort;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlDetailView;
import com.freightos.fms.application.nonbl.projection.NonBlListItem;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.freight.enums.FreightBlType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonBlService implements NonBlUseCase {

    private final HouseBlUseCase houseBlUseCase;
    private final HouseBlPort houseBlPort;
    private final NonBlPersistencePort nonBlPersistencePort;
    private final NonBlSearchPort nonBlSearchPort;
    private final CodeNameResolver codeNameResolver;
    private final FreightInputPort freightInputPort;
    private final HouseBlFreightCommandBuilder houseBlFreightCommandBuilder;
    private final BlAttachmentUseCase blAttachmentUseCase;

    @Override
    public PagedResult<NonBlListItem> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest) {
        PagedResult<NonBlSummary> summaries = nonBlSearchPort.searchNonBlSummaries(cmd.toFilter(), pageRequest);
        Map<String, String> teamNames = resolveListTeamNames(summaries.getContent());
        return summaries.map(s -> toListItem(s, teamNames));
    }

    /** 리스트 페이지 전체에서 team 코드를 distinct 수집 후 1회 조회. */
    private Map<String, String> resolveListTeamNames(List<NonBlSummary> summaries) {
        Set<String> codes = new HashSet<>();
        for (NonBlSummary s : summaries) {
            addIfHasText(codes, s.teamCode());
        }
        return codeNameResolver.findTeamNames(codes);
    }

    private static NonBlListItem toListItem(NonBlSummary s, Map<String, String> teamNames) {
        return new NonBlListItem(
                s.id(),
                s.hblNo(),
                s.jobDiv(),
                s.bound(),
                s.polCode(),
                s.podCode(),
                s.etd(),
                s.eta(),
                s.shipperCode(),
                s.consigneeCode(),
                s.pkgQty(),
                s.pkgUnit(),
                s.createdAt(),
                s.notifyCode(),
                s.settlePartnerCode(),
                s.actualCustomerCode(),
                s.grossWeightKg(),
                s.cbm(),
                s.vesselName(),
                s.voyageNo(),
                s.linerCode(),
                s.linerName(),
                s.teamCode(),
                nameOrEmpty(teamNames, s.teamCode())
        );
    }

    @Override
    public NonBlDetailView findNonBlById(Long id) {
        NonBlDetailResult base = NonBlDetailResult.from(findNonBlDomainById(id));
        return enrichDetail(base);
    }

    private NonBlDetailView enrichDetail(NonBlDetailResult base) {
        Map<String, String> hsCodeNames = resolveHsCodeNames(base);
        Map<String, String> teamNames = resolveTeamNames(base);
        Optional<FreightView> freightView = freightInputPort.findFreightByBl(
                FreightBlType.HOUSE, base.id());
        return new NonBlDetailView(
                base,
                nameOrEmpty(hsCodeNames, base.hsCode()),
                nameOrEmpty(teamNames, base.teamCode()),
                freightView.orElse(null)
        );
    }

    /** base.hsCode 1종 조회. */
    private Map<String, String> resolveHsCodeNames(NonBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.hsCode());
        return codeNameResolver.findHsCodeNames(codes);
    }

    /** base.teamCode 1종 조회. */
    private Map<String, String> resolveTeamNames(NonBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        addIfHasText(codes, base.teamCode());
        return codeNameResolver.findTeamNames(codes);
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
    public Long createNonBl(CreateHouseBlCommand command) {
        return houseBlUseCase.createHouseBl(command);
    }

    @Override
    @Transactional
    public void updateNonBl(Long id, UpdateHouseBlCommand command) {
        nonBlPersistencePort.update(id, command);
        if (command.freight() != null) {
            FreightInputCommand freightCmd = houseBlFreightCommandBuilder.buildFromUpdate(command, command.freight());
            freightInputPort.saveFreight(FreightBlType.HOUSE, id, freightCmd);
        }
    }

    @Override
    @Transactional
    public void deleteNonBlById(Long id) {
        if (freightInputPort.existsFreightLines(FreightBlType.HOUSE, id)) {
            // 운임 라인 존재 시 삭제 차단 — 데이터 정합성 보호
            throw FmsException.conflict("FREIGHT_DELETE_BLOCKED", MessageCode.FREIGHT_DELETE_BLOCKED.message());
        }
        freightInputPort.deleteFreight(FreightBlType.HOUSE, id);
        blAttachmentUseCase.deleteAttachmentsByBl(AttachmentBlKind.NON_BL, id);
        // NON_BL은 jobDiv가 고정이므로 projection 없이 직접 호출 (SELECT 0회 추가)
        houseBlPort.deleteByIdAndJobDiv(id, JobDiv.NON_BL);
        log.info("Deleted NonBl id={}", id);
    }

    @Override
    @Transactional
    public void changeNonBlHblNo(Long id, ChangeHouseBlNoCommand command) {
        BlNumber newHblNo = BlNumber.of(command.hblNo());
        if (newHblNo == null) throw new IllegalArgumentException("hblNo must not be null or blank");
        long affected = houseBlPort.updateHblNoById(id, newHblNo, JobDiv.NON_BL);
        if (affected == 0) throw new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND);
    }

    @Override
    public List<Long> findNonBlKeysByHblNoExact(String hblNo) {
        return nonBlSearchPort.findNonBlKeysByHblNoExact(hblNo);
    }

    private HouseBlNonBl findNonBlDomainById(Long id) {
        return nonBlSearchPort.findNonBlById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));
    }
}
