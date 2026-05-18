package com.freightos.admin.application.menu.port.in;

import com.freightos.admin.application.menu.command.CreateMenuCommand;
import com.freightos.admin.application.menu.command.SearchMenuCommand;
import com.freightos.admin.application.menu.command.UpdateMenuCommand;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menu.entity.Menu;

import java.util.List;
import java.util.Set;

public interface MenuUseCase {
    PagedResult<MenuSummary> searchMenus(SearchMenuCommand command);
    Menu findMenuById(Long menuId);
    Long createMenu(CreateMenuCommand command);
    void updateMenu(Long menuId, UpdateMenuCommand command);
    void deleteMenuById(Long menuId);
    List<Menu> findAccessibleAdminMenus(Set<String> menuCodes);
}
