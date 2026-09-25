package com.zoonza.pay.customer.internal.application.port.in;

import com.zoonza.pay.customer.internal.application.dto.RegisterCustomerCommand;

public interface CustomerCommandUseCase {
    void register(RegisterCustomerCommand command);
}
