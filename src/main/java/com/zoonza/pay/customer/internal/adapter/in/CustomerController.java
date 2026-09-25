package com.zoonza.pay.customer.internal.adapter.in;

import com.zoonza.pay.customer.internal.adapter.in.dto.SignupRequest;
import com.zoonza.pay.customer.internal.application.port.in.CustomerCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerCommandUseCase customerCommandUseCase;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        customerCommandUseCase.register(request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
