package com.freightos.admin.application.code.carrier;

import com.freightos.admin.application.code.carrier.command.CreateCarrierCommand;
import com.freightos.admin.application.code.carrier.command.SaveCarrierChangesCommand;
import com.freightos.admin.application.code.carrier.command.SearchCarrierCommand;
import com.freightos.admin.application.code.carrier.command.UpdateCarrierCommand;
import com.freightos.admin.application.code.carrier.port.in.CarrierUseCase;
import com.freightos.admin.application.code.carrier.port.out.CarrierPort;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.carrier.entity.Carrier;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarrierService implements CarrierUseCase {

    private final CarrierPort carrierPort;
    private final CarrierFactory carrierFactory;

    @Override
    public PagedResult<CarrierSummary> searchCarriers(SearchCarrierCommand command) {
        return carrierPort.searchSummaries(command);
    }

    @Override
    public Carrier getCarrierById(Long id) {
        return carrierPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("CARRIER_NOT_FOUND", MessageCode.CARRIER_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createCarrier(CreateCarrierCommand command) {
        try {
            return carrierPort.save(carrierFactory.from(command));
        } catch (DataIntegrityViolationException e) {
            throw ApplicationException.conflict("CARRIER_DUPLICATE_CODE", MessageCode.CARRIER_DUPLICATE_CODE.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateCarrier(Long id, UpdateCarrierCommand command) {
        Carrier carrier = getCarrierById(id);
        if (carrier.isDeleted()) {
            throw ApplicationException.conflict("CARRIER_ALREADY_DELETED", MessageCode.CARRIER_ALREADY_DELETED.getMessage());
        }
        carrier.applyUpdate(command.name(), command.nameEn(), command.carrierType(), command.carrierAddress(), command.ediCode(), command.active());
        carrierPort.update(id, carrier);
    }

    @Override
    @Transactional
    public void deleteCarrier(Long id) {
        Carrier carrier = getCarrierById(id);
        if (carrier.isDeleted()) {
            throw ApplicationException.conflict("CARRIER_ALREADY_DELETED", MessageCode.CARRIER_ALREADY_DELETED.getMessage());
        }
        carrierPort.softDelete(id);
    }

    @Override
    @Transactional
    public void deleteCarriers(List<Long> ids) {
        for (Long id : ids) {
            deleteCarrier(id);
        }
    }

    @Override
    @Transactional
    public SaveChangesResult saveCarrierChanges(SaveCarrierChangesCommand command) {
        for (Long id : command.deleteIds()) {
            deleteCarrier(id);
        }
        for (SaveCarrierChangesCommand.UpdateEntry entry : command.updates()) {
            updateCarrier(entry.id(), entry.command());
        }
        for (CreateCarrierCommand create : command.creates()) {
            createCarrier(create);
        }
        return new SaveChangesResult(
                command.creates().size(),
                command.updates().size(),
                command.deleteIds().size()
        );
    }

    @Override
    public List<AutocompleteItem> autocompleteCarriers(String query, String type, int limit) {
        return carrierPort.autocomplete(query, type, limit);
    }
}
