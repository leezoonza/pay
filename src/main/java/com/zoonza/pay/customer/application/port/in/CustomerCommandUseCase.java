package com.zoonza.pay.customer.application.port.in;

import com.zoonza.pay.customer.application.dto.RegisterCustomerCommand;

public interface CustomerCommandUseCase {
    void register(RegisterCustomerCommand command);
}
