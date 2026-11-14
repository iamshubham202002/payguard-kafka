package com.shubham.payguard.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.shubham.payguard.common.Channel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    private static final String VALID_BODY = """
            {"endToEndId":"E2E-1","debtorAccount":"GB00DEBTOR","creditorAccount":"GB00CREDITOR",
             "amount":250.00,"currency":"GBP","channel":"ONLINE","creditorCountry":"GB"}
            """;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PaymentService paymentService;

    private static Payment payment() {
        return new Payment(UUID.randomUUID(), "E2E-1", "GB00DEBTOR", "GB00CREDITOR",
                new BigDecimal("250.00"), "GBP", Channel.ONLINE, "GB", Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void newPaymentReturns201WithLocation() throws Exception {
        when(paymentService.create(any())).thenReturn(new PaymentService.CreationResult(payment(), true));

        mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void retryOfSameEndToEndIdReturns200() throws Exception {
        when(paymentService.create(any())).thenReturn(new PaymentService.CreationResult(payment(), false));

        mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void invalidBodyReturns400() throws Exception {
        String invalid = """
                {"endToEndId":"","debtorAccount":"A","creditorAccount":"B",
                 "amount":-5,"currency":"gbp","channel":"ONLINE","creditorCountry":"GB"}
                """;

        mockMvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownPaymentReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(paymentService.get(id)).thenThrow(new PaymentNotFoundException(id));

        mockMvc.perform(get("/api/v1/payments/" + id)).andExpect(status().isNotFound());
    }
}
