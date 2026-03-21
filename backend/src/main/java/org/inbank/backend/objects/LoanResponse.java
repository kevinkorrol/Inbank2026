package org.inbank.backend.objects;

public record LoanResponse(
        Decision decision,
        int approvedAmount,
        int approvedPeriod,
        String message
) {}


