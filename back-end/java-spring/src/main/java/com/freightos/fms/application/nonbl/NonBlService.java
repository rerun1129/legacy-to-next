package com.freightos.fms.application.nonbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.application.nonbl.command.SearchNonBlCommand;
import com.freightos.fms.application.nonbl.port.in.NonBlUseCase;
import com.freightos.fms.application.nonbl.port.out.NonBlSearchPort;
import com.freightos.fms.application.nonbl.projection.NonBlDetailResult;
import com.freightos.fms.application.nonbl.projection.NonBlSummary;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NonBlService implements NonBlUseCase {

    private final HouseBlUseCase houseBlUseCase;
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
        houseBlUseCase.updateHouseBl(id, command);
        return NonBlDetailResult.from(findNonBlDomainById(id));
    }

    @Override
    @Transactional
    public void deleteNonBlById(Long id) {
        houseBlUseCase.deleteHouseBlById(id);
        log.info("Deleted NonBl id={}", id);
    }

    @Override
    @Transactional
    public void changeNonBlHblNo(Long id, ChangeHouseBlNoCommand command) {
        findNonBlDomainById(id); // NON_BL jobDiv 보장 (없으면 ResourceNotFoundException)
        houseBlUseCase.changeHblNo(id, command);
    }

    private HouseBlNonBl findNonBlDomainById(Long id) {
        return nonBlSearchPort.findNonBlById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.NON_BL_NOT_FOUND));
    }
}
