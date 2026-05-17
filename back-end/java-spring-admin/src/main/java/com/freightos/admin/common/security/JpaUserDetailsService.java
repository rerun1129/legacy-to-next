package com.freightos.admin.common.security;

import com.freightos.admin.adapter.out.persistence.user.UserJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserPermissionRepository;
import com.freightos.admin.adapter.out.persistence.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserJpaEntity entity = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + username));
        if (!Boolean.TRUE.equals(entity.getActive())) {
            throw new DisabledException("비활성 사용자: " + username);
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + entity.getRole().name()));
        userPermissionRepository.findAllByUserId(entity.getId())
                .forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getPermission().name())));
        return new User(entity.getUsername(), entity.getPasswordHash(),
                true, true, true, true, authorities);
    }
}
