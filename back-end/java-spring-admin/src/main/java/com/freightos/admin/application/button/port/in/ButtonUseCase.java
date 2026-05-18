package com.freightos.admin.application.button.port.in;

import com.freightos.admin.application.button.command.CreateButtonCommand;
import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.command.UpdateButtonCommand;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.button.entity.Button;

import java.util.List;

public interface ButtonUseCase {
    PagedResult<ButtonSummary> searchButtons(SearchButtonCommand command);
    Button findButtonById(Long buttonId);
    Long createButton(CreateButtonCommand command);
    void updateButton(Long buttonId, UpdateButtonCommand command);
    void deleteButtonById(Long buttonId);
    void deleteButtonsByIds(List<Long> ids);
}
