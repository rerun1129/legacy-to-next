package com.freightos.fms.application.nonbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.common.codename.CodeNameResolver;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.fms.application.nonbl.port.out.NonBlPersistencePort;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlDetailView;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Override
    public PagedResult<NonBlSummary> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest) {
        return nonBlSearchPort.searchNonBlSummaries(cmd.toFilter(), pageRequest);
    }

    @Override
    public NonBlDetailView findNonBlById(Long id) {
        NonBlDetailResult base = NonBlDetailResult.from(findNonBlDomainById(id));
        return enrichDetail(base);
    }

    private NonBlDetailView enrichDetail(NonBlDetailResult base) {
        Map<String, String> hsCodeNames = resolveHsCodeNames(base);
        return new NonBlDetailView(base, nameOrEmpty(hsCodeNames, base.hsCode()));
    }

    /** base.hsCode 1종 조회. */
    private Map<String, String> resolveHsCodeNames(NonBlDetailResult base) {
        Set<String> codes = new HashSet<>();
        if (base.hsCode() != null && !base.hsCode().isBlank()) {
            codes.add(base.hsCode());
        }
        return codeNameResolver.findHsCodeNames(codes);
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
    }

    @Override
    @Transactional
    public void deleteNonBlById(Long id) {
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
