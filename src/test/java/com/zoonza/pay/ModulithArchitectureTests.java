package com.zoonza.pay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTests {
    private final ApplicationModules modules = ApplicationModules.of(PayApplication.class);

    @Test
    @DisplayName("애플리케이션 모듈 경계와 의존성 규칙을 준수한다")
    void verifiesModuleStructure() {
        modules.verify();
    }
}