package com.freightos.admin.common.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.out.persistence.user.UserJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserRepository;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.permissionpreset.ComputeEffectiveAttributeValuesService;
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
    private final ObjectMapper objectMapper;
    private final PolicyEvaluator policyEvaluator;
    private final MenuPolicyPort menuPolicyPort;
    private final ButtonPolicyPort buttonPolicyPort;
    private final ComputeEffectiveAttributeValuesService effectiveAttributesService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserJpaEntity entity = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + username));
        if (!Boolean.TRUE.equals(entity.getActive())) {
            throw new DisabledException("비활성 사용자: " + username);
        }

        // direct ∪ active preset attribute_value union
        Map<String, List<String>> directAttrs = parseAttributes(entity.getAttributes());
        Map<String, List<String>> attrs = effectiveAttributesService.computeEffectiveAttributes(entity.getId(), directAttrs);

        List<MenuEvalRow> menuRows = menuPolicyPort.findAllActiveForEvaluation();
        List<ButtonEvalRow> buttonRows = buttonPolicyPort.findAllActiveForEvaluation();

        Set<String> accessibleMenus = policyEvaluator.accessibleMenuCodes(attrs, menuRows);
        Set<String> accessibleButtons = policyEvaluator.accessibleButtonCodes(attrs, buttonRows);

        List<GrantedAuthority> authorities = new ArrayList<>();
        // attributes의 role 키 값들을 ROLE_*로 부여
        attrs.getOrDefault("role", List.of())
                .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
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
