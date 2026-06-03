package com.freightos.fms.adapter.out.persistence.codename;

import com.freightos.fms.application.common.codename.port.out.CodeNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * admin 스키마 code → name resolve 아웃바운드 어댑터.
 * cross-schema(admin) 접근 진입점. 다른 도메인 어댑터에서 직접 admin 테이블에 접근하지 않도록
 * 이 패키지에만 격리한다.
 */
@Component
@RequiredArgsConstructor
public class CodeNamePersistenceAdapter implements CodeNamePort {

    private final CodeNameQueryRepository queryRepository;

    @Override
    public Map<String, String> findCustomerNames(Collection<String> codes) {
        return queryRepository.fetchCustomerNames(codes);
    }

    @Override
    public Map<String, String> findPortNames(Collection<String> codes) {
        return queryRepository.fetchPortNames(codes);
    }

    @Override
    public Map<String, String> findCarrierNames(Collection<String> codes) {
        return queryRepository.fetchCarrierNames(codes);
    }

    @Override
    public Map<String, String> findUserNames(Collection<String> usernames) {
        return queryRepository.fetchUserNames(usernames);
    }

    @Override
    public Map<String, String> findHsCodeNames(Collection<String> codes) {
        return queryRepository.fetchHsCodeNames(codes);
    }

    @Override
    public Map<String, String> findTeamNames(Collection<String> codes) {
        return queryRepository.fetchTeamNames(codes);
    }

    @Override
    public Map<String, String> findFreightNames(Collection<String> codes) {
        return queryRepository.fetchFreightNames(codes);
    }

    @Override
    public Map<String, String> findCustomerTypes(Collection<String> codes) {
        return queryRepository.fetchCustomerTypes(codes);
    }
}
