package com.freightos.admin.adapter.out.persistence.menu;

import com.freightos.admin.application.menu.command.SearchMenuCommand;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menu.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuPersistenceAdapter implements MenuPort {

    private final MenuRepository menuRepository;
    private final MenuDomainToJpaMapper menuDomainToJpaMapper;
    private final MenuJpaToDomainMapper menuJpaToDomainMapper;

    @Override
    public PagedResult<MenuSummary> searchSummaries(SearchMenuCommand command) {
        return menuRepository.searchSummaries(command);
    }

    @Override
    public Optional<Menu> findMenuById(Long menuId) {
        return menuRepository.findById(menuId).map(menuJpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(Menu menu) {
        MenuJpaEntity entity = menuDomainToJpaMapper.toNewJpa(menu);
        menuRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void update(Long menuId, Menu patchData) {
        MenuJpaEntity entity = menuRepository.findById(menuId)
                .orElseThrow(() -> ApplicationException.notFound("MENU_NOT_FOUND", MessageCode.MENU_NOT_FOUND.getMessage()));
        menuDomainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public void deleteMenuById(Long menuId) {
        if (!menuRepository.existsById(menuId)) {
            throw ApplicationException.notFound("MENU_NOT_FOUND", MessageCode.MENU_NOT_FOUND.getMessage());
        }
        menuRepository.deleteById(menuId);
    }

    @Override
    public boolean existsById(Long menuId) {
        return menuRepository.existsById(menuId);
    }

    @Override
    public boolean existsByMenuCode(String menuCode) {
        return menuRepository.existsByMenuCode(menuCode);
    }

    @Override
    public boolean existsByModuleCode(String moduleCode) {
        return menuRepository.existsByModuleCode(moduleCode);
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return menuRepository.existsByParentId(parentId);
    }

    @Override
    public List<Menu> findAccessibleAdminMenus(Set<String> menuCodes) {
        List<MenuJpaEntity> children = menuRepository.findAllByActiveTrueAndModuleCodeAndMenuCodeIn("ADMIN", menuCodes);
        Set<Long> parentIds = children.stream()
                .map(MenuJpaEntity::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<MenuJpaEntity> parents = parentIds.isEmpty() ? List.of() : menuRepository.findAllByActiveTrueAndIdIn(parentIds);

        // id 기준 dedupe 후 도메인 변환
        Map<Long, MenuJpaEntity> merged = new LinkedHashMap<>();
        children.forEach(e -> merged.put(e.getId(), e));
        parents.forEach(e -> merged.put(e.getId(), e));

        return merged.values().stream().map(menuJpaToDomainMapper::toDomain).toList();
    }
}
