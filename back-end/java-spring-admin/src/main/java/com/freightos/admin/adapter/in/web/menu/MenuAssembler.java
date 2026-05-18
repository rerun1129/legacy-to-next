package com.freightos.admin.adapter.in.web.menu;

import com.freightos.admin.adapter.in.web.menu.dto.AccessibleMenuResponse;
import com.freightos.admin.adapter.in.web.menu.dto.CreateMenuRequest;
import com.freightos.admin.adapter.in.web.menu.dto.MenuDetailResponse;
import com.freightos.admin.adapter.in.web.menu.dto.MenuSummaryResponse;
import com.freightos.admin.adapter.in.web.menu.dto.SearchMenuRequest;
import com.freightos.admin.adapter.in.web.menu.dto.UpdateMenuRequest;
import com.freightos.admin.application.menu.command.CreateMenuCommand;
import com.freightos.admin.application.menu.command.SearchMenuCommand;
import com.freightos.admin.application.menu.command.UpdateMenuCommand;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menu.entity.Menu;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuAssembler {

    public SearchMenuCommand toSearchCommand(SearchMenuRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchMenuCommand(req.menuCode(), req.label(), req.moduleCode(), req.parentId(), req.active(), req.page(), size);
    }

    public CreateMenuCommand toCreateCommand(CreateMenuRequest req) {
        return new CreateMenuCommand(req.menuCode(), req.parentId(), req.path(), req.label(), req.labelEn(), req.icon(), req.sortOrder(), req.active(), req.moduleCode());
    }

    public UpdateMenuCommand toUpdateCommand(UpdateMenuRequest req) {
        return new UpdateMenuCommand(req.parentId(), req.path(), req.label(), req.labelEn(), req.icon(), req.sortOrder(), req.active(), req.moduleCode());
    }

    public MenuSummaryResponse toSummaryResponse(MenuSummary p) {
        return new MenuSummaryResponse(p.id(), p.menuCode(), p.parentId(), p.path(), p.label(), p.labelEn(), p.icon(), p.sortOrder(), p.active(), p.moduleCode(), p.updatedAt());
    }

    public MenuDetailResponse toDetail(Menu domain) {
        return new MenuDetailResponse(
                domain.getId(), domain.getMenuCode(), domain.getParentId(), domain.getPath(),
                domain.getLabel(), domain.getLabelEn(), domain.getIcon(), domain.getSortOrder(),
                domain.getActive(), domain.getModuleCode(),
                domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy()
        );
    }

    public PagedResult<MenuSummaryResponse> toSummaryPage(PagedResult<MenuSummary> src) {
        return src.map(this::toSummaryResponse);
    }

    public List<AccessibleMenuResponse> toAccessibleList(List<Menu> menus) {
        return menus.stream()
                .map(m -> new AccessibleMenuResponse(
                        m.getId(), m.getMenuCode(), m.getParentId(), m.getPath(),
                        m.getLabel(), m.getLabelEn(), m.getIcon(), m.getSortOrder(), m.getModuleCode()))
                .toList();
    }
}
