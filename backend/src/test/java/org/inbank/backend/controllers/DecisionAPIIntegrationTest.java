package org.inbank.backend.controllers;

import org.inbank.backend.BackendApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.hamcrest.Matchers.containsString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BackendApplication.class)
class DecisionAPIIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void postLoanDecision_returnsPositiveForValidInput() throws Exception {
        String requestJson = "{\"personalCode\":\"49002010987\",\"loanAmount\":4000,\"loanPeriod\":24}";

        mockMvc.perform(post("/api/loan/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("POSITIVE"))
                .andExpect(jsonPath("$.approvedAmount").value(7200))
                .andExpect(jsonPath("$.approvedPeriod").value(24));
    }

    @Test
    void postLoanDecision_returnsNegativeForDebtUser() throws Exception {
        String requestJson = "{\"personalCode\":\"49002010965\",\"loanAmount\":4000,\"loanPeriod\":24}";

        mockMvc.perform(post("/api/loan/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("NEGATIVE"))
                .andExpect(jsonPath("$.approvedAmount").value(0))
                .andExpect(jsonPath("$.approvedPeriod").value(0))
                .andExpect(jsonPath("$.message").value("This user has existing debt"));
    }

    @Test
    void postLoanDecision_returnsNegativeForUnknownUser() throws Exception {
        String requestJson = "{\"personalCode\":\"12345678901\",\"loanAmount\":4000,\"loanPeriod\":24}";

        mockMvc.perform(post("/api/loan/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("NEGATIVE"))
                .andExpect(jsonPath("$.message").value("Invalid or unknown personal code"));
    }

    @Test
    void postLoanDecision_returnsBadRequestForMalformedJson() throws Exception {
        String malformedJson = "{\"personalCode\":\"49002010987\",\"loanAmount\":4000,\"loanPeriod\":";

        mockMvc.perform(post("/api/loan/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void postLoanDecision_returnsBadRequestForInvalidLoanAmount() throws Exception {
        String requestJson = "{\"personalCode\":\"49002010987\",\"loanAmount\":1999,\"loanPeriod\":24}";

        mockMvc.perform(post("/api/loan/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]", containsString("loanAmount")));
    }

    @Test
    void postLoanDecision_returnsOnlyExpectedContractFields() throws Exception {
        String requestJson = "{\"personalCode\":\"49002010987\",\"loanAmount\":4000,\"loanPeriod\":24}";

        mockMvc.perform(post("/api/loan/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("POSITIVE"))
                .andExpect(jsonPath("$.approvedAmount").value(7200))
                .andExpect(jsonPath("$.approvedPeriod").value(24))
                .andExpect(jsonPath("$.message").value("Loan approved for requested period"))
                .andExpect(jsonPath("$.personalCode").doesNotExist())
                .andExpect(jsonPath("$.creditModifier").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void optionsLoanDecision_returnsCorsHeadersForAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/loan/decision")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}



