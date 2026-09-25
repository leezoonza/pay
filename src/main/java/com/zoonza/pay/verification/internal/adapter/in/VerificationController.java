package com.zoonza.pay.verification.internal.adapter.in;

import com.zoonza.pay.verification.internal.adapter.in.dto.ConfirmVerificationRequest;
import com.zoonza.pay.verification.internal.adapter.in.dto.RequestVerificationRequest;
import com.zoonza.pay.verification.internal.adapter.in.dto.RequestVerificationResponse;
import com.zoonza.pay.verification.internal.application.port.in.VerificationCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{verificationId}/confirm")
    public ResponseEntity<Void> confirm(
            @PathVariable String verificationId,
            @Valid @RequestBody ConfirmVerificationRequest request
    ) {
        verificationCommandUseCase.confirm(request.toCommand(verificationId));

        return ResponseEntity
                .ok()
                .build();
    }
}
