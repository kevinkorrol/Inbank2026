package org.inbank.backend.objects;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoanRequest(
        @NotBlank(message = "personalCode is required")
        @Pattern(regexp = "\\d{11}", message = "personalCode must be 11 digits")
        String personalCode,
        @Min(value = 2000, message = "loanAmount must be at least 2000")
        @Max(value = 10000, message = "loanAmount must be at most 10000")
        int loanAmount,
        @Min(value = 12, message = "loanPeriod must be at least 12")
        @Max(value = 60, message = "loanPeriod must be at most 60")
        int loanPeriod
) {}