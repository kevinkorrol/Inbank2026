package org.inbank.backend.services;

import org.inbank.backend.objects.Decision;
import org.inbank.backend.objects.LoanRequest;
import org.inbank.backend.objects.LoanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanRequestServiceTest {

    private LoanRequestService loanRequestService;

    @BeforeEach
    void setUp() {
        loanRequestService = new LoanRequestService(new CustomerRegistry());
    }

    @Test
    void processLoanRequest_returnsNegativeWhenRequestIsNull() {
        LoanResponse response = loanRequestService.processLoanRequest(null);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertEquals(0, response.approvedAmount());
        assertEquals(0, response.approvedPeriod());
        assertTrue(response.message().contains("Personal code is required"));
    }

    @Test
    void processLoanRequest_returnsNegativeWhenPersonalCodeIsBlank() {
        LoanRequest request = new LoanRequest("   ", 4000, 24);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertTrue(response.message().contains("Personal code is required"));
    }

    @Test
    void processLoanRequest_returnsNegativeWhenPersonalCodeIsUnknown() {
        LoanRequest request = new LoanRequest("12345678901", 4000, 24);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertTrue(response.message().contains("unknown personal code"));
    }

    @Test
    void processLoanRequest_returnsNegativeWhenAmountIsBelowMinimum() {
        LoanRequest request = new LoanRequest("49002010987", 1999, 24);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertTrue(response.message().contains("Requested loan amount must be between"));
    }

    @Test
    void processLoanRequest_returnsNegativeWhenAmountIsAboveMaximum() {
        LoanRequest request = new LoanRequest("49002010987", 10001, 24);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertTrue(response.message().contains("Requested loan amount must be between"));
    }

    @Test
    void processLoanRequest_returnsNegativeWhenPeriodIsBelowMinimum() {
        LoanRequest request = new LoanRequest("49002010987", 4000, 11);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertTrue(response.message().contains("Requested loan period must be between"));
    }

    @Test
    void processLoanRequest_returnsNegativeWhenPeriodIsAboveMaximum() {
        LoanRequest request = new LoanRequest("49002010987", 4000, 61);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertTrue(response.message().contains("Requested loan period must be between"));
    }

    @Test
    void processLoanRequest_returnsNegativeWhenUserHasDebt() {
        LoanRequest request = new LoanRequest("49002010965", 4000, 24);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.NEGATIVE, response.decision());
        assertEquals(0, response.approvedAmount());
        assertEquals(0, response.approvedPeriod());
        assertTrue(response.message().contains("existing debt"));
    }

    @Test
    void processLoanRequest_returnsCappedAmountWhenMaxExceedsConstraint() {
        LoanRequest request = new LoanRequest("49002010998", 4000, 12);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.POSITIVE, response.decision());
        assertEquals(10000, response.approvedAmount());
        assertEquals(12, response.approvedPeriod());
        assertTrue(response.message().contains("requested period"));
    }

    @Test
    void processLoanRequest_adjustsPeriodWhenRequestedPeriodCannotApproveMinimum() {
        LoanRequest request = new LoanRequest("49002010976", 4000, 12);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.POSITIVE, response.decision());
        assertEquals(6000, response.approvedAmount());
        assertEquals(60, response.approvedPeriod());
        assertTrue(response.message().contains("adjusted period"));
    }

    @Test
    void processLoanRequest_returnsPositiveForUncappedRequestedPeriod() {
        LoanRequest request = new LoanRequest("49002010987", 4000, 12);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.POSITIVE, response.decision());
        assertEquals(3600, response.approvedAmount());
        assertEquals(12, response.approvedPeriod());
        assertTrue(response.message().contains("requested period"));
    }

    @Test
    void processLoanRequest_acceptsPersonalCodeWithOuterWhitespace() {
        LoanRequest request = new LoanRequest(" 49002010987 ", 4000, 12);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.POSITIVE, response.decision());
        assertEquals(3600, response.approvedAmount());
        assertEquals(12, response.approvedPeriod());
    }

    @Test
    void processLoanRequest_handlesMinimumBoundaryInputs() {
        LoanRequest request = new LoanRequest("49002010998", 2000, 12);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.POSITIVE, response.decision());
        assertEquals(10000, response.approvedAmount());
        assertEquals(12, response.approvedPeriod());
    }

    @Test
    void processLoanRequest_handlesMaximumBoundaryInputs() {
        LoanRequest request = new LoanRequest("49002010976", 10000, 60);

        LoanResponse response = loanRequestService.processLoanRequest(request);

        assertEquals(Decision.POSITIVE, response.decision());
        assertEquals(6000, response.approvedAmount());
        assertEquals(60, response.approvedPeriod());
        assertTrue(response.message().contains("requested period"));
    }
}


