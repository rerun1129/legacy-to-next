package com.freightos.admin.application.button;

import com.freightos.admin.application.button.command.CreateButtonCommand;
import com.freightos.admin.application.button.command.SaveButtonChangesCommand;
import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.command.UpdateButtonCommand;
import com.freightos.admin.application.button.port.in.ButtonUseCase;
import com.freightos.admin.application.button.port.in.SaveButtonChangesUseCase;
import com.freightos.admin.application.button.port.out.ButtonPort;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.button.entity.ActionType;
import com.freightos.admin.domain.button.entity.Button;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ButtonService implements ButtonUseCase, SaveButtonChangesUseCase {

    private final ButtonPort buttonPort;
    private final ButtonFactory buttonFactory;
    private final MenuPort menuPort;

    @Override
    public PagedResult<ButtonSummary> searchButtons(SearchButtonCommand command) {
        return buttonPort.searchSummaries(command);
    }

    @Override
    public Button findButtonById(Long buttonId) {
        return buttonPort.findButtonById(buttonId)
                .orElseThrow(() -> ApplicationException.notFound("BUTTON_NOT_FOUND", MessageCode.BUTTON_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createButton(CreateButtonCommand command) {
        if (buttonPort.existsByButtonCode(command.buttonCode())) {
            throw ApplicationException.conflict("BUTTON_DUPLICATE_CODE", MessageCode.BUTTON_DUPLICATE_CODE.getMessage());
        }
        if (!menuPort.existsById(command.menuId())) {
            throw ApplicationException.notFound("BUTTON_MENU_NOT_FOUND", MessageCode.BUTTON_MENU_NOT_FOUND.getMessage());
        }
        Button button = buttonFactory.from(command);
        return buttonPort.save(button);
    }

    @Override
    @Transactional
    public void updateButton(Long buttonId, UpdateButtonCommand command) {
        Button existing = buttonPort.findButtonById(buttonId)
                .orElseThrow(() -> ApplicationException.notFound("BUTTON_NOT_FOUND", MessageCode.BUTTON_NOT_FOUND.getMessage()));
        if (!menuPort.existsById(command.menuId())) {
            throw ApplicationException.notFound("BUTTON_MENU_NOT_FOUND", MessageCode.BUTTON_MENU_NOT_FOUND.getMessage());
        }
        existing.applyUpdate(command.menuId(), command.label(), ActionType.valueOf(command.actionType()),
                command.apiMethod(), command.apiPath(), command.sortOrder(), command.active());
        buttonPort.update(buttonId, existing);
    }

    @Override
    @Transactional
    public SaveChangesResult saveButtonChanges(SaveButtonChangesCommand command) {
        for (SaveButtonChangesCommand.UpdateButtonItem item : command.updates()) {
            updateButton(item.id(), new UpdateButtonCommand(item.menuId(), item.label(), item.actionType(), item.apiMethod(), item.apiPath(), item.sortOrder(), item.active()));
        }
        for (CreateButtonCommand create : command.creates()) {
            createButton(create);
        }
        return new SaveChangesResult(command.creates().size(), command.updates().size(), 0);
    }

    @Override
    public List<AutocompleteItem> autocompleteButtonCodes(String query, int limit) {
        return buttonPort.autocompleteButtonCodes(query, limit);
    }
}
