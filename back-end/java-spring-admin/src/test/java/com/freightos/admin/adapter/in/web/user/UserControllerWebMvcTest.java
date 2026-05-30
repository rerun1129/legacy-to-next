package com.freightos.admin.adapter.in.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.user.dto.CreateUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.SearchUserRequest;
import com.freightos.admin.adapter.in.web.user.dto.UserDetailResponse;
import com.freightos.admin.adapter.in.web.user.dto.UserSummaryResponse;
import com.freightos.admin.application.user.command.CreateUserCommand;
import com.freightos.admin.application.user.command.SearchUserCommand;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.application.user.projection.UserSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.security.JpaUserDetailsService;
import com.freightos.admin.common.security.JwtAuthenticationFilter;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.common.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, JwtAuthenticationFilter.class })
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private UserAssembler userAssembler;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JpaUserDetailsService jpaUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    // ── 미인증 → 401 ──────────────────────────────────────────────────────────

    @Test
    void search_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/admin/user/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // ── MENU_ADMIN_USER_LIST authority → 200 ──────────────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_USER_LIST")
    void search_authenticated_returns200() throws Exception {
        UserSummaryResponse summaryResponse = new UserSummaryResponse(
                1L, "alice", "alice@example.com", true,
                null, LocalDateTime.of(2024, 1, 1, 0, 0), Map.of(), null);
        PagedResult<UserSummary> summaryPage = PagedResult.of(List.of(), 1L, 1, 0, 20);
        PagedResult<UserSummaryResponse> responsePage = PagedResult.of(List.of(summaryResponse), 1L, 1, 0, 20);

        given(userAssembler.toSearchCommand(any())).willReturn(new SearchUserCommand(null, null, 0, 20));
        given(userUseCase.searchUsers(any())).willReturn(summaryPage);
        given(userAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchUserRequest req = new SearchUserRequest(null, null, 0, 20);

        mockMvc.perform(post("/api/admin/user/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].username").value("alice"));
    }

    // ── BTN_ADMIN_USER_LIST_SAVE authority → 201 ──────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_USER_LIST_SAVE")
    void create_returns201WithLocationAndId() throws Exception {
        Map<String, List<String>> attrsUser = Map.of("role", List.of("USER"));
        given(userAssembler.toCreateCommand(any())).willReturn(
                new CreateUserCommand("alice", "alice@example.com", "pass1234", true, attrsUser, null));
        given(userUseCase.createUser(any())).willReturn(42L);

        CreateUserRequest req = new CreateUserRequest("alice", "alice@example.com", "pass1234",
                Boolean.TRUE, attrsUser, null);

        mockMvc.perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(42));
    }

    // ── username @NotBlank → 400 ──────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_USER_LIST_SAVE")
    void create_blankUsername_returns400() throws Exception {
        String body = """
                {"username":"","email":"a@b.com","password":"pass1234","active":true,"attributes":{}}
                """;
        mockMvc.perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── password @Size(min=8) → 400 ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_USER_LIST_SAVE")
    void create_shortPassword_returns400() throws Exception {
        // 7자 비밀번호 → min=8 위반
        String body = """
                {"username":"alice","email":"a@b.com","password":"short12","active":true,"attributes":{}}
                """;
        mockMvc.perform(post("/api/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── BTN_ADMIN_USER_LIST_SAVE authority → 200 ──────────────────────────────

    @Test
    @WithMockUser(authorities = "BTN_ADMIN_USER_LIST_SAVE")
    void delete_returns200() throws Exception {
        willDoNothing().given(userUseCase).deleteUser(any());

        mockMvc.perform(delete("/api/admin/user/1"))
                .andExpect(status().isOk());
    }

    // ── MENU_ADMIN_USER_LIST authority → getById 200 ─────────────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_USER_LIST")
    void getById_returns200WithDetail() throws Exception {
        Map<String, List<String>> attrsUser = Map.of("role", List.of("USER"));
        UserDetailResponse detail = new UserDetailResponse(
                1L, "alice", "alice@example.com", true, null, attrsUser,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0),
                "admin", "admin", null);

        given(userUseCase.findUserById(1L)).willReturn(null);
        given(userAssembler.toDetail(any())).willReturn(detail);

        mockMvc.perform(get("/api/admin/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("alice"))
                // passwordHash 필드가 없음을 확인
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                // role 필드가 없음을 확인 (Phase 4: attributes로 이전)
                .andExpect(jsonPath("$.data.role").doesNotExist());
    }

    // ── @PreAuthorize: MENU_ADMIN_USER_LIST authority → 200 ──────────────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_USER_LIST")
    void search_withUserListMenuAuthority_returns200() throws Exception {
        PagedResult<UserSummary> summaryPage = PagedResult.of(List.of(), 0L, 0, 0, 20);
        PagedResult<UserSummaryResponse> responsePage = PagedResult.of(List.of(), 0L, 0, 0, 20);

        given(userAssembler.toSearchCommand(any())).willReturn(new SearchUserCommand(null, null, 0, 20));
        given(userUseCase.searchUsers(any())).willReturn(summaryPage);
        given(userAssembler.toSummaryPage(any())).willReturn(responsePage);

        SearchUserRequest req = new SearchUserRequest(null, null, 0, 20);

        mockMvc.perform(post("/api/admin/user/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── @PreAuthorize: 다른 authority (MENU_ADMIN_CODE_LIST) → 403 ───────────

    @Test
    @WithMockUser(authorities = "MENU_ADMIN_CODE_LIST")
    void search_withCodeListMenuOnly_returns403() throws Exception {
        mockMvc.perform(post("/api/admin/user/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"page\":0,\"size\":20}"))
                .andExpect(status().isForbidden());
    }
}
