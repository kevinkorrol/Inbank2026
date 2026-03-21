package org.inbank.backend.controllers;

import org.inbank.backend.objects.LoanRequest;
import org.inbank.backend.objects.LoanResponse;
import org.inbank.backend.services.LoanRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api")
public class DecisionAPI {
    private LoanRequestService loanRequestService;

    @Autowired
    public DecisionAPI(LoanRequestService loanRequestService) {
        this.loanRequestService = loanRequestService;
    }

    @PostMapping("/loan/decision")
    public LoanResponse getDecision(@RequestBody LoanRequest loanRequest) {
        LoanResponse loanResponse = loanRequestService.processLoanRequest(loanRequest);
        return loanResponse;
    }
}
