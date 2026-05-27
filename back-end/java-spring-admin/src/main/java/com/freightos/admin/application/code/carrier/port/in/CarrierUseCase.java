package com.freightos.admin.application.code.carrier.port.in;

import com.freightos.admin.application.code.carrier.command.CreateCarrierCommand;
import com.freightos.admin.application.code.carrier.command.SaveCarrierChangesCommand;
import com.freightos.admin.application.code.carrier.command.SearchCarrierCommand;
import com.freightos.admin.application.code.carrier.command.UpdateCarrierCommand;
import com.freightos.admin.application.code.carrier.projection.CarrierSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.carrier.entity.Carrier;

import java.util.List;

public interface CarrierUseCase {
    PagedResult<CarrierSummary> searchCarriers(SearchCarrierCommand command);
    Carrier getCarrierById(Long id);
    Long createCarrier(CreateCarrierCommand command);
    void updateCarrier(Long id, UpdateCarrierCommand command);
    void deleteCarrier(Long id);
    void deleteCarriers(List<Long> ids);
    SaveChangesResult saveCarrierChanges(SaveCarrierChangesCommand command);
    List<AutocompleteItem> autocompleteCarriers(String query, int limit);
}
