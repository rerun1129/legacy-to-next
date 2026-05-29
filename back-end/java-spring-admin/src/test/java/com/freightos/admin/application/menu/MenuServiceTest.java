package com.freightos.admin.application.menu;

import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.menu.command.CreateMenuCommand;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.menu.entity.Menu;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuPort menuPort;

    @Mock
    private MenuFactory menuFactory;

    @Mock
    private AttributeValuePort attributeValuePort;

    @InjectMocks
    private MenuService menuService;

    @Test
    void createMenu_duplicateCode_throwsConflict() {
        CreateMenuCommand command = new CreateMenuCommand("MAIN", null, "/main", "메인", null, null, 1, true, "ACCESS");
        given(menuPort.existsByMenuCode("MAIN")).willReturn(true);

        assertThatThrownBy(() -> menuService.createMenu(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createMenu_moduleNotFound_throwsNotFound() {
        CreateMenuCommand command = new CreateMenuCommand("MAIN", null, "/main", "메인", null, null, 1, true, "MISSING");
        given(menuPort.existsByMenuCode("MAIN")).willReturn(false);
        given(attributeValuePort.findActiveAttributeValuesByKey("module")).willReturn(List.of());

        assertThatThrownBy(() -> menuService.createMenu(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createMenu_valid_savesAndReturnsId() {
        CreateMenuCommand command = new CreateMenuCommand("MAIN", null, "/main", "메인", null, null, 1, true, "ACCESS");
        Menu domain = Menu.create("MAIN", null, "/main", "메인", null, null, 1, true, "ACCESS");
        AttributeValue moduleAttr = AttributeValue.create("module", "ACCESS", "Access", 1, true);
        given(menuPort.existsByMenuCode("MAIN")).willReturn(false);
        given(attributeValuePort.findActiveAttributeValuesByKey("module")).willReturn(List.of(moduleAttr));
        given(menuFactory.from(command)).willReturn(domain);
        given(menuPort.save(domain)).willReturn(10L);

        Long id = menuService.createMenu(command);

        assertThat(id).isEqualTo(10L);
    }

    @Test
    void findMenuById_notFound_throwsNotFound() {
        given(menuPort.findMenuById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.findMenuById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── findAccessibleAdminMenus ──────────────────────────────────────────────

    @Test
    void findAccessibleAdminMenus_includesParentWhenChildAccessible() {
        // 부모 메뉴(id=10, parentId=null)와 자식 메뉴(parentId=10) 모두 반환하는 경우
        Menu parent = Menu.create("PARENT_CODE", null, "/parent", "부모", null, null, 1, true, "ACCESS");
        parent.assignIdentity(10L, null, null, null, null);

        Menu child = Menu.create("CHILD_CODE", 10L, "/child", "자식", null, null, 1, true, "ACCESS");
        child.assignIdentity(20L, null, null, null, null);

        given(menuPort.findAccessibleAdminMenus(Set.of("CHILD_CODE"))).willReturn(List.of(child, parent));

        List<Menu> result = menuService.findAccessibleAdminMenus(Set.of("CHILD_CODE"));

        assertThat(result).contains(child, parent);
    }

    @Test
    void findAccessibleAdminMenus_excludesParentWhenNoChild() {
        // 자식이 없는 root 메뉴만 반환되는 경우 → 후처리로 제거
        Menu root = Menu.create("ROOT_CODE", null, "/root", "루트", null, null, 1, true, "ACCESS");
        root.assignIdentity(5L, null, null, null, null);

        given(menuPort.findAccessibleAdminMenus(Set.of("ROOT_CODE"))).willReturn(List.of(root));

        List<Menu> result = menuService.findAccessibleAdminMenus(Set.of("ROOT_CODE"));

        assertThat(result).isEmpty();
    }

    @Test
    void findAccessibleAdminMenus_emptyCodesReturnsEmpty() {
        List<Menu> result = menuService.findAccessibleAdminMenus(Set.of());

        assertThat(result).isEmpty();
        // Port를 전혀 호출하지 않아야 한다 (빈 Set 조기 반환)
        verifyNoInteractions(menuPort);
    }
}
