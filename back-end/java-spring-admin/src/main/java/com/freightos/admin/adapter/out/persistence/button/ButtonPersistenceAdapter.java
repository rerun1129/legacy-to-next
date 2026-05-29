package com.freightos.admin.adapter.out.persistence.button;

import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.port.out.ButtonPort;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.button.entity.Button;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ButtonPersistenceAdapter implements ButtonPort {

    private final ButtonRepository buttonRepository;
    private final ButtonDomainToJpaMapper buttonDomainToJpaMapper;
    private final ButtonJpaToDomainMapper buttonJpaToDomainMapper;

    @Override
    public PagedResult<ButtonSummary> searchSummaries(SearchButtonCommand command) {
        return buttonRepository.searchSummaries(command);
    }

    @Override
    public Optional<Button> findButtonById(Long buttonId) {
        return buttonRepository.findById(buttonId).map(buttonJpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Button button) {
        ButtonJpaEntity entity = buttonDomainToJpaMapper.toNewJpa(button);
        buttonRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long buttonId, Button patchData) {
        ButtonJpaEntity entity = buttonRepository.findById(buttonId)
                .orElseThrow(() -> ApplicationException.notFound("BUTTON_NOT_FOUND", MessageCode.BUTTON_NOT_FOUND.getMessage()));
        buttonDomainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public boolean existsById(Long buttonId) {
        return buttonRepository.existsById(buttonId);
    }

    @Override
    public boolean existsByButtonCode(String buttonCode) {
        return buttonRepository.existsByButtonCode(buttonCode);
    }

    @Override
    public boolean existsByMenuId(Long menuId) {
        return buttonRepository.existsByMenuId(menuId);
    }

    @Override
    public List<AutocompleteItem> autocompleteButtonCodes(String query, int limit) {
        return buttonRepository.autocompleteButtonCodes(query, limit);
    }
}
