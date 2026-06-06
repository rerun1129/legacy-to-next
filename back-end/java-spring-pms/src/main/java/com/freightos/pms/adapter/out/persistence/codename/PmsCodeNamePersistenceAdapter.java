package com.freightos.pms.adapter.out.persistence.codename;

import com.freightos.pms.application.pms.port.out.PmsCodeNameResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * admin 스키마 code → name resolve 아웃바운드 어댑터.
 * cross-schema 접근 진입점. 다른 도메인 어댑터에서 직접 admin 테이블에 접근하지 않도록 이 패키지에만 격리한다.
 */
@Component
@RequiredArgsConstructor
public class PmsCodeNamePersistenceAdapter implements PmsCodeNameResolver {

    private final PmsCodeNameQueryRepository queryRepository;

    @Override
    public Map<String, String> findCustomerNames(Collection<String> codes) {
        return queryRepository.fetchCustomerNames(codes);
    }

    @Override
    public Map<String, String> findCarrierNames(Collection<String> codes) {
        return queryRepository.fetchCarrierNames(codes);
    }

    @Override
    public Map<String, String> findTeamNames(Collection<String> codes) {
        return queryRepository.fetchTeamNames(codes);
    }

    @Override
    public Map<String, String> findOperatorNames(Collection<String> usernames) {
        return queryRepository.fetchOperatorNames(usernames);
    }
}
