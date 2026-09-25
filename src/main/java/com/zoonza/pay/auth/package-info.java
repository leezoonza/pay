@ApplicationModule(
        allowedDependencies = {"shared::error", "shared::domain", "customer::api", "verification::api"}
)
package com.zoonza.pay.auth;

import org.springframework.modulith.ApplicationModule;
