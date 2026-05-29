package com.freightos.admin.application.menu.port.out;

import com.freightos.admin.application.menu.command.SearchMenuCommand;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menu.entity.Menu;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MenuPort {
    PagedResult<MenuSummary> searchSummaries(SearchMenuCommand command);
    Optional<Menu> findMenuById(Long menuId);
    Long save(Menu menu);
    void update(Long menuId, Menu patchData);
    boolean existsById(Long menuId);
    boolean existsByMenuCode(String menuCode);
    boolean existsByModuleCode(String moduleCode);
    boolean existsByParentId(Long parentId);
    List<Menu> findAccessibleAdminMenus(Set<String> menuCodes);
    List<AutocompleteItem> autocompleteMenuCodes(String query, int limit);
}
