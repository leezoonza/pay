package com.zoonza.pay.customer.application.dto;

import com.zoonza.pay.customer.domain.Name;
import com.zoonza.pay.customer.domain.PhoneNumber;

public record RegisterCustomerCommand(
        Name name,
        PhoneNumber phoneNumber
) {
}
