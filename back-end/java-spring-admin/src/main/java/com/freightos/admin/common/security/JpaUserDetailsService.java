package com.freightos.admin.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.out.persistence.user.UserJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserPermissionRepository;
import com.freightos.admin.adapter.out.persistence.user.UserRepository;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class JpaUserDetailsService implements UserDetailsService {

    private static final TypeReference<Map<String, List<String>>> ATTR_TYPE_REF = new TypeReference<>() {};

    private final UserRepository userRepository;
    // Phase 4에서 제거 예정. 현재는 의존성 유지하되 findAllByUserId 호출 없음.
    private final UserPermissionRepository userPermissionRepository;
    private final ObjectMapper objectMapper;
    private final PolicyEvaluator policyEvaluator;
    private final MenuPolicyPort menuPolicyPort;
    private final ButtonPolicyPort buttonPolicyPort;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserJpaEntity entity = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + username));
        if (!Boolean.TRUE.equals(entity.getActive())) {
            throw new DisabledException("비활성 사용자: " + username);
        }

        Map<String, List<String>> attrs = parseAttributes(entity.getAttributes());

        List<MenuEvalRow> menuRows = menuPolicyPort.findAllActiveForEvaluation();
        List<ButtonEvalRow> buttonRows = buttonPolicyPort.findAllActiveForEvaluation();

        Set<String> accessibleMenus = policyEvaluator.accessibleMenuCodes(attrs, menuRows);
        Set<String> accessibleButtons = policyEvaluator.accessibleButtonCodes(attrs, buttonRows);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + entity.getRole().name()));
        accessibleMenus.forEach(code -> authorities.add(new SimpleGrantedAuthority("MENU_" + code)));
        accessibleButtons.forEach(code -> authorities.add(new SimpleGrantedAuthority("BTN_" + code)));

        return new User(entity.getUsername(), entity.getPasswordHash(),
                true, true, true, true, authorities);
    }

    private Map<String, List<String>> parseAttributes(String json) {
        if (json == null || json.isBlank() || "{}".equals(json.trim())) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, ATTR_TYPE_REF);
        } catch (Exception e) {
            log.warn("사용자 attributes JSON 파싱 실패, 빈 맵으로 처리: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
