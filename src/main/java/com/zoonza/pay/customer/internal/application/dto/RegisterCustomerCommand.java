package com.zoonza.pay.customer.internal.application.dto;

import com.zoonza.pay.customer.internal.domain.Name;
import com.zoonza.pay.shared.domain.PhoneNumber;

public record RegisterCustomerCommand(
        Name name,
        PhoneNumber phoneNumber,
        String verificationId
) {
}
