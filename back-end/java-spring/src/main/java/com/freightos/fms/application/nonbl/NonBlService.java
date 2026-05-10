package com.freightos.fms.application.nonbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.HouseBlFactory;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonBlService implements NonBlUseCase {

    private final HouseBlUseCase houseBlUseCase;
    private final HouseBlPort houseBlPort;
    private final HouseBlFactory houseBlFactory;
    private final NonBlSearchPort nonBlSearchPort;

    @Override
    public PagedResult<NonBlSummary> searchNonBls(SearchNonBlCommand cmd, PageRequest pageRequest) {
        return nonBlSearchPort.searchNonBlSummaries(cmd.toFilter(), pageRequest);
    }

    @Override
    public NonBlDetailResult findNonBlById(Long id) {
        return NonBlDetailResult.from(findNonBlDomainById(id));
    }

    @Override
    @Transactional
    public Long createNonBl(CreateHouseBlCommand command) {
        return houseBlUseCase.createHouseBl(command);
    }

    @Override
    @Transactional
    public NonBlDetailResult updateNonBl(Long id, UpdateHouseBlCommand command) {
        HouseBl existing = houseBlPort.findHouseBlById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));
        houseBlFactory.applyToEntity(command, existing);
        HouseBlNonBl saved = (HouseBlNonBl) houseBlPort.saveHouseBl(existing);
        return NonBlDetailResult.from(saved);
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
