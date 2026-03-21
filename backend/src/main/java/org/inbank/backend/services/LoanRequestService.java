package org.inbank.backend.services;

import org.inbank.backend.objects.Decision;
import org.inbank.backend.objects.LoanRequest;
import org.inbank.backend.objects.LoanResponse;
import org.inbank.backend.objects.UserSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LoanRequestService {
    private CustomerRegistry customerRegistry;
    private static final int MIN_LOAN_AMOUNT = 2000;
    private static final int MAX_LOAN_AMOUNT = 10000;
    private static final int MIN_LOAN_PERIOD = 12;
    private static final int MAX_LOAN_PERIOD = 60;

    @Autowired
    public LoanRequestService(CustomerRegistry customerRegistry) {
        this.customerRegistry = customerRegistry;
    }

    public LoanResponse processLoanRequest(LoanRequest loanRequest) {
        if (loanRequest == null || loanRequest.personalCode() == null || loanRequest.personalCode().isBlank()) {
            return new LoanResponse(Decision.NEGATIVE, 0, 0, "Personal code is required");
        }

        UserSegment userSegment = customerRegistry.getUserSegment(loanRequest.personalCode());

        if (userSegment == null) {
            return new LoanResponse(Decision.NEGATIVE, 0, 0, "Invalid or unknown personal code");
        }

        LoanResponse initialValidation = validateLoanRequest(loanRequest, userSegment);

        if (initialValidation.decision() != Decision.POSITIVE) {
            return initialValidation;
        }

        int modifier = userSegment.creditModifier();
        int requestedPeriod = loanRequest.loanPeriod();

        int maxAmountAtRequestedPeriod = modifier * requestedPeriod;

        if (maxAmountAtRequestedPeriod >= MIN_LOAN_AMOUNT) {
            int finalAmount = Math.min(maxAmountAtRequestedPeriod, MAX_LOAN_AMOUNT);
            return new LoanResponse(Decision.POSITIVE, finalAmount, requestedPeriod, "Loan approved for requested period");
        }

        int bestAmount = 0;
        int bestPeriod = 0;

        for (int period = MIN_LOAN_PERIOD; period <= MAX_LOAN_PERIOD; period++) {
            int potentialAmount = Math.min(modifier * period, MAX_LOAN_AMOUNT);

            if (potentialAmount >= MIN_LOAN_AMOUNT) {
                if (potentialAmount > bestAmount) {
                    bestAmount = potentialAmount;
                    bestPeriod = period;
                }
            }
        }

        if (bestAmount > 0) {
            return new LoanResponse(Decision.POSITIVE, bestAmount, bestPeriod, "Loan approved with adjusted period");
        }

        return new LoanResponse(Decision.NEGATIVE, 0, 0, "No suitable loan found even with period adjustment");
    }

    public LoanResponse validateLoanRequest(LoanRequest loanRequest, UserSegment userSegment) {
        if (loanRequest.loanAmount() < MIN_LOAN_AMOUNT || loanRequest.loanAmount() > MAX_LOAN_AMOUNT){
            return new LoanResponse(Decision.NEGATIVE, 0, 0,
                    String.format("Requested loan amount must be between %d and %d", MIN_LOAN_AMOUNT, MAX_LOAN_AMOUNT));
        }

        if (loanRequest.loanPeriod() < MIN_LOAN_PERIOD || loanRequest.loanPeriod() > MAX_LOAN_PERIOD){
            return new LoanResponse(Decision.NEGATIVE, 0, 0,
                    String.format("Requested loan period must be between %d and %d months", MIN_LOAN_PERIOD, MAX_LOAN_PERIOD));
        }

        if (userSegment.hasDebt()) {
            return new LoanResponse(Decision.DEBT_REJECTION, 0, 0,
                    "This user has existing debt");
        }

        return new LoanResponse(Decision.POSITIVE, 0, 0, "Loan request is valid");
    }
}
