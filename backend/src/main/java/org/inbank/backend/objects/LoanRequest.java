package org.inbank.backend.objects;

public record LoanRequest(
        String personalCode,
        int loanAmount,
        int loanPeriod
) {}