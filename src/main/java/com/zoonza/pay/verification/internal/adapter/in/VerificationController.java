package com.zoonza.pay.verification.internal.adapter.in;

import com.zoonza.pay.verification.internal.adapter.in.dto.RequestVerificationRequest;
import com.zoonza.pay.verification.internal.adapter.in.dto.RequestVerificationResponse;
import com.zoonza.pay.verification.internal.application.port.in.VerificationCommandUseCase;
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
@RequestMapping("/api/verifications")
public class VerificationController {
    private final VerificationCommandUseCase verificationCommandUseCase;

    @PostMapping
    public ResponseEntity<RequestVerificationResponse> request(
            @Valid @RequestBody RequestVerificationRequest request
    ) {
        String verificationId = verificationCommandUseCase.request(request.toCommand());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new RequestVerificationResponse(verificationId));
    }
}
