package com.freightos.admin.application.menu;

import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.menu.command.CreateMenuCommand;
import com.freightos.admin.application.menu.command.SaveMenuChangesCommand;
import com.freightos.admin.application.menu.command.SearchMenuCommand;
import com.freightos.admin.application.menu.command.UpdateMenuCommand;
import com.freightos.admin.application.menu.port.in.MenuUseCase;
import com.freightos.admin.application.menu.port.in.SaveMenuChangesUseCase;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.menu.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService implements MenuUseCase, SaveMenuChangesUseCase {

    private final MenuPort menuPort;
    private final MenuFactory menuFactory;
    private final AttributeValuePort attributeValuePort;

    @Override
    public PagedResult<MenuSummary> searchMenus(SearchMenuCommand command) {
        return menuPort.searchSummaries(command);
    }

    @Override
    public Menu findMenuById(Long menuId) {
        return menuPort.findMenuById(menuId)
                .orElseThrow(() -> ApplicationException.notFound("MENU_NOT_FOUND", MessageCode.MENU_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createMenu(CreateMenuCommand command) {
        if (menuPort.existsByMenuCode(command.menuCode())) {
            throw ApplicationException.conflict("MENU_DUPLICATE_CODE", MessageCode.MENU_DUPLICATE_CODE.getMessage());
        }
        if (command.parentId() != null && !menuPort.existsById(command.parentId())) {
            throw ApplicationException.notFound("MENU_PARENT_NOT_FOUND", MessageCode.MENU_PARENT_NOT_FOUND.getMessage());
        }
        if (attributeValuePort.findActiveAttributeValuesByKey("module").stream()
                .noneMatch(v -> v.getValue().equals(command.moduleCode()))) {
            throw ApplicationException.notFound("MENU_MODULE_NOT_FOUND", MessageCode.MENU_MODULE_NOT_FOUND.getMessage());
        }
        Menu menu = menuFactory.from(command);
        return menuPort.save(menu);
    }

    @Override
    @Transactional
    public void updateMenu(Long menuId, UpdateMenuCommand command) {
        Menu existing = menuPort.findMenuById(menuId)
                .orElseThrow(() -> ApplicationException.notFound("MENU_NOT_FOUND", MessageCode.MENU_NOT_FOUND.getMessage()));
        if (command.parentId() != null && !menuPort.existsById(command.parentId())) {
            throw ApplicationException.notFound("MENU_PARENT_NOT_FOUND", MessageCode.MENU_PARENT_NOT_FOUND.getMessage());
        }
        if (attributeValuePort.findActiveAttributeValuesByKey("module").stream()
                .noneMatch(v -> v.getValue().equals(command.moduleCode()))) {
            throw ApplicationException.notFound("MENU_MODULE_NOT_FOUND", MessageCode.MENU_MODULE_NOT_FOUND.getMessage());
        }
        existing.applyUpdate(command.parentId(), command.path(), command.label(), command.labelEn(),
                command.icon(), command.sortOrder(), command.active(), command.moduleCode());
        menuPort.update(menuId, existing);
    }

    @Override
    @Transactional
    public SaveChangesResult saveMenuChanges(SaveMenuChangesCommand command) {
        for (SaveMenuChangesCommand.UpdateMenuItem item : command.updates()) {
            updateMenu(item.id(), new UpdateMenuCommand(item.parentId(), item.path(), item.label(), item.labelEn(), item.icon(), item.sortOrder(), item.active(), item.moduleCode()));
        }
        for (CreateMenuCommand create : command.creates()) {
            createMenu(create);
        }
        return new SaveChangesResult(command.creates().size(), command.updates().size(), 0);
    }

    @Override
    public List<Menu> findAccessibleAdminMenus(Set<String> menuCodes) {
        if (menuCodes.isEmpty()) {
            return List.of();
        }
        // 어댑터에서 자식 + 자식의 부모 union 반환
        List<Menu> initial = menuPort.findAccessibleAdminMenus(menuCodes);

        // 부모 over-expose 방지: 자식이 없는 root는 제거
        List<Menu> roots = initial.stream().filter(m -> m.getParentId() == null).toList();
        List<Menu> leaves = initial.stream().filter(m -> m.getParentId() != null).toList();

        Set<Long> referencedParentIds = leaves.stream()
                .map(Menu::getParentId)
                .collect(Collectors.toSet());

        List<Menu> survivingRoots = roots.stream()
                .filter(r -> referencedParentIds.contains(r.getId()))
                .toList();

        List<Menu> result = new ArrayList<>(leaves);
        result.addAll(survivingRoots);
        return result;
    }

    @Override
    public List<AutocompleteItem> autocompleteMenuCodes(String query, int limit) {
        return menuPort.autocompleteMenuCodes(query, limit);
    }
}
