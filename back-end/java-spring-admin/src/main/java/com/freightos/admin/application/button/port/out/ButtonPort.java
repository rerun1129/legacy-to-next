package com.freightos.admin.application.button.port.out;

import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.button.entity.Button;

import java.util.Optional;

public interface ButtonPort {
    PagedResult<ButtonSummary> searchSummaries(SearchButtonCommand command);
    Optional<Button> findButtonById(Long buttonId);
    Long save(Button button);
    void update(Long buttonId, Button patchData);
    void deleteButtonById(Long buttonId);
    boolean existsById(Long buttonId);
    boolean existsByButtonCode(String buttonCode);
    boolean existsByMenuId(Long menuId);
}
