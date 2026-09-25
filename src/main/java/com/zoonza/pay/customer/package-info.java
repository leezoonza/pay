@ApplicationModule(
        allowedDependencies = {"shared::error", "shared::domain", "verification::api"}
)
package com.zoonza.pay.customer;

import org.springframework.modulith.ApplicationModule;
