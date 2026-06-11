package com.freightos.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void contextLoads() {
        // 애플리케이션 컨텍스트가 정상 기동되는지 검증
    }

    @Test
    void fiveRoutesAreRegistered() {
        // admin/teams/bms/pms/fms 5개 라우트가 모두 등록되어 있는지 검증
        // 외부 서비스 호출 없이 RouteLocator 빈만 검사하므로 flaky 요소 없음
        StepVerifier.create(
                routeLocator.getRoutes()
                        .map(route -> route.getId())
                        .collectList()
        )
        .assertNext(ids -> {
            assertThat(ids).containsExactlyInAnyOrderElementsOf(
                    List.of("admin-route", "teams-route", "bms-route", "pms-route", "fms-route")
            );
        })
        .verifyComplete();
    }
}
