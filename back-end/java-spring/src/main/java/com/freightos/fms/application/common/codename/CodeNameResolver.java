package com.freightos.fms.application.common.codename;

import com.freightos.fms.application.common.codename.port.out.CodeNamePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * 도메인 Service가 code → name 변환 시 주입받는 애플리케이션 파사드.
 * CodeNamePort를 직접 주입하는 대신 이 클래스를 주입하면, 향후 캐싱·배치 최적화를
 * 이 한 곳에서 진화시킬 수 있다.
 */
@Component
@RequiredArgsConstructor
public class CodeNameResolver {

    private final CodeNamePort codeNamePort;

    public Map<String, String> findCustomerNames(Collection<String> codes) {
        return codeNamePort.findCustomerNames(codes);
    }

    public Map<String, String> findPortNames(Collection<String> codes) {
        return codeNamePort.findPortNames(codes);
    }

    public Map<String, String> findCarrierNames(Collection<String> codes) {
        return codeNamePort.findCarrierNames(codes);
    }

    public Map<String, String> findUserNames(Collection<String> usernames) {
        return codeNamePort.findUserNames(usernames);
    }

    public Map<String, String> findHsCodeNames(Collection<String> codes) {
        return codeNamePort.findHsCodeNames(codes);
    }
}
