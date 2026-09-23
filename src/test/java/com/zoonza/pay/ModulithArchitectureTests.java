package com.zoonza.pay;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTests {
    private final ApplicationModules modules = ApplicationModules.of(PayApplication.class);

    @Test
    void verifiesModuleStructure() {
        modules.verify();
    }
}