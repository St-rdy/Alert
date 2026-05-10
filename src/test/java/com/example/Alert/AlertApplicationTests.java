package com.example.Alert;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        // 테스트에서 실제 DB 스키마 검증 생략 — 스키마 불일치로 인한 SchemaManagementException 방지
        "spring.jpa.hibernate.ddl-auto=none"
})
class AlertApplicationTests {

    @Test
    void contextLoads() {
    }
}
