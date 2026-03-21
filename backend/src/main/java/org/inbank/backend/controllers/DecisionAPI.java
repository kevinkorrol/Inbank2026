package org.inbank.backend.controllers;

import org.inbank.backend.objects.LoanRequest;
import org.inbank.backend.objects.LoanResponse;
import org.inbank.backend.services.LoanRequestService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api")
public class DecisionAPI {
    private static final Logger log = LoggerFactory.getLogger(DecisionAPI.class);
    private final LoanRequestService loanRequestService;

    @Autowired
    public DecisionAPI(LoanRequestService loanRequestService) {
        this.loanRequestService = loanRequestService;
    }

    @PostMapping("/loan/decision")
    public LoanResponse getDecision(@Valid @RequestBody LoanRequest loanRequest) {
        log.info("Loan decision request received");
        return loanRequestService.processLoanRequest(loanRequest);
    }
}
