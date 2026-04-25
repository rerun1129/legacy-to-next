package com.freightos.fms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class FmsApplicationTest {

    @Test
    void contextLoads() {
        // Spring context 로딩 검증
    }
}
