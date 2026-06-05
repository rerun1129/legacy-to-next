package com.freightos.bms.adapter.out.persistence.codename;

import com.freightos.bms.application.port.out.BlDerived;
import com.freightos.bms.application.port.out.CodeNameResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * admin·fms 스키마 code → name resolve 아웃바운드 어댑터.
 * cross-schema(admin/fms) 접근 진입점. 다른 도메인 어댑터에서 직접 admin·fms 테이블에 접근하지 않도록
 * 이 패키지에만 격리한다.
 */
@Component
@RequiredArgsConstructor
public class CodeNamePersistenceAdapter implements CodeNameResolver {

    private final CodeNameQueryRepository queryRepository;

    @Override
    public Map<String, String> findCustomerNames(Collection<String> codes) {
        return queryRepository.fetchCustomerNames(codes);
    }

    @Override
    public Map<String, String> findTeamNames(Collection<String> codes) {
        return queryRepository.fetchTeamNames(codes);
    }

    @Override
    public Map<String, String> findOperatorNames(Collection<String> usernames) {
        return queryRepository.fetchOperatorNames(usernames);
    }

    @Override
    public Map<String, String> findFreightNames(Collection<String> codes) {
        return queryRepository.fetchFreightNames(codes);
    }

    @Override
    public Map<String, BlDerived> findBlDerived(String blType, Collection<String> blIds) {
        if ("HOUSE".equals(blType)) {
            return queryRepository.fetchHouseBlDerived(blIds);
        }
        if ("MASTER".equals(blType)) {
            return queryRepository.fetchMasterBlDerived(blIds);
        }
        return Map.of();
    }
}
